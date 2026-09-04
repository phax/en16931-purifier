# en16931-purifier

<!-- ph-badge-start -->
[![Sonatype Central](https://maven-badges.sml.io/sonatype-central/com.helger/en16931-purifier-parent-pom/badge.svg)](https://maven-badges.sml.io/sonatype-central/com.helger/en16931-purifier-parent-pom/)
[![javadoc](https://javadoc.io/badge2/com.helger/en16931-purifier/javadoc.svg)](https://javadoc.io/doc/com.helger/en16931-purifier)

> If this project saved you some time or made your day a little easier, a star would mean a lot — it helps others find it too.
<!-- ph-badge-end -->

Purifier for EN 16931 invoices in the UBL and the CII syntax.

This is a Java 17+ library that removes everything from an electronic invoice that is **not** part of the EN 16931 core message, and writes the result as an XML Schema valid document in the same syntax.
Typical candidates for removal are syntax elements that carry no business term at all (like `ext:UBLExtensions`, `cbc:UBLVersionID` or `cbc:UUID`), extension data of a CIUS or of a specific ERP system, non standardized attributes (like `schemeAgencyID` or `languageID`) and XML comments.

The input syntax is also the output syntax - this project is not a converter.
Use [en16931-cii2ubl](https://github.com/phax/en16931-cii2ubl) or [en16931-ubl2cii](https://github.com/phax/en16931-ubl2cii) if you need to change the syntax.

This library is licensed under the Apache License Version 2.0.

# Supported document types

| Syntax | Versions | Document types | Marshalling |
|--------|----------|----------------|-------------|
| UBL | 2.1 and 2.5 | Invoice and Credit Note | [ph-ubl](https://github.com/phax/ph-ubl) |
| CII | D16B and D25A | Cross Industry Invoice | [ph-cii](https://github.com/phax/ph-cii) |

All UBL 2.x versions use the same XML namespace URIs and the same element names, and so do all CII versions.
The syntax version can therefore **not** be derived from the document itself - it selects the XML Schema and the JAXB model that are used for reading, validating and writing, and must be provided by the caller.

# Supported EN 16931 versions

| Version | State | Syntax binding |
|---------|-------|----------------|
| `EEN16931Version.V2017` | supported | CEN/TS 16931-3-2 (UBL) and CEN/TS 16931-3-3 (CII) |
| `EEN16931Version.V2026` | declared, not yet implemented | the syntax bindings of the 2026 revision are not yet published |

The core message of every EN 16931 version is described by a set of `PurificationRuleSet` objects - one per syntax kind.
Adding the 2026 revision therefore only means adding new rule sets in the package `com.helger.en16931.purifier.ruleset` and returning them from `EEN16931Version.getRuleSet`; neither the engine nor the purifier classes need to change.
Purifying with `V2026` currently fails with an error, so that no document is silently purified with the wrong rules.

# Usage

The entrance classes are:
* UBL 2.1 Invoice: `com.helger.en16931.purifier.UBL21InvoicePurifier`
* UBL 2.1 Credit Note: `com.helger.en16931.purifier.UBL21CreditNotePurifier`
* UBL 2.5 Invoice: `com.helger.en16931.purifier.UBL25InvoicePurifier`
* UBL 2.5 Credit Note: `com.helger.en16931.purifier.UBL25CreditNotePurifier`
* CII D16B: `com.helger.en16931.purifier.CIID16BPurifier`
* CII D25A: `com.helger.en16931.purifier.CIID25APurifier`

All of them derive from `AbstractEN16931Purifier` and offer the same `purify` methods.
Additionally an `ErrorList` object must be provided as a container for all errors that occur and for the informational entries that describe what was removed.
The purification is deemed successful, if a non-`null` object is returned **and** if the error list contains no error (`errorList.containsNoError ()`).

```java
final ErrorList aErrorList = new ErrorList ();
final UBL21InvoicePurifier aPurifier = new UBL21InvoicePurifier ();
final InvoiceType aPurified = aPurifier.purify (new File ("invoice.xml"), aErrorList);
if (aPurified != null && aErrorList.containsNoError ())
  aPurifier.write (aPurified, new File ("invoice-purified.xml"), aErrorList);
```

The purified document is read back through the XML Schema of the respective syntax version, so a non-`null` result is guaranteed to be XML Schema valid.

The following `purify` overloads are available:

| Source | Result | Remarks |
|--------|--------|---------|
| `File` | JAXB object | reads the file as an XML document |
| `org.w3c.dom.Document` | JAXB object | the source document is not modified |
| JAXB object | JAXB object | the source object is not modified |
| `File` | `File` | reads, purifies and writes in one call |
| `org.w3c.dom.Document` | `File` | purifies and writes in one call |

In addition `purifyToDocument` returns the purified `org.w3c.dom.Document` without converting it to the JAXB domain model, and `write` serializes a purified object to a `File` or an `OutputStream`.

## Configuration

All purifiers offer a fluent API with the following settings:

| Method | Default | Description |
|--------|---------|-------------|
| `setRemoveNonCoreAttributes` | `true` | Remove attributes that the syntax binding does not define, e.g. `schemeAgencyID`, `listAgencyID` or `languageID`. The attributes that carry a business term (like `schemeID`, `listID`, `unitCode` or `format`) and the attributes that the XML Schema requires (like `currencyID` and `mimeCode`) are always kept. |
| `setRemoveEmptyElements` | `true` | Remove aggregate elements that have no child element, no text content and no attribute left after the purification. Elements that are mandatory in the XML Schema are never removed, and elements that carry a value are kept even if the value is empty. |
| `setEnforceCardinalities` | `true` | Remove occurrences beyond the cardinality that EN 16931 allows, e.g. the second `cac:PartyIdentification` of the Buyer, because BT-46 is `0..1`. |
| `setRemoveComments` | `true` | Remove XML comments and processing instructions. |
| `setFormattedOutput` | `true` | Write formatted (pretty printed) XML. |

## Maven usage

Replace `x.y.z` with the effective version you want to use:

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>en16931-purifier</artifactId>
  <version>x.y.z</version>
</dependency>
```

## Commandline usage

Call it via `java -jar en16931-purifier-cli-full.jar` followed by the options and parameters.
The syntax kind (UBL Invoice, UBL Credit Note or CII) is determined automatically from the document element; the syntax version is taken from the `--ubl` respectively the `--cii` option.
The exit code is `0` if all files were purified successfully and `1` otherwise.

```
[INFO] EN 16931 Purifier v1.0.0 (build 2026-09-04T15:06:42Z)
Usage: EN16931Purifier [-hV] [--disable-wildcard-expansion] [--verbose] [--cii
                       version] [--en-version version] [--enforce-cardinalities
                       true|false] [--formatted-output true|false]
                       [--output-suffix filename part] [--remove-comments
                       true|false] [--remove-empty-elements true|false]
                       [--remove-non-core-attributes true|false] [-t directory]
                       [--ubl version] source files...
EN 16931 Purifier - removes everything that is not part of the EN 16931 core
message
      source files...        One or more UBL or CII file(s)
      --cii version          Version of the CII syntax to be used for reading
                               and writing: 'D16B' or 'D25A' (default: 'D16B')
      --disable-wildcard-expansion
                             Disable wildcard expansion of filenames
      --en-version version   Version of the EN 16931 core message: '2017' or
                               '2026' (default: '2017')
      --enforce-cardinalities true|false
                             Remove occurrences beyond the cardinality allowed
                               by EN 16931 (default: 'true')
      --formatted-output true|false
                             Write formatted XML (default: 'true')
  -h, --help                 Show this help message and exit.
      --output-suffix filename part
                             The suffix added to the output filename (default:
                               '-purified')
      --remove-comments true|false
                             Remove XML comments and processing instructions
                               (default: 'true')
      --remove-empty-elements true|false
                             Remove elements that are empty after the
                               purification (default: 'true')
      --remove-non-core-attributes true|false
                             Remove attributes that are not part of the EN
                               16931 core message (default: 'true')
  -t, --target directory     The target directory for result output (default:
                               '.')
      --ubl version          Version of the UBL syntax to be used for reading
                               and writing: '2.1' or '2.5' (default: '2.1')
  -V, --version              Print version information and exit.
      --verbose              Enable debug logging
```

Use `--verbose` to see one log entry per removed element and attribute.

# How it works

The purification is driven by a declarative whitelist and consists of four steps:

1. The source XML is read into a DOM document.
2. The document is pruned against the `PurificationRuleSet` of the configured EN 16931 version and syntax kind. Every element and every attribute that no rule allows is removed and reported in the `ErrorList` as an informational entry carrying its path.
3. The pruned document is unmarshalled with the marshaller of [ph-ubl](https://github.com/phax/ph-ubl) respectively [ph-cii](https://github.com/phax/ph-cii), using the XML Schema. This is what guarantees the XML Schema validity of the result.
4. The resulting JAXB object is written with the same marshaller.

A rule set is a list of paths relative to the document element, each of them transcribed from the syntax binding and annotated with the business term it carries, the cardinality that EN 16931 allows and the attributes that belong to the core message:

```java
aB.add ("BT-1", "/cbc:ID", ONCE);
aB.add ("BT-34", "/cac:AccountingSupplierParty/cac:Party/cbc:EndpointID", ONCE, "schemeID");
aB.add ("BG-24", "/cac:AdditionalDocumentReference", UNBOUNDED);
```

All intermediate elements of a path are whitelisted implicitly.
A path step may carry a predicate to distinguish business terms that share the same element name:

```java
aB.add ("BT-18", "/cac:AdditionalDocumentReference[cbc:DocumentTypeCode='130']", ONCE);
aB.add ("BT-31", "/cac:AccountingSupplierParty/cac:Party/cac:PartyTaxScheme[cac:TaxScheme/cbc:ID='VAT']", ONCE);
aB.add ("BT-48", ".../ram:SpecifiedTaxRegistration[ram:ID/@schemeID='VA']", ONCE);
aB.add ("BT-48", ".../ram:SpecifiedTaxRegistration[not(ram:ID/@schemeID)]", ONCE);
```

Rules with a predicate take precedence over rules without one, so the more specific rule always wins.
The supported predicates are `relative/path='value'`, `not(relative/path)` and `relative/path`, each of them optionally addressing an attribute with a trailing `/@attribute` respectively a leading `@attribute`.

# Notes on the purification result

The purification is strictly based on the syntax bindings CEN/TS 16931-3-2 (UBL) and CEN/TS 16931-3-3 (CII).
A few consequences are worth knowing:

* `cbc:UBLVersionID` is not a business term and is removed. The UBL version is implied by the XML Schema that was used.
* In CII, only `udt:DateTimeString` and `udt:DateString` with the `format` attribute are part of the core message. The alternative `udt:DateTime` and `udt:Date` representations of the same choice are removed.
* In CII, a header level `ram:AdditionalReferencedDocument` is identified by its `ram:TypeCode`: `50` is BT-17, `130` is BT-18 and `916` is BG-24. An `ram:AdditionalReferencedDocument` with any other or without a `ram:TypeCode` cannot be assigned to a business term and is removed.
* In CII, `ram:SpecifiedTaxRegistration/ram:ID` must use `@schemeID="VA"` for a VAT identifier and `@schemeID="FC"` for a tax registration identifier. A registration with a different scheme identifier - `VAT` is a frequent mistake - is not recognized by the EN 16931 validation rules either and is removed.
* In CII, BT-149 and BT-150 are bound to `ram:NetPriceProductTradePrice/ram:BasisQuantity`. A `ram:BasisQuantity` below `ram:GrossPriceProductTradePrice` carries no business term and is removed.
* In CII, `@currencyID` is only part of the core message on `ram:TaxTotalAmount`, where it discriminates BT-110 from BT-111. On all other amounts it is removed.
* The UBL Credit Note rule set contains the union of the UBL 2.1 and the UBL 2.2+ representation of BT-9 and BT-11, because both of them carry the same business term.

The test suite verifies for every test document that the purified result raises **no EN 16931 validation rule that the source document did not raise already**, and that purifying an already purified document does not remove anything else.

# Building

Requires Java 17 or higher.

```bash
mvn clean install                # Build and test everything
mvn clean package -pl en16931-purifier-cli
# Output: en16931-purifier-cli/target/en16931-purifier-cli-full.jar
```

# News and noteworthy

* v1.0.0 - work in progress
    * Initial version
    * Supports EN 16931:2017 in the UBL syntax (Invoice and Credit Note) and in the CII syntax
    * Supports UBL 2.1, UBL 2.5, CII D16B and CII D25A for reading, XML Schema validation and writing
    * Contains the extension point for EN 16931:2026

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
It is appreciated if you star the GitHub project if you like it.
