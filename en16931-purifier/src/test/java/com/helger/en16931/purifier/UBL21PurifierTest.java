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
 * Test class for classes {@link UBL21InvoicePurifier} and {@link UBL21CreditNotePurifier}.
 *
 * @author Philip Helger
 */
public final class UBL21PurifierTest extends AbstractPurifierFuncTest
{
  @Test
  public void testPurifyAllInvoices ()
  {
    final UBL21InvoicePurifier aPurifier = new UBL21InvoicePurifier ();
    for (final File aFile : MockTestFiles.getAllUBLInvoiceFiles ())
      purifyAndCheck (aPurifier, aFile);
  }

  @Test
  public void testPurifyAllCreditNotes ()
  {
    final UBL21CreditNotePurifier aPurifier = new UBL21CreditNotePurifier ();
    for (final File aFile : MockTestFiles.getAllUBLCreditNoteFiles ())
      purifyAndCheck (aPurifier, aFile);
  }
}
