/**
 * File: $HeadURL:
 * https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/BaseDictionary.java $
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

package org.rdfhdt.hdt.dictionary.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.dictionary.CompositeDictionary;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.dictionary.GraphDictionary;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.dictionary.impl.section.CompositeDictionarySection;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.util.io.CountInputStream;

/**
 * @author José M. Giménez-García
 *
 */
public class ReificationDictionary implements DictionaryPrivate<CompositeDictionary> {

    protected TriplesDictionary		 triplesDictionary;
    protected GraphDictionary		 graphDictionary;

    protected CompositeDictionarySection shared	  = null;
    protected CompositeDictionarySection subjects = null;
    protected CompositeDictionarySection objects  = null;
    protected CompositeDictionarySection graphs	  = null;

    protected long			 maxId	  = -1;

    public ReificationDictionary(final TriplesDictionary bd, final GraphDictionary gd) {
	this.triplesDictionary = bd;
	this.graphDictionary = gd;
    }

    protected long getMaxId() {
	if (this.maxId == -1) {
	    this.maxId = Math.max(this.triplesDictionary.getNsubjects(), this.graphDictionary.getNobjects());
	}
	return this.maxId;
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.Dictionary#idToString(int, datatypes.TripleComponentRole)
     */
    @Override
    public CharSequence idToString(final int id, final TripleComponentRole position) {
	CharSequence string;
	if (id <= this.getMaxId()) {
	    string = this.triplesDictionary.idToString(id, position);
	} else {
	    string = this.graphDictionary.idToString(id, position);
	}
	return string;
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.Dictionary#stringToId(java.lang.CharSequence, datatypes.TripleComponentRole)
     */
    @Override
    public int stringToId(final CharSequence str, final TripleComponentRole position) {
	int ret = -1;
	if (str == null || str.length() == 0) {
	    ret = 0;
	} else if ((ret = this.triplesDictionary.stringToId(str, position)) == -1) {
	    ret = this.graphDictionary.stringToId(str, position);
	}
	return ret;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#getNumberOfElements()
     */
    @Override
    public long getNumberOfElements() {
	return this.triplesDictionary.getNumberOfElements() + this.graphDictionary.getNumberOfElements();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#size()
     */
    @Override
    public long size() {
	return this.triplesDictionary.size() + this.graphDictionary.size();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNsubjects()
     */
    @Override
    public long getNsubjects() {
	return this.triplesDictionary.getNsubjects() + this.graphDictionary.getNsubjects();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNpredicates()
     */
    @Override
    public long getNpredicates() {
	return this.triplesDictionary.getNpredicates();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.GraphDictionary#getNgraphs()
     */
    @Override
    public long getNgraphs() {
	return this.graphDictionary.getNumberOfElements();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNobjects()
     */
    @Override
    public long getNobjects() {
	return this.triplesDictionary.getNobjects() + this.graphDictionary.getNobjects();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNshared()
     */
    @Override
    public long getNshared() {
	return this.triplesDictionary.getNshared() + this.graphDictionary.getNshared();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getSubjects()
     */
    @Override
    public DictionarySection getSubjects() {
	if (this.subjects == null) {
	    this.subjects = new CompositeDictionarySection(this.triplesDictionary.getSubjects(), this.graphDictionary.getSubjects());
	}
	return this.subjects;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getPredicates()
     */
    @Override
    public DictionarySection getPredicates() {
	return this.triplesDictionary.getPredicates();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getObjects()
     */
    @Override
    public DictionarySection getObjects() {
	if (this.objects == null) {
	    this.objects = new CompositeDictionarySection(this.triplesDictionary.getObjects(), this.graphDictionary.getObjects());
	}
	return this.objects;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getShared()
     */
    @Override
    public DictionarySection getShared() {
	if (this.shared == null) {
	    this.shared = new CompositeDictionarySection(this.triplesDictionary.getShared(), this.graphDictionary.getShared());
	}
	return this.shared;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.GraphDictionary#getGraphs()
     */
    @Override
    public DictionarySection getGraphs() {
	if (this.graphs == null) {
	    this.graphs = new CompositeDictionarySection(this.graphDictionary.getShared(), this.graphDictionary.getSubjects(), this.graphDictionary.getObjects(), this.graphDictionary.getGraphs());
	}
	return this.graphs;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#populateHeader(org.rdfhdt.hdt.header.Header, java.lang.String)
     */
    @Override
    public void populateHeader(final Header header, final String rootNode) {
	// TODO implement
	throw new NotImplementedException();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#getType()
     */
    @Override
    public String getType() {
	return HDTVocabulary.DICTIONARY_TYPE_REIFICATION;
    }

    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
	this.triplesDictionary.close();
	this.graphDictionary.close();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#load(java.io.InputStream, org.rdfhdt.hdt.options.ControlInfo, org.rdfhdt.hdt.listener.ProgressListener)
     */
    @Override
    public void load(final InputStream input, final ControlInfo ci, final ProgressListener listener) throws IOException {
	this.triplesDictionary.load(input, ci, listener);
	this.graphDictionary.load(input, ci, listener);
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#mapFromFile(org.rdfhdt.hdt.util.io.CountInputStream, java.io.File, org.rdfhdt.hdt.listener.ProgressListener)
     */
    @Override
    public void mapFromFile(final CountInputStream in, final File f, final ProgressListener listener) throws IOException {
	this.triplesDictionary.mapFromFile(in, f, listener);
	this.graphDictionary.mapFromFile(in, f, listener);
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#load(org.rdfhdt.hdt.dictionary.TempDictionary, org.rdfhdt.hdt.listener.ProgressListener)
     */
    @Override
    public void load(final CompositeDictionary other, final ProgressListener listener) {
	this.triplesDictionary.load(other.getTriplesDictionary(), listener);
	this.graphDictionary.load(other.getGraphDictionary(), listener);
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#save(java.io.OutputStream, org.rdfhdt.hdt.options.ControlInfo, org.rdfhdt.hdt.listener.ProgressListener)
     */
    @Override
    public void save(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {
	this.triplesDictionary.save(output, ci, listener);
	this.graphDictionary.save(output, ci, listener);
    }

}