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

import java.util.Map;

import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

/**
 * Parser for the compact path syntax used to author {@link PurificationRuleSet} instances. A path
 * is a sequence of steps relative to the document element, each step optionally carrying a
 * predicate:
 *
 * <pre>
 * /cac:AccountingSupplierParty/cac:Party/cbc:EndpointID
 * /cac:AdditionalDocumentReference[cbc:DocumentTypeCode='130']/cbc:ID
 * /cac:AdditionalDocumentReference[not(cbc:DocumentTypeCode)]/cbc:ID
 * /cac:AccountingSupplierParty/cac:Party/cac:PartyTaxScheme[cac:TaxScheme/cbc:ID='VAT']
 * </pre>
 *
 * The supported predicates are <code>relative/path='value'</code>,
 * <code>not(relative/path)</code> and <code>relative/path</code> for elements, and the same three
 * forms with a trailing <code>/@attribute</code> respectively a leading <code>@attribute</code>
 * for attributes.
 *
 * @author Philip Helger
 */
@Immutable
public final class PurificationPathParser
{
  /**
   * A single parsed path step, consisting of the XML element name and an optional filter derived
   * from the predicate.
   *
   * @author Philip Helger
   */
  @Immutable
  public static final class PathStep
  {
    private final QName m_aElementName;
    private final IPurificationElementFilter m_aFilter;

    public PathStep (@NonNull final QName aElementName, @Nullable final IPurificationElementFilter aFilter)
    {
      ValueEnforcer.notNull (aElementName, "ElementName");
      m_aElementName = aElementName;
      m_aFilter = aFilter;
    }

    @NonNull
    public QName getElementName ()
    {
      return m_aElementName;
    }

    @Nullable
    public IPurificationElementFilter getFilter ()
    {
      return m_aFilter;
    }

    @Override
    public String toString ()
    {
      return new ToStringGenerator (this).append ("ElementName", m_aElementName)
                                         .appendIfNotNull ("Filter", m_aFilter)
                                         .getToString ();
    }
  }

  private PurificationPathParser ()
  {}

  @NonNull
  private static QName _parseQName (@NonNull @Nonempty final String sQName,
                                    @NonNull final Map <String, String> aPrefixMap)
  {
    final int nIndex = sQName.indexOf (':');
    if (nIndex < 0)
      throw new IllegalArgumentException ("The element name '" + sQName + "' is missing a namespace prefix");

    final String sPrefix = sQName.substring (0, nIndex);
    final String sLocalName = sQName.substring (nIndex + 1);
    if (StringHelper.isEmpty (sLocalName))
      throw new IllegalArgumentException ("The element name '" + sQName + "' has no local name");

    final String sNamespaceURI = aPrefixMap.get (sPrefix);
    if (sNamespaceURI == null)
      throw new IllegalArgumentException ("The namespace prefix '" + sPrefix + "' of '" + sQName + "' is unknown");

    return new QName (sNamespaceURI, sLocalName, sPrefix);
  }

  @NonNull
  @Nonempty
  private static ICommonsList <QName> _parseRelativePath (@NonNull @Nonempty final String sRelativePath,
                                                          @NonNull final Map <String, String> aPrefixMap)
  {
    final ICommonsList <QName> ret = new CommonsArrayList <> ();
    for (final String sPart : StringHelper.getExploded ('/', sRelativePath))
    {
      if (StringHelper.isEmpty (sPart))
        throw new IllegalArgumentException ("The relative path '" + sRelativePath + "' contains an empty step");
      ret.add (_parseQName (sPart, aPrefixMap));
    }
    return ret;
  }

  @NonNull
  private static String _unquote (@NonNull @Nonempty final String sPredicate, @NonNull final String sValue)
  {
    if (sValue.length () < 2 || sValue.charAt (0) != '\'' || sValue.charAt (sValue.length () - 1) != '\'')
      throw new IllegalArgumentException ("The predicate value of '" + sPredicate + "' is not single quoted");
    return sValue.substring (1, sValue.length () - 1);
  }

