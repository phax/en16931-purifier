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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.numeric.mutable.MutableInt;
import com.helger.base.state.ESuccess;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsMap;
import com.helger.diagnostics.error.IError;
import com.helger.diagnostics.error.SingleError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.xml.NodeListIterator;
import com.helger.xml.XMLHelper;

/**
 * The purification engine. It walks a DOM document in parallel to the whitelist tree of a
 * {@link PurificationRuleSet} and removes everything that is not covered by the rule set.
 *
 * @author Philip Helger
 */
@Immutable
public final class PurificationEngine
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PurificationEngine.class);

  private PurificationEngine ()
  {}

  @NonNull
  private static IError _buildInfo (@NonNull @Nonempty final String sPath, @NonNull final String sMsg)
  {
    return SingleError.builderInfo ().errorFieldName (sPath).errorText (sMsg).build ();
  }

  @NonNull
  private static IError _buildError (@Nullable final String sPath, @NonNull final String sMsg)
  {
    return SingleError.builderError ().errorFieldName (sPath).errorText (sMsg).build ();
  }

  @NonNull
  @Nonempty
  private static String _getBusinessTermSuffix (@NonNull final PurificationRuleNode aNode)
  {
    final String sBT = aNode.getBusinessTermID ();
    return StringHelper.isEmpty (sBT) ? "" : " (" + sBT + ")";
  }

  private static boolean _isNamespaceDeclaration (@NonNull final Attr aAttr)
  {
    return XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals (aAttr.getNamespaceURI ()) ||
           XMLConstants.XMLNS_ATTRIBUTE.equals (aAttr.getName ());
  }

  @NonNull
  @Nonempty
  private static String _getAttributeName (@NonNull final Attr aAttr)
  {
    final String sLocalName = aAttr.getLocalName ();
    return sLocalName != null ? sLocalName : aAttr.getName ();
  }

  private static void _purifyAttributes (@NonNull final Element aElement,
                                         @NonNull final PurificationRuleNode aNode,
                                         @NonNull @Nonempty final String sPath,
                                         @NonNull final IPurificationSettings aSettings,
                                         @NonNull final ErrorList aErrorList)
  {
    if (!aSettings.isRemoveNonCoreAttributes ())
      return;

    for (final Attr aAttr : XMLHelper.getAllAttributesAsList (aElement))
    {
      if (_isNamespaceDeclaration (aAttr))
        continue;
      if (aNode.isAttributeAllowed (_getAttributeName (aAttr)))
        continue;

      aElement.removeAttributeNode (aAttr);
      aErrorList.add (_buildInfo (sPath + "/@" + aAttr.getName (),
                                  "Removed the attribute because it is not part of the EN 16931 core message"));
    }
  }

  private static boolean _isEmptyElement (@NonNull final Element aElement)
  {
    if (XMLHelper.hasChildElementNodes (aElement))
      return false;

    for (final Attr aAttr : XMLHelper.getAllAttributesAsList (aElement))
      if (!_isNamespaceDeclaration (aAttr))
        return false;

    return StringHelper.isEmpty (StringHelper.trim (aElement.getTextContent ()));
  }

  /**
   * Check if an element that became empty by the purification may be removed. Only elements that
   * are aggregates in the rule set are candidates - elements that carry a value are always kept,
   * even if the value is empty. Elements that are mandatory in the XML Schema are never removed,
   * because that would create XSD invalid output.
   */
  private static boolean _isRemovableEmptyElement (@NonNull final Element aElement,
                                                   @NonNull final PurificationRuleNode aNode,
                                                   @NonNull final IPurificationSettings aSettings)
  {
    return aSettings.isRemoveEmptyElements () &&
           aNode.hasChildren () &&
           !aNode.isKeepWhenEmpty () &&
           _isEmptyElement (aElement);
  }

  private static void _purifyChildrenRecursive (@NonNull final Element aParent,
                                                @NonNull final PurificationRuleNode aParentNode,
                                                @NonNull @Nonempty final String sParentPath,
                                                @NonNull final IPurificationSettings aSettings,
                                                @NonNull final ErrorList aErrorList)
  {
    // Take a snapshot, because the child list is modified while iterating
    final ICommonsList <Node> aAllChildren = new CommonsArrayList <> (NodeListIterator.createChildNodeIterator (aParent));
    final ICommonsMap <PurificationRuleNode, MutableInt> aOccurrences = new CommonsHashMap <> ();

    for (final Node aChild : aAllChildren)
    {
      final short nNodeType = aChild.getNodeType ();
      if (nNodeType == Node.COMMENT_NODE || nNodeType == Node.PROCESSING_INSTRUCTION_NODE)
      {
        if (aSettings.isRemoveComments ())
          aParent.removeChild (aChild);
        continue;
      }
      if (nNodeType != Node.ELEMENT_NODE)
        continue;

      final Element aChildElement = (Element) aChild;
      final String sChildPath = sParentPath + '/' + aChildElement.getNodeName ();

      final PurificationRuleNode aChildNode = aParentNode.findMatchingChild (aChildElement);
      if (aChildNode == null)
      {
        aParent.removeChild (aChild);
        aErrorList.add (_buildInfo (sChildPath,
                                    "Removed the element because it is not part of the EN 16931 core message"));
        continue;
      }

      if (aSettings.isEnforceCardinalities () && !aChildNode.isUnbounded ())
      {
        final int nMaxOccurs = aChildNode.getMaxOccurs ();
        if (aOccurrences.computeIfAbsent (aChildNode, k -> new MutableInt (0)).inc () > nMaxOccurs)
        {
          aParent.removeChild (aChild);
          aErrorList.add (_buildInfo (sChildPath,
                                      "Removed the element because EN 16931 allows at most " +
                                                  nMaxOccurs +
                                                  " occurrence(s)" +
                                                  _getBusinessTermSuffix (aChildNode)));
          continue;
        }
      }

      _purifyAttributes (aChildElement, aChildNode, sChildPath, aSettings, aErrorList);
      _purifyChildrenRecursive (aChildElement, aChildNode, sChildPath, aSettings, aErrorList);

      if (_isRemovableEmptyElement (aChildElement, aChildNode, aSettings))
      {
        aParent.removeChild (aChild);
        // Don't count an element that is removed again
        final MutableInt aOccurrence = aOccurrences.get (aChildNode);
        if (aOccurrence != null)
          aOccurrence.inc (-1);
        aErrorList.add (_buildInfo (sChildPath, "Removed the element because it is empty after the purification"));
      }
    }
  }

  /**
   * Purify the provided document in place.
   *
   * @param aDoc
   *        The document to be purified. It is modified by this method. May not be
   *        <code>null</code>.
   * @param aRuleSet
   *        The rule set defining the EN 16931 core message. May not be <code>null</code>.
   * @param aSettings
   *        The settings to be used. May not be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. Every removal is added as an information entry. May not be
   *        <code>null</code>.
   * @return {@link ESuccess#SUCCESS} if the document could be purified, {@link ESuccess#FAILURE}
   *         if the document element does not match the rule set.
   */
  @NonNull
  public static ESuccess purify (@NonNull final Document aDoc,
                                 @NonNull final PurificationRuleSet aRuleSet,
                                 @NonNull final IPurificationSettings aSettings,
                                 @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aDoc, "Doc");
    ValueEnforcer.notNull (aRuleSet, "RuleSet");
    ValueEnforcer.notNull (aSettings, "Settings");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    final Element aRoot = aDoc.getDocumentElement ();
    if (aRoot == null)
    {
      aErrorList.add (_buildError (null, "The provided XML document contains no document element"));
      return ESuccess.FAILURE;
    }

    final QName aExpectedRootName = aRuleSet.getRootElementName ();
    if (!PurificationRuleNode.isSameName (aExpectedRootName, aRoot))
    {
      aErrorList.add (_buildError ("/" + aRoot.getNodeName (),
                                   "The document element does not match the expected element " +
                                                               aExpectedRootName.toString () +
                                                               " of rule set '" +
                                                               aRuleSet.getID () +
                                                               "'"));
      return ESuccess.FAILURE;
    }

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Purifying the XML document using the rule set '" + aRuleSet.getID () + "'");

    final String sRootPath = "/" + aRoot.getNodeName ();
    final PurificationRuleNode aRootNode = aRuleSet.getRootNode ();
    _purifyAttributes (aRoot, aRootNode, sRootPath, aSettings, aErrorList);
    _purifyChildrenRecursive (aRoot, aRootNode, sRootPath, aSettings, aErrorList);

    return ESuccess.SUCCESS;
  }
}
