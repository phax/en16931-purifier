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

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.en16931.basics.EEN16931Edition;

/**
 * Functional test for the EN 16931:2026 rule sets. Beside the purpose built 2026 documents that
 * contain every business term that is new in the 2026 revision, all 2017 test documents are
 * purified with the 2026 rule sets as well: purifying them removes everything that moved to a
 * different syntax element in the 2026 revision, but the result must still be XML Schema valid and
 * the purification must still be idempotent.
 *
 * @author Philip Helger
 */
public final class EN16931Rules2026Test extends AbstractPurifierFuncTest
{
  private static final File UBL_FILE = new File (MockTestFiles.PURIFY_2026_DIR, "ubl-invoice-2026.xml");
  private static final File CII_FILE = new File (MockTestFiles.PURIFY_2026_DIR, "cii-2026.xml");

  private static final String [] UBL_MUST_BE_KEPT = { "<cbc:IssueTime>",
                                                      "<cac:BuyerAssignedReference>",
                                                      "<cbc:BuyerReferenceCode>ADE</cbc:BuyerReferenceCode>",
                                                      "<cbc:BuyerReference>BuyerReference2026</cbc:BuyerReference>",
                                                      "<cac:Annotation>",
                                                      "<cbc:SubjectCode>AAA</cbc:SubjectCode>",
                                                      "<cbc:AnnotationContent>",
                                                      "<cac:TaxExchangeRate>",
                                                      "<cbc:CalculationRate>",
                                                      "<cac:DeliveryNoteDocumentReference>",
                                                      "DELNOTE-1",
                                                      "<cbc:DocumentTypeCode>380</cbc:DocumentTypeCode>",
                                                      "<cac:SettlementPeriod>",
                                                      "<cbc:SettlementDiscountPercent>",
                                                      "<cbc:SettlementDiscountAmount",
                                                      "<cac:PenaltyPeriod>",
                                                      "<cac:PenaltyInterestRate>",
                                                      "<cbc:PenaltyAmount",
                                                      "<cbc:SupplyTypeCode>1</cbc:SupplyTypeCode>",
                                                      "<cbc:SupplyTypeCode>2</cbc:SupplyTypeCode>",
                                                      "<cbc:SupplyTypeCode>3</cbc:SupplyTypeCode>",
                                                      "<cbc:SupplyTypeCode>4</cbc:SupplyTypeCode>",
                                                      "listID=\"5153\"",
                                                      "<cac:CollectionInvoiceLine>",
                                                      "<cbc:TaxInclusiveLineExtensionAmount",
                                                      "<cbc:SalesOrderLineID>456</cbc:SalesOrderLineID>",
                                                      "<cac:DespatchLineReference>",
                                                      "<cac:ReceiptLineReference>",
                                                      "<cac:BillingReferenceLine>",
                                                      "<cac:DeliveryNoteLineReference>",
                                                      "<cbc:NameCode>COL</cbc:NameCode>",
                                                      "<cbc:ValueQuantity unitCode=\"KGM\">",
                                                      "Debited account name",
                                                      "DEBTORBIC1",
                                                      "GB-LOCAL-4711",
                                                      "<cbc:ID>LOC</cbc:ID>",
                                                      "Line deliver to party",
                                                      "Line delivery street 7" };

  private static final String [] UBL_MUST_BE_REMOVED = { "UBLVersionID",
                                                         "<cbc:Note>Legacy note",
                                                         "LegacyBuyerReference",
                                                         "UndiscriminatedDocumentReference",
                                                         "NetworkID",
                                                         "<!--" };

