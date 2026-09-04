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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.helger.diagnostics.error.list.ErrorList;
import com.helger.en16931.purifier.rule.PurificationRuleSet;

/**
 * Test class for the enums {@link EEN16931Version}, {@link EEN16931SyntaxKind} and
 * {@link EEN16931DocumentType}.
 *
 * @author Philip Helger
 */
public final class EEN16931VersionTest
{
  @Test
  public void testAllRuleSetsOf2017 ()
  {
    for (final EEN16931SyntaxKind eSyntaxKind : EEN16931SyntaxKind.values ())
    {
      final PurificationRuleSet aRuleSet = EEN16931Version.V2017.getRuleSet (eSyntaxKind);
      assertNotNull ("No rule set for " + eSyntaxKind, aRuleSet);
      assertEquals (eSyntaxKind.getRootElementName (), aRuleSet.getRootElementName ());
      assertTrue (aRuleSet.getRootNode ().hasChildren ());
    }
    assertTrue (EEN16931Version.V2017.isSupported ());
    assertSame (EEN16931Version.V2017, EEN16931Version.DEFAULT);
  }

  @Test
  public void testNoRuleSetsOf2026 ()
  {
    assertFalse (EEN16931Version.V2026.isSupported ());
    for (final EEN16931SyntaxKind eSyntaxKind : EEN16931SyntaxKind.values ())
      assertNull (EEN16931Version.V2026.getRuleSet (eSyntaxKind));
  }

  @Test
  public void testPurifyWith2026Fails ()
  {
    final ErrorList aErrorList = new ErrorList ();
    final UBL21InvoicePurifier aPurifier = new UBL21InvoicePurifier (EEN16931Version.V2026);
    assertNull (aPurifier.getRuleSet ());
    assertNull (aPurifier.purify (new File (MockTestFiles.UBL_INVOICE_DIR, "base-example.xml"), aErrorList));
    assertTrue (aErrorList.containsAtLeastOneError ());
  }

  @Test
  public void testGetFromID ()
  {
    for (final EEN16931Version e : EEN16931Version.values ())
      assertSame (e, EEN16931Version.getFromIDOrNull (e.getID ()));
    for (final EEN16931SyntaxKind e : EEN16931SyntaxKind.values ())
    {
      assertSame (e, EEN16931SyntaxKind.getFromIDOrNull (e.getID ()));
      assertSame (e, EEN16931SyntaxKind.getFromRootElementNameOrNull (e.getRootElementName ()));
    }
    for (final EEN16931DocumentType e : EEN16931DocumentType.values ())
    {
      assertSame (e, EEN16931DocumentType.getFromIDOrNull (e.getID ()));
      assertSame (e,
                  EEN16931DocumentType.getFromSyntaxKindAndVersionOrNull (e.getSyntaxKind (), e.getSyntaxVersion ()));
      final AbstractEN16931Purifier <?, ?> aPurifier = e.createPurifier (EEN16931Version.V2017);
      assertNotNull (aPurifier);
      assertSame (e.getSyntaxKind (), aPurifier.getSyntaxKind ());
      assertSame (EEN16931Version.V2017, aPurifier.getVersion ());
    }
    assertNull (EEN16931Version.getFromIDOrNull ("bogus"));
    assertNull (EEN16931SyntaxKind.getFromIDOrNull ("bogus"));
    assertNull (EEN16931DocumentType.getFromIDOrNull ("bogus"));
    assertNull (EEN16931DocumentType.getFromSyntaxKindAndVersionOrNull (EEN16931SyntaxKind.CII, "D99Z"));
  }
}
