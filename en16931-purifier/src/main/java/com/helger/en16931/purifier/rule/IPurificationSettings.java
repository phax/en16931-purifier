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

/**
 * Read-only settings that control how aggressive the {@link PurificationEngine} works.
 *
 * @author Philip Helger
 */
public interface IPurificationSettings
{
  /**
   * @return <code>true</code> if attributes that are not part of the EN 16931 core message should
   *         be removed, <code>false</code> if all attributes of retained elements should be kept.
   */
  boolean isRemoveNonCoreAttributes ();

  /**
   * @return <code>true</code> if elements that have no child element, no text content and no
   *         attribute left after the purification should be removed as well.
   */
  boolean isRemoveEmptyElements ();

  /**
   * @return <code>true</code> if occurrences beyond the cardinality allowed by EN 16931 should be
   *         removed, <code>false</code> if only unknown elements should be removed.
   */
  boolean isEnforceCardinalities ();

  /**
   * @return <code>true</code> if XML comments and processing instructions should be removed.
   */
  boolean isRemoveComments ();
}
