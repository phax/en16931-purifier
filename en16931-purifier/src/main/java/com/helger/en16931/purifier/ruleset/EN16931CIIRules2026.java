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

import org.jspecify.annotations.NonNull;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.en16931.basics.CEN16931Syntax;
import com.helger.en16931.purifier.rule.PurificationRuleNode;
import com.helger.en16931.purifier.rule.PurificationRuleSet;
import com.helger.en16931.purifier.rule.PurificationRuleSetBuilder;

/**
 * The EN 16931:2026 core message in the CII syntax, according to CEN/TS 16931-3-3:2026.
 * <p>
 * Other than the 2017 edition, this binding is bound to <b>UN/CEFACT SCRDM D25A or later</b>. The
 * source document states that "only CII D25A and any subsequent Dxxx version have all syntax
 * elements which are needed". Business terms that were added in a newer CII release are BT-10 in
 * <code>ram:BuyerReferenceID</code> (D25A), BG-3 and BG-39 with an unbounded
 * <code>ram:InvoiceReferencedDocument</code> (D22B) and the goods and services codes in
 * <code>ram:SupplyTypeCode</code> (D25A).
 * <p>
 * All dates use the <code>udt:DateTimeString</code> respectively <code>udt:DateString</code>
 * representation with the <code>format</code> attribute, as required by the syntax binding. The
 * alternative <code>udt:DateTime</code> and <code>udt:Date</code> representations are not part of
 * the core message and are therefore removed.
 *
 * @author Philip Helger
 */
@Immutable
public final class EN16931CIIRules2026
{
  private static final int ONCE = 1;
  private static final int TWICE = 2;
  private static final int UNBOUNDED = PurificationRuleNode.UNBOUNDED;

  private static final String CONTEXT = "/rsm:ExchangedDocumentContext";
  private static final String DOCUMENT = "/rsm:ExchangedDocument";
  private static final String TRANSACTION = "/rsm:SupplyChainTradeTransaction";
  private static final String LINE = TRANSACTION + "/ram:IncludedSupplyChainTradeLineItem";
  private static final String LINE_AGREEMENT = LINE + "/ram:SpecifiedLineTradeAgreement";
  private static final String LINE_DELIVERY = LINE + "/ram:SpecifiedLineTradeDelivery";
  private static final String LINE_SETTLEMENT = LINE + "/ram:SpecifiedLineTradeSettlement";
  private static final String PRODUCT = LINE + "/ram:SpecifiedTradeProduct";
  private static final String AGREEMENT = TRANSACTION + "/ram:ApplicableHeaderTradeAgreement";
  private static final String DELIVERY = TRANSACTION + "/ram:ApplicableHeaderTradeDelivery";
  private static final String SETTLEMENT = TRANSACTION + "/ram:ApplicableHeaderTradeSettlement";
  private static final String SELLER = AGREEMENT + "/ram:SellerTradeParty";
  private static final String BUYER = AGREEMENT + "/ram:BuyerTradeParty";
  private static final String TAX_REP = AGREEMENT + "/ram:SellerTaxRepresentativeTradeParty";
  private static final String SHIP_TO = DELIVERY + "/ram:ShipToTradeParty";
  private static final String PAYEE = SETTLEMENT + "/ram:PayeeTradeParty";
  private static final String PAYMENT_MEANS = SETTLEMENT + "/ram:SpecifiedTradeSettlementPaymentMeans";
  private static final String TRADE_TAX = SETTLEMENT + "/ram:ApplicableTradeTax";
  private static final String PERIOD = SETTLEMENT + "/ram:BillingSpecifiedPeriod";
  private static final String ALLOWANCE_CHARGE = SETTLEMENT + "/ram:SpecifiedTradeAllowanceCharge";
  private static final String PAYMENT_TERMS = SETTLEMENT + "/ram:SpecifiedTradePaymentTerms";
  private static final String TOTALS = SETTLEMENT + "/ram:SpecifiedTradeSettlementHeaderMonetarySummation";
  private static final String PRECEDING = SETTLEMENT + "/ram:InvoiceReferencedDocument";
  private static final String THIRD_PARTY = SETTLEMENT + "/ram:SpecifiedFinancialAdjustment";
  private static final String EXCHANGE = SETTLEMENT + "/ram:InvoiceApplicableTradeCurrencyExchange";
  private static final String STR_VA = "/ram:SpecifiedTaxRegistration[ram:ID/@schemeID='VA']";
  private static final String STR_FC = "/ram:SpecifiedTaxRegistration[ram:ID/@schemeID='FC']";
  private static final String STR_NO_SCHEME = "/ram:SpecifiedTaxRegistration[not(ram:ID/@schemeID)]";
  private static final String ADR_BT17 = AGREEMENT + "/ram:AdditionalReferencedDocument[ram:TypeCode='50']";
  private static final String ADR_BT18 = AGREEMENT + "/ram:AdditionalReferencedDocument[ram:TypeCode='130']";
  private static final String ADR_BG24 = AGREEMENT + "/ram:AdditionalReferencedDocument[ram:TypeCode='916']";

  private static final PurificationRuleSet CROSS_INDUSTRY_INVOICE = _createRuleSet ();

  private EN16931CIIRules2026 ()
  {}

  private static void _addDateTimeRules (@NonNull final PurificationRuleSetBuilder aB,
                                         @NonNull final String sBusinessTermID,
                                         @NonNull @Nonempty final String sBase)
  {
    aB.add (sBusinessTermID, sBase, ONCE);
    aB.add (sBusinessTermID, sBase + "/udt:DateTimeString", ONCE, "format");
  }

  private static void _addChargeIndicatorRules (@NonNull final PurificationRuleSetBuilder aB,
                                                @NonNull final String sBusinessTermID,
                                                @NonNull @Nonempty final String sBase)
  {
    // Mandatory discriminator between an allowance and a charge
    aB.addKeepWhenEmpty (sBusinessTermID, sBase + "/ram:ChargeIndicator", ONCE);
    aB.add (sBusinessTermID, sBase + "/ram:ChargeIndicator/udt:Indicator", ONCE);
  }