  private static final String [] CII_MUST_BE_KEPT = { "format=\"208\"",
                                                      "<ram:BuyerReferenceID schemeID=\"ADE\">",
                                                      "<ram:InvoiceApplicableTradeCurrencyExchange>",
                                                      "<ram:ConversionRate>",
                                                      "<ram:DeliveryNoteReferencedDocument>",
                                                      "DELNOTE-1",
                                                      "<ram:ApplicableTradePaymentDiscountTerms>",
                                                      "<ram:ActualDiscountAmount>",
                                                      "<ram:ApplicableTradePaymentPenaltyTerms>",
                                                      "<ram:ActualPenaltyAmount>",
                                                      "<ram:SpecifiedFinancialAdjustment>",
                                                      "<ram:SupplyTypeCode>1</ram:SupplyTypeCode>",
                                                      "<ram:SupplyTypeCode>2</ram:SupplyTypeCode>",
                                                      "<ram:SupplyTypeCode>3</ram:SupplyTypeCode>",
                                                      "<ram:SupplyTypeCode>4</ram:SupplyTypeCode>",
                                                      "<ram:CurrencyCode>EUR</ram:CurrencyCode>",
                                                      "listID=\"5153\"",
                                                      "listAgencyID=\"6\"",
                                                      "<ram:ValueMeasure unitCode=\"KGM\">",
                                                      "<ram:TypeCode>COL</ram:TypeCode>",
                                                      "<ram:DepartmentName>",
                                                      "Debited account name",
                                                      "DEBTORBIC1",
                                                      "GB-LOCAL-4711",
                                                      "Line deliver to party",
                                                      "Line delivery street 7",
                                                      "<ram:TypeCode>380</ram:TypeCode>",
                                                      "<ram:LineID>7</ram:LineID>",
                                                      "<ram:LineID>11</ram:LineID>",
                                                      "<ram:LineID>12</ram:LineID>",
                                                      "<ram:LineID>13</ram:LineID>",
                                                      "<ram:LineID>123</ram:LineID>",
                                                      "<ram:LineID>456</ram:LineID>" };

  private static final String [] CII_MUST_BE_REMOVED = { "TestIndicator",
                                                         "<ram:Name>Invoice</ram:Name>",
                                                         "LegacyBuyerReference",
                                                         "UndiscriminatedDocumentReference",
                                                         "BrandName",
                                                         "<!--" };

  @NonNull
  private static <T> String _purifyAndWrite (@NonNull final AbstractEN16931Purifier <T, ?> aPurifier,
                                             @NonNull final File aSrcFile)
  {
    final ErrorList aErrorList = new ErrorList ();
    final T aPurified = aPurifier.purify (aSrcFile, aErrorList);
    assertNotNull ("Failed to purify '" + aSrcFile.getPath () + "': " + aErrorList.getAllErrors (), aPurified);
    assertEquals (aErrorList.getAllErrors ().toString (), 0, aErrorList.getAllErrors ().size ());

    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
    {
      assertTrue (aPurifier.write (aPurified, aBAOS, aErrorList).isSuccess ());
      return aBAOS.getAsString (StandardCharsets.UTF_8);
    }
  }

  private static void _assertTokens (@NonNull final String sPurified,
                                     final String [] aMustBeKept,
                                     final String [] aMustBeRemoved)
  {
    for (final String sToken : aMustBeKept)
      assertTrue ("The purified document is missing '" + sToken + "'", sPurified.contains (sToken));
    for (final String sToken : aMustBeRemoved)
      assertFalse ("The purified document still contains '" + sToken + "'", sPurified.contains (sToken));
  }

  @Test
  public void testUBLCoreMessage2026 ()
  {
    final UBL25InvoicePurifier aPurifier = new UBL25InvoicePurifier (EEN16931Edition.EN2026);
    _assertTokens (_purifyAndWrite (aPurifier, UBL_FILE), UBL_MUST_BE_KEPT, UBL_MUST_BE_REMOVED);
    purifyAndCheck (aPurifier, UBL_FILE);
  }

  @Test
  public void testCIICoreMessage2026 ()
  {
    final CIID25APurifier aPurifier = new CIID25APurifier (EEN16931Edition.EN2026);
    _assertTokens (_purifyAndWrite (aPurifier, CII_FILE), CII_MUST_BE_KEPT, CII_MUST_BE_REMOVED);
    purifyAndCheck (aPurifier, CII_FILE);
  }

  @Test
  public void testPurifyAllUBLInvoices ()
  {
    final UBL25InvoicePurifier aPurifier = new UBL25InvoicePurifier (EEN16931Edition.EN2026);
    for (final File aFile : MockTestFiles.getAllUBLInvoiceFiles ())
      purifyAndCheck (aPurifier, aFile);
  }

  @Test
  public void testPurifyAllUBLCreditNotes ()
  {
    final UBL25CreditNotePurifier aPurifier = new UBL25CreditNotePurifier (EEN16931Edition.EN2026);
    for (final File aFile : MockTestFiles.getAllUBLCreditNoteFiles ())
      purifyAndCheck (aPurifier, aFile);
  }

  @Test
  public void testPurifyAllCII ()
  {
    final CIID25APurifier aPurifier = new CIID25APurifier (EEN16931Edition.EN2026);
    for (final File aFile : MockTestFiles.getAllCIIFiles ())
      purifyAndCheck (aPurifier, aFile);
  }
}
