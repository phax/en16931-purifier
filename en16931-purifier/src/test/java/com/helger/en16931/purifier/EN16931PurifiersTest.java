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
import com.helger.en16931.basics.EEN16931DocumentType;
import com.helger.en16931.basics.EEN16931Edition;
import com.helger.en16931.basics.EEN16931SyntaxKind;
import com.helger.en16931.purifier.rule.PurificationRuleSet;

/**
 * Test class for class {@link EN16931Purifiers}.
 *
 * @author Philip Helger
 */
public final class EN16931PurifiersTest
{
  @Test
  public void testAllRuleSetsOf2017 ()
  {
    for (final EEN16931SyntaxKind eSyntaxKind : EEN16931SyntaxKind.values ())
    {
      final PurificationRuleSet aRuleSet = EN16931Purifiers.getRuleSet (EEN16931Edition.EN2017, eSyntaxKind);
      assertNotNull ("No rule set for " + eSyntaxKind, aRuleSet);
      assertEquals (eSyntaxKind.getRootElementName (), aRuleSet.getRootElementName ());
      assertTrue (aRuleSet.getRootNode ().hasChildren ());
    }
    assertTrue (EN16931Purifiers.isSupported (EEN16931Edition.EN2017));
    assertSame (EEN16931Edition.EN2017, EN16931Purifiers.DEFAULT_EDITION);
  }

  @Test
  public void testNoRuleSetsOf2026 ()
  {
    assertFalse (EN16931Purifiers.isSupported (EEN16931Edition.EN2026));
    for (final EEN16931SyntaxKind eSyntaxKind : EEN16931SyntaxKind.values ())
      assertNull (EN16931Purifiers.getRuleSet (EEN16931Edition.EN2026, eSyntaxKind));
  }

  @Test
  public void testPurifyWith2026Fails ()
  {
    final ErrorList aErrorList = new ErrorList ();
    final UBL21InvoicePurifier aPurifier = new UBL21InvoicePurifier (EEN16931Edition.EN2026);
    assertNull (aPurifier.getRuleSet ());
    assertNull (aPurifier.purify (new File (MockTestFiles.UBL_INVOICE_DIR, "base-example.xml"), aErrorList));
    assertTrue (aErrorList.containsAtLeastOneError ());
  }

  @Test
  public void testCreatePurifier ()
  {
    for (final EEN16931DocumentType e : EEN16931DocumentType.values ())
    {
      final AbstractEN16931Purifier <?, ?> aPurifier = EN16931Purifiers.createPurifier (e, EEN16931Edition.EN2017);
      assertNotNull (aPurifier);
      assertSame (e.getSyntaxKind (), aPurifier.getSyntaxKind ());
      assertSame (EEN16931Edition.EN2017, aPurifier.getEdition ());

      final AbstractEN16931Purifier <?, ?> aDefaultPurifier = EN16931Purifiers.createPurifier (e);
      assertNotNull (aDefaultPurifier);
      assertSame (e.getSyntaxKind (), aDefaultPurifier.getSyntaxKind ());
      assertSame (EN16931Purifiers.DEFAULT_EDITION, aDefaultPurifier.getEdition ());
    }
  }
}
