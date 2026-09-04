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
import org.w3c.dom.Element;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.string.StringHelper;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsLinkedHashSet;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsOrderedSet;

/**
 * A single node of the whitelist tree of a {@link PurificationRuleSet}. Every node represents one
 * XML element that is part of the EN 16931 core message, optionally discriminated by an
 * {@link IPurificationElementFilter}.
 * <p>
 * Instances are mutable while a rule set is built and must be treated read-only afterwards.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class PurificationRuleNode
{
  /** Constant to indicate that an element may occur an unlimited number of times */
  public static final int UNBOUNDED = -1;

  private final QName m_aElementName;
  private final IPurificationElementFilter m_aFilter;
  private final ICommonsList <PurificationRuleNode> m_aChildren = new CommonsArrayList <> ();
  private final ICommonsOrderedSet <String> m_aAllowedAttributes = new CommonsLinkedHashSet <> ();
  private String m_sBusinessTermID;
  private int m_nMaxOccurs = UNBOUNDED;
  private boolean m_bKeepWhenEmpty = false;

  /**
   * Constructor
   *
   * @param aElementName
   *        The XML element name this node represents. May not be <code>null</code>.
   * @param aFilter
   *        The optional filter to discriminate this node from other nodes with the same element
   *        name. May be <code>null</code>.
   */
  public PurificationRuleNode (@NonNull final QName aElementName, @Nullable final IPurificationElementFilter aFilter)
  {
    ValueEnforcer.notNull (aElementName, "ElementName");
    m_aElementName = aElementName;
    m_aFilter = aFilter;
  }

  @NonNull
  public final QName getElementName ()
  {
    return m_aElementName;
  }

  @Nullable
  public final IPurificationElementFilter getFilter ()
  {
    return m_aFilter;
  }

  public final boolean hasFilter ()
  {
    return m_aFilter != null;
  }

  /**
   * @return The ID of the business term or business group this node belongs to (e.g.
   *         <code>BT-1</code>). May be <code>null</code> for elements that are only required by
   *         the XML Schema but do not carry a business term themselves.
   */
  @Nullable
  public final String getBusinessTermID ()
  {
    return m_sBusinessTermID;
  }

  @NonNull
  public final PurificationRuleNode setBusinessTermID (@Nullable final String sBusinessTermID)
  {
    m_sBusinessTermID = sBusinessTermID;
    return this;
  }

  /**
   * @return The maximum number of occurrences of this element within a single parent element as
   *         defined by EN 16931, or {@link #UNBOUNDED}.
   */
  public final int getMaxOccurs ()
  {
    return m_nMaxOccurs;
  }

  @NonNull
  public final PurificationRuleNode setMaxOccurs (final int nMaxOccurs)
  {
    ValueEnforcer.isTrue ( () -> nMaxOccurs == UNBOUNDED || nMaxOccurs > 0, "MaxOccurs must be > 0 or UNBOUNDED");
    m_nMaxOccurs = nMaxOccurs;
    return this;
  }

  public final boolean isUnbounded ()
  {
    return m_nMaxOccurs == UNBOUNDED;
  }

  /**
   * @return <code>true</code> if this element must be kept even if it is empty after the
   *         purification, because it is mandatory in the XML Schema of the surrounding element.
   */
  public final boolean isKeepWhenEmpty ()
  {
    return m_bKeepWhenEmpty;
  }

  @NonNull
  public final PurificationRuleNode setKeepWhenEmpty (final boolean bKeepWhenEmpty)
  {
    m_bKeepWhenEmpty = bKeepWhenEmpty;
    return this;
  }

  public final boolean isAttributeAllowed (@NonNull @Nonempty final String sAttrName)
  {
    return m_aAllowedAttributes.contains (sAttrName);
  }

  @NonNull
  public final PurificationRuleNode addAllowedAttributes (final String @Nullable [] aAttrNames)
  {
    if (aAttrNames != null)
      for (final String sAttrName : aAttrNames)
        if (StringHelper.isNotEmpty (sAttrName))
          m_aAllowedAttributes.add (sAttrName);
    return this;
  }

  @NonNull
  @ReturnsMutableCopy
  public final ICommonsOrderedSet <String> getAllAllowedAttributes ()
  {
    return m_aAllowedAttributes.getClone ();
  }

  public final boolean hasChildren ()
  {
    return m_aChildren.isNotEmpty ();
  }

  @NonNull
  @ReturnsMutableCopy
  public final ICommonsList <PurificationRuleNode> getAllChildren ()
  {
    return m_aChildren.getClone ();
  }

  /**
   * Find the existing child node with the provided element name and filter description, or create
   * a new one. This is only to be used while building a rule set.
   *
   * @param aElementName
   *        The XML element name to search. May not be <code>null</code>.
   * @param aFilter
   *        The filter to search. May be <code>null</code>.
   * @return Never <code>null</code>.
   */
  @NonNull
  public final PurificationRuleNode getOrCreateChild (@NonNull final QName aElementName,
                                                      @Nullable final IPurificationElementFilter aFilter)
  {
    final String sFilterDesc = aFilter == null ? null : aFilter.getDescription ();
    for (final PurificationRuleNode aChild : m_aChildren)
    {
      final String sChildFilterDesc = aChild.m_aFilter == null ? null : aChild.m_aFilter.getDescription ();
      if (aChild.m_aElementName.equals (aElementName) && EqualsHelper.equals (sChildFilterDesc, sFilterDesc))
        return aChild;
    }

    final PurificationRuleNode aNewChild = new PurificationRuleNode (aElementName, aFilter);
    m_aChildren.add (aNewChild);
    return aNewChild;
  }

  /**
   * Check if the provided element name matches the provided element.
   *
   * @param aName
   *        The element name to compare with. May not be <code>null</code>.
   * @param aElement
   *        The element to be compared. May not be <code>null</code>.
   * @return <code>true</code> if namespace URI and local name are identical.
   */
  public static boolean isSameName (@NonNull final QName aName, @NonNull final Element aElement)
  {
    return aName.getLocalPart ().equals (aElement.getLocalName ()) &&
           aName.getNamespaceURI ().equals (StringHelper.getNotNull (aElement.getNamespaceURI ()));
  }

  /**
   * Find the child node that matches the provided element. Nodes with a filter take precedence
   * over nodes without a filter, so that the more specific rule always wins.
   *
   * @param aElement
   *        The element to be matched. May not be <code>null</code>.
   * @return <code>null</code> if the element is not part of the EN 16931 core message.
   */
  @Nullable
  public final PurificationRuleNode findMatchingChild (@NonNull final Element aElement)
  {
    PurificationRuleNode aFallback = null;
    for (final PurificationRuleNode aChild : m_aChildren)
    {
      if (!isSameName (aChild.m_aElementName, aElement))
        continue;

      if (aChild.m_aFilter == null)
      {
        if (aFallback == null)
          aFallback = aChild;
      }
      else
        if (aChild.m_aFilter.matches (aElement))
          return aChild;
    }
    return aFallback;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ElementName", m_aElementName)
                                       .appendIfNotNull ("Filter", m_aFilter)
                                       .appendIfNotNull ("BusinessTermID", m_sBusinessTermID)
                                       .append ("MaxOccurs", m_nMaxOccurs)
                                       .append ("KeepWhenEmpty", m_bKeepWhenEmpty)
                                       .append ("AllowedAttributes", m_aAllowedAttributes)
                                       .append ("ChildCount", m_aChildren.size ())
                                       .getToString ();
  }
}
