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

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.id.IHasID;
import com.helger.base.tostring.ToStringGenerator;

/**
 * The whitelist of all XML elements and attributes that belong to the EN 16931 core message of one
 * specific document type. Everything that is not covered by a rule set is considered to be an
 * extension and is removed by the purification.
 *
 * @author Philip Helger
 */
@Immutable
public class PurificationRuleSet implements IHasID <String>
{
  private final String m_sID;
  private final QName m_aRootElementName;
  private final PurificationRuleNode m_aRootNode;

  /**
   * Constructor
   *
   * @param sID
   *        The ID of this rule set, used in log messages. May neither be <code>null</code> nor
   *        empty.
   * @param aRootElementName
   *        The expected name of the document element. May not be <code>null</code>.
   * @param aRootNode
   *        The root node of the whitelist tree. May not be <code>null</code>.
   */
  public PurificationRuleSet (@NonNull @Nonempty final String sID,
                              @NonNull final QName aRootElementName,
                              @NonNull final PurificationRuleNode aRootNode)
  {
    ValueEnforcer.notEmpty (sID, "ID");
    ValueEnforcer.notNull (aRootElementName, "RootElementName");
    ValueEnforcer.notNull (aRootNode, "RootNode");
    m_sID = sID;
    m_aRootElementName = aRootElementName;
    m_aRootNode = aRootNode;
  }

  @NonNull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  /**
   * @return The expected name of the document element. Never <code>null</code>.
   */
  @NonNull
  public final QName getRootElementName ()
  {
    return m_aRootElementName;
  }

  /**
   * @return The root node of the whitelist tree, representing the document element itself. Never
   *         <code>null</code>.
   */
  @NonNull
  public final PurificationRuleNode getRootNode ()
  {
    return m_aRootNode;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ID", m_sID)
                                       .append ("RootElementName", m_aRootElementName)
                                       .append ("RootNode", m_aRootNode)
                                       .getToString ();
  }
}