  private static void _addAddressRules (@NonNull final PurificationRuleSetBuilder aB,
                                        @NonNull @Nonempty final String sBase,
                                        @NonNull final String sBTGroup,
                                        @NonNull final String sBTPostCode,
                                        @NonNull final String sBTLine1,
                                        @NonNull final String sBTLine2,
                                        @NonNull final String sBTLine3,
                                        @NonNull final String sBTCity,
                                        @NonNull final String sBTCountryCode,
                                        @NonNull final String sBTCountrySubdivision)
  {
    aB.add (sBTGroup, sBase, ONCE);
    aB.add (sBTPostCode, sBase + "/ram:PostcodeCode", ONCE);
    aB.add (sBTLine1, sBase + "/ram:LineOne", ONCE);
    aB.add (sBTLine2, sBase + "/ram:LineTwo", ONCE);
    aB.add (sBTLine3, sBase + "/ram:LineThree", ONCE);
    aB.add (sBTCity, sBase + "/ram:CityName", ONCE);
    aB.add (sBTCountryCode, sBase + "/ram:CountryID", ONCE);
    aB.add (sBTCountrySubdivision, sBase + "/ram:CountrySubDivisionName", ONCE);
  }

  private static void _addContactRules (@NonNull final PurificationRuleSetBuilder aB,
                                        @NonNull @Nonempty final String sBase,
                                        @NonNull final String sBTGroup,
                                        @NonNull final String sBTName,
                                        @NonNull final String sBTTelephone,
                                        @NonNull final String sBTElectronicMail)
  {
    final String sContact = sBase + "/ram:DefinedTradeContact";
    aB.add (sBTGroup, sContact, ONCE);
    // The contact point is either the person name or the department name
    aB.add (sBTName, sContact + "/ram:PersonName", ONCE);
    aB.add (sBTName, sContact + "/ram:DepartmentName", ONCE);
    aB.add (sBTTelephone, sContact + "/ram:TelephoneUniversalCommunication", ONCE);
    aB.add (sBTTelephone, sContact + "/ram:TelephoneUniversalCommunication/ram:CompleteNumber", ONCE);
    aB.add (sBTElectronicMail, sContact + "/ram:EmailURIUniversalCommunication", ONCE);
    aB.add (sBTElectronicMail, sContact + "/ram:EmailURIUniversalCommunication/ram:URIID", ONCE);
  }

  private static void _addPriceRules (@NonNull final PurificationRuleSetBuilder aB,
                                      @NonNull @Nonempty final String sBase,
                                      @NonNull final String sBTAmount)
  {
    aB.add (sBTAmount, sBase, ONCE);
    aB.add (sBTAmount, sBase + "/ram:ChargeAmount", ONCE);
    aB.add ("BT-149", sBase + "/ram:BasisQuantity", ONCE, "unitCode");
  }

  private static void _addDocumentContextRules (@NonNull final PurificationRuleSetBuilder aB)
  {
    // rsm:ExchangedDocumentContext is mandatory in the XML Schema
    aB.addKeepWhenEmpty ("BG-2", CONTEXT, ONCE);
    aB.add ("BT-23", CONTEXT + "/ram:BusinessProcessSpecifiedDocumentContextParameter", ONCE);
    aB.add ("BT-23", CONTEXT + "/ram:BusinessProcessSpecifiedDocumentContextParameter/ram:ID", ONCE);
    aB.add ("BT-24", CONTEXT + "/ram:GuidelineSpecifiedDocumentContextParameter", ONCE);
    aB.add ("BT-24", CONTEXT + "/ram:GuidelineSpecifiedDocumentContextParameter/ram:ID", ONCE);

    // rsm:ExchangedDocument is mandatory in the XML Schema
    aB.addKeepWhenEmpty (null, DOCUMENT, ONCE);
    aB.add ("BT-1", DOCUMENT + "/ram:ID", ONCE);
    aB.add ("BT-3", DOCUMENT + "/ram:TypeCode", ONCE);
    // BT-2 and BT-166 share one element, discriminated by BT-2-1 respectively BT-166-1, the
    // @format
    _addDateTimeRules (aB, "BT-2/BT-166", DOCUMENT + "/ram:IssueDateTime");
    // BG-1 INVOICE NOTE
    aB.add ("BG-1", DOCUMENT + "/ram:IncludedNote", UNBOUNDED);
    aB.add ("BT-22", DOCUMENT + "/ram:IncludedNote/ram:Content", ONCE);
    aB.add ("BT-21", DOCUMENT + "/ram:IncludedNote/ram:SubjectCode", ONCE);
  }

