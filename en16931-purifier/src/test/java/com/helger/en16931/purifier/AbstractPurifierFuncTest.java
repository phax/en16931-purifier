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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.IError;
import com.helger.diagnostics.error.level.EErrorLevel;
import com.helger.diagnostics.error.list.ErrorList;

/**
 * Base class for all functional purifier tests.
 *
 * @author Philip Helger
 */
public abstract class AbstractPurifierFuncTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractPurifierFuncTest.class);

  @NonNull
  private static ICommonsList <IError> _getAllRemovals (@NonNull final ErrorList aErrorList)
  {
    // Every removal is added as an information entry
    return aErrorList.getAll (x -> x.getErrorLevel ().isEQ (EErrorLevel.INFO));
  }

  @NonNull
  private static String _getAsString (@NonNull final ICommonsList <IError> aErrors)
  {
    return aErrors.getAllMapped (x -> x.getAsString (Locale.US)).toString ();
  }

  /**
   * Purify the provided file, assert that the result is XML Schema valid and that purifying it a
   * second time does not remove anything else.
   *
   * @param aPurifier
   *        The purifier to be used. May not be <code>null</code>.
   * @param aSrcFile
   *        The source file to be purified. May not be <code>null</code>.
   * @param <T>
   *        The JAXB document type of the purifier
   */
  protected static <T> void purifyAndCheck (@NonNull final AbstractEN16931Purifier <T, ?> aPurifier,
                                            @NonNull final File aSrcFile)
  {
    final ErrorList aErrorList = new ErrorList ();
    final T aPurified = aPurifier.purify (aSrcFile, aErrorList);
    assertNotNull ("Failed to purify '" + aSrcFile.getPath () + "': " + _getAsString (aErrorList.getAllErrors ()),
                   aPurified);
    assertEquals ("Purifying '" + aSrcFile.getPath () + "' created errors: " +
                  _getAsString (aErrorList.getAllErrors ()), 0, aErrorList.getAllErrors ().size ());

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Purified '" + aSrcFile.getPath () + "' and removed " + _getAllRemovals (aErrorList).size () + " items");

    // The purified object must be serializable
    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
    {
      final ErrorList aWriteErrors = new ErrorList ();
      assertTrue ("Failed to write the purified '" + aSrcFile.getPath () + "': " +
                  _getAsString (aWriteErrors.getAllErrors ()),
                  aPurifier.write (aPurified, aBAOS, aWriteErrors).isSuccess ());
      assertTrue (aBAOS.size () > 0);
    }

    // Purification must be idempotent
    final ErrorList aErrorList2 = new ErrorList ();
    final T aPurified2 = aPurifier.purify (aPurified, aErrorList2);
    assertNotNull (aPurified2);
    final ICommonsList <IError> aRemovals = _getAllRemovals (aErrorList2);
    assertTrue ("Purifying the already purified '" + aSrcFile.getPath () + "' removed more content: " +
                _getAsString (aRemovals), aRemovals.isEmpty ());
  }
}
