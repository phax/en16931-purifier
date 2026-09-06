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

import org.jspecify.annotations.NonNull;

import com.helger.annotation.Nonempty;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.io.file.FileSystemRecursiveIterator;
import com.helger.io.file.FilenameHelper;

/**
 * Central access to all test files.
 *
 * @author Philip Helger
 */
final class MockTestFiles
{
  static final String BASE_TEST_DIR = "src/test/resources/external/";
  static final String UBL_INVOICE_DIR = BASE_TEST_DIR + "ubl/invoice";
  static final String UBL_CREDIT_NOTE_DIR = BASE_TEST_DIR + "ubl/creditnote";
  static final String CII_DIR = BASE_TEST_DIR + "cii";
  static final String PURIFY_DIR = BASE_TEST_DIR + "purify";
  static final String PURIFY_2026_DIR = BASE_TEST_DIR + "purify2026";

  private MockTestFiles ()
  {}

  @NonNull
  @Nonempty
  @ReturnsMutableCopy
  private static ICommonsList <File> _getAllXMLFiles (@NonNull final String sDirectory)
  {
    final File aDir = new File (sDirectory);
    if (!aDir.isDirectory ())
      throw new IllegalStateException ("The test directory '" + aDir.getAbsolutePath () + "' does not exist");

    final ICommonsList <File> ret = new CommonsArrayList <> ();
    for (final File aFile : new FileSystemRecursiveIterator (aDir))
      if (aFile.isFile () && "xml".equalsIgnoreCase (FilenameHelper.getExtension (aFile)))
        ret.add (aFile);

    if (ret.isEmpty ())
      throw new IllegalStateException ("The test directory '" + aDir.getAbsolutePath () + "' contains no XML file");

    // Ensure a deterministic order
    ret.sort ( (x, y) -> x.getAbsolutePath ().compareTo (y.getAbsolutePath ()));
    return ret;
  }

  @NonNull
  @Nonempty
  @ReturnsMutableCopy
  static ICommonsList <File> getAllUBLInvoiceFiles ()
  {
    return _getAllXMLFiles (UBL_INVOICE_DIR);
  }

  @NonNull
  @Nonempty
  @ReturnsMutableCopy
  static ICommonsList <File> getAllUBLCreditNoteFiles ()
  {
    return _getAllXMLFiles (UBL_CREDIT_NOTE_DIR);
  }

  @NonNull
  @Nonempty
  @ReturnsMutableCopy
  static ICommonsList <File> getAllCIIFiles ()
  {
    return _getAllXMLFiles (CII_DIR);
  }
}
