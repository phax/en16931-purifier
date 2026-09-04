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

import java.util.List;

import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.xml.XMLHelper;

/**
 * Factory for the {@link IPurificationElementFilter} implementations needed by the EN 16931 syntax
 * bindings.
 *
 * @author Philip Helger
 */
@Immutable
public final class PurificationElementFilters
{
  private PurificationElementFilters ()
  {}

  @NonNull
  private static String _getPathAsString (@NonNull final List <QName> aRelativePath)
  {
    return StringImplode.getImplodedMapped ('/', aRelativePath, QName::getLocalPart);
  }

  @Nullable
  private static Element _findRelativeElement (@NonNull final Element aStartElement,
                                               @NonNull final List <QName> aRelativePath)
  {
    Element aCur = aStartElement;
    for (final QName aName : aRelativePath)
    {
      aCur = XMLHelper.getFirstChildElementOfName (aCur, aName.getNamespaceURI (), aName.getLocalPart ());
      if (aCur == null)
        return null;
    }
    return aCur;
  }

  @NonNull
  private static String _getAttributePathAsString (@NonNull final List <QName> aRelativePath,
                                                   @NonNull @Nonempty final String sAttrName)
  {
    final String sPath = _getPathAsString (aRelativePath);
    return StringHelper.isEmpty (sPath) ? "@" + sAttrName : sPath + "/@" + sAttrName;
  }

  @Nullable
  private static String _findAttributeValue (@NonNull final Element aStartElement,
                                             @NonNull final List <QName> aRelativePath,
                                             @NonNull @Nonempty final String sAttrName)
  {
    final Element aElement = _findRelativeElement (aStartElement, aRelativePath);
    if (aElement == null || !aElement.hasAttribute (sAttrName))
      return null;
    return StringHelper.trim (aElement.getAttribute (sAttrName));
  }

  /**
   * Create a filter that matches if a relative child element exists and has exactly the provided
   * text content.
   *
   * @param aRelativePath
   *        The relative element path to be resolved. May neither be <code>null</code> nor empty.
   * @param sExpectedValue
   *        The expected text content. May neither be <code>null</code> nor empty.
   * @return Never <code>null</code>.
   */
  @NonNull
  public static IPurificationElementFilter childValue (@NonNull @Nonempty final List <QName> aRelativePath,
                                                       @NonNull @Nonempty final String sExpectedValue)
  {
    ValueEnforcer.notEmpty (aRelativePath, "RelativePath");
    ValueEnforcer.notEmpty (sExpectedValue, "ExpectedValue");

    final ICommonsList <QName> aPath = new CommonsArrayList <> (aRelativePath);
    final String sDescription = _getPathAsString (aPath) + "='" + sExpectedValue + "'";
    return new IPurificationElementFilter ()
    {
      public boolean matches (@NonNull final Element aElement)
      {
        final Element aChild = _findRelativeElement (aElement, aPath);
        return aChild != null && sExpectedValue.equals (StringHelper.trim (aChild.getTextContent ()));
      }

      @NonNull
      @Nonempty
      public String getDescription ()
      {
        return sDescription;
      }
    };
  }

  /**
   * Create a filter that matches if a relative child element exists, independent of its value.
   *
   * @param aRelativePath
   *        The relative element path to be resolved. May neither be <code>null</code> nor empty.
   * @return Never <code>null</code>.
   */
  @NonNull
  public static IPurificationElementFilter childPresent (@NonNull @Nonempty final List <QName> aRelativePath)
  {
    ValueEnforcer.notEmpty (aRelativePath, "RelativePath");

    final ICommonsList <QName> aPath = new CommonsArrayList <> (aRelativePath);
    final String sDescription = _getPathAsString (aPath);
    return new IPurificationElementFilter ()
    {
      public boolean matches (@NonNull final Element aElement)
      {
        return _findRelativeElement (aElement, aPath) != null;
      }

      @NonNull
      @Nonempty
      public String getDescription ()
      {
        return sDescription;
      }
    };
  }

