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

import org.jspecify.annotations.NonNull;
import org.w3c.dom.Element;

import com.helger.annotation.Nonempty;

/**
 * Filter to discriminate multiple rules that use the same XML element name but differ in the
 * business terms they carry. A typical example is
 * <code>cac:AdditionalDocumentReference</code> that is BT-18 if it contains a
 * <code>cbc:DocumentTypeCode</code> with value <code>130</code> and BG-24 if it does not.
 *
 * @author Philip Helger
 */
public interface IPurificationElementFilter
{
  /**
   * Check if the provided element matches this filter or not.
   *
   * @param aElement
   *        The element to be checked. May not be <code>null</code>.
   * @return <code>true</code> if the element matches, <code>false</code> if not.
   */
  boolean matches (@NonNull Element aElement);

  /**
   * @return A short human readable description of this filter, used in error messages. Neither
   *         <code>null</code> nor empty.
   */
  @NonNull
  @Nonempty
  String getDescription ();
}
