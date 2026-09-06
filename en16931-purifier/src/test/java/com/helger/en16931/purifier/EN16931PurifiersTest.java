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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.collection.commons.CommonsHashSet;
import com.helger.collection.commons.ICommonsSet;
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
  public void testAllRuleSets ()
  {
    for (final EEN16931Edition eEdition : EEN16931Edition.values ())
    {
      for (final EEN16931SyntaxKind eSyntaxKind : EEN16931SyntaxKind.values ())
      {
        final PurificationRuleSet aRuleSet = EN16931Purifiers.getRuleSet (eEdition, eSyntaxKind);
        assertNotNull ("No rule set for " + eEdition + " and " + eSyntaxKind, aRuleSet);
        assertEquals (eSyntaxKind.getRootElementName (), aRuleSet.getRootElementName ());
        assertTrue (aRuleSet.getRootNode ().hasChildren ());
      }
      assertTrue (EN16931Purifiers.isSupported (eEdition));
    }
    assertSame (EEN16931Edition.EN2017, EN16931Purifiers.DEFAULT_EDITION);
  }

  @Test
  public void testRuleSetIDsAreUnique ()
  {
    final ICommonsSet <String> aIDs = new CommonsHashSet <> ();
    for (final EEN16931Edition eEdition : EEN16931Edition.values ())
      for (final EEN16931SyntaxKind eSyntaxKind : EEN16931SyntaxKind.values ())
        assertTrue ("Duplicate rule set ID for " + eEdition + " and " + eSyntaxKind,
                    aIDs.add (EN16931Purifiers.getRuleSet (eEdition, eSyntaxKind).getID ()));
  }

  @Test
  public void testCreatePurifier ()
  {
    for (final EEN16931DocumentType e : EEN16931DocumentType.values ())
    {
      for (final EEN16931Edition eEdition : EEN16931Edition.values ())
      {
        final AbstractEN16931Purifier <?, ?> aPurifier = EN16931Purifiers.createPurifier (e, eEdition);
        assertNotNull (aPurifier);
        assertSame (e.getSyntaxKind (), aPurifier.getSyntaxKind ());
        assertSame (eEdition, aPurifier.getEdition ());
        assertNotNull (aPurifier.getRuleSet ());
      }

      final AbstractEN16931Purifier <?, ?> aDefaultPurifier = EN16931Purifiers.createPurifier (e);
      assertNotNull (aDefaultPurifier);
      assertSame (e.getSyntaxKind (), aDefaultPurifier.getSyntaxKind ());
      assertSame (EN16931Purifiers.DEFAULT_EDITION, aDefaultPurifier.getEdition ());
    }
  }
}
