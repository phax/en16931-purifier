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
package com.helger.en16931.purifier.ruleset;

import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.en16931.basics.CEN16931Syntax;
import com.helger.en16931.purifier.rule.PurificationRuleNode;
import com.helger.en16931.purifier.rule.PurificationRuleSet;
import com.helger.en16931.purifier.rule.PurificationRuleSetBuilder;

/**
 * The EN 16931:2026 core message in the UBL syntax, according to CEN/TS 16931-3-2:2026.
 * <p>
 * Other than the 2017 edition, this binding is bound to <b>UBL 2.5 or later</b>. The source
 * document states that "only UBL 2.5 and any subsequent UBL 2.x version have all syntax elements
 * which are needed for the current revision EN 16931-1:2026". Several business terms moved to
 * elements that do not exist in UBL 2.1 at all - BT-10 to <code>cac:BuyerAssignedReference</code>,
 * BT-21 and BT-22 to <code>cac:Annotation</code>, BG-34 to
 * <code>cac:CollectionInvoiceLine</code> - and <code>cac:CardAccount/cbc:NetworkID</code>, which
 * is mandatory in the UBL 2.1 XML Schema, is neither a business term nor mandatory any more and is
 * therefore removed.
 * <p>
 * In the 2026 edition the Credit Note binding is a purely mechanical rename of the Invoice
 * binding, so both rule sets are created by the same method: <code>/Invoice</code> becomes
 * <code>/CreditNote</code>, <code>cac:InvoiceLine</code> becomes <code>cac:CreditNoteLine</code>,
 * <code>cac:CollectionInvoiceLine</code> becomes <code>cac:CollectionCreditNoteLine</code>,
 * <code>cbc:InvoicedQuantity</code> becomes <code>cbc:CreditedQuantity</code> and
 * <code>cbc:InvoiceTypeCode</code> becomes <code>cbc:CreditNoteTypeCode</code>.
 *
 * @author Philip Helger
 */
@Immutable
public final class EN16931UBLRules2026
{
  private static final int ONCE = 1;
  private static final int TWICE = 2;
  private static final int UNBOUNDED = PurificationRuleNode.UNBOUNDED;

  private static final String SELLER = "/cac:AccountingSupplierParty/cac:Party";
  private static final String BUYER = "/cac:AccountingCustomerParty/cac:Party";
  private static final String PAYEE = "/cac:PayeeParty";
  private static final String TAX_REP = "/cac:TaxRepresentativeParty";
  private static final String DELIVERY = "/cac:Delivery";
  private static final String PAYMENT_MEANS = "/cac:PaymentMeans";
  private static final String PAYMENT_TERMS = "/cac:PaymentTerms";
  private static final String ALLOWANCE_CHARGE = "/cac:AllowanceCharge";
  private static final String TAX_TOTAL = "/cac:TaxTotal";
  private static final String TOTALS = "/cac:LegalMonetaryTotal";
  private static final String PTS_VAT = "/cac:PartyTaxScheme[cac:TaxScheme/cbc:ID='VAT']";
  private static final String PTS_LOC = "/cac:PartyTaxScheme[cac:TaxScheme/cbc:ID='LOC']";
  private static final String PTS_NO_SCHEME = "/cac:PartyTaxScheme[not(cac:TaxScheme/cbc:ID)]";
  private static final String ADR_BT18 = "/cac:AdditionalDocumentReference[cbc:DocumentTypeCode='130']";
  private static final String ADR_BG24 = "/cac:AdditionalDocumentReference[cbc:DocumentTypeCode='916']";

  private static final PurificationRuleSet INVOICE = _createInvoiceRuleSet ();
  private static final PurificationRuleSet CREDIT_NOTE = _createCreditNoteRuleSet ();

  private EN16931UBLRules2026 ()
  {}

  private static void _addTaxSchemeRules (@NonNull final PurificationRuleSetBuilder aB,
                                          @NonNull final String sBTTaxSchemeCode,
                                          @NonNull @Nonempty final String sBase)
  {
    // cac:TaxScheme is mandatory in cac:TaxCategory and cac:PartyTaxScheme
    aB.addKeepWhenEmpty (null, sBase + "/cac:TaxScheme", ONCE);
    aB.add (sBTTaxSchemeCode, sBase + "/cac:TaxScheme/cbc:ID", ONCE);
  }

  private static void _addTaxCategoryRules (@NonNull final PurificationRuleSetBuilder aB,
                                            @NonNull @Nonempty final String sBase,
                                            @NonNull final String sBTCategoryCode,
                                            @NonNull final String sBTTaxSchemeCode,
                                            @NonNull final String sBTRate,
                                            @NonNull final String sBTExemptionReason,
                                            @NonNull final String sBTExemptionReasonCode,
                                            @NonNull final String sBTSupplyTypeCode)
  {
    aB.add (sBTCategoryCode, sBase + "/cbc:ID", ONCE);
    aB.add (sBTRate, sBase + "/cbc:Percent", ONCE);
    aB.add (sBTExemptionReason, sBase + "/cbc:TaxExemptionReason", ONCE);
    aB.add (sBTExemptionReasonCode, sBase + "/cbc:TaxExemptionReasonCode", ONCE);
    aB.add (sBTSupplyTypeCode, sBase + "/cbc:SupplyTypeCode", ONCE);
    _addTaxSchemeRules (aB, sBTTaxSchemeCode, sBase);
  }

