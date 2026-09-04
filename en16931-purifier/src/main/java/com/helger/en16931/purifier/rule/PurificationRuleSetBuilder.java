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

import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.collection.commons.CommonsHashSet;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.collection.commons.ICommonsSet;
import com.helger.en16931.purifier.rule.PurificationPathParser.PathStep;

/**
 * Builder for {@link PurificationRuleSet} objects. Every call to
 * {@link #add(String, String, int, String...)} whitelists one path; all intermediate elements of
 * that path are whitelisted implicitly.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class PurificationRuleSetBuilder
{
  private final String m_sID;
  private final QName m_aRootElementName;
  private final PurificationRuleNode m_aRootNode;
  private final ICommonsOrderedMap <String, String> m_aPrefixMap = new CommonsLinkedHashMap <> ();
  private final ICommonsSet <String> m_aUsedPaths = new CommonsHashSet <> ();

  /**
   * Constructor
   *
   * @param sID
   *        The ID of the rule set to be created. May neither be <code>null</code> nor empty.
   * @param aRootElementName
   *        The expected name of the document element. May not be <code>null</code>.
   */
  public PurificationRuleSetBuilder (@NonNull @Nonempty final String sID, @NonNull final QName aRootElementName)
  {
    ValueEnforcer.notEmpty (sID, "ID");
    ValueEnforcer.notNull (aRootElementName, "RootElementName");
    m_sID = sID;
    m_aRootElementName = aRootElementName;
    m_aRootNode = new PurificationRuleNode (aRootElementName, null);
  }

  /**
   * Register a namespace prefix that may be used in the paths of this rule set.
   *
   * @param sPrefix
   *        The namespace prefix. May neither be <code>null</code> nor empty.
   * @param sNamespaceURI
   *        The namespace URI. May neither be <code>null</code> nor empty.
   * @return this for chaining
   */
  @NonNull
  public PurificationRuleSetBuilder addNamespacePrefix (@NonNull @Nonempty final String sPrefix,
                                                        @NonNull @Nonempty final String sNamespaceURI)
  {
    ValueEnforcer.notEmpty (sPrefix, "Prefix");
    ValueEnforcer.notEmpty (sNamespaceURI, "NamespaceURI");
    m_aPrefixMap.put (sPrefix, sNamespaceURI);
    return this;
  }

  @NonNull
  private PurificationRuleNode _add (final boolean bKeepWhenEmpty,
                                     @Nullable final String sBusinessTermID,
                                     @NonNull @Nonempty final String sPath,
                                     final int nMaxOccurs,
                                     final String @Nullable [] aAllowedAttributes)
  {
    ValueEnforcer.notEmpty (sPath, "Path");
    if (!m_aUsedPaths.add (sPath))
      throw new IllegalArgumentException ("The path '" + sPath + "' was already added to rule set '" + m_sID + "'");

    PurificationRuleNode aNode = m_aRootNode;
    for (final PathStep aStep : PurificationPathParser.parsePath (sPath, m_aPrefixMap))
      aNode = aNode.getOrCreateChild (aStep.getElementName (), aStep.getFilter ());

    return aNode.setBusinessTermID (sBusinessTermID)
                .setMaxOccurs (nMaxOccurs)
                .setKeepWhenEmpty (bKeepWhenEmpty)
                .addAllowedAttributes (aAllowedAttributes);
  }

  /**
   * Whitelist a single path. All intermediate elements of the path are whitelisted implicitly with
   * an unbounded cardinality and without any allowed attribute.
   *
   * @param sBusinessTermID
   *        The ID of the business term or business group carried by this element, e.g.
   *        <code>BT-1</code>. Use <code>null</code> for elements that are only required by the XML
   *        Schema.
   * @param sPath
   *        The path relative to the document element. May neither be <code>null</code> nor empty.
   * @param nMaxOccurs
   *        The maximum number of occurrences within a single parent element as defined by EN
   *        16931. Use {@link PurificationRuleNode#UNBOUNDED} for an unlimited number.
   * @param aAllowedAttributes
   *        The local names of all attributes that are part of the EN 16931 core message or
   *        required by the XML Schema. May be <code>null</code> or empty.
   * @return this for chaining
   */
  @NonNull
  public PurificationRuleSetBuilder add (@Nullable final String sBusinessTermID,
                                         @NonNull @Nonempty final String sPath,
                                         final int nMaxOccurs,
                                         final String @Nullable... aAllowedAttributes)
  {
    _add (false, sBusinessTermID, sPath, nMaxOccurs, aAllowedAttributes);
    return this;
  }

  /**
   * Whitelist a single path like {@link #add(String, String, int, String...)} does, but keep the
   * element even if it is empty after the purification. This must be used for all elements that
   * are mandatory in the XML Schema of the surrounding element, because removing them would create
   * XSD invalid output.
   *
   * @param sBusinessTermID
   *        The ID of the business term or business group carried by this element. May be
   *        <code>null</code>.
   * @param sPath
   *        The path relative to the document element. May neither be <code>null</code> nor empty.
   * @param nMaxOccurs
   *        The maximum number of occurrences within a single parent element as defined by EN
   *        16931. Use {@link PurificationRuleNode#UNBOUNDED} for an unlimited number.
   * @param aAllowedAttributes
   *        The local names of all allowed attributes. May be <code>null</code> or empty.
   * @return this for chaining
   */
  @NonNull
  public PurificationRuleSetBuilder addKeepWhenEmpty (@Nullable final String sBusinessTermID,
                                                      @NonNull @Nonempty final String sPath,
                                                      final int nMaxOccurs,
                                                      final String @Nullable... aAllowedAttributes)
  {
    _add (true, sBusinessTermID, sPath, nMaxOccurs, aAllowedAttributes);
    return this;
  }

  /**
   * @return The created rule set. Never <code>null</code>.
   */
  @NonNull
  public PurificationRuleSet build ()
  {
    return new PurificationRuleSet (m_sID, m_aRootElementName, m_aRootNode);
  }
}
