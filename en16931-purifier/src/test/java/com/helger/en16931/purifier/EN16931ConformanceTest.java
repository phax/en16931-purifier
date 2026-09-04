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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.jspecify.annotations.NonNull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.state.ESuccess;
import com.helger.collection.commons.CommonsTreeSet;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsSortedSet;
import com.helger.diagnostics.error.IError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.diver.api.coord.DVRCoordinate;
import com.helger.io.file.FilenameHelper;
import com.helger.io.resource.FileSystemResource;
import com.helger.phive.api.execute.ValidationExecutionManager;
import com.helger.phive.api.executorset.ValidationExecutorSetRegistry;
import com.helger.phive.api.result.ValidationResult;
import com.helger.phive.api.result.ValidationResultList;
import com.helger.phive.api.validity.IValidityDeterminator;
import com.helger.phive.en16931.EN16931Validation;
import com.helger.phive.xml.source.IValidationSourceXML;
import com.helger.phive.xml.source.ValidationSourceXML;

/**
 * Check that the purification never breaks the conformance to the EN 16931 validation rules: every
 * rule that fails for the purified document must already have failed for the source document.
 *
 * @author Philip Helger
 */
public final class EN16931ConformanceTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (EN16931ConformanceTest.class);
  private static final File TARGET_DIR = new File ("target/test-output/purified");

  private static final ValidationExecutorSetRegistry <IValidationSourceXML> VES_REGISTRY = new ValidationExecutorSetRegistry <> ();
  static
  {
    EN16931Validation.initEN16931 (VES_REGISTRY);
  }

  @NonNull
  private static ICommonsSortedSet <String> _getAllFailedRuleIDs (@NonNull final DVRCoordinate aVID,
                                                                  @NonNull final File aFile)
  {
    final ValidationResultList aResultList = ValidationExecutionManager.executeValidation (IValidityDeterminator.createDefault (),
                                                                                           VES_REGISTRY.getOfID (aVID),
                                                                                           ValidationSourceXML.create (new FileSystemResource (aFile)));
    assertNotNull (aResultList);

    final ICommonsSortedSet <String> ret = new CommonsTreeSet <> ();
    for (final ValidationResult aResult : aResultList)
      for (final IError aError : aResult.getErrorList ())
        ret.add (aError.getErrorID () != null ? aError.getErrorID () : aError.getErrorFieldName ());
    return ret;
  }

  private static <T> void _checkConformance (@NonNull final AbstractEN16931Purifier <T, ?> aPurifier,
                                             @NonNull final DVRCoordinate aVID,
                                             @NonNull final ICommonsList <File> aSrcFiles)
  {
    for (final File aSrcFile : aSrcFiles)
    {
      final File aDestFile = new File (TARGET_DIR, FilenameHelper.getBaseName (aSrcFile) + "-purified.xml");

      final ErrorList aErrorList = new ErrorList ();
      final ESuccess eSuccess = aPurifier.purify (aSrcFile, aDestFile, aErrorList);
      assertTrue ("Failed to purify '" + aSrcFile.getPath () + "': " + aErrorList.getAllErrors (),
                  eSuccess.isSuccess ());

      final ICommonsSortedSet <String> aSrcFailures = _getAllFailedRuleIDs (aVID, aSrcFile);
      final ICommonsSortedSet <String> aDestFailures = _getAllFailedRuleIDs (aVID, aDestFile);

      final ICommonsSortedSet <String> aNewFailures = aDestFailures.getClone ();
      aNewFailures.removeAll (aSrcFailures);
      assertTrue ("Purifying '" + aSrcFile.getPath () + "' introduced new EN 16931 rule failures: " + aNewFailures,
                  aNewFailures.isEmpty ());

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Purified '" +
                      aSrcFile.getPath () +
                      "' - source failures: " +
                      aSrcFailures.size () +
                      ", purified failures: " +
                      aDestFailures.size ());
    }
  }

  @Test
  public void testUBLInvoiceConformance ()
  {
    _checkConformance (new UBL21InvoicePurifier (),
                       EN16931Validation.VID_UBL_INVOICE_1316.getWithVersionLatestRelease (),
                       MockTestFiles.getAllUBLInvoiceFiles ());
  }

  @Test
  public void testUBLCreditNoteConformance ()
  {
    _checkConformance (new UBL21CreditNotePurifier (),
                       EN16931Validation.VID_UBL_CREDIT_NOTE_1316.getWithVersionLatestRelease (),
                       MockTestFiles.getAllUBLCreditNoteFiles ());
  }

  @Test
  public void testCIIConformance ()
  {
    _checkConformance (new CIID16BPurifier (),
                       EN16931Validation.VID_CII_1316.getWithVersionLatestRelease (),
                       MockTestFiles.getAllCIIFiles ());
  }
}