  private static void _addPostalAddressRules (@NonNull final PurificationRuleSetBuilder aB,
                                              @NonNull @Nonempty final String sBase,
                                              @NonNull final String sBTGroup,
                                              @NonNull final String sBTLine1,
                                              @NonNull final String sBTLine2,
                                              @NonNull final String sBTLine3,
                                              @NonNull final String sBTCity,
                                              @NonNull final String sBTPostCode,
                                              @NonNull final String sBTCountrySubdivision,
                                              @NonNull final String sBTCountryCode)
  {
    aB.add (sBTGroup, sBase, ONCE);
    aB.add (sBTLine1, sBase + "/cbc:StreetName", ONCE);
    aB.add (sBTLine2, sBase + "/cbc:AdditionalStreetName", ONCE);
    aB.add (sBTCity, sBase + "/cbc:CityName", ONCE);
    aB.add (sBTPostCode, sBase + "/cbc:PostalZone", ONCE);
    aB.add (sBTCountrySubdivision, sBase + "/cbc:CountrySubentity", ONCE);
    aB.add (sBTLine3, sBase + "/cac:AddressLine", ONCE);
    aB.add (sBTLine3, sBase + "/cac:AddressLine/cbc:Line", ONCE);
    aB.add (sBTCountryCode, sBase + "/cac:Country", ONCE);
    aB.add (sBTCountryCode, sBase + "/cac:Country/cbc:IdentificationCode", ONCE);
  }

  private static void _addContactRules (@NonNull final PurificationRuleSetBuilder aB,
                                        @NonNull @Nonempty final String sBase,
                                        @NonNull final String sBTGroup,
                                        @NonNull final String sBTName,
                                        @NonNull final String sBTTelephone,
                                        @NonNull final String sBTElectronicMail)
  {
    aB.add (sBTGroup, sBase + "/cac:Contact", ONCE);
    aB.add (sBTName, sBase + "/cac:Contact/cbc:Name", ONCE);
    aB.add (sBTTelephone, sBase + "/cac:Contact/cbc:Telephone", ONCE);
    aB.add (sBTElectronicMail, sBase + "/cac:Contact/cbc:ElectronicMail", ONCE);
  }

  private static void _addDeliveryRules (@NonNull final PurificationRuleSetBuilder aB,
                                         @NonNull @Nonempty final String sBase,
                                         @NonNull final String sBTGroup,
                                         @NonNull final String sBTPartyName,
                                         @NonNull final String sBTLocationID,
                                         @NonNull final String sBTActualDeliveryDate,
                                         @NonNull final String sBTAddressGroup,
                                         @NonNull final String sBTLine1,
                                         @NonNull final String sBTLine2,
                                         @NonNull final String sBTLine3,
                                         @NonNull final String sBTCity,
                                         @NonNull final String sBTPostCode,
                                         @NonNull final String sBTCountrySubdivision,
                                         @NonNull final String sBTCountryCode)
  {
    aB.add (sBTGroup, sBase, ONCE);
    aB.add (sBTActualDeliveryDate, sBase + "/cbc:ActualDeliveryDate", ONCE);
    aB.add (sBTLocationID, sBase + "/cac:DeliveryLocation", ONCE);
    aB.add (sBTLocationID, sBase + "/cac:DeliveryLocation/cbc:ID", ONCE, "schemeID");
    _addPostalAddressRules (aB,
                            sBase + "/cac:DeliveryLocation/cac:Address",
                            sBTAddressGroup,
                            sBTLine1,
                            sBTLine2,
                            sBTLine3,
                            sBTCity,
                            sBTPostCode,
                            sBTCountrySubdivision,
                            sBTCountryCode);
    aB.add (sBTPartyName, sBase + "/cac:DeliveryParty", ONCE);
    aB.add (sBTPartyName, sBase + "/cac:DeliveryParty/cac:PartyName", ONCE);
    aB.add (sBTPartyName, sBase + "/cac:DeliveryParty/cac:PartyName/cbc:Name", ONCE);
  }

  private static void _addPaymentTermsRules (@NonNull final PurificationRuleSetBuilder aB)
  {
    // BG-33 PAYMENT TERMS, BG-35 EARLY PAYMENT DISCOUNT and BG-36 LATE PAYMENT PENALTY all share
    // the cac:PaymentTerms base path. The source gives no explicit discriminator; the two new
    // groups are identified by the elements they use
    aB.add ("BG-33/BG-35/BG-36", PAYMENT_TERMS, UNBOUNDED);
    aB.add ("BT-20", PAYMENT_TERMS + "/cbc:Note", ONCE);
    // BG-35 EARLY PAYMENT DISCOUNT
    aB.add ("BG-35", PAYMENT_TERMS + "/cac:SettlementPeriod", ONCE);
    aB.add ("BT-170", PAYMENT_TERMS + "/cac:SettlementPeriod/cbc:EndDate", ONCE);
    aB.add ("BT-171", PAYMENT_TERMS + "/cbc:SettlementDiscountPercent", ONCE);
    aB.add ("BT-172", PAYMENT_TERMS + "/cbc:SettlementDiscountAmount", ONCE, "currencyID");
    // BG-36 LATE PAYMENT PENALTY
    aB.add ("BG-36", PAYMENT_TERMS + "/cac:PenaltyPeriod", ONCE);
    aB.add ("BT-181", PAYMENT_TERMS + "/cac:PenaltyPeriod/cbc:StartDate", ONCE);
    aB.add ("BT-182", PAYMENT_TERMS + "/cac:PenaltyInterestRate", ONCE);
    aB.add ("BT-182", PAYMENT_TERMS + "/cac:PenaltyInterestRate/cbc:InterestRatePercent", ONCE);
    aB.add ("BT-183", PAYMENT_TERMS + "/cbc:PenaltyAmount", ONCE, "currencyID");
  }

