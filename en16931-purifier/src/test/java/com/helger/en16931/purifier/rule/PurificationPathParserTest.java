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
package com.helger.en16931.purifier.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.en16931.purifier.rule.PurificationPathParser.PathStep;
import com.helger.xml.XMLHelper;
import com.helger.xml.serialize.read.DOMReader;

/**
 * Test class for class {@link PurificationPathParser}.
 *
 * @author Philip Helger
 */
public final class PurificationPathParserTest
{
  private static final String NS_CAC = "urn:cac";
  private static final String NS_CBC = "urn:cbc";
  private static final Map <String, String> PREFIX_MAP = new CommonsHashMap <> ();
  static
  {
    PREFIX_MAP.put ("cac", NS_CAC);
    PREFIX_MAP.put ("cbc", NS_CBC);
  }

  private static Element _parseElement (final String sXML)
  {
    final Document aDoc = DOMReader.readXMLDOM (sXML);
    assertNotNull (aDoc);
    return aDoc.getDocumentElement ();
  }

  @Test
  public void testSimplePath ()
  {
    final ICommonsList <PathStep> aSteps = PurificationPathParser.parsePath ("/cac:Party/cbc:ID", PREFIX_MAP);
    assertEquals (2, aSteps.size ());
    assertEquals (NS_CAC, aSteps.get (0).getElementName ().getNamespaceURI ());
    assertEquals ("Party", aSteps.get (0).getElementName ().getLocalPart ());
    assertNull (aSteps.get (0).getFilter ());
    assertEquals (NS_CBC, aSteps.get (1).getElementName ().getNamespaceURI ());
    assertEquals ("ID", aSteps.get (1).getElementName ().getLocalPart ());
  }

  @Test
  public void testChildValuePredicate ()
  {
    final ICommonsList <PathStep> aSteps = PurificationPathParser.parsePath ("/cac:Ref[cbc:TypeCode='130']/cbc:ID",
                                                                            PREFIX_MAP);
    assertEquals (2, aSteps.size ());
    final IPurificationElementFilter aFilter = aSteps.get (0).getFilter ();
    assertNotNull (aFilter);
    assertEquals ("TypeCode='130'", aFilter.getDescription ());
    assertTrue (aFilter.matches (_parseElement ("<Ref xmlns='urn:cac' xmlns:cbc='urn:cbc'><cbc:TypeCode>130</cbc:TypeCode></Ref>")));
    assertFalse (aFilter.matches (_parseElement ("<Ref xmlns='urn:cac' xmlns:cbc='urn:cbc'><cbc:TypeCode>916</cbc:TypeCode></Ref>")));
    assertFalse (aFilter.matches (_parseElement ("<Ref xmlns='urn:cac'/>")));
  }

  @Test
  public void testChildAbsentPredicate ()
  {
    final ICommonsList <PathStep> aSteps = PurificationPathParser.parsePath ("/cac:Ref[not(cbc:TypeCode)]", PREFIX_MAP);
    final IPurificationElementFilter aFilter = aSteps.get (0).getFilter ();
    assertNotNull (aFilter);
    assertEquals ("not(TypeCode)", aFilter.getDescription ());
    assertTrue (aFilter.matches (_parseElement ("<Ref xmlns='urn:cac'/>")));
    assertFalse (aFilter.matches (_parseElement ("<Ref xmlns='urn:cac' xmlns:cbc='urn:cbc'><cbc:TypeCode>130</cbc:TypeCode></Ref>")));
  }

  @Test
  public void testNestedPredicatePath ()
  {
    final ICommonsList <PathStep> aSteps = PurificationPathParser.parsePath ("/cac:Party[cac:Scheme/cbc:ID='VAT']",
                                                                            PREFIX_MAP);
    final IPurificationElementFilter aFilter = aSteps.get (0).getFilter ();
    assertNotNull (aFilter);
    assertEquals ("Scheme/ID='VAT'", aFilter.getDescription ());
    assertTrue (aFilter.matches (_parseElement ("<Party xmlns='urn:cac' xmlns:cbc='urn:cbc'><Scheme><cbc:ID>VAT</cbc:ID></Scheme></Party>")));
    assertFalse (aFilter.matches (_parseElement ("<Party xmlns='urn:cac' xmlns:cbc='urn:cbc'><Scheme><cbc:ID>FC</cbc:ID></Scheme></Party>")));
  }