  /**
   * Create a filter that matches if a relative child element does <b>not</b> exist.
   *
   * @param aRelativePath
   *        The relative element path to be resolved. May neither be <code>null</code> nor empty.
   * @return Never <code>null</code>.
   */
  @NonNull
  public static IPurificationElementFilter childAbsent (@NonNull @Nonempty final List <QName> aRelativePath)
  {
    ValueEnforcer.notEmpty (aRelativePath, "RelativePath");

    final ICommonsList <QName> aPath = new CommonsArrayList <> (aRelativePath);
    final String sDescription = "not(" + _getPathAsString (aPath) + ")";
    return new IPurificationElementFilter ()
    {
      public boolean matches (@NonNull final Element aElement)
      {
        return _findRelativeElement (aElement, aPath) == null;
      }

      @NonNull
      @Nonempty
      public String getDescription ()
      {
        return sDescription;
      }
    };
  }

  /**
   * Create a filter that matches if an attribute of a relative child element exists and has
   * exactly the provided value.
   *
   * @param aRelativePath
   *        The relative element path to be resolved. May be empty to address the element itself.
   *        May not be <code>null</code>.
   * @param sAttrName
   *        The local name of the attribute. May neither be <code>null</code> nor empty.
   * @param sExpectedValue
   *        The expected attribute value. May neither be <code>null</code> nor empty.
   * @return Never <code>null</code>.
   */
  @NonNull
  public static IPurificationElementFilter attributeValue (@NonNull final List <QName> aRelativePath,
                                                           @NonNull @Nonempty final String sAttrName,
                                                           @NonNull @Nonempty final String sExpectedValue)
  {
    ValueEnforcer.notNull (aRelativePath, "RelativePath");
    ValueEnforcer.notEmpty (sAttrName, "AttrName");
    ValueEnforcer.notEmpty (sExpectedValue, "ExpectedValue");

    final ICommonsList <QName> aPath = new CommonsArrayList <> (aRelativePath);
    final String sDescription = _getAttributePathAsString (aPath, sAttrName) + "='" + sExpectedValue + "'";
    return new IPurificationElementFilter ()
    {
      public boolean matches (@NonNull final Element aElement)
      {
        return sExpectedValue.equals (_findAttributeValue (aElement, aPath, sAttrName));
      }

      @NonNull
      @Nonempty
      public String getDescription ()
      {
        return sDescription;
      }
    };
  }

  /**
   * Create a filter that matches if an attribute of a relative child element exists, independent
   * of its value.
   *
   * @param aRelativePath
   *        The relative element path to be resolved. May be empty to address the element itself.
   *        May not be <code>null</code>.
   * @param sAttrName
   *        The local name of the attribute. May neither be <code>null</code> nor empty.
   * @return Never <code>null</code>.
   */
  @NonNull
  public static IPurificationElementFilter attributePresent (@NonNull final List <QName> aRelativePath,
                                                             @NonNull @Nonempty final String sAttrName)
  {
    ValueEnforcer.notNull (aRelativePath, "RelativePath");
    ValueEnforcer.notEmpty (sAttrName, "AttrName");

    final ICommonsList <QName> aPath = new CommonsArrayList <> (aRelativePath);
    final String sDescription = _getAttributePathAsString (aPath, sAttrName);
    return new IPurificationElementFilter ()
    {
      public boolean matches (@NonNull final Element aElement)
      {
        return _findAttributeValue (aElement, aPath, sAttrName) != null;
      }

      @NonNull
      @Nonempty
      public String getDescription ()
      {
        return sDescription;
      }
    };
  }

  /**
   * Create a filter that matches if an attribute of a relative child element does <b>not</b>
   * exist.
   *
   * @param aRelativePath
   *        The relative element path to be resolved. May be empty to address the element itself.
   *        May not be <code>null</code>.
   * @param sAttrName
   *        The local name of the attribute. May neither be <code>null</code> nor empty.
   * @return Never <code>null</code>.
   */
  @NonNull
  public static IPurificationElementFilter attributeAbsent (@NonNull final List <QName> aRelativePath,
                                                            @NonNull @Nonempty final String sAttrName)
  {
    ValueEnforcer.notNull (aRelativePath, "RelativePath");
    ValueEnforcer.notEmpty (sAttrName, "AttrName");

    final ICommonsList <QName> aPath = new CommonsArrayList <> (aRelativePath);
    final String sDescription = "not(" + _getAttributePathAsString (aPath, sAttrName) + ")";
    return new IPurificationElementFilter ()
    {
      public boolean matches (@NonNull final Element aElement)
      {
        return _findAttributeValue (aElement, aPath, sAttrName) == null;
      }

      @NonNull
      @Nonempty
      public String getDescription ()
      {
        return sDescription;
      }
    };
  }
}