  private static void _addBG24Rules (@NonNull final PurificationRuleSetBuilder aB)
  {
    // BG-24 ADDITIONAL SUPPORTING DOCUMENTS - discriminated by BT-122-1
    aB.add ("BG-24", ADR_BG24, UNBOUNDED);
    aB.add ("BT-122", ADR_BG24 + "/cbc:ID", ONCE);
    aB.add ("BT-122-1", ADR_BG24 + "/cbc:DocumentTypeCode", ONCE, "listID");
    aB.add ("BT-123", ADR_BG24 + "/cbc:DocumentDescription", ONCE);
    aB.add ("BT-125", ADR_BG24 + "/cac:Attachment", ONCE);
    aB.add ("BT-125",
            ADR_BG24 + "/cac:Attachment/cbc:EmbeddedDocumentBinaryObject",
            ONCE,
            "mimeCode",
            "filename");
    aB.add ("BT-124", ADR_BG24 + "/cac:Attachment/cac:ExternalReference", ONCE);
    aB.add ("BT-124", ADR_BG24 + "/cac:Attachment/cac:ExternalReference/cbc:URI", ONCE);
  }

  private static void _addLineRules (@NonNull final PurificationRuleSetBuilder aB,
                                     @NonNull @Nonempty final String sLine,
                                     @NonNull @Nonempty final String sQuantityElement)
  {
    aB.addKeepWhenEmpty ("BG-25", sLine, UNBOUNDED);
    aB.add ("BT-126", sLine + "/cbc:ID", ONCE);
    aB.add ("BT-127", sLine + "/cbc:Note", ONCE);
    aB.add ("BT-129", sLine + "/cbc:" + sQuantityElement, ONCE, "unitCode");
    aB.add ("BT-131", sLine + "/cbc:LineExtensionAmount", ONCE, "currencyID");
    aB.add ("BT-133", sLine + "/cbc:AccountingCost", ONCE);

    // BT-128 Invoice line object identifier
    final String sLineDocRef = sLine + "/cac:DocumentReference[cbc:DocumentTypeCode='130']";
    aB.add ("BT-128", sLineDocRef, ONCE);
    aB.add ("BT-128", sLineDocRef + "/cbc:ID", ONCE, "schemeID");
    aB.add ("BT-128-2", sLineDocRef + "/cbc:DocumentTypeCode", ONCE);

    // BT-188, BT-132, BT-200 and BT-201 purchase and sales order references
    aB.add ("BT-132/BT-188/BT-200/BT-201", sLine + "/cac:OrderLineReference", ONCE);
    aB.add ("BT-132", sLine + "/cac:OrderLineReference/cbc:LineID", ONCE);
    aB.add ("BT-201", sLine + "/cac:OrderLineReference/cbc:SalesOrderLineID", ONCE);
    aB.add ("BT-188/BT-200", sLine + "/cac:OrderLineReference/cac:OrderReference", ONCE);
    aB.add ("BT-188", sLine + "/cac:OrderLineReference/cac:OrderReference/cbc:ID", ONCE);
    aB.add ("BT-200", sLine + "/cac:OrderLineReference/cac:OrderReference/cbc:SalesOrderID", ONCE);

    // BT-189 and BT-190 despatch advice reference
    aB.add ("BT-189/BT-190", sLine + "/cac:DespatchLineReference", ONCE);
    aB.add ("BT-190", sLine + "/cac:DespatchLineReference/cbc:LineID", ONCE);
    aB.add ("BT-189", sLine + "/cac:DespatchLineReference/cac:DocumentReference", ONCE);
    aB.add ("BT-189", sLine + "/cac:DespatchLineReference/cac:DocumentReference/cbc:ID", ONCE);
    // BT-191 and BT-192 receiving advice reference
    aB.add ("BT-191/BT-192", sLine + "/cac:ReceiptLineReference", ONCE);
    aB.add ("BT-192", sLine + "/cac:ReceiptLineReference/cbc:LineID", ONCE);
    aB.add ("BT-191", sLine + "/cac:ReceiptLineReference/cac:DocumentReference", ONCE);
    aB.add ("BT-191", sLine + "/cac:ReceiptLineReference/cac:DocumentReference/cbc:ID", ONCE);

    // BG-39 LINE-LEVEL PRECEDING INVOICE REFERENCE
    final String sLineBillingRef = sLine + "/cac:BillingReference";
    aB.add ("BG-39", sLineBillingRef, UNBOUNDED);
    aB.add ("BG-39", sLineBillingRef + "/cac:InvoiceDocumentReference", ONCE);
    aB.add ("BT-217", sLineBillingRef + "/cac:InvoiceDocumentReference/cbc:ID", ONCE);
    // The source maps BT-218 "Line-level preceding invoice issue date" to cbc:IssueTime, while
    // its CII counterpart is a date; cbc:IssueDate is kept as well, because that is what the
    // business term asks for
    aB.add ("BT-218", sLineBillingRef + "/cac:InvoiceDocumentReference/cbc:IssueTime", ONCE);
    aB.add ("BT-218", sLineBillingRef + "/cac:InvoiceDocumentReference/cbc:IssueDate", ONCE);
    aB.add ("BT-219", sLineBillingRef + "/cac:InvoiceDocumentReference/cbc:DocumentTypeCode", ONCE);
    aB.add ("BT-220", sLineBillingRef + "/cac:BillingReferenceLine", ONCE);
    aB.add ("BT-220", sLineBillingRef + "/cac:BillingReferenceLine/cbc:ID", ONCE);

    // BG-37 INVOICE LINE DELIVERY INFORMATION and BG-38 INVOICE LINE DELIVER TO ADDRESS
    _addDeliveryRules (aB,
                       sLine + "/cac:Delivery",
                       "BG-37",
                       "BT-185",
                       "BT-186",
                       "BT-187",
                       "BG-38",
                       "BT-203",
                       "BT-204",
                       "BT-205",
                       "BT-206",
                       "BT-207",
                       "BT-208",
                       "BT-209");
    // BT-198 and BT-199 delivery note reference
    aB.add ("BT-198", sLine + "/cac:Delivery/cac:DeliveryNoteDocumentReference", ONCE);
    aB.add ("BT-198", sLine + "/cac:Delivery/cac:DeliveryNoteDocumentReference/cbc:ID", ONCE);
    aB.add ("BT-199", sLine + "/cac:Delivery/cac:DeliveryNoteLineReference", ONCE);
    aB.add ("BT-199", sLine + "/cac:Delivery/cac:DeliveryNoteLineReference/cbc:LineID", ONCE);

    // BG-26 INVOICE LINE PERIOD
    aB.add ("BG-26", sLine + "/cac:InvoicePeriod", ONCE);
    aB.add ("BT-134", sLine + "/cac:InvoicePeriod/cbc:StartDate", ONCE);
    aB.add ("BT-135", sLine + "/cac:InvoicePeriod/cbc:EndDate", ONCE);

    // BG-27 INVOICE LINE ALLOWANCES and BG-28 INVOICE LINE CHARGES AND TAXES
    aB.add ("BG-27/BG-28", sLine + "/cac:AllowanceCharge", UNBOUNDED);
    aB.add ("BG-27-1/BG-28-1", sLine + "/cac:AllowanceCharge/cbc:ChargeIndicator", ONCE);
    // BT-193 Invoice line-level non-VAT tax type code shares the element with BT-140 and BT-145
    // and is discriminated by BT-193-1, the @listID
    aB.add ("BT-140/BT-145/BT-193",
            sLine + "/cac:AllowanceCharge/cbc:AllowanceChargeReasonCode",
            ONCE,
            "listID");
    aB.add ("BT-139/BT-144", sLine + "/cac:AllowanceCharge/cbc:AllowanceChargeReason", ONCE);
    aB.add ("BT-138/BT-143", sLine + "/cac:AllowanceCharge/cbc:MultiplierFactorNumeric", ONCE);
    aB.add ("BT-136/BT-141", sLine + "/cac:AllowanceCharge/cbc:Amount", ONCE, "currencyID");
    aB.add ("BT-137/BT-142", sLine + "/cac:AllowanceCharge/cbc:BaseAmount", ONCE, "currencyID");

    // BG-31 ITEM INFORMATION
    aB.addKeepWhenEmpty ("BG-31", sLine + "/cac:Item", ONCE);
    aB.add ("BT-154", sLine + "/cac:Item/cbc:Description", ONCE);
    aB.add ("BT-153", sLine + "/cac:Item/cbc:Name", ONCE);
    aB.add ("BT-156", sLine + "/cac:Item/cac:BuyersItemIdentification", ONCE);
    aB.add ("BT-156", sLine + "/cac:Item/cac:BuyersItemIdentification/cbc:ID", ONCE);
    aB.add ("BT-155", sLine + "/cac:Item/cac:SellersItemIdentification", ONCE);
    aB.add ("BT-155", sLine + "/cac:Item/cac:SellersItemIdentification/cbc:ID", ONCE);
    aB.add ("BT-157", sLine + "/cac:Item/cac:StandardItemIdentification", ONCE);
    aB.add ("BT-157", sLine + "/cac:Item/cac:StandardItemIdentification/cbc:ID", ONCE, "schemeID");
    aB.add ("BT-159", sLine + "/cac:Item/cac:OriginCountry", ONCE);
    aB.add ("BT-159", sLine + "/cac:Item/cac:OriginCountry/cbc:IdentificationCode", ONCE);
    aB.add ("BT-158", sLine + "/cac:Item/cac:CommodityClassification", UNBOUNDED);
    aB.add ("BT-158",
            sLine + "/cac:Item/cac:CommodityClassification/cbc:ItemClassificationCode",
            ONCE,
            "listID",
            "listVersionID");

    // BG-30 LINE VAT INFORMATION
    aB.add ("BG-30", sLine + "/cac:Item/cac:ClassifiedTaxCategory", ONCE);
    _addTaxCategoryRules (aB,
                          sLine + "/cac:Item/cac:ClassifiedTaxCategory",
                          "BT-151",
                          "BT-151-1",
                          "BT-152",
                          "BT-194",
                          "BT-195",
                          "BT-196");

    // BG-32 ITEM ATTRIBUTE
    aB.add ("BG-32", sLine + "/cac:Item/cac:AdditionalItemProperty", UNBOUNDED);
    aB.add ("BT-211", sLine + "/cac:Item/cac:AdditionalItemProperty/cbc:NameCode", ONCE);
    aB.add ("BT-160", sLine + "/cac:Item/cac:AdditionalItemProperty/cbc:Name", ONCE);
    // BT-161 is either the text in cbc:Value or the numeric value in cbc:ValueQuantity
    aB.add ("BT-161", sLine + "/cac:Item/cac:AdditionalItemProperty/cbc:Value", ONCE);
    aB.add ("BT-161", sLine + "/cac:Item/cac:AdditionalItemProperty/cbc:ValueQuantity", ONCE, "unitCode");

    // BG-29 PRICE DETAILS
    aB.add ("BG-29", sLine + "/cac:Price", ONCE);
    aB.add ("BT-146", sLine + "/cac:Price/cbc:PriceAmount", ONCE, "currencyID");
    aB.add ("BT-149", sLine + "/cac:Price/cbc:BaseQuantity", ONCE, "unitCode");
    aB.add ("BT-147/BT-148", sLine + "/cac:Price/cac:AllowanceCharge", ONCE);
    aB.add ("BT-147-1/BT-148-1", sLine + "/cac:Price/cac:AllowanceCharge/cbc:ChargeIndicator", ONCE);
    aB.add ("BT-147", sLine + "/cac:Price/cac:AllowanceCharge/cbc:Amount", ONCE, "currencyID");
    aB.add ("BT-148", sLine + "/cac:Price/cac:AllowanceCharge/cbc:BaseAmount", ONCE, "currencyID");
  }

