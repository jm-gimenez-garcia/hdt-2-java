/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/hdt/impl/HDTImpl.java $
 * Revision: $Rev: 202 $
 * Last modified: $Date: 2013-05-10 18:04:41 +0100 (vie, 10 may 2013) $
 * Last modified by: $Author: mario.arias $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * Contacting the authors:
 * Mario Arias: mario.arias@deri.org
 * Javier D. Fernandez: jfergar@infor.uva.es
 * Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 * Alejandro Andres: fuzzy.alej@gmail.com
 */

package org.rdfhdt.hdt.hdt.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.dictionary.TriplesDictionaryPrivate;
import org.rdfhdt.hdt.dictionary.TriplesTempDictionary;
import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDTPrivate;
import org.rdfhdt.hdt.hdt.HDTVersion;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.hdt.TempHDT;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.header.HeaderFactory;
import org.rdfhdt.hdt.header.HeaderPrivate;
import org.rdfhdt.hdt.iterator.DictionaryTranslateIterator;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TempTriples;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.triples.Triples;
import org.rdfhdt.hdt.triples.TriplesFactory;
import org.rdfhdt.hdt.triples.TriplesPrivate;
import org.rdfhdt.hdt.util.StringUtil;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.io.IOUtil;
import org.rdfhdt.hdt.util.listener.IntermediateListener;

/**
 * Basic implementation of HDT interface
 *
 */
public class HDTImpl implements HDTPrivate {

    private final HDTOptions	       spec;

    protected HeaderPrivate	       header;
    protected TriplesDictionaryPrivate dictionary;
    protected TriplesPrivate	       triples;

    private String		       hdtFileName;
    private String		       baseUri;
    private boolean		       isMapped;

    private void createComponents() {
	this.header = HeaderFactory.createHeader(this.spec);
	this.dictionary = DictionaryFactory.createDictionary(this.spec);
	this.triples = TriplesFactory.createTriples(this.spec);
    }

    @Override
    public void populateHeaderStructure(final String baseUri) {
	this.header.insert(baseUri, HDTVocabulary.RDF_TYPE, HDTVocabulary.HDT_DATASET);
	this.header.insert(baseUri, HDTVocabulary.RDF_TYPE, HDTVocabulary.VOID_DATASET);

	// VOID
	// this.header.insert(baseUri, HDTVocabulary.VOID_TRIPLES, this.triples.getNumberOfElements());
	// this.header.insert(baseUri, HDTVocabulary.VOID_DISTINCT_SUBJECTS, this.dictionary.getNsubjects());
	// this.header.insert(baseUri, HDTVocabulary.VOID_DISTINCT_OBJECTS, this.dictionary.getNobjects());

	// Structure
	final String formatNode = "_:format";
	final String dictNode = "_:dictionary";
	final String triplesNode = "_:triples";
	final String statisticsNode = "_:statistics";
	final String publicationInfoNode = "_:publicationInformation";

	this.header.insert(baseUri, HDTVocabulary.HDT_FORMAT_INFORMATION, formatNode);
	this.header.insert(formatNode, HDTVocabulary.HDT_DICTIONARY, dictNode);
	this.header.insert(formatNode, HDTVocabulary.HDT_TRIPLES, triplesNode);
	this.header.insert(baseUri, HDTVocabulary.HDT_STATISTICAL_INFORMATION, statisticsNode);
	this.header.insert(baseUri, HDTVocabulary.HDT_PUBLICATION_INFORMATION, publicationInfoNode);

	this.dictionary.populateHeader(this.header, dictNode);
	this.triples.populateHeader(this.header, triplesNode);

	this.header.insert(statisticsNode, HDTVocabulary.HDT_SIZE, this.getDictionary().size() + this.getTriples().size());

	// Current time
	this.header.insert(publicationInfoNode, HDTVocabulary.DUBLIN_CORE_ISSUED, StringUtil.formatDate(new Date()));
    }

    /**
     * @param spec2
     */
    public HDTImpl(final HDTOptions spec) {
	this.spec = spec;

	this.createComponents();
    }

    @Override
    public void loadFromHDT(final InputStream input, final ProgressListener listener) throws IOException {
	final ControlInfo ci = new ControlInformation();
	final IntermediateListener iListener = new IntermediateListener(listener);

	// Load Global ControlInformation
	ci.clear();
	ci.load(input);
	final String hdtFormat = ci.getFormat();
	if (!hdtFormat.equals(
		HDTVocabulary.HDT_CONTAINER)) { throw new IllegalFormatException("This software (v" + HDTVersion.HDT_VERSION + ".x.x) cannot open this version of HDT File (" + hdtFormat + ")"); }

	// Load header
	ci.clear();
	ci.load(input);
	iListener.setRange(0, 5);
	this.header = HeaderFactory.createHeader(ci);
	this.header.load(input, ci, iListener);

	// Set base URI.
	try {
	    final IteratorTripleString it = this.header.search("", HDTVocabulary.RDF_TYPE, HDTVocabulary.HDT_DATASET);
	    if (it.hasNext()) {
		this.baseUri = it.next().getSubject().toString();
	    }
	} catch (final NotFoundException e) {
	    e.printStackTrace();
	}

	// Load dictionary
	ci.clear();
	ci.load(input);
	iListener.setRange(5, 60);
	this.dictionary = DictionaryFactory.createDictionary(ci);
	this.dictionary.load(input, ci, iListener);

	// Load Triples
	ci.clear();
	ci.load(input);
	iListener.setRange(60, 100);
	this.triples = TriplesFactory.createTriples(ci);
	this.triples.load(input, ci, iListener);
    }

