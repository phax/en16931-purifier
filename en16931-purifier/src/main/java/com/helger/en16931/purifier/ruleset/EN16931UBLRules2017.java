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
import com.helger.en16931.purifier.CEN16931Syntax;
import com.helger.en16931.purifier.rule.PurificationRuleNode;
import com.helger.en16931.purifier.rule.PurificationRuleSet;
import com.helger.en16931.purifier.rule.PurificationRuleSetBuilder;

/**
 * The EN 16931:2017 core message in the UBL syntax, according to CEN/TS 16931-3-2. The rule sets
 * created by this class are valid for all UBL 2.x versions, because all of them use the same XML
 * namespace URIs and the same element names.
 * <p>
 * The UBL Credit Note rule set contains the union of the UBL 2.1 and the UBL 2.2+ representation
 * of BT-9 and BT-11, because both of them carry the same business term. In a UBL 2.1 Credit Note
 * the UBL 2.2+ elements cannot occur without breaking the XML Schema validation anyway.
 *
 * @author Philip Helger
 */
@Immutable
public final class EN16931UBLRules2017
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
  private static final String ALLOWANCE_CHARGE = "/cac:AllowanceCharge";
  private static final String TAX_TOTAL = "/cac:TaxTotal";
  private static final String TOTALS = "/cac:LegalMonetaryTotal";
  private static final String PTS_VAT = "/cac:PartyTaxScheme[cac:TaxScheme/cbc:ID='VAT']";
  private static final String PTS_NO_SCHEME = "/cac:PartyTaxScheme[not(cac:TaxScheme/cbc:ID)]";
  private static final String ADR_BT18_INVOICE = "/cac:AdditionalDocumentReference[cbc:DocumentTypeCode='130']";
  private static final String ADR_BT18_CREDIT_NOTE = "/cac:AdditionalDocumentReference[cbc:DocumentType='ATS']";
  private static final String ADR_BG24 = "/cac:AdditionalDocumentReference";

  private static final PurificationRuleSet INVOICE = _createInvoiceRuleSet ();
  private static final PurificationRuleSet CREDIT_NOTE = _createCreditNoteRuleSet ();

  private EN16931UBLRules2017 ()
  {}

  private static void _addTaxSchemeRules (@NonNull final PurificationRuleSetBuilder aB,
                                          @NonNull @Nonempty final String sBase)
  {
    // cac:TaxScheme is mandatory in cac:TaxCategory and cac:PartyTaxScheme
    aB.addKeepWhenEmpty (null, sBase + "/cac:TaxScheme", ONCE);
    aB.add (null, sBase + "/cac:TaxScheme/cbc:ID", ONCE);
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

    // BG-26 INVOICE LINE PERIOD
    aB.add ("BG-26", sLine + "/cac:InvoicePeriod", ONCE);
    aB.add ("BT-134", sLine + "/cac:InvoicePeriod/cbc:StartDate", ONCE);
    aB.add ("BT-135", sLine + "/cac:InvoicePeriod/cbc:EndDate", ONCE);

    // BT-132 Referenced purchase order line reference
    aB.add ("BT-132", sLine + "/cac:OrderLineReference", ONCE);
    aB.add ("BT-132", sLine + "/cac:OrderLineReference/cbc:LineID", ONCE);

    // BT-128 Invoice line object identifier
    aB.add ("BT-128", sLine + "/cac:DocumentReference", ONCE);
    aB.add ("BT-128", sLine + "/cac:DocumentReference/cbc:ID", ONCE, "schemeID");
    aB.add (null, sLine + "/cac:DocumentReference/cbc:DocumentTypeCode", ONCE);

    // BG-27 INVOICE LINE ALLOWANCES and BG-28 INVOICE LINE CHARGES
    aB.add ("BG-27/BG-28", sLine + "/cac:AllowanceCharge", UNBOUNDED);
    aB.add (null, sLine + "/cac:AllowanceCharge/cbc:ChargeIndicator", ONCE);
    aB.add ("BT-140/BT-145", sLine + "/cac:AllowanceCharge/cbc:AllowanceChargeReasonCode", ONCE);
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
    aB.add ("BT-151", sLine + "/cac:Item/cac:ClassifiedTaxCategory/cbc:ID", ONCE);
    aB.add ("BT-152", sLine + "/cac:Item/cac:ClassifiedTaxCategory/cbc:Percent", ONCE);
    _addTaxSchemeRules (aB, sLine + "/cac:Item/cac:ClassifiedTaxCategory");

    // BG-32 ITEM ATTRIBUTES
    aB.add ("BG-32", sLine + "/cac:Item/cac:AdditionalItemProperty", UNBOUNDED);
    aB.add ("BT-160", sLine + "/cac:Item/cac:AdditionalItemProperty/cbc:Name", ONCE);
    aB.add ("BT-161", sLine + "/cac:Item/cac:AdditionalItemProperty/cbc:Value", ONCE);

    // BG-29 PRICE DETAILS
    aB.add ("BG-29", sLine + "/cac:Price", ONCE);
    aB.add ("BT-146", sLine + "/cac:Price/cbc:PriceAmount", ONCE, "currencyID");
    aB.add ("BT-149", sLine + "/cac:Price/cbc:BaseQuantity", ONCE, "unitCode");
    aB.add ("BT-147/BT-148", sLine + "/cac:Price/cac:AllowanceCharge", ONCE);
    aB.add (null, sLine + "/cac:Price/cac:AllowanceCharge/cbc:ChargeIndicator", ONCE);
    aB.add ("BT-147", sLine + "/cac:Price/cac:AllowanceCharge/cbc:Amount", ONCE, "currencyID");
    aB.add ("BT-148", sLine + "/cac:Price/cac:AllowanceCharge/cbc:BaseAmount", ONCE, "currencyID");
  }

  private static void _addBG24Rules (@NonNull final PurificationRuleSetBuilder aB)
  {
    // BG-24 ADDITIONAL SUPPORTING DOCUMENTS - the catch all without a discriminator
    aB.add ("BG-24", ADR_BG24, UNBOUNDED);
    aB.add ("BT-122", ADR_BG24 + "/cbc:ID", ONCE);
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

  @NonNull
  private static PurificationRuleSetBuilder _createCommonBuilder (@NonNull @Nonempty final String sID,
                                                                  @NonNull final QName aRootElementName)
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
    aB.add ("BT-9", "/cbc:DueDate", ONCE);
    aB.add ("BT-7", "/cbc:TaxPointDate", ONCE);
    // BG-1 INVOICE NOTE - BT-21 is embedded in the text of BT-22
    aB.add ("BG-1/BT-22", "/cbc:Note", UNBOUNDED);
    aB.add ("BT-5", "/cbc:DocumentCurrencyCode", ONCE);
    aB.add ("BT-6", "/cbc:TaxCurrencyCode", ONCE);
    aB.add ("BT-19", "/cbc:AccountingCost", ONCE);
    aB.add ("BT-10", "/cbc:BuyerReference", ONCE);

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

    // BT-16, BT-15, BT-17 and BT-12
    aB.add ("BT-16", "/cac:DespatchDocumentReference", ONCE);
    aB.add ("BT-16", "/cac:DespatchDocumentReference/cbc:ID", ONCE);
    aB.add ("BT-15", "/cac:ReceiptDocumentReference", ONCE);
    aB.add ("BT-15", "/cac:ReceiptDocumentReference/cbc:ID", ONCE);
    aB.add ("BT-17", "/cac:OriginatorDocumentReference", ONCE);
    aB.add ("BT-17", "/cac:OriginatorDocumentReference/cbc:ID", ONCE);
    aB.add ("BT-12", "/cac:ContractDocumentReference", ONCE);
    aB.add ("BT-12", "/cac:ContractDocumentReference/cbc:ID", ONCE);

    // BT-11 Project reference - UBL 2.1 Invoice and UBL 2.2+ Credit Note
    aB.add ("BT-11", "/cac:ProjectReference", ONCE);
    aB.add ("BT-11", "/cac:ProjectReference/cbc:ID", ONCE);

    // BG-4 SELLER
    aB.addKeepWhenEmpty ("BG-4", "/cac:AccountingSupplierParty", ONCE);
    aB.add ("BG-4", "/cac:AccountingSupplierParty/cac:Party", ONCE);
    aB.add ("BT-34", SELLER + "/cbc:EndpointID", ONCE, "schemeID");
    // BT-29 Seller identifier and BT-90 Bank assigned creditor identifier
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
    // by the tax scheme, so that enforcing the cardinality cannot drop the wrong one
    aB.add ("BT-31", SELLER + PTS_VAT, ONCE);
    aB.add ("BT-31", SELLER + PTS_VAT + "/cbc:CompanyID", ONCE);
    _addTaxSchemeRules (aB, SELLER + PTS_VAT);
    aB.add ("BT-32", SELLER + "/cac:PartyTaxScheme", ONCE);
    aB.add ("BT-32", SELLER + "/cac:PartyTaxScheme/cbc:CompanyID", ONCE);
    _addTaxSchemeRules (aB, SELLER + "/cac:PartyTaxScheme");
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
    aB.add ("BT-46", BUYER + "/cac:PartyIdentification", ONCE);
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
    aB.add ("BT-48", BUYER + PTS_VAT + "/cbc:CompanyID", ONCE);
    _addTaxSchemeRules (aB, BUYER + PTS_VAT);
    aB.add ("BT-48", BUYER + PTS_NO_SCHEME, ONCE);
    aB.add ("BT-48", BUYER + PTS_NO_SCHEME + "/cbc:CompanyID", ONCE);
    _addTaxSchemeRules (aB, BUYER + PTS_NO_SCHEME);
    aB.add ("BT-44/BT-47", BUYER + "/cac:PartyLegalEntity", ONCE);
    aB.add ("BT-44", BUYER + "/cac:PartyLegalEntity/cbc:RegistrationName", ONCE);
    aB.add ("BT-47", BUYER + "/cac:PartyLegalEntity/cbc:CompanyID", ONCE, "schemeID");
    // BG-9 BUYER CONTACT
    _addContactRules (aB, BUYER, "BG-9", "BT-56", "BT-57", "BT-58");

    // BG-10 PAYEE
    aB.add ("BG-10", PAYEE, ONCE);
    // BT-60 Payee identifier and BT-90 Bank assigned creditor identifier
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
    aB.add ("BT-63", TAX_REP + PTS_VAT + "/cbc:CompanyID", ONCE);
    _addTaxSchemeRules (aB, TAX_REP + PTS_VAT);
    aB.add ("BT-63", TAX_REP + PTS_NO_SCHEME, ONCE);
    aB.add ("BT-63", TAX_REP + PTS_NO_SCHEME + "/cbc:CompanyID", ONCE);
    _addTaxSchemeRules (aB, TAX_REP + PTS_NO_SCHEME);
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

    // BG-13 DELIVERY INFORMATION
    aB.add ("BG-13", DELIVERY, ONCE);
    aB.add ("BT-72", DELIVERY + "/cbc:ActualDeliveryDate", ONCE);
    aB.add ("BT-71", DELIVERY + "/cac:DeliveryLocation", ONCE);
    aB.add ("BT-71", DELIVERY + "/cac:DeliveryLocation/cbc:ID", ONCE, "schemeID");
    // BG-15 DELIVER TO ADDRESS
    _addPostalAddressRules (aB,
                            DELIVERY + "/cac:DeliveryLocation/cac:Address",
                            "BG-15",
                            "BT-75",
                            "BT-76",
                            "BT-165",
                            "BT-77",
                            "BT-78",
                            "BT-79",
                            "BT-80");
    aB.add ("BT-70", DELIVERY + "/cac:DeliveryParty", ONCE);
    aB.add ("BT-70", DELIVERY + "/cac:DeliveryParty/cac:PartyName", ONCE);
    aB.add ("BT-70", DELIVERY + "/cac:DeliveryParty/cac:PartyName/cbc:Name", ONCE);

    // BG-16 PAYMENT INSTRUCTIONS
    aB.add ("BG-16", PAYMENT_MEANS, UNBOUNDED);
    // BT-82 Payment means text is the "name" attribute of BT-81
    aB.add ("BT-81/BT-82", PAYMENT_MEANS + "/cbc:PaymentMeansCode", ONCE, "name");
    aB.add ("BT-83", PAYMENT_MEANS + "/cbc:PaymentID", ONCE);
    // BG-18 PAYMENT CARD INFORMATION
    aB.add ("BG-18", PAYMENT_MEANS + "/cac:CardAccount", ONCE);
    aB.add ("BT-87", PAYMENT_MEANS + "/cac:CardAccount/cbc:PrimaryAccountNumberID", ONCE);
    // cbc:NetworkID is mandatory in the UBL XML Schema but is not an EN 16931 business term
    aB.add (null, PAYMENT_MEANS + "/cac:CardAccount/cbc:NetworkID", ONCE);
    aB.add ("BT-88", PAYMENT_MEANS + "/cac:CardAccount/cbc:HolderName", ONCE);
    // BG-17 CREDIT TRANSFER
    aB.add ("BG-17", PAYMENT_MEANS + "/cac:PayeeFinancialAccount", ONCE);
    aB.add ("BT-84", PAYMENT_MEANS + "/cac:PayeeFinancialAccount/cbc:ID", ONCE);
    aB.add ("BT-85", PAYMENT_MEANS + "/cac:PayeeFinancialAccount/cbc:Name", ONCE);
    aB.add ("BT-86", PAYMENT_MEANS + "/cac:PayeeFinancialAccount/cac:FinancialInstitutionBranch", ONCE);
    aB.add ("BT-86", PAYMENT_MEANS + "/cac:PayeeFinancialAccount/cac:FinancialInstitutionBranch/cbc:ID", ONCE);
    // BG-19 DIRECT DEBIT
    aB.add ("BG-19", PAYMENT_MEANS + "/cac:PaymentMandate", ONCE);
    aB.add ("BT-89", PAYMENT_MEANS + "/cac:PaymentMandate/cbc:ID", ONCE);
    aB.add ("BT-91", PAYMENT_MEANS + "/cac:PaymentMandate/cac:PayerFinancialAccount", ONCE);
    aB.add ("BT-91", PAYMENT_MEANS + "/cac:PaymentMandate/cac:PayerFinancialAccount/cbc:ID", ONCE);

    // BT-20 PAYMENT TERMS
    aB.add ("BT-20", "/cac:PaymentTerms", ONCE);
    aB.add ("BT-20", "/cac:PaymentTerms/cbc:Note", ONCE);

    // BG-20 DOCUMENT LEVEL ALLOWANCES and BG-21 DOCUMENT LEVEL CHARGES
    aB.add ("BG-20/BG-21", ALLOWANCE_CHARGE, UNBOUNDED);
    aB.add (null, ALLOWANCE_CHARGE + "/cbc:ChargeIndicator", ONCE);
    aB.add ("BT-98/BT-105", ALLOWANCE_CHARGE + "/cbc:AllowanceChargeReasonCode", ONCE);
    aB.add ("BT-97/BT-104", ALLOWANCE_CHARGE + "/cbc:AllowanceChargeReason", ONCE);
    aB.add ("BT-94/BT-101", ALLOWANCE_CHARGE + "/cbc:MultiplierFactorNumeric", ONCE);
    aB.add ("BT-92/BT-99", ALLOWANCE_CHARGE + "/cbc:Amount", ONCE, "currencyID");
    aB.add ("BT-93/BT-100", ALLOWANCE_CHARGE + "/cbc:BaseAmount", ONCE, "currencyID");
    aB.add ("BT-95/BT-102", ALLOWANCE_CHARGE + "/cac:TaxCategory", ONCE);
    aB.add ("BT-95/BT-102", ALLOWANCE_CHARGE + "/cac:TaxCategory/cbc:ID", ONCE);
    aB.add ("BT-96/BT-103", ALLOWANCE_CHARGE + "/cac:TaxCategory/cbc:Percent", ONCE);
    _addTaxSchemeRules (aB, ALLOWANCE_CHARGE + "/cac:TaxCategory");

    // BT-110 Invoice total VAT amount and BT-111 in accounting currency
    aB.add ("BT-110/BT-111", TAX_TOTAL, TWICE);
    aB.add ("BT-110/BT-111", TAX_TOTAL + "/cbc:TaxAmount", ONCE, "currencyID");
    // BG-23 VAT BREAKDOWN
    aB.add ("BG-23", TAX_TOTAL + "/cac:TaxSubtotal", UNBOUNDED);
    aB.add ("BT-116", TAX_TOTAL + "/cac:TaxSubtotal/cbc:TaxableAmount", ONCE, "currencyID");
    aB.add ("BT-117", TAX_TOTAL + "/cac:TaxSubtotal/cbc:TaxAmount", ONCE, "currencyID");
    aB.addKeepWhenEmpty ("BG-23", TAX_TOTAL + "/cac:TaxSubtotal/cac:TaxCategory", ONCE);
    aB.add ("BT-118", TAX_TOTAL + "/cac:TaxSubtotal/cac:TaxCategory/cbc:ID", ONCE);
    aB.add ("BT-119", TAX_TOTAL + "/cac:TaxSubtotal/cac:TaxCategory/cbc:Percent", ONCE);
    aB.add ("BT-121", TAX_TOTAL + "/cac:TaxSubtotal/cac:TaxCategory/cbc:TaxExemptionReasonCode", ONCE);
    aB.add ("BT-120", TAX_TOTAL + "/cac:TaxSubtotal/cac:TaxCategory/cbc:TaxExemptionReason", ONCE);
    _addTaxSchemeRules (aB, TAX_TOTAL + "/cac:TaxSubtotal/cac:TaxCategory");

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

    return aB;
  }

  @NonNull
  private static PurificationRuleSet _createInvoiceRuleSet ()
  {
    final PurificationRuleSetBuilder aB = _createCommonBuilder ("en16931-2017-ubl-invoice",
                                                                CEN16931Syntax.QNAME_UBL_INVOICE);

    // BT-3 Invoice type code
    aB.add ("BT-3", "/cbc:InvoiceTypeCode", ONCE);

    // BT-18 Invoiced object identifier
    aB.add ("BT-18", ADR_BT18_INVOICE, ONCE);
    aB.add ("BT-18", ADR_BT18_INVOICE + "/cbc:ID", ONCE, "schemeID");
    aB.add (null, ADR_BT18_INVOICE + "/cbc:DocumentTypeCode", ONCE);
    _addBG24Rules (aB);

    // BG-25 INVOICE LINE
    _addLineRules (aB, "/cac:InvoiceLine", "InvoicedQuantity");

    return aB.build ();
  }

  @NonNull
  private static PurificationRuleSet _createCreditNoteRuleSet ()
  {
    final PurificationRuleSetBuilder aB = _createCommonBuilder ("en16931-2017-ubl-creditnote",
                                                                CEN16931Syntax.QNAME_UBL_CREDIT_NOTE);

    // BT-3 Invoice type code
    aB.add ("BT-3", "/cbc:CreditNoteTypeCode", ONCE);

    // BT-9 Payment due date - in a UBL 2.1 Credit Note it is on the payment means
    aB.add ("BT-9", PAYMENT_MEANS + "/cbc:PaymentDueDate", ONCE);

    // BT-18 Invoiced object identifier - a Credit Note uses cbc:DocumentType, but cope with the
    // Invoice representation as well
    aB.add ("BT-18", ADR_BT18_CREDIT_NOTE, ONCE);
    aB.add ("BT-18", ADR_BT18_CREDIT_NOTE + "/cbc:ID", ONCE, "schemeID");
    aB.add (null, ADR_BT18_CREDIT_NOTE + "/cbc:DocumentType", ONCE);
    aB.add ("BT-18", ADR_BT18_INVOICE, ONCE);
    aB.add ("BT-18", ADR_BT18_INVOICE + "/cbc:ID", ONCE, "schemeID");
    aB.add (null, ADR_BT18_INVOICE + "/cbc:DocumentTypeCode", ONCE);
    // In UBL 2.1 BT-11 uses cac:AdditionalDocumentReference without a discriminator, which is
    // covered by the BG-24 catch all rules
    _addBG24Rules (aB);

    // BG-25 INVOICE LINE
    _addLineRules (aB, "/cac:CreditNoteLine", "CreditedQuantity");

    return aB.build ();
  }

  /**
   * @return The EN 16931:2017 rule set for a UBL Invoice. Never <code>null</code>.
   */
  @NonNull
  public static PurificationRuleSet getInvoiceRuleSet ()
  {
    return INVOICE;
  }

  /**
   * @return The EN 16931:2017 rule set for a UBL Credit Note. Never <code>null</code>.
   */
  @NonNull
  public static PurificationRuleSet getCreditNoteRuleSet ()
  {
    return CREDIT_NOTE;
  }
}