  @NonNull
  private static PurificationRuleSet _createRuleSet (@NonNull @Nonempty final String sID,
                                                     @NonNull final QName aRootElementName,
                                                     @NonNull @Nonempty final String sTypeCodeElement,
                                                     @NonNull @Nonempty final String sLine,
                                                     @NonNull @Nonempty final String sQuantityElement,
                                                     @NonNull @Nonempty final String sCollectionLine)
  {
    final PurificationRuleSetBuilder aB = new PurificationRuleSetBuilder (sID, aRootElementName);
    aB.addNamespacePrefix (CEN16931Syntax.PREFIX_UBL_CBC, CEN16931Syntax.NS_URI_UBL_CBC);
    aB.addNamespacePrefix (CEN16931Syntax.PREFIX_UBL_CAC, CEN16931Syntax.NS_URI_UBL_CAC);

    // BG-2 PROCESS CONTROL
    aB.add ("BT-24", "/cbc:CustomizationID", ONCE);
    aB.add ("BT-23", "/cbc:ProfileID", ONCE);

    // Header level fields
    aB.add ("BT-1", "/cbc:ID", ONCE);
    aB.add ("BT-2", "/cbc:IssueDate", ONCE);
    aB.add ("BT-166", "/cbc:IssueTime", ONCE);
    aB.add ("BT-3", "/cbc:" + sTypeCodeElement, ONCE);
    aB.add ("BT-9", "/cbc:DueDate", ONCE);
    aB.add ("BT-7", "/cbc:TaxPointDate", ONCE);
    aB.add ("BT-5", "/cbc:DocumentCurrencyCode", ONCE);
    aB.add ("BT-6", "/cbc:TaxCurrencyCode", ONCE);
    aB.add ("BT-19", "/cbc:AccountingCost", ONCE);

    // BT-167 VAT accounting currency exchange rate
    aB.add ("BT-167", "/cac:TaxExchangeRate", ONCE);
    aB.add ("BT-167", "/cac:TaxExchangeRate/cbc:CalculationRate", ONCE);
    aB.add ("BT-167-1", "/cac:TaxExchangeRate/cbc:TargetCurrencyCode", ONCE);
    aB.add ("BT-167-2", "/cac:TaxExchangeRate/cbc:SourceCurrencyCode", ONCE);

    // BT-10 Buyer reference - since UBL 2.5 it carries an optional code
    aB.add ("BT-10", "/cac:BuyerAssignedReference", UNBOUNDED);
    aB.add ("BT-10", "/cac:BuyerAssignedReference/cbc:BuyerReference", UNBOUNDED);
    aB.add ("BT-10-1", "/cac:BuyerAssignedReference/cbc:BuyerReferenceCode", ONCE);

    // BG-1 INVOICE NOTE - since UBL 2.5 the subject code is a dedicated element
    aB.add ("BG-1", "/cac:Annotation", UNBOUNDED);
    aB.add ("BT-21", "/cac:Annotation/cbc:SubjectCode", ONCE);
    aB.add ("BT-22", "/cac:Annotation/cbc:AnnotationContent", ONCE);

    // BG-14 INVOICING PERIOD
    aB.add ("BG-14", "/cac:InvoicePeriod", ONCE);
    aB.add ("BT-73", "/cac:InvoicePeriod/cbc:StartDate", ONCE);
    aB.add ("BT-74", "/cac:InvoicePeriod/cbc:EndDate", ONCE);
    aB.add ("BT-8", "/cac:InvoicePeriod/cbc:DescriptionCode", ONCE);

    // BT-13 and BT-14
    aB.add ("BT-13/BT-14", "/cac:OrderReference", ONCE);
    aB.add ("BT-13", "/cac:OrderReference/cbc:ID", ONCE);
    aB.add ("BT-14", "/cac:OrderReference/cbc:SalesOrderID", ONCE);

    // BG-3 PRECEDING INVOICE REFERENCE
    aB.add ("BG-3", "/cac:BillingReference", UNBOUNDED);
    aB.add ("BG-3", "/cac:BillingReference/cac:InvoiceDocumentReference", ONCE);
    aB.add ("BT-25", "/cac:BillingReference/cac:InvoiceDocumentReference/cbc:ID", ONCE);
    aB.add ("BT-26", "/cac:BillingReference/cac:InvoiceDocumentReference/cbc:IssueDate", ONCE);
    aB.add ("BT-202", "/cac:BillingReference/cac:InvoiceDocumentReference/cbc:DocumentTypeCode", ONCE);

    // BT-16, BT-15, BT-197, BT-17 and BT-12
    aB.add ("BT-16", "/cac:DespatchDocumentReference", ONCE);
    aB.add ("BT-16", "/cac:DespatchDocumentReference/cbc:ID", ONCE);
    aB.add ("BT-15", "/cac:ReceiptDocumentReference", ONCE);
    aB.add ("BT-15", "/cac:ReceiptDocumentReference/cbc:ID", ONCE);
    aB.add ("BT-197", "/cac:DeliveryNoteDocumentReference", ONCE);
    aB.add ("BT-197", "/cac:DeliveryNoteDocumentReference/cbc:ID", ONCE);
    aB.add ("BT-17", "/cac:OriginatorDocumentReference", ONCE);
    aB.add ("BT-17", "/cac:OriginatorDocumentReference/cbc:ID", ONCE);
    aB.add ("BT-12", "/cac:ContractDocumentReference", ONCE);
    aB.add ("BT-12", "/cac:ContractDocumentReference/cbc:ID", ONCE);

    // BT-11 Project reference
    aB.add ("BT-11", "/cac:ProjectReference", ONCE);
    aB.add ("BT-11", "/cac:ProjectReference/cbc:ID", ONCE);

    // BT-18 Invoiced object identifier - discriminated by BT-18-2
    aB.add ("BT-18", ADR_BT18, ONCE);
    aB.add ("BT-18", ADR_BT18 + "/cbc:ID", ONCE, "schemeID");
    aB.add ("BT-18-2", ADR_BT18 + "/cbc:DocumentTypeCode", ONCE);
    _addBG24Rules (aB);

    // BG-4 SELLER
    aB.addKeepWhenEmpty ("BG-4", "/cac:AccountingSupplierParty", ONCE);
    aB.add ("BG-4", "/cac:AccountingSupplierParty/cac:Party", ONCE);
    aB.add ("BT-34", SELLER + "/cbc:EndpointID", ONCE, "schemeID");
    // BT-29 Seller identifier and BT-90a Bank assigned creditor identifier
    aB.add ("BT-29/BT-90", SELLER + "/cac:PartyIdentification", UNBOUNDED);
    aB.add ("BT-29/BT-90", SELLER + "/cac:PartyIdentification/cbc:ID", ONCE, "schemeID");
    aB.add ("BT-28", SELLER + "/cac:PartyName", ONCE);
    aB.add ("BT-28", SELLER + "/cac:PartyName/cbc:Name", ONCE);
    // BG-5 SELLER POSTAL ADDRESS
    _addPostalAddressRules (aB,
                            SELLER + "/cac:PostalAddress",
                            "BG-5",
                            "BT-35",
                            "BT-36",
                            "BT-162",
                            "BT-37",
                            "BT-38",
                            "BT-39",
                            "BT-40");
    // BT-31 Seller VAT identifier and BT-32 Seller tax registration identifier are discriminated
    // by the tax scheme. Other than in the 2017 edition BT-32 is 'LOC' and no longer "not VAT";
    // a party tax scheme without a tax scheme code is kept for both of them
    aB.add ("BT-31", SELLER + PTS_VAT, ONCE);
    aB.add ("BT-31", SELLER + PTS_VAT + "/cbc:CompanyID", ONCE, "schemeID");
    _addTaxSchemeRules (aB, "BT-31-2", SELLER + PTS_VAT);
    aB.add ("BT-32", SELLER + PTS_LOC, ONCE);
    aB.add ("BT-32", SELLER + PTS_LOC + "/cbc:CompanyID", ONCE, "schemeID");
    _addTaxSchemeRules (aB, "BT-32-2", SELLER + PTS_LOC);
    aB.add ("BT-31/BT-32", SELLER + PTS_NO_SCHEME, ONCE);
    aB.add ("BT-31/BT-32", SELLER + PTS_NO_SCHEME + "/cbc:CompanyID", ONCE, "schemeID");
    _addTaxSchemeRules (aB, "BT-31-2/BT-32-2", SELLER + PTS_NO_SCHEME);
    aB.add ("BT-27/BT-30/BT-33", SELLER + "/cac:PartyLegalEntity", ONCE);
    aB.add ("BT-27", SELLER + "/cac:PartyLegalEntity/cbc:RegistrationName", ONCE);
    aB.add ("BT-30", SELLER + "/cac:PartyLegalEntity/cbc:CompanyID", ONCE, "schemeID");
    aB.add ("BT-33", SELLER + "/cac:PartyLegalEntity/cbc:CompanyLegalForm", ONCE);
    // BG-6 SELLER CONTACT
    _addContactRules (aB, SELLER, "BG-6", "BT-41", "BT-42", "BT-43");

    // BG-7 BUYER
    aB.addKeepWhenEmpty ("BG-7", "/cac:AccountingCustomerParty", ONCE);
    aB.add ("BG-7", "/cac:AccountingCustomerParty/cac:Party", ONCE);
    aB.add ("BT-49", BUYER + "/cbc:EndpointID", ONCE, "schemeID");
    aB.add ("BT-46", BUYER + "/cac:PartyIdentification", UNBOUNDED);
    aB.add ("BT-46", BUYER + "/cac:PartyIdentification/cbc:ID", ONCE, "schemeID");
    aB.add ("BT-45", BUYER + "/cac:PartyName", ONCE);
    aB.add ("BT-45", BUYER + "/cac:PartyName/cbc:Name", ONCE);
    // BG-8 BUYER POSTAL ADDRESS
    _addPostalAddressRules (aB,
                            BUYER + "/cac:PostalAddress",
                            "BG-8",
                            "BT-50",
                            "BT-51",
                            "BT-163",
                            "BT-52",
                            "BT-53",
                            "BT-54",
                            "BT-55");
    // BT-48 Buyer VAT identifier - a Buyer party tax scheme with another tax scheme is not part
    // of the core message
    aB.add ("BT-48", BUYER + PTS_VAT, ONCE);
    aB.add ("BT-48", BUYER + PTS_VAT + "/cbc:CompanyID", ONCE, "schemeID");
    _addTaxSchemeRules (aB, "BT-48-2", BUYER + PTS_VAT);
    aB.add ("BT-48", BUYER + PTS_NO_SCHEME, ONCE);
    aB.add ("BT-48", BUYER + PTS_NO_SCHEME + "/cbc:CompanyID", ONCE, "schemeID");
    _addTaxSchemeRules (aB, "BT-48-2", BUYER + PTS_NO_SCHEME);
    aB.add ("BT-44/BT-47", BUYER + "/cac:PartyLegalEntity", ONCE);
    aB.add ("BT-44", BUYER + "/cac:PartyLegalEntity/cbc:RegistrationName", ONCE);
    aB.add ("BT-47", BUYER + "/cac:PartyLegalEntity/cbc:CompanyID", ONCE, "schemeID");
    // BG-9 BUYER CONTACT
    _addContactRules (aB, BUYER, "BG-9", "BT-56", "BT-57", "BT-58");

    // BG-10 PAYEE
    aB.add ("BG-10", PAYEE, ONCE);
    // BT-60 Payee identifier and BT-90b Bank assigned creditor identifier
    aB.add ("BT-60/BT-90", PAYEE + "/cac:PartyIdentification", TWICE);
    aB.add ("BT-60/BT-90", PAYEE + "/cac:PartyIdentification/cbc:ID", ONCE, "schemeID");
    aB.add ("BT-59", PAYEE + "/cac:PartyName", ONCE);
    aB.add ("BT-59", PAYEE + "/cac:PartyName/cbc:Name", ONCE);
    aB.add ("BT-61", PAYEE + "/cac:PartyLegalEntity", ONCE);
    aB.add ("BT-61", PAYEE + "/cac:PartyLegalEntity/cbc:CompanyID", ONCE, "schemeID");

    // BG-11 SELLER TAX REPRESENTATIVE PARTY
    aB.add ("BG-11", TAX_REP, ONCE);
    aB.add ("BT-62", TAX_REP + "/cac:PartyName", ONCE);
    aB.add ("BT-62", TAX_REP + "/cac:PartyName/cbc:Name", ONCE);
    aB.add ("BT-63", TAX_REP + PTS_VAT, ONCE);
    aB.add ("BT-63", TAX_REP + PTS_VAT + "/cbc:CompanyID", ONCE, "schemeID");
    _addTaxSchemeRules (aB, "BT-63-2", TAX_REP + PTS_VAT);
    aB.add ("BT-63", TAX_REP + PTS_NO_SCHEME, ONCE);
    aB.add ("BT-63", TAX_REP + PTS_NO_SCHEME + "/cbc:CompanyID", ONCE, "schemeID");
    _addTaxSchemeRules (aB, "BT-63-2", TAX_REP + PTS_NO_SCHEME);
    // BG-12 SELLER TAX REPRESENTATIVE POSTAL ADDRESS
    _addPostalAddressRules (aB,
                            TAX_REP + "/cac:PostalAddress",
                            "BG-12",
                            "BT-64",
                            "BT-65",
                            "BT-164",
                            "BT-66",
                            "BT-67",
                            "BT-68",
                            "BT-69");

    // BG-13 DELIVERY INFORMATION and BG-15 DELIVER TO ADDRESS
    _addDeliveryRules (aB,
                       DELIVERY,
                       "BG-13",
                       "BT-70",
                       "BT-71",
                       "BT-72",
                       "BG-15",
                       "BT-75",
                       "BT-76",
                       "BT-165",
                       "BT-77",
                       "BT-78",
                       "BT-79",
                       "BT-80");

    // BG-16 PAYMENT INSTRUCTIONS
    aB.add ("BG-16", PAYMENT_MEANS, UNBOUNDED);
    // BT-82 Payment means text is the "name" attribute of BT-81
    aB.add ("BT-81/BT-82", PAYMENT_MEANS + "/cbc:PaymentMeansCode", ONCE, "name");
    aB.add ("BT-83", PAYMENT_MEANS + "/cbc:PaymentID", ONCE);
    // BG-18 PAYMENT CARD INFORMATION - other than in UBL 2.1 cbc:NetworkID is not mandatory in
    // the UBL 2.5 XML Schema, so it is not part of the core message any more
    aB.add ("BG-18", PAYMENT_MEANS + "/cac:CardAccount", ONCE);
    aB.add ("BT-87", PAYMENT_MEANS + "/cac:CardAccount/cbc:PrimaryAccountNumberID", ONCE);
    aB.add ("BT-88", PAYMENT_MEANS + "/cac:CardAccount/cbc:HolderName", ONCE);
    // BG-17 CREDIT TRANSFER
    aB.add ("BG-17", PAYMENT_MEANS + "/cac:PayeeFinancialAccount", ONCE);
    aB.add ("BT-84", PAYMENT_MEANS + "/cac:PayeeFinancialAccount/cbc:ID", ONCE);
    aB.add ("BT-85", PAYMENT_MEANS + "/cac:PayeeFinancialAccount/cbc:Name", ONCE);
    aB.add ("BT-86", PAYMENT_MEANS + "/cac:PayeeFinancialAccount/cac:FinancialInstitutionBranch", ONCE);
    aB.add ("BT-86", PAYMENT_MEANS + "/cac:PayeeFinancialAccount/cac:FinancialInstitutionBranch/cbc:ID", ONCE);
    // BG-19 DIRECT DEBIT
    final String sMandate = PAYMENT_MEANS + "/cac:PaymentMandate";
    aB.add ("BG-19", sMandate, ONCE);
    aB.add ("BT-89", sMandate + "/cbc:ID", ONCE);
    aB.add ("BT-91/BT-215/BT-216", sMandate + "/cac:PayerFinancialAccount", ONCE);
    aB.add ("BT-91", sMandate + "/cac:PayerFinancialAccount/cbc:ID", ONCE);
    aB.add ("BT-216", sMandate + "/cac:PayerFinancialAccount/cbc:Name", ONCE);
    aB.add ("BT-215", sMandate + "/cac:PayerFinancialAccount/cac:FinancialInstitutionBranch", ONCE);
    aB.add ("BT-215", sMandate + "/cac:PayerFinancialAccount/cac:FinancialInstitutionBranch/cbc:ID", ONCE);

    // BG-33 PAYMENT TERMS, BG-35 EARLY PAYMENT DISCOUNT and BG-36 LATE PAYMENT PENALTY
    _addPaymentTermsRules (aB);

    // BG-20 DOCUMENT LEVEL ALLOWANCES and BG-21 DOCUMENT LEVEL CHARGES AND TAXES
    aB.add ("BG-20/BG-21", ALLOWANCE_CHARGE, UNBOUNDED);
    aB.add ("BG-20-1/BG-21-1", ALLOWANCE_CHARGE + "/cbc:ChargeIndicator", ONCE);
    // BT-177 Document level non-VAT tax code shares the element with BT-98 and BT-105 and is
    // discriminated by BT-177-1, the @listID
    aB.add ("BT-98/BT-105/BT-177", ALLOWANCE_CHARGE + "/cbc:AllowanceChargeReasonCode", ONCE, "listID");
    aB.add ("BT-97/BT-104", ALLOWANCE_CHARGE + "/cbc:AllowanceChargeReason", ONCE);
    aB.add ("BT-94/BT-101", ALLOWANCE_CHARGE + "/cbc:MultiplierFactorNumeric", ONCE);
    aB.add ("BT-92/BT-99", ALLOWANCE_CHARGE + "/cbc:Amount", ONCE, "currencyID");
    aB.add ("BT-93/BT-100", ALLOWANCE_CHARGE + "/cbc:BaseAmount", ONCE, "currencyID");
    aB.add ("BT-95/BT-102", ALLOWANCE_CHARGE + "/cac:TaxCategory", ONCE);
    _addTaxCategoryRules (aB,
                          ALLOWANCE_CHARGE + "/cac:TaxCategory",
                          "BT-95/BT-102",
                          "BT-95-1/BT-102-1",
                          "BT-96/BT-103",
                          "BT-173/BT-175",
                          "BT-174/BT-176",
                          "BT-213/BT-214");

    // BT-110 Invoice total VAT amount and BT-111 in accounting currency
    aB.add ("BT-110/BT-111", TAX_TOTAL, TWICE);
    aB.add ("BT-110/BT-111", TAX_TOTAL + "/cbc:TaxAmount", ONCE, "currencyID");
    // BG-23 VAT BREAKDOWN
    aB.add ("BG-23", TAX_TOTAL + "/cac:TaxSubtotal", UNBOUNDED);
    aB.add ("BT-116", TAX_TOTAL + "/cac:TaxSubtotal/cbc:TaxableAmount", ONCE, "currencyID");
    aB.add ("BT-117/BT-184", TAX_TOTAL + "/cac:TaxSubtotal/cbc:TaxAmount", ONCE, "currencyID");
    aB.addKeepWhenEmpty ("BG-23", TAX_TOTAL + "/cac:TaxSubtotal/cac:TaxCategory", ONCE);
    _addTaxCategoryRules (aB,
                          TAX_TOTAL + "/cac:TaxSubtotal/cac:TaxCategory",
                          "BT-118",
                          "BT-118-1",
                          "BT-119",
                          "BT-120",
                          "BT-121",
                          "BT-210");

    // BG-22 DOCUMENT TOTALS
    aB.addKeepWhenEmpty ("BG-22", TOTALS, ONCE);
    aB.add ("BT-106", TOTALS + "/cbc:LineExtensionAmount", ONCE, "currencyID");
    aB.add ("BT-109", TOTALS + "/cbc:TaxExclusiveAmount", ONCE, "currencyID");
    aB.add ("BT-112", TOTALS + "/cbc:TaxInclusiveAmount", ONCE, "currencyID");
    aB.add ("BT-107", TOTALS + "/cbc:AllowanceTotalAmount", ONCE, "currencyID");
    aB.add ("BT-108", TOTALS + "/cbc:ChargeTotalAmount", ONCE, "currencyID");
    aB.add ("BT-113", TOTALS + "/cbc:PrepaidAmount", ONCE, "currencyID");
    aB.add ("BT-114", TOTALS + "/cbc:PayableRoundingAmount", ONCE, "currencyID");
    aB.add ("BT-115", TOTALS + "/cbc:PayableAmount", ONCE, "currencyID");

    // BG-34 CHARGES ON BEHALF OF A THIRD PARTY
    aB.add ("BG-34", sCollectionLine, UNBOUNDED);
    aB.add ("BT-179-1", sCollectionLine + "/cbc:ID", ONCE);
    aB.add ("BT-179", sCollectionLine + "/cbc:TaxInclusiveLineExtensionAmount", ONCE, "currencyID");
    // cac:Item is mandatory in the UBL Invoice line XML Schema
    aB.addKeepWhenEmpty ("BT-180", sCollectionLine + "/cac:Item", ONCE);
    aB.add ("BT-180", sCollectionLine + "/cac:Item/cbc:Description", ONCE);

    // BG-25 INVOICE LINE
    _addLineRules (aB, sLine, sQuantityElement);

    return aB.build ();
  }