    @Override
    public void loadFromHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
	InputStream in;
	if (hdtFileName.endsWith(".gz")) {
	    in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(hdtFileName)));
	} else {
	    in = new CountInputStream(new BufferedInputStream(new FileInputStream(hdtFileName)));
	}
	this.loadFromHDT(in, listener);
	in.close();

	this.hdtFileName = hdtFileName;
    }

    @Override
    public void mapFromHDT(File f, final long offset, final ProgressListener listener) throws IOException {
	this.hdtFileName = f.toString();
	this.isMapped = true;

	CountInputStream input;
	if (this.hdtFileName.endsWith(".gz")) {
	    final File old = f;
	    this.hdtFileName = this.hdtFileName.substring(0, this.hdtFileName.length() - 3);
	    f = new File(this.hdtFileName);

	    if (!f.exists()) {
		System.err.println("We cannot map a gzipped HDT, decompressing into " + this.hdtFileName + " first.");
		IOUtil.decompressGzip(old, f);
		System.err.println("Gzipped HDT successfully decompressed. You might want to delete " + old.getAbsolutePath() + " to save disk space.");
	    } else {
		System.err.println("We cannot map a gzipped HDT, using " + this.hdtFileName + " instead.");
	    }
	}

	input = new CountInputStream(new BufferedInputStream(new FileInputStream(this.hdtFileName)));

	final ControlInfo ci = new ControlInformation();
	final IntermediateListener iListener = new IntermediateListener(listener);

	// Load Global ControlInformation
	ci.clear();
	ci.load(input);
	final String hdtFormat = ci.getFormat();
	if (!hdtFormat.equals(
		HDTVocabulary.HDT_CONTAINER)) { throw new IllegalFormatException("This software (v" + HDTVersion.HDT_VERSION + ".x.x) cannot open this version of HDT File (" + hdtFormat + ")"); }

	// Load header
	ci.clear();
	ci.load(input);
	iListener.setRange(0, 5);
	this.header = HeaderFactory.createHeader(ci);
	this.header.load(input, ci, iListener);

	// Set base URI.
	try {
	    final IteratorTripleString it = this.header.search("", HDTVocabulary.RDF_TYPE, HDTVocabulary.HDT_DATASET);
	    if (it.hasNext()) {
		this.baseUri = it.next().getSubject().toString();
	    }
	} catch (final NotFoundException e) {
	    e.printStackTrace();
	}

	// Load dictionary
	ci.clear();
	input.mark(1024);
	ci.load(input);
	input.reset();
	iListener.setRange(5, 60);
	this.dictionary = DictionaryFactory.createDictionary(ci);
	this.dictionary.mapFromFile(input, f, iListener);

	// Load Triples
	ci.clear();
	input.mark(1024);
	ci.load(input);
	input.reset();
	iListener.setRange(60, 100);
	this.triples = TriplesFactory.createTriples(ci);
	this.triples.mapFromFile(input, f, iListener);

	input.close();
    }

    /*
     * (non-Javadoc)
     * @see hdt.HDT#saveToHDT(java.io.OutputStream)
     */
    @Override
    public void saveToHDT(final OutputStream output, final ProgressListener listener) throws IOException {
	final ControlInfo ci = new ControlInformation();
	final IntermediateListener iListener = new IntermediateListener(listener);

	ci.clear();
	ci.setType(ControlInfo.Type.GLOBAL);
	ci.setFormat(HDTVocabulary.HDT_CONTAINER);
	ci.save(output);

	ci.clear();
	ci.setType(ControlInfo.Type.HEADER);
	this.header.save(output, ci, iListener);

	ci.clear();
	ci.setType(ControlInfo.Type.DICTIONARY);
	this.dictionary.save(output, ci, iListener);

	ci.clear();
	ci.setType(ControlInfo.Type.TRIPLES);
	this.triples.save(output, ci, iListener);
    }

    /*
     * (non-Javadoc)
     * @see hdt.HDT#saveToHDT(java.io.OutputStream)
     */
    @Override
    public void saveToHDT(final String fileName, final ProgressListener listener) throws IOException {
	final OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
	// OutputStream out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
	this.saveToHDT(out, listener);
	out.close();

	this.hdtFileName = fileName;
    }

    @Override
    public IteratorTripleString search(final CharSequence subject, final CharSequence predicate, final CharSequence object) throws NotFoundException {

	// Conversion from TripleString to TripleID
	final TripleID triple = new TripleID(
		this.dictionary.stringToId(subject, TripleComponentRole.SUBJECT),
		this.dictionary.stringToId(predicate, TripleComponentRole.PREDICATE),
		this.dictionary.stringToId(object, TripleComponentRole.OBJECT));

	if (triple.getSubject() == -1 || triple.getPredicate() == -1 || triple.getObject() == -1) {
	    // throw new NotFoundException("String not found in dictionary");
	    return new IteratorTripleString() {
		@Override
		public TripleString next() {
		    return null;
		}

		@Override
		public boolean hasNext() {
		    return false;
		}

		@Override
		public TripleString previous() {
		    return null;
		}

		@Override
		public ResultEstimationType numResultEstimation() {
		    return ResultEstimationType.EXACT;
		}

		@Override
		public boolean hasPrevious() {
		    return false;
		}

		@Override
		public void goToStart() {
		}

		@Override
		public long estimatedNumResults() {
		    return 0;
		}
	    };
	}

	return new DictionaryTranslateIterator(this.triples.search(triple), this.dictionary, subject, predicate, object);
    }

    /*
     * (non-Javadoc)
     * @see hdt.HDT#getHeader()
     */
    @Override
    public Header getHeader() {
	return this.header;
    }

    /*
     * (non-Javadoc)
     * @see hdt.HDT#getDictionary()
     */
    @Override
    public TriplesDictionary getDictionary() {
	return this.dictionary;
    }

    /*
     * (non-Javadoc)
     * @see hdt.HDT#getTriples()
     */
    @Override
    public Triples getTriples() {
	return this.triples;
    }

    /*
     * (non-Javadoc)
     * @see hdt.hdt.HDT#getSize()
     */
    @Override
    public long size() {
	return this.dictionary.size() + this.triples.size();
    }

    public void loadFromModifiableHDT(final TempHDT modHdt, final ProgressListener listener) {

	modHdt.reorganizeDictionary(listener);
	modHdt.reorganizeTriples(listener);

	// Get parts
	final TempTriples modifiableTriples = modHdt.getTriples();
	final TriplesTempDictionary modifiableDictionary = modHdt.getDictionary();

	// Convert triples to final format
	if (this.triples.getClass().equals(modifiableTriples.getClass())) {
	    this.triples = modifiableTriples;
	} else {
	    // StopWatch tripleConvTime = new StopWatch();
	    this.triples.load(modifiableTriples, listener);
	    // System.out.println("Triples conversion time: "+tripleConvTime.stopAndShow());
	}

	// Convert dictionary to final format
	if (this.dictionary.getClass().equals(modifiableDictionary.getClass())) {
	    this.dictionary = (TriplesDictionaryPrivate) modifiableDictionary;
	} else {
	    // StopWatch dictConvTime = new StopWatch();
	    this.dictionary.load(modifiableDictionary, listener);
	    // System.out.println("Dictionary conversion time: "+dictConvTime.stopAndShow());
	}

	this.baseUri = modHdt.getBaseURI();
    }

    /*
     * (non-Javadoc)
     * @see hdt.hdt.HDT#generateIndex(hdt.listener.ProgressListener)
     */
    @Override
    public void loadOrCreateIndex(final ProgressListener listener) {
	final ControlInfo ci = new ControlInformation();
	String indexName = this.hdtFileName + HDTVersion.get_index_suffix("-");
	indexName = indexName.replaceAll("\\.hdt\\.gz", "hdt");
	final String versionName = indexName;
	File ff = new File(indexName);
	// backward compatibility
	if (!ff.isFile() || !ff.canRead()) {
	    indexName = this.hdtFileName + (".index");
	    indexName = indexName.replaceAll("\\.hdt\\.gz", "hdt");
	    ff = new File(indexName);
	}

	CountInputStream in = null;
	try {
	    in = new CountInputStream(new BufferedInputStream(new FileInputStream(ff)));
	    ci.load(in);
	    if (this.isMapped) {
		this.triples.mapIndex(in, new File(indexName), ci, listener);
	    } else {
		this.triples.loadIndex(in, ci, listener);
	    }
	    // in.close();
	} catch (final Exception e) {
	    System.out.println("Could not read .hdt.index, Generating a new one.");

	    // GENERATE
	    this.triples.generateIndex(listener);

	    FileOutputStream out = null;
	    // SAVE
	    try {
		out = new FileOutputStream(versionName);
		ci.clear();
		this.triples.saveIndex(out, ci, listener);
		out.close();
	    } catch (final IOException e2) {

	    } finally {
		IOUtil.closeQuietly(out);
	    }
	} finally {
	    IOUtil.closeQuietly(in);
	}
    }

    @Override
    public String getBaseURI() {
	return this.baseUri;
    }

    protected void setTriples(final TriplesPrivate triples) {
	this.triples = triples;
    }

    @Override
    public void close() throws IOException {
	this.dictionary.close();
	this.triples.close();
    }

}
