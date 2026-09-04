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

import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.base.id.IHasID;
import com.helger.base.lang.EnumHelper;
import com.helger.base.name.IHasDisplayName;

/**
 * The kind of document syntax an EN 16931 core message may use. It is independent of the concrete
 * syntax version, because all UBL 2.x versions use the same XML namespace URIs and element names,
 * and so do all CII versions.
 *
 * @author Philip Helger
 */
public enum EEN16931SyntaxKind implements IHasID <String>, IHasDisplayName
{
  /** UBL Invoice */
  UBL_INVOICE ("ubl-invoice", "UBL Invoice", CEN16931Syntax.QNAME_UBL_INVOICE),
  /** UBL Credit Note */
  UBL_CREDIT_NOTE ("ubl-creditnote", "UBL Credit Note", CEN16931Syntax.QNAME_UBL_CREDIT_NOTE),
  /** CII Cross Industry Invoice */
  CII ("cii", "CII Cross Industry Invoice", CEN16931Syntax.QNAME_CII);

  private final String m_sID;
  private final String m_sDisplayName;
  private final QName m_aRootElementName;

  EEN16931SyntaxKind (@NonNull @Nonempty final String sID,
                      @NonNull @Nonempty final String sDisplayName,
                      @NonNull final QName aRootElementName)
  {
    m_sID = sID;
    m_sDisplayName = sDisplayName;
    m_aRootElementName = aRootElementName;
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
   * @return The name of the document element of this syntax kind. Never <code>null</code>.
   */
  @NonNull
  public QName getRootElementName ()
  {
    return m_aRootElementName;
  }

  @Nullable
  public static EEN16931SyntaxKind getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EEN16931SyntaxKind.class, sID);
  }

  /**
   * Determine the syntax kind from the name of the document element.
   *
   * @param aRootElementName
   *        The document element name to search. May be <code>null</code>.
   * @return <code>null</code> if no syntax kind uses that document element.
   */
  @Nullable
  public static EEN16931SyntaxKind getFromRootElementNameOrNull (@Nullable final QName aRootElementName)
  {
    if (aRootElementName != null)
      for (final EEN16931SyntaxKind e : values ())
        if (e.m_aRootElementName.equals (aRootElementName))
          return e;
    return null;
  }
}