  private static void _addLineRules (@NonNull final PurificationRuleSetBuilder aB)
  {
    // BG-25 INVOICE LINE
    aB.add ("BG-25", LINE, UNBOUNDED);
    // ram:AssociatedDocumentLineDocument is mandatory in the XML Schema
    aB.addKeepWhenEmpty ("BT-126", LINE + "/ram:AssociatedDocumentLineDocument", ONCE);
    aB.add ("BT-126", LINE + "/ram:AssociatedDocumentLineDocument/ram:LineID", ONCE);
    aB.add ("BT-127", LINE + "/ram:AssociatedDocumentLineDocument/ram:IncludedNote", ONCE);
    aB.add ("BT-127", LINE + "/ram:AssociatedDocumentLineDocument/ram:IncludedNote/ram:Content", ONCE);

    // BG-31 ITEM INFORMATION
    aB.add ("BG-31", PRODUCT, ONCE);
    aB.add ("BT-157", PRODUCT + "/ram:GlobalID", ONCE, "schemeID");
    aB.add ("BT-155", PRODUCT + "/ram:SellerAssignedID", ONCE);
    aB.add ("BT-156", PRODUCT + "/ram:BuyerAssignedID", ONCE);
    aB.add ("BT-153", PRODUCT + "/ram:Name", ONCE);
    aB.add ("BT-154", PRODUCT + "/ram:Description", ONCE);
    // BG-32 ITEM ATTRIBUTE
    aB.add ("BG-32", PRODUCT + "/ram:ApplicableProductCharacteristic", UNBOUNDED);
    aB.add ("BT-211", PRODUCT + "/ram:ApplicableProductCharacteristic/ram:TypeCode", ONCE);
    aB.add ("BT-160", PRODUCT + "/ram:ApplicableProductCharacteristic/ram:Description", ONCE);
    // BT-161 is either the text in ram:Value or the numeric value in ram:ValueMeasure
    aB.add ("BT-161", PRODUCT + "/ram:ApplicableProductCharacteristic/ram:Value", ONCE);
    aB.add ("BT-161", PRODUCT + "/ram:ApplicableProductCharacteristic/ram:ValueMeasure", ONCE, "unitCode");
    aB.add ("BT-158", PRODUCT + "/ram:DesignatedProductClassification", UNBOUNDED);
    aB.add ("BT-158",
            PRODUCT + "/ram:DesignatedProductClassification/ram:ClassCode",
            ONCE,
            "listID",
            "listVersionID");
    aB.add ("BT-159", PRODUCT + "/ram:OriginTradeCountry", ONCE);
    aB.add ("BT-159", PRODUCT + "/ram:OriginTradeCountry/ram:ID", ONCE);

    // BG-29 PRICE DETAILS
    aB.add ("BG-29", LINE_AGREEMENT, ONCE);
    aB.add ("BT-132/BT-188", LINE_AGREEMENT + "/ram:BuyerOrderReferencedDocument", ONCE);
    aB.add ("BT-188", LINE_AGREEMENT + "/ram:BuyerOrderReferencedDocument/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-132", LINE_AGREEMENT + "/ram:BuyerOrderReferencedDocument/ram:LineID", ONCE);
    aB.add ("BT-200/BT-201", LINE_AGREEMENT + "/ram:SellerOrderReferencedDocument", ONCE);
    aB.add ("BT-200", LINE_AGREEMENT + "/ram:SellerOrderReferencedDocument/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-201", LINE_AGREEMENT + "/ram:SellerOrderReferencedDocument/ram:LineID", ONCE);
    // BT-149 may be on the gross price as well as on the net price
    _addPriceRules (aB, LINE_AGREEMENT + "/ram:GrossPriceProductTradePrice", "BT-148");
    aB.add ("BT-147", LINE_AGREEMENT + "/ram:GrossPriceProductTradePrice/ram:AppliedTradeAllowanceCharge", ONCE);
    _addChargeIndicatorRules (aB,
                              "BT-147-1",
                              LINE_AGREEMENT + "/ram:GrossPriceProductTradePrice/ram:AppliedTradeAllowanceCharge");
    aB.add ("BT-147",
            LINE_AGREEMENT + "/ram:GrossPriceProductTradePrice/ram:AppliedTradeAllowanceCharge/ram:ActualAmount",
            ONCE);
    _addPriceRules (aB, LINE_AGREEMENT + "/ram:NetPriceProductTradePrice", "BT-146");

    // BG-37 INVOICE LINE DELIVERY INFORMATION
    aB.add ("BG-37", LINE_DELIVERY, ONCE);
    aB.add ("BT-129", LINE_DELIVERY + "/ram:BilledQuantity", ONCE, "unitCode");
    aB.add ("BT-185/BT-186", LINE_DELIVERY + "/ram:ShipToTradeParty", ONCE);
    aB.add ("BT-185", LINE_DELIVERY + "/ram:ShipToTradeParty/ram:Name", ONCE);
    aB.add ("BT-186", LINE_DELIVERY + "/ram:ShipToTradeParty/ram:GlobalID", ONCE, "schemeID");
    // BG-38 INVOICE LINE DELIVER TO ADDRESS
    _addAddressRules (aB,
                      LINE_DELIVERY + "/ram:ShipToTradeParty/ram:PostalTradeAddress",
                      "BG-38",
                      "BT-207",
                      "BT-203",
                      "BT-204",
                      "BT-205",
                      "BT-206",
                      "BT-209",
                      "BT-208");
    // BT-187 Invoice line actual delivery date
    aB.add ("BT-187", LINE_DELIVERY + "/ram:ActualDeliverySupplyChainEvent", ONCE);
    _addDateTimeRules (aB, "BT-187", LINE_DELIVERY + "/ram:ActualDeliverySupplyChainEvent/ram:OccurrenceDateTime");
    // BT-189, BT-190, BT-191, BT-192, BT-198 and BT-199 line level document references
    aB.add ("BT-189/BT-190", LINE_DELIVERY + "/ram:DespatchAdviceReferencedDocument", ONCE);
    aB.add ("BT-189", LINE_DELIVERY + "/ram:DespatchAdviceReferencedDocument/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-190", LINE_DELIVERY + "/ram:DespatchAdviceReferencedDocument/ram:LineID", ONCE);
    aB.add ("BT-191/BT-192", LINE_DELIVERY + "/ram:ReceivingAdviceReferencedDocument", ONCE);
    aB.add ("BT-191", LINE_DELIVERY + "/ram:ReceivingAdviceReferencedDocument/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-192", LINE_DELIVERY + "/ram:ReceivingAdviceReferencedDocument/ram:LineID", ONCE);
    aB.add ("BT-198/BT-199", LINE_DELIVERY + "/ram:DeliveryNoteReferencedDocument", ONCE);
    aB.add ("BT-198", LINE_DELIVERY + "/ram:DeliveryNoteReferencedDocument/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-199", LINE_DELIVERY + "/ram:DeliveryNoteReferencedDocument/ram:LineID", ONCE);

    // ram:SpecifiedLineTradeSettlement is mandatory in the XML Schema
    aB.addKeepWhenEmpty (null, LINE_SETTLEMENT, ONCE);
    // BG-30 LINE VAT INFORMATION
    aB.add ("BG-30", LINE_SETTLEMENT + "/ram:ApplicableTradeTax", ONCE);
    aB.add ("BT-151-1", LINE_SETTLEMENT + "/ram:ApplicableTradeTax/ram:TypeCode", ONCE);
    aB.add ("BT-151", LINE_SETTLEMENT + "/ram:ApplicableTradeTax/ram:CategoryCode", ONCE);
    aB.add ("BT-152", LINE_SETTLEMENT + "/ram:ApplicableTradeTax/ram:RateApplicablePercent", ONCE);
    aB.add ("BT-194", LINE_SETTLEMENT + "/ram:ApplicableTradeTax/ram:ExemptionReason", ONCE);
    aB.add ("BT-195", LINE_SETTLEMENT + "/ram:ApplicableTradeTax/ram:ExemptionReasonCode", ONCE);
    aB.add ("BT-196", LINE_SETTLEMENT + "/ram:ApplicableTradeTax/ram:SupplyTypeCode", ONCE);
    // BG-26 INVOICE LINE PERIOD
    aB.add ("BG-26", LINE_SETTLEMENT + "/ram:BillingSpecifiedPeriod", ONCE);
    _addDateTimeRules (aB, "BT-134", LINE_SETTLEMENT + "/ram:BillingSpecifiedPeriod/ram:StartDateTime");
    _addDateTimeRules (aB, "BT-135", LINE_SETTLEMENT + "/ram:BillingSpecifiedPeriod/ram:EndDateTime");
    // BG-27 INVOICE LINE ALLOWANCES and BG-28 INVOICE LINE CHARGES AND TAXES
    final String sLineAllowanceCharge = LINE_SETTLEMENT + "/ram:SpecifiedTradeAllowanceCharge";
    aB.add ("BG-27/BG-28", sLineAllowanceCharge, UNBOUNDED);
    _addChargeIndicatorRules (aB, "BG-27-1/BG-28-1", sLineAllowanceCharge);
    aB.add ("BT-138/BT-143", sLineAllowanceCharge + "/ram:CalculationPercent", ONCE);
    aB.add ("BT-137/BT-142", sLineAllowanceCharge + "/ram:BasisAmount", ONCE);
    aB.add ("BT-136/BT-141", sLineAllowanceCharge + "/ram:ActualAmount", ONCE);
    // BT-193 Invoice line-level non-VAT tax type code shares the element with BT-140 and BT-145
    // and requires @listID (BT-193-1) and @listAgencyID, see CII-DT-102 and CII-DT-103
    aB.add ("BT-140/BT-145/BT-193",
            sLineAllowanceCharge + "/ram:ReasonCode",
            ONCE,
            "listID",
            "listAgencyID");
    aB.add ("BT-139/BT-144", sLineAllowanceCharge + "/ram:Reason", ONCE);
    // BT-131 Invoice line net amount
    aB.add ("BT-131", LINE_SETTLEMENT + "/ram:SpecifiedTradeSettlementLineMonetarySummation", ONCE);
    aB.add ("BT-131", LINE_SETTLEMENT + "/ram:SpecifiedTradeSettlementLineMonetarySummation/ram:LineTotalAmount", ONCE);
    // BT-128 Invoice line object identifier - discriminated by BT-128-2
    final String sLineADR = LINE_SETTLEMENT + "/ram:AdditionalReferencedDocument[ram:TypeCode='130']";
    aB.add ("BT-128", sLineADR, ONCE);
    aB.add ("BT-128", sLineADR + "/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-128-1", sLineADR + "/ram:ReferenceTypeCode", ONCE);
    aB.add ("BT-128-2", sLineADR + "/ram:TypeCode", ONCE);
    // BT-133 Invoice line Buyer accounting reference
    aB.add ("BT-133", LINE_SETTLEMENT + "/ram:ReceivableSpecifiedTradeAccountingAccount", ONCE);
    aB.add ("BT-133", LINE_SETTLEMENT + "/ram:ReceivableSpecifiedTradeAccountingAccount/ram:ID", ONCE);
    // BG-39 LINE-LEVEL PRECEDING INVOICE REFERENCE
    final String sLinePreceding = LINE_SETTLEMENT + "/ram:InvoiceReferencedDocument";
    aB.add ("BG-39", sLinePreceding, UNBOUNDED);
    aB.add ("BT-217", sLinePreceding + "/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-218", sLinePreceding + "/ram:FormattedIssueDateTime", ONCE);
    aB.add ("BT-218", sLinePreceding + "/ram:FormattedIssueDateTime/qdt:DateTimeString", ONCE, "format");
    aB.add ("BT-219", sLinePreceding + "/ram:TypeCode", ONCE);
    aB.add ("BT-220", sLinePreceding + "/ram:LineID", ONCE);
  }