  @Test
  public void testAttributePredicate ()
  {
    final ICommonsList <PathStep> aSteps = PurificationPathParser.parsePath ("/cac:Reg[cbc:ID/@schemeID='VA']",
                                                                            PREFIX_MAP);
    final IPurificationElementFilter aFilter = aSteps.get (0).getFilter ();
    assertNotNull (aFilter);
    assertEquals ("ID/@schemeID='VA'", aFilter.getDescription ());
    assertTrue (aFilter.matches (_parseElement ("<Reg xmlns='urn:cac' xmlns:cbc='urn:cbc'><cbc:ID schemeID='VA'>x</cbc:ID></Reg>")));
    assertFalse (aFilter.matches (_parseElement ("<Reg xmlns='urn:cac' xmlns:cbc='urn:cbc'><cbc:ID schemeID='FC'>x</cbc:ID></Reg>")));
    assertFalse (aFilter.matches (_parseElement ("<Reg xmlns='urn:cac' xmlns:cbc='urn:cbc'><cbc:ID>x</cbc:ID></Reg>")));
  }

  @Test
  public void testAttributeAbsentPredicate ()
  {
    final ICommonsList <PathStep> aSteps = PurificationPathParser.parsePath ("/cac:Reg[not(cbc:ID/@schemeID)]",
                                                                            PREFIX_MAP);
    final IPurificationElementFilter aFilter = aSteps.get (0).getFilter ();
    assertNotNull (aFilter);
    assertEquals ("not(ID/@schemeID)", aFilter.getDescription ());
    assertTrue (aFilter.matches (_parseElement ("<Reg xmlns='urn:cac' xmlns:cbc='urn:cbc'><cbc:ID>x</cbc:ID></Reg>")));
    assertFalse (aFilter.matches (_parseElement ("<Reg xmlns='urn:cac' xmlns:cbc='urn:cbc'><cbc:ID schemeID='VA'>x</cbc:ID></Reg>")));
  }

  @Test
  public void testOwnAttributePredicate ()
  {
    final ICommonsList <PathStep> aSteps = PurificationPathParser.parsePath ("/cbc:Amount[@currencyID='EUR']",
                                                                            PREFIX_MAP);
    final IPurificationElementFilter aFilter = aSteps.get (0).getFilter ();
    assertNotNull (aFilter);
    assertEquals ("@currencyID='EUR'", aFilter.getDescription ());
    assertTrue (aFilter.matches (_parseElement ("<Amount xmlns='urn:cbc' currencyID='EUR'>1</Amount>")));
    assertFalse (aFilter.matches (_parseElement ("<Amount xmlns='urn:cbc' currencyID='SEK'>1</Amount>")));
  }

  @Test
  public void testInvalidPaths ()
  {
    for (final String sPath : new String [] { "cbc:ID",
                                              "/",
                                              "//cbc:ID",
                                              "/ID",
                                              "/xyz:ID",
                                              "/cbc:",
                                              "/cac:Ref[cbc:TypeCode='130'",
                                              "/cac:Ref[]",
                                              "/cac:Ref[cbc:TypeCode=130]",
                                              "/cac:Ref[not(cbc:TypeCode='130')]",
                                              "/cac:Ref[cbc:ID/@]" })
      try
      {
        PurificationPathParser.parsePath (sPath, PREFIX_MAP);
        fail ("The path '" + sPath + "' should not be parseable");
      }
      catch (final IllegalArgumentException ex)
      {
        // expected
      }
  }

  @Test
  public void testXMLHelperIsUsable ()
  {
    // Sanity check that the test helper really creates namespace aware elements
    final Element aElement = _parseElement ("<Ref xmlns='urn:cac' xmlns:cbc='urn:cbc'><cbc:ID>x</cbc:ID></Ref>");
    assertNotNull (XMLHelper.getFirstChildElementOfName (aElement, NS_CBC, "ID"));
  }
}
