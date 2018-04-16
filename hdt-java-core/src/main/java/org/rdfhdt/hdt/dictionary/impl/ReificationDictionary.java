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
public class ReificationDictionary extends BaseReificationDictionary<BaseTriplesDictionary, BaseGraphDictionary> implements DictionaryPrivate<CompositeDictionary> {

    public ReificationDictionary(final BaseTriplesDictionary td, final BaseGraphDictionary gd) {
	super(td, gd);
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