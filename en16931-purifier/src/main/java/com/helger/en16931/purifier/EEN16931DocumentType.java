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

import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.id.IHasID;
import com.helger.base.lang.EnumHelper;
import com.helger.base.name.IHasDisplayName;

/**
 * All document types that can be purified, being the combination of a syntax kind and a concrete
 * syntax version. The syntax version cannot be derived from the document itself, because all UBL
 * 2.x versions share the same XML namespace URIs and so do all CII versions. It therefore needs to
 * be selected by the caller.
 *
 * @author Philip Helger
 */
public enum EEN16931DocumentType implements IHasID <String>, IHasDisplayName
{
  /** UBL 2.1 Invoice */
  UBL21_INVOICE ("ubl21-invoice", "UBL 2.1 Invoice", EEN16931SyntaxKind.UBL_INVOICE, "2.1", UBL21InvoicePurifier::new),
  /** UBL 2.1 Credit Note */
  UBL21_CREDIT_NOTE ("ubl21-creditnote",
                     "UBL 2.1 Credit Note",
                     EEN16931SyntaxKind.UBL_CREDIT_NOTE,
                     "2.1",
                     UBL21CreditNotePurifier::new),
  /** UBL 2.5 Invoice */
  UBL25_INVOICE ("ubl25-invoice", "UBL 2.5 Invoice", EEN16931SyntaxKind.UBL_INVOICE, "2.5", UBL25InvoicePurifier::new),
  /** UBL 2.5 Credit Note */
  UBL25_CREDIT_NOTE ("ubl25-creditnote",
                     "UBL 2.5 Credit Note",
                     EEN16931SyntaxKind.UBL_CREDIT_NOTE,
                     "2.5",
                     UBL25CreditNotePurifier::new),
  /** CII D16B Cross Industry Invoice */
  CII_D16B ("cii-d16b", "CII D16B Cross Industry Invoice", EEN16931SyntaxKind.CII, "D16B", CIID16BPurifier::new),
  /** CII D25A Cross Industry Invoice */
  CII_D25A ("cii-d25a", "CII D25A Cross Industry Invoice", EEN16931SyntaxKind.CII, "D25A", CIID25APurifier::new);

  private final String m_sID;
  private final String m_sDisplayName;
  private final EEN16931SyntaxKind m_eSyntaxKind;
  private final String m_sSyntaxVersion;
  private final Function <EEN16931Version, AbstractEN16931Purifier <?, ?>> m_aPurifierFactory;

  EEN16931DocumentType (@NonNull @Nonempty final String sID,
                        @NonNull @Nonempty final String sDisplayName,
                        @NonNull final EEN16931SyntaxKind eSyntaxKind,
                        @NonNull @Nonempty final String sSyntaxVersion,
                        @NonNull final Function <EEN16931Version, AbstractEN16931Purifier <?, ?>> aPurifierFactory)
  {
    m_sID = sID;
    m_sDisplayName = sDisplayName;
    m_eSyntaxKind = eSyntaxKind;
    m_sSyntaxVersion = sSyntaxVersion;
    m_aPurifierFactory = aPurifierFactory;
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
   * @return The syntax kind of this document type. Never <code>null</code>.
   */
  @NonNull
  public EEN16931SyntaxKind getSyntaxKind ()
  {
    return m_eSyntaxKind;
  }

  /**
   * @return The version of the syntax, e.g. <code>2.1</code> for UBL or <code>D16B</code> for CII.
   *         Neither <code>null</code> nor empty.
   */
  @NonNull
  @Nonempty
  public String getSyntaxVersion ()
  {
    return m_sSyntaxVersion;
  }

  /**
   * Create a new purifier for this document type.
   *
   * @param eVersion
   *        The EN 16931 version defining the core message. May not be <code>null</code>.
   * @return Never <code>null</code>.
   */
  @NonNull
  public AbstractEN16931Purifier <?, ?> createPurifier (@NonNull final EEN16931Version eVersion)
  {
    ValueEnforcer.notNull (eVersion, "Version");
    return m_aPurifierFactory.apply (eVersion);
  }

  @Nullable
  public static EEN16931DocumentType getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EEN16931DocumentType.class, sID);
  }

  /**
   * Find the document type of the provided syntax kind that uses the provided syntax version.
   *
   * @param eSyntaxKind
   *        The syntax kind to search. May be <code>null</code>.
   * @param sSyntaxVersion
   *        The syntax version to search, case insensitive. May be <code>null</code>.
   * @return <code>null</code> if no such document type exists.
   */
  @Nullable
  public static EEN16931DocumentType getFromSyntaxKindAndVersionOrNull (@Nullable final EEN16931SyntaxKind eSyntaxKind,
                                                                        @Nullable final String sSyntaxVersion)
  {
    if (eSyntaxKind != null && sSyntaxVersion != null)
      for (final EEN16931DocumentType e : values ())
        if (e.m_eSyntaxKind == eSyntaxKind && e.m_sSyntaxVersion.equalsIgnoreCase (sSyntaxVersion))
          return e;
    return null;
  }
}
