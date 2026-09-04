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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.diagnostics.error.list.ErrorList;

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Test that all content that is not part of the EN 16931 core message is really removed.
 *
 * @author Philip Helger
 */
public final class PurifierRemovalTest
{
  private static final File SRC_FILE = new File (MockTestFiles.PURIFY_DIR, "ubl-invoice-extensions.xml");

  private static final String [] MUST_BE_REMOVED = { "UBLExtensions",
                                                     "SomeVendorSpecificData",
                                                     "UBLVersionID",
                                                     "CopyIndicator",
                                                     "UUID",
                                                     "IssueTime",
                                                     "LineCountNumeric",
                                                     "StatementDocumentReference",
                                                     "cac:Signature",
                                                     "CustomerAssignedAccountID",
                                                     "WebsiteURI",
                                                     "cac:Person",
                                                     "FirstName",
                                                     "TrackingID",
                                                     "InstructionNote",
                                                     "<cbc:CurrencyCode>",
                                                     "RoundingAmount",
                                                     "FreeOfChargeIndicator",
                                                     "BrandName",
                                                     "OrderableUnitFactorRate",
                                                     "schemeAgencyID",
                                                     "listAgencyID",
                                                     "languageID",
                                                     "unitCodeListID",
                                                     "schemaLocation",
                                                     "<!--" };

  private static final String [] MUST_BE_KEPT = { "<cbc:CustomizationID>",
                                                  "<cbc:ProfileID>",
                                                  "INV-2026-0001",
                                                  "<cbc:IssueDate>",
                                                  "<cbc:DueDate>",
                                                  "<cbc:InvoiceTypeCode>",
                                                  "An invoice note",
                                                  "<cbc:DocumentCurrencyCode>",
                                                  "<cbc:BuyerReference>",
                                                  "schemeID=\"0088\"",
                                                  "SupplierTradingName Ltd.",
                                                  "SupplierOfficialName Ltd",
                                                  "Buyer Official Name",
                                                  "name=\"Credit transfer\"",
                                                  "IBAN32423940",
                                                  "<cbc:PayableAmount currencyID=\"EUR\">",
                                                  "unitCode=\"C62\"",
                                                  "item name" };

  @Test
  public void testRemoveAllNonCoreContent ()
  {
    final ErrorList aErrorList = new ErrorList ();
    final UBL21InvoicePurifier aPurifier = new UBL21InvoicePurifier ();
    final InvoiceType aPurified = aPurifier.purify (SRC_FILE, aErrorList);
    assertNotNull (aPurified);
    assertEquals (aErrorList.getAllErrors ().toString (), 0, aErrorList.getAllErrors ().size ());

    final String sPurified;
    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
    {
      assertTrue (aPurifier.write (aPurified, aBAOS, aErrorList).isSuccess ());
      sPurified = aBAOS.getAsString (StandardCharsets.UTF_8);
    }

    for (final String sToken : MUST_BE_REMOVED)
      assertFalse ("The purified document still contains '" + sToken + "'", sPurified.contains (sToken));
    for (final String sToken : MUST_BE_KEPT)
      assertTrue ("The purified document is missing '" + sToken + "'", sPurified.contains (sToken));
  }

  @Test
  public void testKeepEverythingIfDisabled ()
  {
    final ErrorList aErrorList = new ErrorList ();
    final UBL21InvoicePurifier aPurifier = new UBL21InvoicePurifier ().setRemoveNonCoreAttributes (false);
    final InvoiceType aPurified = aPurifier.purify (SRC_FILE, aErrorList);
    assertNotNull (aPurified);

    final String sPurified;
    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
    {
      assertTrue (aPurifier.write (aPurified, aBAOS, aErrorList).isSuccess ());
      sPurified = aBAOS.getAsString (StandardCharsets.UTF_8);
    }

    // Non core elements are still removed, but non core attributes are kept
    assertFalse (sPurified.contains ("UBLVersionID"));
    assertTrue (sPurified.contains ("schemeAgencyID"));
    assertTrue (sPurified.contains ("languageID"));
  }
}
