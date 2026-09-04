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

import java.io.File;
import java.io.OutputStream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.ESuccess;
import com.helger.base.trait.IGenericImplTrait;
import com.helger.diagnostics.error.IError;
import com.helger.diagnostics.error.SingleError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.en16931.purifier.rule.IPurificationSettings;
import com.helger.en16931.purifier.rule.PurificationEngine;
import com.helger.en16931.purifier.rule.PurificationRuleSet;
import com.helger.jaxb.GenericJAXBMarshaller;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;
import com.helger.xml.serialize.read.DOMReader;

/**
 * Base class for all EN 16931 purifiers. A purifier removes everything from an EN 16931 invoice
 * that is not part of the core message of the configured EN 16931 version, and returns the result
 * as an XML Schema valid JAXB object.
 *
 * @author Philip Helger
 * @param <JAXBTYPE>
 *        The JAXB document type of the syntax to be purified
 * @param <IMPLTYPE>
 *        The implementation type
 */
public abstract class AbstractEN16931Purifier <JAXBTYPE, IMPLTYPE extends AbstractEN16931Purifier <JAXBTYPE, IMPLTYPE>>
                                              implements
                                              IPurificationSettings,
                                              IGenericImplTrait <IMPLTYPE>
{
  /** Remove non core attributes by default */
  public static final boolean DEFAULT_REMOVE_NON_CORE_ATTRIBUTES = true;
  /** Remove elements that are empty after the purification by default */
  public static final boolean DEFAULT_REMOVE_EMPTY_ELEMENTS = true;
  /** Enforce the EN 16931 cardinalities by default */
  public static final boolean DEFAULT_ENFORCE_CARDINALITIES = true;
  /** Remove XML comments and processing instructions by default */
  public static final boolean DEFAULT_REMOVE_COMMENTS = true;
  /** Write formatted XML by default */
  public static final boolean DEFAULT_FORMATTED_OUTPUT = true;

  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractEN16931Purifier.class);

  private final EEN16931Version m_eVersion;
  private final EEN16931SyntaxKind m_eSyntaxKind;
  private boolean m_bRemoveNonCoreAttributes = DEFAULT_REMOVE_NON_CORE_ATTRIBUTES;
  private boolean m_bRemoveEmptyElements = DEFAULT_REMOVE_EMPTY_ELEMENTS;
  private boolean m_bEnforceCardinalities = DEFAULT_ENFORCE_CARDINALITIES;
  private boolean m_bRemoveComments = DEFAULT_REMOVE_COMMENTS;
  private boolean m_bFormattedOutput = DEFAULT_FORMATTED_OUTPUT;

  /**
   * Constructor
   *
   * @param eVersion
   *        The EN 16931 version defining the core message. May not be <code>null</code>.
   * @param eSyntaxKind
   *        The syntax kind handled by this purifier. May not be <code>null</code>.
   */
  protected AbstractEN16931Purifier (@NonNull final EEN16931Version eVersion,
                                     @NonNull final EEN16931SyntaxKind eSyntaxKind)
  {
    ValueEnforcer.notNull (eVersion, "Version");
    ValueEnforcer.notNull (eSyntaxKind, "SyntaxKind");
    m_eVersion = eVersion;
    m_eSyntaxKind = eSyntaxKind;
  }

  @NonNull
  private static IError _buildError (@NonNull final String sErrorMsg)
  {
    return SingleError.builderError ().errorText (sErrorMsg).build ();
  }

  @NonNull
  private GenericJAXBMarshaller <JAXBTYPE> _createConfiguredMarshaller (@NonNull final ErrorList aErrorList)
  {
    final GenericJAXBMarshaller <JAXBTYPE> ret = createMarshaller ();
    ret.setValidationEventHandler (new WrappedCollectingValidationEventHandler (aErrorList));
    // Ensure the result is XML Schema valid
    ret.setUseSchema (true);
    ret.setFormattedOutput (m_bFormattedOutput);
    return ret;
  }

  /**
   * @return The marshaller of the concrete syntax version, used to read, validate and write the
   *         purified document. Never <code>null</code>.
   */
  @NonNull
  protected abstract GenericJAXBMarshaller <JAXBTYPE> createMarshaller ();

  /**
   * @return The EN 16931 version defining the core message. Never <code>null</code>.
   */
  @NonNull
  public final EEN16931Version getVersion ()
  {
    return m_eVersion;
  }

  /**
   * @return The syntax kind handled by this purifier. Never <code>null</code>.
   */
  @NonNull
  public final EEN16931SyntaxKind getSyntaxKind ()
  {
    return m_eSyntaxKind;
  }

  /**
   * @return The rule set used by this purifier or <code>null</code> if the configured EN 16931
   *         version has no rule set for the syntax kind of this purifier.
   */
  @Nullable
  public final PurificationRuleSet getRuleSet ()
  {
    return m_eVersion.getRuleSet (m_eSyntaxKind);
  }

  public final boolean isRemoveNonCoreAttributes ()
  {
    return m_bRemoveNonCoreAttributes;
  }

  @NonNull
  public final IMPLTYPE setRemoveNonCoreAttributes (final boolean bRemoveNonCoreAttributes)
  {
    m_bRemoveNonCoreAttributes = bRemoveNonCoreAttributes;
    return thisAsT ();
  }

  public final boolean isRemoveEmptyElements ()
  {
    return m_bRemoveEmptyElements;
  }

  @NonNull
  public final IMPLTYPE setRemoveEmptyElements (final boolean bRemoveEmptyElements)
  {
    m_bRemoveEmptyElements = bRemoveEmptyElements;
    return thisAsT ();
  }

  public final boolean isEnforceCardinalities ()
  {
    return m_bEnforceCardinalities;
  }

  @NonNull
  public final IMPLTYPE setEnforceCardinalities (final boolean bEnforceCardinalities)
  {
    m_bEnforceCardinalities = bEnforceCardinalities;
    return thisAsT ();
  }

  public final boolean isRemoveComments ()
  {
    return m_bRemoveComments;
  }

  @NonNull
  public final IMPLTYPE setRemoveComments (final boolean bRemoveComments)
  {
    m_bRemoveComments = bRemoveComments;
    return thisAsT ();
  }

  public final boolean isFormattedOutput ()
  {
    return m_bFormattedOutput;
  }

  @NonNull
  public final IMPLTYPE setFormattedOutput (final boolean bFormattedOutput)
  {
    m_bFormattedOutput = bFormattedOutput;
    return thisAsT ();
  }

  /**
   * Purify the provided XML document. The source document is not modified.
   *
   * @param aSrcDoc
   *        The XML document to be purified. May not be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. Every removal is added as an information entry. May not be
   *        <code>null</code>.
   * @return <code>null</code> in case of error. Otherwise a purified copy of the source document.
   */
  @Nullable
  public Document purifyToDocument (@NonNull final Document aSrcDoc, @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aSrcDoc, "SrcDoc");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    final PurificationRuleSet aRuleSet = getRuleSet ();
    if (aRuleSet == null)
    {
      aErrorList.add (_buildError ("No rule set is available for " +
                                   m_eVersion.getDisplayName () +
                                   " in the syntax '" +
                                   m_eSyntaxKind.getDisplayName () +
                                   "'"));
      return null;
    }

    final Document aDoc = (Document) aSrcDoc.cloneNode (true);
    if (PurificationEngine.purify (aDoc, aRuleSet, this, aErrorList).isFailure ())
      return null;

    return aDoc;
  }

  /**
   * Purify the provided XML document and convert it to the JAXB domain model. The source document
   * is not modified. Reading the purified document uses the XML Schema, so a non-<code>null</code>
   * result is guaranteed to be XML Schema valid.
   *
   * @param aSrcDoc
   *        The XML document to be purified. May not be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return <code>null</code> in case of error.
   */
  @Nullable
  public JAXBTYPE purify (@NonNull final Document aSrcDoc, @NonNull final ErrorList aErrorList)
  {
    final Document aPurifiedDoc = purifyToDocument (aSrcDoc, aErrorList);
    if (aPurifiedDoc == null)
      return null;

    return _createConfiguredMarshaller (aErrorList).read (aPurifiedDoc);
  }

  /**
   * Purify the XML document contained in the provided file.
   *
   * @param aSrcFile
   *        The file to be read and purified. May not be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return <code>null</code> in case of error.
   */
  @Nullable
  public JAXBTYPE purify (@NonNull final File aSrcFile, @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aSrcFile, "SrcFile");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    final Document aSrcDoc = DOMReader.readXMLDOM (aSrcFile);
    if (aSrcDoc == null)
    {
      aErrorList.add (_buildError ("Failed to read '" + aSrcFile.getAbsolutePath () + "' as XML document"));
      return null;
    }

    return purify (aSrcDoc, aErrorList);
  }

  /**
   * Purify an already parsed JAXB domain object. The source object is not modified.
   *
   * @param aSrcObject
   *        The object to be purified. May not be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return <code>null</code> in case of error.
   */
  @Nullable
  public JAXBTYPE purify (@NonNull final JAXBTYPE aSrcObject, @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aSrcObject, "SrcObject");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    final Document aSrcDoc = _createConfiguredMarshaller (aErrorList).getAsDocument (aSrcObject);
    if (aSrcDoc == null)
    {
      aErrorList.add (_buildError ("Failed to convert the provided object to an XML document"));
      return null;
    }

    return purify (aSrcDoc, aErrorList);
  }

  /**
   * Write a purified object to the provided file.
   *
   * @param aObject
   *        The object to be written. May not be <code>null</code>.
   * @param aDestFile
   *        The destination file. May not be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return {@link ESuccess#SUCCESS} if writing was successful.
   */
  @NonNull
  public ESuccess write (@NonNull final JAXBTYPE aObject,
                         @NonNull final File aDestFile,
                         @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aObject, "Object");
    ValueEnforcer.notNull (aDestFile, "DestFile");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    return _createConfiguredMarshaller (aErrorList).write (aObject, aDestFile);
  }

  /**
   * Write a purified object to the provided output stream.
   *
   * @param aObject
   *        The object to be written. May not be <code>null</code>.
   * @param aOS
   *        The destination output stream. It is closed by this method. May not be
   *        <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return {@link ESuccess#SUCCESS} if writing was successful.
   */
  @NonNull
  public ESuccess write (@NonNull final JAXBTYPE aObject,
                         @NonNull final OutputStream aOS,
                         @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aObject, "Object");
    ValueEnforcer.notNull (aOS, "OutputStream");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    return _createConfiguredMarshaller (aErrorList).write (aObject, aOS);
  }

  /**
   * Purify the provided XML document and write the result to the destination file.
   *
   * @param aSrcDoc
   *        The XML document to be purified. It is not modified. May not be <code>null</code>.
   * @param aDestFile
   *        The destination file. May not be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return {@link ESuccess#SUCCESS} only if purifying and writing were both successful.
   */
  @NonNull
  public ESuccess purify (@NonNull final Document aSrcDoc,
                          @NonNull final File aDestFile,
                          @NonNull final ErrorList aErrorList)
  {
    final JAXBTYPE aPurified = purify (aSrcDoc, aErrorList);
    if (aPurified == null)
      return ESuccess.FAILURE;

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Writing the purified document to '" + aDestFile.getAbsolutePath () + "'");

    return write (aPurified, aDestFile, aErrorList);
  }

  /**
   * Read the source file, purify it and write the result to the destination file.
   *
   * @param aSrcFile
   *        The file to be read and purified. May not be <code>null</code>.
   * @param aDestFile
   *        The destination file. May not be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return {@link ESuccess#SUCCESS} only if reading, purifying and writing were all successful.
   */
  @NonNull
  public ESuccess purify (@NonNull final File aSrcFile,
                          @NonNull final File aDestFile,
                          @NonNull final ErrorList aErrorList)
  {
    final JAXBTYPE aPurified = purify (aSrcFile, aErrorList);
    if (aPurified == null)
      return ESuccess.FAILURE;

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Writing the purified document to '" + aDestFile.getAbsolutePath () + "'");

    return write (aPurified, aDestFile, aErrorList);
  }
}
