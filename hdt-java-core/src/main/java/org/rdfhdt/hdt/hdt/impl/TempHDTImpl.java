/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/hdt/impl/TempHDTImpl.java $
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

import java.io.IOException;

import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.TriplesTempDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.TempHDT;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.header.HeaderFactory;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.TempTriples;
import org.rdfhdt.hdt.triples.TriplesFactory;

/**
 * @author mario.arias, Eugen
 *
 */
public class TempHDTImpl implements TempHDT {

    protected Header		    header;
    protected TriplesTempDictionary dictionary;
    protected TempTriples	    triples;

    protected String		    baseUri;
    protected boolean		    reif;
    protected ModeOfLoading	    modeOfLoading;
    protected boolean		    isOrganized;
    protected long		    rawsize;

    public TempHDTImpl(final HDTOptions spec, final String baseUri, final ModeOfLoading modeOfLoading, final boolean reif) {

	this.baseUri = baseUri;
	this.modeOfLoading = modeOfLoading;
	this.reif = reif;
	this.header = HeaderFactory.createHeader(spec);
	this.dictionary = DictionaryFactory.createTempDictionary(spec, reif);
	this.triples = TriplesFactory.createTempTriples(spec, reif);
    }

    /**
     * @param spec
     * @param baseUri
     * @param modeOfLoading
     */
    public TempHDTImpl(final HDTOptions spec, final String baseUri, final ModeOfLoading modeOfLoading) {
	this(spec, baseUri, modeOfLoading, false);
    }

    @Override
    public Header getHeader() {
	return this.header;
    }

    @Override
    public TriplesTempDictionary getDictionary() {
	return this.dictionary;
    }

    @Override
    public TempTriples getTriples() {
	return this.triples;
    }

    @Override
    public void insert(final CharSequence subject, final CharSequence predicate, final CharSequence object) {
	this.rawsize += subject.length() + predicate.length() + object.length() + 4;
	this.triples.insert(
		this.dictionary.insert(subject, TripleComponentRole.SUBJECT),
		this.dictionary.insert(predicate, TripleComponentRole.PREDICATE),
		this.dictionary.insert(object, TripleComponentRole.OBJECT));
	this.isOrganized = false;
	this.modeOfLoading = null;
    }

    @Override
    public void insert(final CharSequence subject, final CharSequence predicate, final CharSequence object, final CharSequence graph) {
	this.rawsize += subject.length() + predicate.length() + object.length() + graph.length() + 4;
	this.triples.insert(
		this.dictionary.insert(subject, TripleComponentRole.SUBJECT),
		this.dictionary.insert(predicate, TripleComponentRole.PREDICATE),
		this.dictionary.insert(object, TripleComponentRole.OBJECT));
	this.isOrganized = false;
	this.modeOfLoading = null;
    }

    @Override
    public void clear() {
	this.dictionary.clear();
	this.triples.clear();

	this.isOrganized = false;
    }

    @Override
    public void close() throws IOException {
	this.dictionary.close();
	this.triples.close();
    }

    @Override
    public String getBaseURI() {
	return this.baseUri;
    }

    @Override
    public void reorganizeDictionary(final ProgressListener listener) {
	if (this.isOrganized || this.dictionary.isOrganized())
	    return;

	// Reorganize dictionary
	// StopWatch reorgStp = new StopWatch();
	if (ModeOfLoading.ONE_PASS.equals(this.modeOfLoading)) {
	    this.dictionary.reorganize(this.triples);
	} else if (ModeOfLoading.TWO_PASS.equals(this.modeOfLoading)) {
	    this.dictionary.reorganize();
	} else if (this.modeOfLoading == null) {
	    this.dictionary.reorganize(this.triples);
	}
	// System.out.println("Dictionary reorganized in "+reorgStp.stopAndShow());
    }

    @Override
    public void reorganizeTriples(final ProgressListener listener) {
	if (this.isOrganized)
	    return;

	if (!this.dictionary.isOrganized())
	    throw new RuntimeException("Cannot reorganize triples before dictionary is reorganized!");

	// Sort and remove duplicates.
	// StopWatch sortDupTime = new StopWatch();
	this.triples.sort(listener);
	this.triples.removeDuplicates(listener);
	// System.out.println("Sort triples and remove duplicates: "+sortDupTime.stopAndShow());

	this.isOrganized = true;
    }

    @Override
    public boolean isOrganized() {
	return this.isOrganized;
    }

    public long getRawSize() {
	return this.rawsize;
    }
}
