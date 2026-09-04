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

import com.helger.cii.d25a.CIID25ACrossIndustryInvoiceTypeMarshaller;
import com.helger.jaxb.GenericJAXBMarshaller;

import un.unece.uncefact.data.standard.cii.d25a.CrossIndustryInvoiceType;

/**
 * Purifier for CII Cross Industry Invoice documents, serialized with CII D25A.
 *
 * @author Philip Helger
 */
public class CIID25APurifier extends AbstractEN16931Purifier <CrossIndustryInvoiceType, CIID25APurifier>
{
  /**
   * Constructor using {@link EEN16931Version#DEFAULT}
   */
  public CIID25APurifier ()
  {
    this (EEN16931Version.DEFAULT);
  }

  /**
   * Constructor
   *
   * @param eVersion
   *        The EN 16931 version defining the core message. May not be <code>null</code>.
   */
  public CIID25APurifier (@NonNull final EEN16931Version eVersion)
  {
    super (eVersion, EEN16931SyntaxKind.CII);
  }

  @Override
  @NonNull
  protected GenericJAXBMarshaller <CrossIndustryInvoiceType> createMarshaller ()
  {
    return new CIID25ACrossIndustryInvoiceTypeMarshaller ();
  }
}