  private static void _addAgreementRules (@NonNull final PurificationRuleSetBuilder aB)
  {
    // ram:ApplicableHeaderTradeAgreement is mandatory in the XML Schema
    aB.addKeepWhenEmpty (null, AGREEMENT, ONCE);
    // BT-10 Buyer reference - since CII D25A it is repeatable and carries an optional code
    aB.add ("BT-10", AGREEMENT + "/ram:BuyerReferenceID", UNBOUNDED, "schemeID");

    // BG-4 SELLER
    aB.add ("BG-4", SELLER, ONCE);
    aB.add ("BT-29", SELLER + "/ram:GlobalID", UNBOUNDED, "schemeID");
    aB.add ("BT-27", SELLER + "/ram:Name", ONCE);
    aB.add ("BT-33", SELLER + "/ram:Description", ONCE);
    aB.add ("BT-28/BT-30", SELLER + "/ram:SpecifiedLegalOrganization", ONCE);
    aB.add ("BT-30", SELLER + "/ram:SpecifiedLegalOrganization/ram:ID", ONCE, "schemeID");
    aB.add ("BT-28", SELLER + "/ram:SpecifiedLegalOrganization/ram:TradingBusinessName", ONCE);
    // BG-6 SELLER CONTACT
    _addContactRules (aB, SELLER, "BG-6", "BT-41", "BT-42", "BT-43");
    // BG-5 SELLER POSTAL ADDRESS
    _addAddressRules (aB,
                      SELLER + "/ram:PostalTradeAddress",
                      "BG-5",
                      "BT-38",
                      "BT-35",
                      "BT-36",
                      "BT-162",
                      "BT-37",
                      "BT-40",
                      "BT-39");
    aB.add ("BT-34", SELLER + "/ram:URIUniversalCommunication", ONCE);
    aB.add ("BT-34", SELLER + "/ram:URIUniversalCommunication/ram:URIID", ONCE, "schemeID");
    // BT-31 Seller VAT identifier and BT-32 Seller tax registration identifier are discriminated
    // by the scheme identifier, so that enforcing the cardinality cannot drop the wrong one
    aB.add ("BT-31", SELLER + STR_VA, ONCE);
    aB.add ("BT-31", SELLER + STR_VA + "/ram:ID", ONCE, "schemeID");
    aB.add ("BT-32", SELLER + STR_FC, ONCE);
    aB.add ("BT-32", SELLER + STR_FC + "/ram:ID", ONCE, "schemeID");
    aB.add ("BT-31/BT-32", SELLER + STR_NO_SCHEME, ONCE);
    aB.add ("BT-31/BT-32", SELLER + STR_NO_SCHEME + "/ram:ID", ONCE, "schemeID");

    // BG-7 BUYER
    aB.add ("BG-7", BUYER, ONCE);
    aB.add ("BT-46", BUYER + "/ram:GlobalID", UNBOUNDED, "schemeID");
    aB.add ("BT-44", BUYER + "/ram:Name", ONCE);
    aB.add ("BT-45/BT-47", BUYER + "/ram:SpecifiedLegalOrganization", ONCE);
    aB.add ("BT-47", BUYER + "/ram:SpecifiedLegalOrganization/ram:ID", ONCE, "schemeID");
    aB.add ("BT-45", BUYER + "/ram:SpecifiedLegalOrganization/ram:TradingBusinessName", ONCE);
    // BG-9 BUYER CONTACT
    _addContactRules (aB, BUYER, "BG-9", "BT-56", "BT-57", "BT-58");
    // BG-8 BUYER POSTAL ADDRESS
    _addAddressRules (aB,
                      BUYER + "/ram:PostalTradeAddress",
                      "BG-8",
                      "BT-53",
                      "BT-50",
                      "BT-51",
                      "BT-163",
                      "BT-52",
                      "BT-55",
                      "BT-54");
    aB.add ("BT-49", BUYER + "/ram:URIUniversalCommunication", ONCE);
    aB.add ("BT-49", BUYER + "/ram:URIUniversalCommunication/ram:URIID", ONCE, "schemeID");
    // BT-48 Buyer VAT identifier - a Buyer tax registration with another scheme identifier is not
    // part of the core message
    aB.add ("BT-48", BUYER + STR_VA, ONCE);
    aB.add ("BT-48", BUYER + STR_VA + "/ram:ID", ONCE, "schemeID");
    aB.add ("BT-48", BUYER + STR_NO_SCHEME, ONCE);
    aB.add ("BT-48", BUYER + STR_NO_SCHEME + "/ram:ID", ONCE, "schemeID");

    // BG-11 SELLER TAX REPRESENTATIVE PARTY
    aB.add ("BG-11", TAX_REP, ONCE);
    aB.add ("BT-62", TAX_REP + "/ram:Name", ONCE);
    // BG-12 SELLER TAX REPRESENTATIVE POSTAL ADDRESS
    _addAddressRules (aB,
                      TAX_REP + "/ram:PostalTradeAddress",
                      "BG-12",
                      "BT-67",
                      "BT-64",
                      "BT-65",
                      "BT-164",
                      "BT-66",
                      "BT-69",
                      "BT-68");
    aB.add ("BT-63", TAX_REP + STR_VA, ONCE);
    aB.add ("BT-63", TAX_REP + STR_VA + "/ram:ID", ONCE, "schemeID");
    aB.add ("BT-63", TAX_REP + STR_NO_SCHEME, ONCE);
    aB.add ("BT-63", TAX_REP + STR_NO_SCHEME + "/ram:ID", ONCE, "schemeID");

    // BT-13 Purchase order reference
    aB.add ("BT-13", AGREEMENT + "/ram:BuyerOrderReferencedDocument", ONCE);
    aB.add ("BT-13", AGREEMENT + "/ram:BuyerOrderReferencedDocument/ram:IssuerAssignedID", ONCE);
    // BT-14 Sales order reference
    aB.add ("BT-14", AGREEMENT + "/ram:SellerOrderReferencedDocument", ONCE);
    aB.add ("BT-14", AGREEMENT + "/ram:SellerOrderReferencedDocument/ram:IssuerAssignedID", ONCE);
    // BT-12 Contract reference
    aB.add ("BT-12", AGREEMENT + "/ram:ContractReferencedDocument", ONCE);
    aB.add ("BT-12", AGREEMENT + "/ram:ContractReferencedDocument/ram:IssuerAssignedID", ONCE);

    // BT-17 Tender or lot reference
    aB.add ("BT-17", ADR_BT17, ONCE);
    aB.add ("BT-17", ADR_BT17 + "/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-17-1", ADR_BT17 + "/ram:TypeCode", ONCE);
    // BT-18 Invoiced object identifier
    aB.add ("BT-18", ADR_BT18, ONCE);
    aB.add ("BT-18", ADR_BT18 + "/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-18-1", ADR_BT18 + "/ram:ReferenceTypeCode", ONCE);
    aB.add ("BT-18-2", ADR_BT18 + "/ram:TypeCode", ONCE);
    // BG-24 ADDITIONAL SUPPORTING DOCUMENTS
    aB.add ("BG-24", ADR_BG24, UNBOUNDED);
    aB.add ("BT-122", ADR_BG24 + "/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-123", ADR_BG24 + "/ram:Name", ONCE);
    aB.add ("BT-124", ADR_BG24 + "/ram:URIID", ONCE);
    aB.add ("BT-125", ADR_BG24 + "/ram:AttachmentBinaryObject", ONCE, "mimeCode", "filename");
    aB.add ("BT-122-1", ADR_BG24 + "/ram:TypeCode", ONCE);

    // BT-11 Project reference - ram:Name is mandatory in the XML Schema and carries BT-11-1
    aB.add ("BT-11", AGREEMENT + "/ram:SpecifiedProcuringProject", ONCE);
    aB.add ("BT-11", AGREEMENT + "/ram:SpecifiedProcuringProject/ram:ID", ONCE);
    aB.add ("BT-11-1", AGREEMENT + "/ram:SpecifiedProcuringProject/ram:Name", ONCE);
  }

