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

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.id.IHasID;
import com.helger.base.lang.EnumHelper;
import com.helger.base.name.IHasDisplayName;
import com.helger.en16931.purifier.rule.PurificationRuleSet;
import com.helger.en16931.purifier.ruleset.EN16931CIIRules2017;
import com.helger.en16931.purifier.ruleset.EN16931UBLRules2017;

/**
 * The version of the EN 16931 semantic data model that defines the core message.
 *
 * @author Philip Helger
 */
public enum EEN16931Version implements IHasID <String>, IHasDisplayName
{
  /** EN 16931-1:2017 including A1:2019 and A2:2020 */
  V2017 ("2017", "EN 16931:2017", true),
  /**
   * The 2026 revision of EN 16931-1. The syntax bindings are not yet published, so no rule sets are
   * available and every purification with this version fails with an error.
   */
  V2026 ("2026", "EN 16931:2026", false);

  /** The default version to be used */
  public static final EEN16931Version DEFAULT = V2017;

  private final String m_sID;
  private final String m_sDisplayName;
  private final boolean m_bSupported;

  EEN16931Version (@NonNull @Nonempty final String sID,
                   @NonNull @Nonempty final String sDisplayName,
                   final boolean bSupported)
  {
    m_sID = sID;
    m_sDisplayName = sDisplayName;
    m_bSupported = bSupported;
  }

  @NonNull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  @NonNull
  @Nonempty
  public String getDisplayName ()
  {
    return m_sDisplayName;
  }

  /**
   * @return <code>true</code> if the rule sets of this version are available, <code>false</code> if
   *         the syntax bindings of this version are not yet implemented.
   */
  public boolean isSupported ()
  {
    return m_bSupported;
  }

  /**
   * Get the rule set defining the core message of this EN 16931 version in the provided syntax.
   *
   * @param eSyntaxKind
   *        The syntax kind to get the rule set for. May not be <code>null</code>.
   * @return <code>null</code> if this version has no rule set for the provided syntax kind yet.
   */
  @Nullable
  public PurificationRuleSet getRuleSet (@NonNull final EEN16931SyntaxKind eSyntaxKind)
  {
    ValueEnforcer.notNull (eSyntaxKind, "SyntaxKind");

    return switch (this)
    {
      case V2017 -> switch (eSyntaxKind)
      {
        case UBL_INVOICE -> EN16931UBLRules2017.getInvoiceRuleSet ();
        case UBL_CREDIT_NOTE -> EN16931UBLRules2017.getCreditNoteRuleSet ();
        case CII -> EN16931CIIRules2017.getCrossIndustryInvoiceRuleSet ();
      };
      // The syntax bindings of EN 16931:2026 are not yet available
      case V2026 -> null;
    };
  }

  @Nullable
  public static EEN16931Version getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EEN16931Version.class, sID);
  }
}