  @NonNull
  private static IPurificationElementFilter _parsePredicate (@NonNull @Nonempty final String sPredicate,
                                                             @NonNull final Map <String, String> aPrefixMap)
  {
    String sRest = sPredicate;

    final boolean bNegated = sRest.startsWith ("not(") && sRest.endsWith (")");
    if (bNegated)
      sRest = sRest.substring (4, sRest.length () - 1).trim ();

    String sValue = null;
    final int nEqualsIndex = sRest.indexOf ('=');
    if (nEqualsIndex >= 0)
    {
      if (bNegated)
        throw new IllegalArgumentException ("The predicate '" + sPredicate + "' must not negate a value comparison");
      sValue = _unquote (sPredicate, sRest.substring (nEqualsIndex + 1).trim ());
      sRest = sRest.substring (0, nEqualsIndex).trim ();
    }

    String sAttrName = null;
    final int nAtIndex = sRest.indexOf ('@');
    if (nAtIndex >= 0)
    {
      if (nAtIndex > 0 && sRest.charAt (nAtIndex - 1) != '/')
        throw new IllegalArgumentException ("The attribute of the predicate '" + sPredicate + "' is malformed");
      sAttrName = sRest.substring (nAtIndex + 1);
      if (StringHelper.isEmpty (sAttrName))
        throw new IllegalArgumentException ("The predicate '" + sPredicate + "' has an empty attribute name");
      sRest = nAtIndex == 0 ? "" : sRest.substring (0, nAtIndex - 1);
    }

    if (sAttrName != null)
    {
      final ICommonsList <QName> aPath = StringHelper.isEmpty (sRest) ? new CommonsArrayList <> ()
                                                                      : _parseRelativePath (sRest, aPrefixMap);
      if (sValue != null)
        return PurificationElementFilters.attributeValue (aPath, sAttrName, sValue);
      return bNegated ? PurificationElementFilters.attributeAbsent (aPath, sAttrName)
                      : PurificationElementFilters.attributePresent (aPath, sAttrName);
    }

    final ICommonsList <QName> aPath = _parseRelativePath (sRest, aPrefixMap);
    if (sValue != null)
      return PurificationElementFilters.childValue (aPath, sValue);
    return bNegated ? PurificationElementFilters.childAbsent (aPath) : PurificationElementFilters.childPresent (aPath);
  }

  @NonNull
  private static PathStep _parseStep (@NonNull @Nonempty final String sStep,
                                      @NonNull final Map <String, String> aPrefixMap)
  {
    final int nIndex = sStep.indexOf ('[');
    if (nIndex < 0)
      return new PathStep (_parseQName (sStep, aPrefixMap), null);

    if (!sStep.endsWith ("]"))
      throw new IllegalArgumentException ("The path step '" + sStep + "' has an unterminated predicate");

    final String sElementName = sStep.substring (0, nIndex);
    final String sPredicate = sStep.substring (nIndex + 1, sStep.length () - 1).trim ();
    if (StringHelper.isEmpty (sPredicate))
      throw new IllegalArgumentException ("The path step '" + sStep + "' has an empty predicate");

    return new PathStep (_parseQName (sElementName, aPrefixMap), _parsePredicate (sPredicate, aPrefixMap));
  }

  /**
   * Parse a path into its single steps.
   *
   * @param sPath
   *        The path to be parsed, relative to the document element and starting with a
   *        <code>/</code>. May neither be <code>null</code> nor empty.
   * @param aPrefixMap
   *        The namespace prefix to namespace URI mapping to be used. May not be <code>null</code>.
   * @return The list of parsed steps. Neither <code>null</code> nor empty.
   * @throws IllegalArgumentException
   *         If the path is syntactically invalid or uses an unknown namespace prefix.
   */
  @NonNull
  @Nonempty
  public static ICommonsList <PathStep> parsePath (@NonNull @Nonempty final String sPath,
                                                   @NonNull final Map <String, String> aPrefixMap)
  {
    ValueEnforcer.notEmpty (sPath, "Path");
    ValueEnforcer.notNull (aPrefixMap, "PrefixMap");
    if (sPath.charAt (0) != '/')
      throw new IllegalArgumentException ("The path '" + sPath + "' must start with a '/'");

    final ICommonsList <PathStep> ret = new CommonsArrayList <> ();
    final StringBuilder aSB = new StringBuilder ();
    int nPredicateDepth = 0;
    // Skip the leading separator
    for (int i = 1; i < sPath.length (); ++i)
    {
      final char c = sPath.charAt (i);
      if (c == '[')
        nPredicateDepth++;
      else
        if (c == ']')
          nPredicateDepth--;

      if (c == '/' && nPredicateDepth == 0)
      {
        if (aSB.length () == 0)
          throw new IllegalArgumentException ("The path '" + sPath + "' contains an empty step");
        ret.add (_parseStep (aSB.toString (), aPrefixMap));
        aSB.setLength (0);
      }
      else
        aSB.append (c);
    }

    if (nPredicateDepth != 0)
      throw new IllegalArgumentException ("The path '" + sPath + "' has an unbalanced predicate");
    if (aSB.length () == 0)
      throw new IllegalArgumentException ("The path '" + sPath + "' contains an empty step");
    ret.add (_parseStep (aSB.toString (), aPrefixMap));

    return ret;
  }
}