  private static void _addDeliveryRules (@NonNull final PurificationRuleSetBuilder aB)
  {
    // ram:ApplicableHeaderTradeDelivery is regularly empty, and it is mandatory in the XML Schema
    // of all CII releases before D25A
    aB.addKeepWhenEmpty ("BG-13", DELIVERY, ONCE);
    aB.add ("BG-13", SHIP_TO, ONCE);
    aB.add ("BT-71", SHIP_TO + "/ram:GlobalID", ONCE, "schemeID");
    aB.add ("BT-70", SHIP_TO + "/ram:Name", ONCE);
    // BG-15 DELIVER TO ADDRESS
    _addAddressRules (aB,
                      SHIP_TO + "/ram:PostalTradeAddress",
                      "BG-15",
                      "BT-78",
                      "BT-75",
                      "BT-76",
                      "BT-165",
                      "BT-77",
                      "BT-80",
                      "BT-79");

    // BT-72 Actual delivery date
    aB.add ("BT-72", DELIVERY + "/ram:ActualDeliverySupplyChainEvent", ONCE);
    _addDateTimeRules (aB, "BT-72", DELIVERY + "/ram:ActualDeliverySupplyChainEvent/ram:OccurrenceDateTime");

    // BT-16 Despatch advice reference
    aB.add ("BT-16", DELIVERY + "/ram:DespatchAdviceReferencedDocument", ONCE);
    aB.add ("BT-16", DELIVERY + "/ram:DespatchAdviceReferencedDocument/ram:IssuerAssignedID", ONCE);
    // BT-15 Receiving advice reference
    aB.add ("BT-15", DELIVERY + "/ram:ReceivingAdviceReferencedDocument", ONCE);
    aB.add ("BT-15", DELIVERY + "/ram:ReceivingAdviceReferencedDocument/ram:IssuerAssignedID", ONCE);
    // BT-197 Delivery note reference
    aB.add ("BT-197", DELIVERY + "/ram:DeliveryNoteReferencedDocument", ONCE);
    aB.add ("BT-197", DELIVERY + "/ram:DeliveryNoteReferencedDocument/ram:IssuerAssignedID", ONCE);
  }

