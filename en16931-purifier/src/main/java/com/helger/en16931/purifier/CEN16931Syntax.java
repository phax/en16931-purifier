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

import com.helger.annotation.concurrent.Immutable;
import com.helger.cii.d16b.CCIID16B;
import com.helger.ubl21.CUBL21;

/**
 * Constants of the XML syntaxes used by the EN 16931 syntax bindings.
 * <p>
 * All UBL 2.x versions use the same XML namespace URIs, and so do all CII versions. Therefore the
 * constants in this class are valid for UBL 2.1 as well as UBL 2.5, and for CII D16B as well as
 * CII D25A.
 *
 * @author Philip Helger
 */
@Immutable
public final class CEN16931Syntax
{
  /** The XML namespace URI of the UBL Invoice document */
  public static final String NS_URI_UBL_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
  /** The XML namespace URI of the UBL CreditNote document */
  public static final String NS_URI_UBL_CREDIT_NOTE = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2";
  /** The XML namespace URI of the UBL Common Basic Components */
  public static final String NS_URI_UBL_CBC = CUBL21.XML_SCHEMA_CBC_NAMESPACE_URL;
  /** The XML namespace URI of the UBL Common Aggregate Components */
  public static final String NS_URI_UBL_CAC = CUBL21.XML_SCHEMA_CAC_NAMESPACE_URL;

  /** The XML namespace URI of the CII CrossIndustryInvoice document */
  public static final String NS_URI_CII_RSM = CCIID16B.XML_SCHEMA_RSM_NAMESPACE_URL;
  /** The XML namespace URI of the CII Reusable Aggregate Business Information Entities */
  public static final String NS_URI_CII_RAM = CCIID16B.XML_SCHEMA_RAM_NAMESPACE_URL;
  /** The XML namespace URI of the CII Unqualified Data Types */
  public static final String NS_URI_CII_UDT = CCIID16B.XML_SCHEMA_UDT_NAMESPACE_URL;
  /** The XML namespace URI of the CII Qualified Data Types */
  public static final String NS_URI_CII_QDT = CCIID16B.XML_SCHEMA_QDT_NAMESPACE_URL;

  /** The XML namespace prefix used in the rule set paths of UBL Common Basic Components */
  public static final String PREFIX_UBL_CBC = "cbc";
  /** The XML namespace prefix used in the rule set paths of UBL Common Aggregate Components */
  public static final String PREFIX_UBL_CAC = "cac";
  /** The XML namespace prefix used in the rule set paths of the CII document */
  public static final String PREFIX_CII_RSM = "rsm";
  /** The XML namespace prefix used in the rule set paths of the CII aggregates */
  public static final String PREFIX_CII_RAM = "ram";
  /** The XML namespace prefix used in the rule set paths of the CII unqualified data types */
  public static final String PREFIX_CII_UDT = "udt";
  /** The XML namespace prefix used in the rule set paths of the CII qualified data types */
  public static final String PREFIX_CII_QDT = "qdt";

  /** The document element of a UBL Invoice */
  public static final QName QNAME_UBL_INVOICE = new QName (NS_URI_UBL_INVOICE, "Invoice");
  /** The document element of a UBL CreditNote */
  public static final QName QNAME_UBL_CREDIT_NOTE = new QName (NS_URI_UBL_CREDIT_NOTE, "CreditNote");
  /** The document element of a CII CrossIndustryInvoice */
  public static final QName QNAME_CII = new QName (NS_URI_CII_RSM, "CrossIndustryInvoice");

  private CEN16931Syntax ()
  {}
}
