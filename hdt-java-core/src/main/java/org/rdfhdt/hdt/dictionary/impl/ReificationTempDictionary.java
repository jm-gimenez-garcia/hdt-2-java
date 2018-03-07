/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/HashDictionary.java $
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

import java.io.IOException;

import org.rdfhdt.hdt.dictionary.CompositeDictionary;
import org.rdfhdt.hdt.dictionary.GraphDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionarySection;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.triples.TempTriples;

/**
 * @author José M. Giménez-García
 *
 */
public class ReificationTempDictionary implements CompositeDictionary, TempDictionary {

    protected BaseTempTriplesDictionary	triplesDictionary;
    protected BaseTempGraphDictionary	graphDictionary;

    public ReificationTempDictionary(final BaseTempTriplesDictionary td, final BaseTempGraphDictionary gd) {
	this.triplesDictionary = td;
	this.graphDictionary = gd;
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.Dictionary#reorganize(hdt.triples.TempTriples)
     */
    @Override
    public void reorganize(final TempTriples triples) {
	this.triplesDictionary.reorganize();
	this.graphDictionary.reorganize();
    }

    @Override
    public void startProcessing() {
	this.triplesDictionary.startProcessing();
	this.graphDictionary.startProcessing();
    }

    @Override
    public void endProcessing() {
	this.triplesDictionary.startProcessing();
	this.graphDictionary.startProcessing();
    }

    @Override
    public void close() throws IOException {
	this.triplesDictionary.close();
	this.graphDictionary.close();
    }

    @Override
    public TempDictionarySection getSubjects() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public TempDictionarySection getPredicates() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public TempDictionarySection getObjects() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public TempDictionarySection getShared() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int insert(final CharSequence str, final TripleComponentRole position) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void reorganize() {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean isOrganized() {
	return this.triplesDictionary.isOrganized() && this.graphDictionary.isOrganized();
    }

    @Override
    public void clear() {
	this.triplesDictionary.clear();
	this.graphDictionary.clear();
    }

    @Override
    public int stringToId(final CharSequence subject, final TripleComponentRole role) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public TempDictionarySection getGraphs() {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNsubjects()
     */
    @Override
    public long getNsubjects() {
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNpredicates()
     */
    @Override
    public long getNpredicates() {
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNobjects()
     */
    @Override
    public long getNobjects() {
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNshared()
     */
    @Override
    public long getNshared() {
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#idToString(int, org.rdfhdt.hdt.enums.TripleComponentRole)
     */
    @Override
    public CharSequence idToString(final int id, final TripleComponentRole position) {
	// TODO Auto-generated method stub
	return null;
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
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#populateHeader(org.rdfhdt.hdt.header.Header, java.lang.String)
     */
    @Override
    public void populateHeader(final Header header, final String rootNode) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#getType()
     */
    @Override
    public String getType() {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.GraphDictionary#getNgraphs()
     */
    @Override
    public long getNgraphs() {
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.CompositeDictionary#getTriplesDictionary()
     */
    @Override
    public TriplesDictionary getTriplesDictionary() {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.CompositeDictionary#getGraphDictionary()
     */
    @Override
    public GraphDictionary getGraphDictionary() {
	// TODO Auto-generated method stub
	return null;
    }
}
