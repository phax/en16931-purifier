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
package com.helger.en16931.purifier.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

import picocli.CommandLine;

/**
 * Test class for class {@link EN16931PurifierCLI}.
 *
 * @author Philip Helger
 */
public final class EN16931PurifierCLITest
{
  private static final String SRC_DIR = "src/test/resources/external/";
  private static final String TARGET_DIR = "target/test-output/cli/";

  private static int _execute (final String... aArgs)
  {
    final CommandLine cmd = new CommandLine (new EN16931PurifierCLI ());
    cmd.setCaseInsensitiveEnumValuesAllowed (true);
    return cmd.execute (aArgs);
  }

  private static String _readFile (final File aFile) throws Exception
  {
    return new String (Files.readAllBytes (aFile.toPath ()), StandardCharsets.UTF_8);
  }

  @Test
  public void testPurifyUBLInvoice () throws Exception
  {
    final File aDestFile = new File (TARGET_DIR, "ubl-invoice-extensions-purified.xml");
    Files.deleteIfExists (aDestFile.toPath ());

    assertEquals (0, _execute ("-t", TARGET_DIR, SRC_DIR + "ubl-invoice-extensions.xml"));
    assertTrue (aDestFile.isFile ());

    final String sPurified = _readFile (aDestFile);
    assertFalse (sPurified.contains ("UBLExtensions"));
    assertFalse (sPurified.contains ("UBLVersionID"));
    assertTrue (sPurified.contains ("INV-2026-0001"));
  }

  @Test
  public void testPurifyCII () throws Exception
  {
    final File aDestFile = new File (TARGET_DIR, "CII_example1-cii.xml");
    Files.deleteIfExists (aDestFile.toPath ());

    assertEquals (0, _execute ("-t", TARGET_DIR, "--output-suffix", "-cii", SRC_DIR + "CII_example1.xml"));
    assertTrue (aDestFile.isFile ());
    assertTrue (_readFile (aDestFile).contains ("CrossIndustryInvoice"));
  }

  @Test
  public void testKeepNonCoreAttributes () throws Exception
  {
    final File aDestFile = new File (TARGET_DIR, "ubl-invoice-extensions-kept.xml");
    Files.deleteIfExists (aDestFile.toPath ());

    assertEquals (0,
                  _execute ("-t",
                            TARGET_DIR,
                            "--output-suffix",
                            "-kept",
                            "--remove-non-core-attributes",
                            "false",
                            SRC_DIR + "ubl-invoice-extensions.xml"));
    assertTrue (_readFile (aDestFile).contains ("schemeAgencyID"));
  }

  @Test
  public void testUnsupportedInput ()
  {
    // Well formed XML, but neither a UBL nor a CII document
    assertEquals (1, _execute ("-t", TARGET_DIR, "pom.xml"));
  }

  @Test
  public void testNonExistingSyntaxVersion ()
  {
    assertEquals (1, _execute ("-t", TARGET_DIR, "--ubl", "2.9", SRC_DIR + "ubl-invoice-extensions.xml"));
  }
}
