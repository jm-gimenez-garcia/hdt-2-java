/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/header/PlainHeader.java $
 * Revision: $Rev: 191 $
 * Last modified: $Date: 2013-03-03 11:41:43 +0000 (dom, 03 mar 2013) $
 * Last modified by: $Author: mario.arias $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contacting the authors:
 *   Mario Arias:               mario.arias@deri.org
 *   Javier D. Fernandez:       jfergar@infor.uva.es
 *   Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 *   Alejandro Andres:          fuzzy.alej@gmail.com
 */

package org.rdfhdt.hdt.header;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.RDFParserCallback.RDFCallback;
import org.rdfhdt.hdt.rdf.parsers.RDFParserSimple;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.QuadString;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.util.io.IOUtil;

/**
 * @author mario.arias
 *
 */
public class PlainHeader implements HeaderPrivate, RDFCallback {

    protected HDTOptions spec;
    protected List<TripleString> triples= new ArrayList<>();

    public PlainHeader() {
	this.spec = new HDTSpecification();
    }

    public PlainHeader(final HDTOptions spec) {
	this.spec = spec;
    }

    /* (non-Javadoc)
     * @see hdt.rdf.RDFStorage#insert(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void insert(final CharSequence subject, final CharSequence predicate, final CharSequence object) {
	final String objStr = object.toString();
	if(objStr.charAt(0)=='<'|| objStr.charAt(0)=='"' || objStr.startsWith("http://")||objStr.startsWith("file://")) {
	    this.triples.add(new TripleString(HeaderUtil.cleanURI(subject), HeaderUtil.cleanURI(predicate), object));
	} else {
	    this.triples.add(new TripleString(HeaderUtil.cleanURI(subject), HeaderUtil.cleanURI(predicate), '"'+objStr+'"'));
	}
    }

    /* (non-Javadoc)
     * @see hdt.rdf.RDFStorage#insert(java.lang.String, java.lang.String, long)
     */
    @Override
    public void insert(final CharSequence subject, final CharSequence predicate, final long object) {
	this.triples.add(new TripleString(HeaderUtil.cleanURI(subject), HeaderUtil.cleanURI(predicate), '"'+Long.toString(object)+'"'));
    }

    /* (non-Javadoc)
     * @see hdt.header.Header#save(java.io.OutputStream, hdt.ControlInfo, hdt.ProgressListener)
     */
    @Override
    public void save(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {

	// Dump header into an array to calculate size and have it prepared.
	final ByteArrayOutputStream headerData = new ByteArrayOutputStream();
	final IteratorTripleString iterator = this.search("", "", "");
	while(iterator.hasNext()) {
	    final TripleString next = iterator.next();
	    IOUtil.writeString(headerData, next.asNtriple().toString());
	}

	// Create ControlInfo
	ci.clear();
	ci.setType(ControlInfo.Type.HEADER);
	ci.setFormat(HDTVocabulary.HEADER_NTRIPLES);
	ci.setInt("length",headerData.size());
	ci.save(output);

	// Save Data
	output.write(headerData.toByteArray());
    }

    /* (non-Javadoc)
     * @see hdt.header.Header#load(java.io.InputStream, hdt.ControlInfo, hdt.ProgressListener)
     */
    @Override
    public void load(final InputStream input, final ControlInfo ci, final ProgressListener listener) throws IOException {
	final String format = ci.getFormat();
	if(!format.equals(HDTVocabulary.HEADER_NTRIPLES)) {
	    // FIXME: Add support for other formats
	    throw new IllegalArgumentException("Cannot parse this Header Format");
	}

	final long headerSize = ci.getInt("length");
	final byte [] headerData = IOUtil.readBuffer(input, (int)headerSize, listener);

	try {
	    final RDFParserSimple parser = new RDFParserSimple();
	    parser.doParse(new ByteArrayInputStream(headerData), "http://www.rdfhdt.org", RDFNotation.NTRIPLES, this);
	} catch (final ParserException e) {
	    e.printStackTrace();
	    throw new IllegalFormatException("Error parsing header");
	}
    }

    /* (non-Javadoc)
     * @see hdt.header.Header#getNumberOfElements()
     */
    @Override
    public int getNumberOfElements() {
	return this.triples.size();
    }

    /* (non-Javadoc)
     * @see hdt.header.Header#search(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public IteratorTripleString search(final CharSequence subject, final CharSequence predicate, final CharSequence object) {
	final TripleString pattern = new TripleString(subject.toString(), predicate.toString(), object.toString());
	return new PlainHeaderIterator(this, pattern);
    }

    @Override
    public IteratorTripleString search(final CharSequence subject, final CharSequence predicate, final CharSequence object, final CharSequence graph) {
	final QuadString pattern = new QuadString(subject.toString(), predicate.toString(), object.toString(), graph.toString());
	return new PlainHeaderIterator(this, pattern);
    }

    @Override
    public void processTriple(final TripleString triple, final long pos) {
	this.triples.add(new TripleString(triple));
    }

    @Override
    public void processQuad(final QuadString quad, final long pos) {
	this.processTriple(quad, pos);
    }

    @Override
    public void remove(final CharSequence subject, final CharSequence predicate, final CharSequence object) {
	final TripleString pattern = new TripleString(subject.toString(), predicate.toString(), object.toString());
	final Iterator<TripleString> iter = this.triples.iterator();
	while(iter.hasNext()) {
	    final TripleString next = iter.next();
	    if(next.match(pattern)) {
		iter.remove();
	    }
	}
    }

    @Override
    public CharSequence getBaseURI() {
	final IteratorTripleString it = this.search("", HDTVocabulary.RDF_TYPE, HDTVocabulary.HDT_DATASET);
	if(it.hasNext()) {
	    final TripleString ts = it.next();
	    return ts.getSubject();
	}
	return "";
    }

}
