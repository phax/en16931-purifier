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

import org.junit.Test;

/**
 * Test class for class {@link CIID25APurifier}. All CII versions share the same XML namespace
 * URIs, so the CII D16B test files can be used as CII D25A documents as well.
 *
 * @author Philip Helger
 */
public final class CIID25APurifierTest extends AbstractPurifierFuncTest
{
  @Test
  public void testPurifyAll ()
  {
    final CIID25APurifier aPurifier = new CIID25APurifier ();
    for (final File aFile : MockTestFiles.getAllCIIFiles ())
      purifyAndCheck (aPurifier, aFile);
  }
}