  @NonNull
  private static PurificationRuleSet _createInvoiceRuleSet ()
  {
    return _createRuleSet ("en16931-2026-ubl-invoice",
                           CEN16931Syntax.QNAME_UBL_INVOICE,
                           "InvoiceTypeCode",
                           "/cac:InvoiceLine",
                           "InvoicedQuantity",
                           "/cac:CollectionInvoiceLine");
  }

  @NonNull
  private static PurificationRuleSet _createCreditNoteRuleSet ()
  {
    return _createRuleSet ("en16931-2026-ubl-creditnote",
                           CEN16931Syntax.QNAME_UBL_CREDIT_NOTE,
                           "CreditNoteTypeCode",
                           "/cac:CreditNoteLine",
                           "CreditedQuantity",
                           "/cac:CollectionCreditNoteLine");
  }

  /**
   * @return The EN 16931:2026 rule set for a UBL Invoice. Never <code>null</code>.
   */
  @NonNull
  public static PurificationRuleSet getInvoiceRuleSet ()
  {
    return INVOICE;
  }

  /**
   * @return The EN 16931:2026 rule set for a UBL Credit Note. Never <code>null</code>.
   */
  @NonNull
  public static PurificationRuleSet getCreditNoteRuleSet ()
  {
    return CREDIT_NOTE;
  }
}