  private static void _addPaymentTermsRules (@NonNull final PurificationRuleSetBuilder aB)
  {
    // BG-33 PAYMENT TERMS
    aB.add ("BG-33", PAYMENT_TERMS, UNBOUNDED);
    aB.add ("BT-20", PAYMENT_TERMS + "/ram:Description", ONCE);
    _addDateTimeRules (aB, "BT-9", PAYMENT_TERMS + "/ram:DueDateDateTime");
    aB.add ("BT-89", PAYMENT_TERMS + "/ram:DirectDebitMandateID", ONCE);
    // BG-35 EARLY PAYMENT DISCOUNT
    final String sDiscount = PAYMENT_TERMS + "/ram:ApplicableTradePaymentDiscountTerms";
    aB.add ("BG-35", sDiscount, ONCE);
    _addDateTimeRules (aB, "BT-170", sDiscount + "/ram:BasisDateTime");
    aB.add ("BT-171", sDiscount + "/ram:CalculationPercent", ONCE);
    aB.add ("BT-172", sDiscount + "/ram:ActualDiscountAmount", ONCE);
    // BG-36 LATE PAYMENT PENALTY
    final String sPenalty = PAYMENT_TERMS + "/ram:ApplicableTradePaymentPenaltyTerms";
    aB.add ("BG-36", sPenalty, ONCE);
    _addDateTimeRules (aB, "BT-181", sPenalty + "/ram:BasisDateTime");
    aB.add ("BT-182", sPenalty + "/ram:CalculationPercent", ONCE);
    aB.add ("BT-183", sPenalty + "/ram:ActualPenaltyAmount", ONCE);
  }

