/*
 * Copyright (C) 2026 Philip Helger
 * http://www.helger.com
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.en16931.purifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.en16931.basics.EEN16931DocumentType;
import com.helger.en16931.basics.EEN16931Edition;
import com.helger.en16931.basics.EEN16931SyntaxKind;
import com.helger.en16931.purifier.rule.PurificationRuleSet;
import com.helger.en16931.purifier.ruleset.EN16931CIIRules2017;
import com.helger.en16931.purifier.ruleset.EN16931UBLRules2017;

/**
 * The lookups of this project on top of the data only enums of <code>en16931-basics</code>: the
 * rule set that defines the core message of an EN 16931 edition in a syntax kind, and the purifier
 * that handles a document type. Both are kept here, because they are typed to this project and not
 * to the standard.
 *
 * @author Philip Helger
 */
@Immutable
public final class EN16931Purifiers
{
  /** The EN 16931 edition to be used if none is provided */
  public static final EEN16931Edition DEFAULT_EDITION = EEN16931Edition.EN2017;

  private EN16931Purifiers ()
  {}

  /**
   * Get the rule set defining the core message of the provided EN 16931 edition in the provided
   * syntax.
   *
   * @param eEdition
   *        The EN 16931 edition to get the rule set for. May not be <code>null</code>.
   * @param eSyntaxKind
   *        The syntax kind to get the rule set for. May not be <code>null</code>.
   * @return <code>null</code> if the provided edition has no rule set for the provided syntax kind
   *         yet.
   */
  @Nullable
  public static PurificationRuleSet getRuleSet (@NonNull final EEN16931Edition eEdition,
                                                @NonNull final EEN16931SyntaxKind eSyntaxKind)
  {
    ValueEnforcer.notNull (eEdition, "Edition");
    ValueEnforcer.notNull (eSyntaxKind, "SyntaxKind");

    return switch (eEdition)
    {
      case EN2017 -> switch (eSyntaxKind)
      {
        case UBL_INVOICE -> EN16931UBLRules2017.getInvoiceRuleSet ();
        case UBL_CREDIT_NOTE -> EN16931UBLRules2017.getCreditNoteRuleSet ();
        case CII -> EN16931CIIRules2017.getCrossIndustryInvoiceRuleSet ();
      };
      // The syntax bindings of EN 16931:2026 are not yet available
      case EN2026 -> null;
    };
  }

  /**
   * @param eEdition
   *        The EN 16931 edition to check. May not be <code>null</code>.
   * @return <code>true</code> if the rule sets of the provided edition are available,
   *         <code>false</code> if the syntax bindings of that edition are not yet implemented.
   */
  public static boolean isSupported (@NonNull final EEN16931Edition eEdition)
  {
    ValueEnforcer.notNull (eEdition, "Edition");

    for (final EEN16931SyntaxKind eSyntaxKind : EEN16931SyntaxKind.values ())
      if (getRuleSet (eEdition, eSyntaxKind) == null)
        return false;
    return true;
  }

  /**
   * Create a new purifier for the provided document type, using {@link #DEFAULT_EDITION}.
   *
   * @param eDocType
   *        The document type to create the purifier for. May not be <code>null</code>.
   * @return Never <code>null</code>.
   */
  @NonNull
  public static AbstractEN16931Purifier <?, ?> createPurifier (@NonNull final EEN16931DocumentType eDocType)
  {
    return createPurifier (eDocType, DEFAULT_EDITION);
  }

  /**
   * Create a new purifier for the provided document type.
   *
   * @param eDocType
   *        The document type to create the purifier for. May not be <code>null</code>.
   * @param eEdition
   *        The EN 16931 edition defining the core message. May not be <code>null</code>.
   * @return Never <code>null</code>.
   */
  @NonNull
  public static AbstractEN16931Purifier <?, ?> createPurifier (@NonNull final EEN16931DocumentType eDocType,
                                                               @NonNull final EEN16931Edition eEdition)
  {
    ValueEnforcer.notNull (eDocType, "DocType");
    ValueEnforcer.notNull (eEdition, "Edition");

    return switch (eDocType)
    {
      case UBL21_INVOICE -> new UBL21InvoicePurifier (eEdition);
      case UBL21_CREDIT_NOTE -> new UBL21CreditNotePurifier (eEdition);
      case UBL25_INVOICE -> new UBL25InvoicePurifier (eEdition);
      case UBL25_CREDIT_NOTE -> new UBL25CreditNotePurifier (eEdition);
      case CII_D16B -> new CIID16BPurifier (eEdition);
      case CII_D25A -> new CIID25APurifier (eEdition);
    };
  }
}
