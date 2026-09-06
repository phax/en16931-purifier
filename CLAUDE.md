# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Purifier for EN 16931 invoices: removes everything from a UBL or CII invoice that is not part of the EN 16931 core message and writes the result as an XML Schema valid document in the same syntax. Pure Java library + optional CLI wrapper. The input syntax is always the output syntax - this is not a converter.

## Build & Test Commands

```bash
mvn clean compile                # Compile
mvn clean test                   # Run all tests
mvn clean install                # Build and install everything

# Single test class
mvn test -pl en16931-purifier -Dtest=CIID16BPurifierTest

# Single test method
mvn test -pl en16931-purifier -Dtest=EN16931ConformanceTest#testCIIConformance

# Build CLI fat JAR
mvn clean package -pl en16931-purifier-cli
# Output: en16931-purifier-cli/target/en16931-purifier-cli-full.jar
```

Requires Java 17+.

## Module Structure

- **en16931-purifier/** — Core purification library
- **en16931-purifier-cli/** — CLI wrapper using picocli (produces shaded fat JAR)

## Architecture

The purification is **declarative**. The EN 16931 core message is described as a whitelist of XML
paths per (EN edition x syntax kind); a generic engine prunes the DOM against that whitelist and
ph-ubl / ph-cii perform the XML Schema validation and the serialization.

The facts about the standard itself - the editions, the syntax kinds, the document types and the
XML namespace URIs - come from [en16931-basics](https://github.com/phax/en16931-basics) and are
deliberately **not** duplicated here:

```
com.helger.en16931.basics       (external, from en16931-basics)
  EEN16931Edition          EN2017 and EN2026 - both have rule sets
  EEN16931SyntaxKind       UBL_INVOICE, UBL_CREDIT_NOTE, CII - selects the rule set
  EEN16931DocumentType     syntax kind x syntax version, data only
  CEN16931Syntax           namespace URIs, prefixes and document element names

com.helger.en16931.purifier
  EN16931Purifiers         the lookups on top of those data only enums:
                           getRuleSet (edition, syntaxKind), isSupported (edition),
                           createPurifier (docType, edition), DEFAULT_EDITION
  AbstractEN16931Purifier  settings, DOM pruning, JAXB round trip, writing
    UBL21InvoicePurifier / UBL21CreditNotePurifier
    UBL25InvoicePurifier / UBL25CreditNotePurifier
    CIID16BPurifier / CIID25APurifier
  PurifierVersion          build version constants loaded from properties

com.helger.en16931.purifier.rule
  PurificationRuleSet      whitelist of one document type, holds the rule tree
  PurificationRuleSetBuilder   authoring API - add (BT, path, maxOccurs, attributes...)
  PurificationPathParser   parses "/cac:X[cbc:Y='v']/cbc:Z" into path steps
  PurificationRuleNode     one node of the whitelist tree, may exist multiple times per
                           element name when discriminated by a filter
  IPurificationElementFilter / PurificationElementFilters   the predicates
  PurificationEngine       the DOM pruner
  IPurificationSettings    how aggressive the engine works

com.helger.en16931.purifier.ruleset
  EN16931UBLRules2017      UBL Invoice and UBL Credit Note rule sets, EN 16931:2017
  EN16931CIIRules2017      CII Cross Industry Invoice rule set, EN 16931:2017
  EN16931UBLRules2026      UBL Invoice and UBL Credit Note rule sets, EN 16931:2026
  EN16931CIIRules2026      CII Cross Industry Invoice rule set, EN 16931:2026
```

The 2017 rule sets work with every UBL 2.x respectively every CII version. The 2026 rule sets are
bound to UBL 2.5 and CII D25A, because the 2026 binding uses syntax elements that older versions
do not have.

### Purification Flow

1. Read the source XML into a DOM document
2. Clone it and prune the clone against the `PurificationRuleSet`; every removal is added to the
   `ErrorList` as an information entry carrying the path
3. Unmarshal the pruned document with the ph-ubl / ph-cii marshaller **using the XML Schema** -
   this is what guarantees the XML Schema validity of the result
4. Write the JAXB object with the same marshaller

### Adding a new EN 16931 edition

Add a `EN16931UBLRulesXXXX` and a `EN16931CIIRulesXXXX` class in
`com.helger.en16931.purifier.ruleset`, add the constant to `EEN16931Edition` **in en16931-basics**
and return the rule sets from `EN16931Purifiers.getRuleSet`. `isSupported` derives itself from
that, so nothing else needs to be flipped, and neither the engine nor any purifier class needs to
change.

### Adding a new syntax version

Add a purifier class deriving from `AbstractEN16931Purifier` that returns the new marshaller from
`createMarshaller`, add the constant to `EEN16931DocumentType` **in en16931-basics** and add the
matching branch to `EN16931Purifiers.createPurifier`. The rule sets are shared, because all UBL 2.x
versions and all CII versions use the same XML namespace URIs and element names.

## Rule Set Authoring

The rule sets are a direct transcription of the three-way mapping of every business term to its
UBL Invoice, UBL Credit Note and CII path (see the Field Mapping Reference below).

- Every rule carries the business term ID, the path, the EN 16931 cardinality and the allowed attributes
- Intermediate elements of a path are whitelisted implicitly
- `addKeepWhenEmpty` must be used for every element that is **mandatory in the XML Schema** of its
  parent, otherwise the empty element removal can create XSD invalid output
- Elements that the XML Schema requires but that carry no business term (e.g.
  `cac:CardAccount/cbc:NetworkID`, `ram:SpecifiedProcuringProject/ram:Name`) are added with a
  `null` business term ID
- Use a predicate when the cardinality would otherwise truncate the wrong occurrence, e.g. for
  `ram:SpecifiedTaxRegistration` which is BT-31 or BT-32 depending on `ram:ID/@schemeID`

## Testing

- JUnit 4
- `MockTestFiles` discovers all test documents below `src/test/resources/external/`
- `AbstractPurifierFuncTest.purifyAndCheck` asserts that the result is non-null, error free,
  serializable and that **purification is idempotent**
- `EN16931ConformanceTest` validates source and result with the EN 16931 Schematron rules via
  phive-rules and asserts that the purification introduces **no new rule failure**
- `PurifierRemovalTest` uses a purpose built document in `src/test/resources/external/purify/` to
  assert that all non core content is really removed

## Key Dependencies

- **en16931-basics** — the shared facts about EN 16931: editions, syntax kinds, document types and
  the XML namespace URIs. Everything that changes when the *standard* changes lives there, not here
- **ph-commons** — Helger utilities, DOM helpers, error handling, collection types
- **ph-ubl** — UBL 2.1 and 2.5 JAXB models, XSDs and marshalling
- **ph-cii** — CII D16B and D25A JAXB models, XSDs and marshalling
- **phive-rules-en16931** — EN 16931 validation rules (test scope only)
- **picocli** — CLI argument parsing (CLI module only)

## Field Mapping Reference

The field mappings with the BT identifiers are not part of this repository:

- EN 16931:2017 — `../en16931-cii2ubl/docs/en16931-2017-syntax.md`
- EN 16931:2026 — the extraction of the CEN/TC 434 draft deliverables of CEN/TS 16931-3-2:2026 and
  CEN/TS 16931-3-3:2026 (`~/svn-philip/AustrianStandards/TC 434/EN 2026/3/extraction.md`); the
  copy in `../en16931-cii2ubl/docs/en16931-2026-syntax.md` is an older and shorter revision