  private static void _addSettlementRules (@NonNull final PurificationRuleSetBuilder aB)
  {
    // ram:ApplicableHeaderTradeSettlement is mandatory in the XML Schema of all CII releases
    // before D25A
    aB.addKeepWhenEmpty (null, SETTLEMENT, ONCE);
    aB.add ("BT-90", SETTLEMENT + "/ram:CreditorReferenceID", ONCE);
    aB.add ("BT-83", SETTLEMENT + "/ram:PaymentReference", ONCE);
    aB.add ("BT-6", SETTLEMENT + "/ram:TaxCurrencyCode", ONCE);
    aB.add ("BT-5", SETTLEMENT + "/ram:InvoiceCurrencyCode", ONCE);

    // BT-167 VAT accounting currency exchange rate
    aB.add ("BT-167", EXCHANGE, ONCE);
    aB.add ("BT-167", EXCHANGE + "/ram:ConversionRate", ONCE);
    aB.add ("BT-167-1", EXCHANGE + "/ram:TargetCurrencyCode", ONCE);
    aB.add ("BT-167-2", EXCHANGE + "/ram:SourceCurrencyCode", ONCE);

    // BG-10 PAYEE
    aB.add ("BG-10", PAYEE, ONCE);
    aB.add ("BT-60", PAYEE + "/ram:GlobalID", ONCE, "schemeID");
    aB.add ("BT-59", PAYEE + "/ram:Name", ONCE);
    aB.add ("BT-61", PAYEE + "/ram:SpecifiedLegalOrganization", ONCE);
    aB.add ("BT-61", PAYEE + "/ram:SpecifiedLegalOrganization/ram:ID", ONCE, "schemeID");

    // BG-16 PAYMENT INSTRUCTIONS
    aB.add ("BG-16", PAYMENT_MEANS, UNBOUNDED);
    aB.add ("BT-81", PAYMENT_MEANS + "/ram:TypeCode", ONCE);
    aB.add ("BT-82", PAYMENT_MEANS + "/ram:Information", ONCE);
    // BG-18 PAYMENT CARD INFORMATION
    aB.add ("BG-18", PAYMENT_MEANS + "/ram:ApplicableTradeSettlementFinancialCard", ONCE);
    aB.add ("BT-87", PAYMENT_MEANS + "/ram:ApplicableTradeSettlementFinancialCard/ram:ID", ONCE);
    aB.add ("BT-88", PAYMENT_MEANS + "/ram:ApplicableTradeSettlementFinancialCard/ram:CardholderName", ONCE);
    // BG-19 DIRECT DEBIT - BT-91 is the IBAN if applicable and the proprietary identifier else
    aB.add ("BT-91/BT-216", PAYMENT_MEANS + "/ram:PayerPartyDebtorFinancialAccount", ONCE);
    aB.add ("BT-91", PAYMENT_MEANS + "/ram:PayerPartyDebtorFinancialAccount/ram:IBANID", ONCE);
    aB.add ("BT-91", PAYMENT_MEANS + "/ram:PayerPartyDebtorFinancialAccount/ram:ProprietaryID", ONCE);
    aB.add ("BT-216", PAYMENT_MEANS + "/ram:PayerPartyDebtorFinancialAccount/ram:AccountName", ONCE);
    aB.add ("BT-215", PAYMENT_MEANS + "/ram:PayerSpecifiedDebtorFinancialInstitution", ONCE);
    aB.add ("BT-215", PAYMENT_MEANS + "/ram:PayerSpecifiedDebtorFinancialInstitution/ram:BICID", ONCE);
    // BG-17 CREDIT TRANSFER - BT-84 is the IBAN if applicable and the proprietary identifier else
    aB.add ("BG-17", PAYMENT_MEANS + "/ram:PayeePartyCreditorFinancialAccount", ONCE);
    aB.add ("BT-84", PAYMENT_MEANS + "/ram:PayeePartyCreditorFinancialAccount/ram:IBANID", ONCE);
    aB.add ("BT-84", PAYMENT_MEANS + "/ram:PayeePartyCreditorFinancialAccount/ram:ProprietaryID", ONCE);
    aB.add ("BT-85", PAYMENT_MEANS + "/ram:PayeePartyCreditorFinancialAccount/ram:AccountName", ONCE);
    // BT-86 Payment service provider identifier
    aB.add ("BT-86", PAYMENT_MEANS + "/ram:PayeeSpecifiedCreditorFinancialInstitution", ONCE);
    aB.add ("BT-86", PAYMENT_MEANS + "/ram:PayeeSpecifiedCreditorFinancialInstitution/ram:BICID", ONCE);

    // BG-23 VAT BREAKDOWN
    aB.add ("BG-23", TRADE_TAX, UNBOUNDED);
    aB.add ("BT-117", TRADE_TAX + "/ram:CalculatedAmount", ONCE);
    aB.add ("BT-118-1", TRADE_TAX + "/ram:TypeCode", ONCE);
    aB.add ("BT-120", TRADE_TAX + "/ram:ExemptionReason", ONCE);
    aB.add ("BT-116", TRADE_TAX + "/ram:BasisAmount", ONCE);
    aB.add ("BT-184", TRADE_TAX + "/ram:CurrencyCode", ONCE);
    aB.add ("BT-118", TRADE_TAX + "/ram:CategoryCode", ONCE);
    aB.add ("BT-121", TRADE_TAX + "/ram:ExemptionReasonCode", ONCE);
    aB.add ("BT-7", TRADE_TAX + "/ram:TaxPointDate", ONCE);
    aB.add ("BT-7", TRADE_TAX + "/ram:TaxPointDate/udt:DateString", ONCE, "format");
    aB.add ("BT-8", TRADE_TAX + "/ram:DueDateTypeCode", ONCE);
    aB.add ("BT-119", TRADE_TAX + "/ram:RateApplicablePercent", ONCE);
    aB.add ("BT-210", TRADE_TAX + "/ram:SupplyTypeCode", ONCE);

    // BG-14 INVOICING PERIOD
    aB.add ("BG-14", PERIOD, ONCE);
    _addDateTimeRules (aB, "BT-73", PERIOD + "/ram:StartDateTime");
    _addDateTimeRules (aB, "BT-74", PERIOD + "/ram:EndDateTime");

    // BG-20 DOCUMENT LEVEL ALLOWANCES and BG-21 DOCUMENT LEVEL CHARGES AND TAXES
    aB.add ("BG-20/BG-21", ALLOWANCE_CHARGE, UNBOUNDED);
    _addChargeIndicatorRules (aB, "BG-20-1/BG-21-1", ALLOWANCE_CHARGE);
    aB.add ("BT-94/BT-101", ALLOWANCE_CHARGE + "/ram:CalculationPercent", ONCE);
    aB.add ("BT-93/BT-100", ALLOWANCE_CHARGE + "/ram:BasisAmount", ONCE);
    aB.add ("BT-92/BT-99", ALLOWANCE_CHARGE + "/ram:ActualAmount", ONCE);
    // BT-177 Document level non-VAT tax code shares the element with BT-98 and BT-105 and
    // requires @listID (BT-177-1) and @listAgencyID, see CII-DT-100 and CII-DT-101
    aB.add ("BT-98/BT-105/BT-177", ALLOWANCE_CHARGE + "/ram:ReasonCode", ONCE, "listID", "listAgencyID");
    aB.add ("BT-97/BT-104", ALLOWANCE_CHARGE + "/ram:Reason", ONCE);
    aB.add ("BT-95/BT-102", ALLOWANCE_CHARGE + "/ram:CategoryTradeTax", ONCE);
    aB.add ("BT-95-1/BT-102-1", ALLOWANCE_CHARGE + "/ram:CategoryTradeTax/ram:TypeCode", ONCE);
    aB.add ("BT-95/BT-102", ALLOWANCE_CHARGE + "/ram:CategoryTradeTax/ram:CategoryCode", ONCE);
    aB.add ("BT-96/BT-103", ALLOWANCE_CHARGE + "/ram:CategoryTradeTax/ram:RateApplicablePercent", ONCE);
    aB.add ("BT-173/BT-175", ALLOWANCE_CHARGE + "/ram:CategoryTradeTax/ram:ExemptionReason", ONCE);
    aB.add ("BT-174/BT-176", ALLOWANCE_CHARGE + "/ram:CategoryTradeTax/ram:ExemptionReasonCode", ONCE);
    aB.add ("BT-213/BT-214", ALLOWANCE_CHARGE + "/ram:CategoryTradeTax/ram:SupplyTypeCode", ONCE);

    // BG-33 PAYMENT TERMS, BG-35 EARLY PAYMENT DISCOUNT and BG-36 LATE PAYMENT PENALTY
    _addPaymentTermsRules (aB);

    // BG-22 DOCUMENT TOTALS
    aB.add ("BG-22", TOTALS, ONCE);
    aB.add ("BT-106", TOTALS + "/ram:LineTotalAmount", ONCE);
    aB.add ("BT-108", TOTALS + "/ram:ChargeTotalAmount", ONCE);
    aB.add ("BT-107", TOTALS + "/ram:AllowanceTotalAmount", ONCE);
    aB.add ("BT-109", TOTALS + "/ram:TaxBasisTotalAmount", ONCE);
    aB.add ("BT-110/BT-111", TOTALS + "/ram:TaxTotalAmount", TWICE, "currencyID");
    aB.add ("BT-112", TOTALS + "/ram:GrandTotalAmount", ONCE);
    aB.add ("BT-114", TOTALS + "/ram:RoundingAmount", ONCE);
    aB.add ("BT-113", TOTALS + "/ram:TotalPrepaidAmount", ONCE);
    aB.add ("BT-115", TOTALS + "/ram:DuePayableAmount", ONCE);

    // BG-34 CHARGES ON BEHALF OF A THIRD PARTY
    aB.add ("BG-34", THIRD_PARTY, UNBOUNDED);
    aB.add ("BT-179", THIRD_PARTY + "/ram:ActualAmount", ONCE);
    aB.add ("BT-180", THIRD_PARTY + "/ram:Reason", ONCE);

    // BG-3 PRECEDING INVOICE REFERENCE
    aB.add ("BG-3", PRECEDING, UNBOUNDED);
    aB.add ("BT-25", PRECEDING + "/ram:IssuerAssignedID", ONCE);
    aB.add ("BT-26", PRECEDING + "/ram:FormattedIssueDateTime", ONCE);
    aB.add ("BT-26", PRECEDING + "/ram:FormattedIssueDateTime/qdt:DateTimeString", ONCE, "format");
    aB.add ("BT-202", PRECEDING + "/ram:TypeCode", ONCE);

    // BT-19 Buyer accounting reference
    aB.add ("BT-19", SETTLEMENT + "/ram:ReceivableSpecifiedTradeAccountingAccount", ONCE);
    aB.add ("BT-19", SETTLEMENT + "/ram:ReceivableSpecifiedTradeAccountingAccount/ram:ID", ONCE);
  }

