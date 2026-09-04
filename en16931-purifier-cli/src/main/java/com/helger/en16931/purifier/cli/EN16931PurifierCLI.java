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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.base.state.ESuccess;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.IError;
import com.helger.diagnostics.error.level.EErrorLevel;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.en16931.purifier.AbstractEN16931Purifier;
import com.helger.en16931.purifier.EEN16931DocumentType;
import com.helger.en16931.purifier.EEN16931SyntaxKind;
import com.helger.en16931.purifier.EEN16931Version;
import com.helger.en16931.purifier.PurifierVersion;
import com.helger.io.file.FileSystemIterator;
import com.helger.io.file.FileSystemRecursiveIterator;
import com.helger.io.file.FilenameHelper;
import com.helger.xml.serialize.read.DOMReader;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main command line client
 *
 * @author Philip Helger
 */
@Command (description = "EN 16931 Purifier - removes everything that is not part of the EN 16931 core message",
          name = "EN16931Purifier",
          mixinStandardHelpOptions = true,
          separator = " ")
public class EN16931PurifierCLI implements Callable <Integer>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (EN16931PurifierCLI.class);

  @Option (names = "--en-version",
           paramLabel = "version",
           defaultValue = "2017",
           description = "Version of the EN 16931 core message: '2017' or '2026' (default: '${DEFAULT-VALUE}')")
  private String m_sENVersion;

  @Option (names = "--ubl",
           paramLabel = "version",
           defaultValue = "2.1",
           description = "Version of the UBL syntax to be used for reading and writing: '2.1' or '2.5' (default: '${DEFAULT-VALUE}')")
  private String m_sUBLVersion;

  @Option (names = "--cii",
           paramLabel = "version",
           defaultValue = "D16B",
           description = "Version of the CII syntax to be used for reading and writing: 'D16B' or 'D25A' (default: '${DEFAULT-VALUE}')")
  private String m_sCIIVersion;

  @Option (names = { "-t", "--target" },
           paramLabel = "directory",
           defaultValue = ".",
           description = "The target directory for result output (default: '${DEFAULT-VALUE}')")
  private String m_sOutputDir;

  @Option (names = "--output-suffix",
           paramLabel = "filename part",
           defaultValue = "-purified",
           description = "The suffix added to the output filename (default: '${DEFAULT-VALUE}')")
  private String m_sOutputFileSuffix;

  @Option (names = "--remove-non-core-attributes",
           paramLabel = "true|false",
           arity = "1",
           defaultValue = "true",
           description = "Remove attributes that are not part of the EN 16931 core message (default: '${DEFAULT-VALUE}')")
  private boolean m_bRemoveNonCoreAttributes;

  @Option (names = "--remove-empty-elements",
           paramLabel = "true|false",
           arity = "1",
           defaultValue = "true",
           description = "Remove elements that are empty after the purification (default: '${DEFAULT-VALUE}')")
  private boolean m_bRemoveEmptyElements;

  @Option (names = "--enforce-cardinalities",
           paramLabel = "true|false",
           arity = "1",
           defaultValue = "true",
           description = "Remove occurrences beyond the cardinality allowed by EN 16931 (default: '${DEFAULT-VALUE}')")
  private boolean m_bEnforceCardinalities;

  @Option (names = "--remove-comments",
           paramLabel = "true|false",
           arity = "1",
           defaultValue = "true",
           description = "Remove XML comments and processing instructions (default: '${DEFAULT-VALUE}')")
  private boolean m_bRemoveComments;

  @Option (names = "--formatted-output",
           paramLabel = "true|false",
           arity = "1",
           defaultValue = "true",
           description = "Write formatted XML (default: '${DEFAULT-VALUE}')")
  private boolean m_bFormattedOutput;

  @Option (names = "--verbose", description = "Enable debug logging")
  private boolean m_bVerbose;

  @Option (names = "--disable-wildcard-expansion", description = "Disable wildcard expansion of filenames")
  private boolean m_bDisableWildcardExpansion;

  @Parameters (arity = "1..*", paramLabel = "source files", description = "One or more UBL or CII file(s)")
  private List <String> m_aSourceFilenames;

  private void _verboseLog (@NonNull final Supplier <String> aSupplier)
  {
    if (m_bVerbose)
      LOGGER.info (aSupplier.get ());
  }

  @NonNull
  private String _normalizeOutputDirectory (@NonNull final String sDirectory)
  {
    _verboseLog ( () -> "CLI option output directory '" + sDirectory + "'");
    final String ret = Paths.get (sDirectory).toAbsolutePath ().normalize ().toString ();
    if (!sDirectory.equals (ret))
      _verboseLog ( () -> "Normalized output directory '" + ret + "'");
    return ret;
  }

  @NonNull
  private static File _normalizeFile (@NonNull final Path aPath)
  {
    return aPath.toAbsolutePath ().normalize ().toFile ();
  }

  @NonNull
  private ICommonsList <File> _resolveWildcards (@NonNull final List <String> aFilenames) throws IOException
  {
    final ICommonsList <File> ret = new CommonsArrayList <> (aFilenames.size ());

    final File aRootDir = new File (".").getCanonicalFile ();
    for (final String sFilename : aFilenames)
    {
      if (sFilename.indexOf ('*') >= 0 ||
          sFilename.indexOf ('?') >= 0 ||
          (sFilename.indexOf ('[') >= 0 && sFilename.indexOf (']') >= 0))
      {
        // Make search pattern absolute
        final String sRealName = new File (sFilename).getAbsolutePath ();
        _verboseLog ( () -> "Trying to resolve wildcards for '" + sRealName + "'");
        final PathMatcher matcher = FileSystems.getDefault ().getPathMatcher ("glob:" + sRealName);
        for (final File f : new FileSystemRecursiveIterator (aRootDir))
          if (matcher.matches (f.toPath ()))
          {
            _verboseLog ( () -> "  Found wildcard match '" + f + "'");
            ret.add (f);
          }
      }
      else
        ret.add (new File (sFilename));
    }
    return ret;
  }

  @NonNull
  private ICommonsList <File> _normalizeInputFiles (@NonNull final List <String> aFilenames) throws IOException
  {
    final ICommonsList <File> aFiles;
    if (m_bDisableWildcardExpansion)
    {
      aFiles = new CommonsArrayList <> (aFilenames, File::new);
      _verboseLog ( () -> "Using the input files '" + aFiles + "'");
    }
    else
    {
      _verboseLog ( () -> "Normalizing the input files '" + aFilenames + "'");
      aFiles = _resolveWildcards (aFilenames);
      _verboseLog ( () -> "Resolved wildcards of input files to '" + aFiles + "'");
    }

    final ICommonsList <File> ret = new CommonsArrayList <> ();

    for (final File aFile : aFiles)
    {
      if (aFile.isDirectory ())
      {
        _verboseLog ( () -> "Input '" + aFile.toString () + "' is a Directory");
        // collecting readable and normalized absolute path files
        for (final File aChildFile : new FileSystemIterator (aFile))
        {
          final Path p = aChildFile.toPath ();
          if (Files.isReadable (p) && !Files.isDirectory (p))
          {
            ret.add (_normalizeFile (p));
            _verboseLog ( () -> "Added file '" + ret.getLastOrNull ().toString () + "'");
          }
        }
      }
      else
        // Does not need to be file - only needs to be readable
        if (aFile.canRead ())
        {
          _verboseLog ( () -> "Input '" + aFile.toString () + "' is a readable File");
          ret.add (_normalizeFile (aFile.toPath ()));
        }
        else
          LOGGER.warn ("Ignoring non-existing file " + aFile.getAbsolutePath ());
    }

    _verboseLog ( () -> "Purifying the following files: " + ret.getAllMapped (File::getAbsolutePath));
    return ret;
  }

  private static void _log (@NonNull final IError aError)
  {
    final String sMsg = "  " + aError.getAsString (Locale.US);
    if (aError.isError ())
      LOGGER.error (sMsg);
    else
      if (aError.getErrorLevel ().isGE (EErrorLevel.WARN))
        LOGGER.warn (sMsg);
      else
        LOGGER.info (sMsg);
  }

  @Nullable
  private static EEN16931SyntaxKind _getSyntaxKind (@NonNull final Document aDoc)
  {
    final Element aRoot = aDoc.getDocumentElement ();
    if (aRoot == null)
      return null;

    final QName aRootElementName = new QName (StringHelper.getNotNull (aRoot.getNamespaceURI ()),
                                              aRoot.getLocalName ());
    return EEN16931SyntaxKind.getFromRootElementNameOrNull (aRootElementName);
  }

  @Nullable
  private EEN16931DocumentType _getDocumentType (@NonNull final File aSrcFile, @NonNull final Document aDoc)
  {
    final EEN16931SyntaxKind eSyntaxKind = _getSyntaxKind (aDoc);
    if (eSyntaxKind == null)
    {
      LOGGER.error ("The file '" +
                    aSrcFile.getAbsolutePath () +
                    "' is neither a UBL Invoice, a UBL Credit Note nor a CII Cross Industry Invoice");
      return null;
    }

    final String sSyntaxVersion = eSyntaxKind == EEN16931SyntaxKind.CII ? m_sCIIVersion : m_sUBLVersion;
    final EEN16931DocumentType eDocType = EEN16931DocumentType.getFromSyntaxKindAndVersionOrNull (eSyntaxKind,
                                                                                                  sSyntaxVersion);
    if (eDocType == null)
      LOGGER.error ("The syntax version '" +
                    sSyntaxVersion +
                    "' is not supported for a " +
                    eSyntaxKind.getDisplayName ());
    return eDocType;
  }

  // doing the business
  public Integer call () throws Exception
  {
    if (m_bVerbose)
      System.setProperty ("org.slf4j.simpleLogger.defaultLogLevel", "debug");

    final EEN16931Version eENVersion = EEN16931Version.getFromIDOrNull (m_sENVersion);
    if (eENVersion == null)
      throw new IllegalStateException ("Unsupported EN 16931 version '" + m_sENVersion + "' provided.");
    if (!eENVersion.isSupported ())
      throw new IllegalStateException ("The syntax bindings of " +
                                       eENVersion.getDisplayName () +
                                       " are not yet available.");

    m_sOutputDir = _normalizeOutputDirectory (m_sOutputDir);
    final ICommonsList <File> aSourceFiles = _normalizeInputFiles (m_aSourceFilenames);

    int nFailures = 0;
    for (final File aSrcFile : aSourceFiles)
    {
      LOGGER.info ("Purifying the file '" + aSrcFile.getAbsolutePath () + "'");

      final Document aSrcDoc = DOMReader.readXMLDOM (aSrcFile);
      if (aSrcDoc == null)
      {
        LOGGER.error ("Failed to read '" + aSrcFile.getAbsolutePath () + "' as XML document");
        nFailures++;
        continue;
      }

      final EEN16931DocumentType eDocType = _getDocumentType (aSrcFile, aSrcDoc);
      if (eDocType == null)
      {
        nFailures++;
        continue;
      }

      _verboseLog ( () -> "Determined the document to be a " + eDocType.getDisplayName ());

      final AbstractEN16931Purifier <?, ?> aPurifier = eDocType.createPurifier (eENVersion);
      aPurifier.setRemoveNonCoreAttributes (m_bRemoveNonCoreAttributes)
               .setRemoveEmptyElements (m_bRemoveEmptyElements)
               .setEnforceCardinalities (m_bEnforceCardinalities)
               .setRemoveComments (m_bRemoveComments)
               .setFormattedOutput (m_bFormattedOutput);

      final File aDestFile = new File (m_sOutputDir,
                                       FilenameHelper.getBaseName (aSrcFile) + m_sOutputFileSuffix + ".xml");

      final ErrorList aErrorList = new ErrorList ();
      final ESuccess eSuccess = aPurifier.purify (aSrcDoc, aDestFile, aErrorList);
      for (final IError aError : aErrorList)
        if (m_bVerbose || aError.getErrorLevel ().isGE (EErrorLevel.WARN))
          _log (aError);

      if (eSuccess.isSuccess ())
        LOGGER.info ("Successfully wrote the purified file '" + aDestFile.getAbsolutePath () + "'");
      else
      {
        LOGGER.error ("Failed to purify the file '" + aSrcFile.getAbsolutePath () + "'");
        nFailures++;
      }
    }

    return Integer.valueOf (nFailures == 0 ? 0 : 1);
  }

  public static void main (final String [] aArgs)
  {
    LOGGER.info ("EN 16931 Purifier v" +
                 PurifierVersion.BUILD_VERSION +
                 " (build " +
                 PurifierVersion.BUILD_TIMESTAMP +
                 ")");

    final CommandLine cmd = new CommandLine (new EN16931PurifierCLI ());
    cmd.setCaseInsensitiveEnumValuesAllowed (true);
    final int nExitCode = cmd.execute (aArgs);
    System.exit (nExitCode);
  }
}
