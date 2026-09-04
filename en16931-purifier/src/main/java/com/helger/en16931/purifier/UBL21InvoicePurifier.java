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
import com.helger.ubl21.UBL21Marshaller;

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Purifier for UBL Invoice documents, serialized with UBL 2.1.
 *
 * @author Philip Helger
 */
public class UBL21InvoicePurifier extends AbstractEN16931Purifier <InvoiceType, UBL21InvoicePurifier>
{
  /**
   * Constructor using {@link EEN16931Version#DEFAULT}
   */
  public UBL21InvoicePurifier ()
  {
    this (EEN16931Version.DEFAULT);
  }

  /**
   * Constructor
   *
   * @param eVersion
   *        The EN 16931 version defining the core message. May not be <code>null</code>.
   */
  public UBL21InvoicePurifier (@NonNull final EEN16931Version eVersion)
  {
    super (eVersion, EEN16931SyntaxKind.UBL_INVOICE);
  }

  @Override
  @NonNull
  protected GenericJAXBMarshaller <InvoiceType> createMarshaller ()
  {
    return UBL21Marshaller.invoice ();
  }
}