  @NonNull
  private static PurificationRuleSet _createRuleSet ()
  {
    final PurificationRuleSetBuilder aB = new PurificationRuleSetBuilder ("en16931-2026-cii",
                                                                          CEN16931Syntax.QNAME_CII);
    aB.addNamespacePrefix (CEN16931Syntax.PREFIX_CII_RSM, CEN16931Syntax.NS_URI_CII_RSM);
    aB.addNamespacePrefix (CEN16931Syntax.PREFIX_CII_RAM, CEN16931Syntax.NS_URI_CII_RAM);
    aB.addNamespacePrefix (CEN16931Syntax.PREFIX_CII_UDT, CEN16931Syntax.NS_URI_CII_UDT);
    aB.addNamespacePrefix (CEN16931Syntax.PREFIX_CII_QDT, CEN16931Syntax.NS_URI_CII_QDT);

    _addDocumentContextRules (aB);

    // rsm:SupplyChainTradeTransaction is mandatory in the XML Schema
    aB.addKeepWhenEmpty (null, TRANSACTION, ONCE);
    _addLineRules (aB);
    _addAgreementRules (aB);
    _addDeliveryRules (aB);
    _addSettlementRules (aB);

    return aB.build ();
  }

  /**
   * @return The EN 16931:2026 rule set for a CII CrossIndustryInvoice. Never <code>null</code>.
   */
  @NonNull
  public static PurificationRuleSet getCrossIndustryInvoiceRuleSet ()
  {
    return CROSS_INDUSTRY_INVOICE;
  }
}
