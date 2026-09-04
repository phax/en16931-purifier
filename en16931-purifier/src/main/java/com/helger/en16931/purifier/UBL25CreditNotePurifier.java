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

import com.helger.jaxb.GenericJAXBMarshaller;
import com.helger.ubl25.UBL25Marshaller;

import oasis.names.specification.ubl.schema.xsd.creditnote_25.CreditNoteType;

/**
 * Purifier for UBL Credit Note documents, serialized with UBL 2.5.
 *
 * @author Philip Helger
 */
public class UBL25CreditNotePurifier extends AbstractEN16931Purifier <CreditNoteType, UBL25CreditNotePurifier>
{
  /**
   * Constructor using {@link EEN16931Version#DEFAULT}
   */
  public UBL25CreditNotePurifier ()
  {
    this (EEN16931Version.DEFAULT);
  }

  /**
   * Constructor
   *
   * @param eVersion
   *        The EN 16931 version defining the core message. May not be <code>null</code>.
   */
  public UBL25CreditNotePurifier (@NonNull final EEN16931Version eVersion)
  {
    super (eVersion, EEN16931SyntaxKind.UBL_CREDIT_NOTE);
  }

  @Override
  @NonNull
  protected GenericJAXBMarshaller <CreditNoteType> createMarshaller ()
  {
    return UBL25Marshaller.creditNote ();
  }
}
