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
import org.rdfhdt.hdt.dictionary.CompositeDictionaryPrivate;
import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.dictionary.GraphsDictionary;
import org.rdfhdt.hdt.dictionary.GraphsDictionaryPrivate;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.dictionary.TriplesDictionaryPrivate;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInfo.Type;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.util.io.CountInputStream;

/**
 * @author José M. Giménez-García
 *
 */
public class ReificationDictionary extends BaseReificationDictionary<BaseTriplesDictionary, BaseGraphsDictionary> implements CompositeDictionaryPrivate {

	public ReificationDictionary(final BaseTriplesDictionary td, final BaseGraphsDictionary gd) {
		super(td, gd);
	}

	@Override
	public TriplesDictionaryPrivate getTriplesDictionary() {
		return this.triplesDictionary;
	}

	@Override
	public GraphsDictionaryPrivate getGraphsDictionary() {
		return this.graphDictionary;
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.dictionary.Dictionary#populateHeader(org.rdfhdt.hdt.header.Header, java.lang.String)
	 */
	@Override
	public void populateHeader(final Header header, final String rootNode) {
		final String dictionaryTriples = "_:dictionaryTriples";
		final String dictionaryGraphs = "_:dictionaryGraphs";

		header.insert(rootNode, HDTVocabulary.DICTIONARY_TYPE, HDTVocabulary.DICTIONARY_TYPE_REIFICATION);
		header.insert(rootNode, HDTVocabulary.DICTIONARY_NUMSUBJECTS, getNsubjects());
		header.insert(rootNode, HDTVocabulary.DICTIONARY_NUMPREDICATES, getNpredicates());
		header.insert(rootNode, HDTVocabulary.DICTIONARY_NUMOBJECTS, getNobjects());
		header.insert(rootNode, HDTVocabulary.DICTIONARY_NUMSHARED, getNshared());
		header.insert(rootNode, HDTVocabulary.DICTIONARY_SIZE_STRINGS, size());
		header.insert(rootNode, HDTVocabulary.HDT_DICTIONARY_TRIPLES, dictionaryTriples);
		header.insert(rootNode, HDTVocabulary.HDT_DICTIONARY_TRIPLES, dictionaryGraphs);

		getTriplesDictionary().populateHeader(header, dictionaryTriples);
		getGraphsDictionary().populateHeader(header, dictionaryGraphs);
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
		if (ci.getType() != ControlInfo.Type.DICTIONARY) {
			throw new IllegalFormatException("Trying to read a dictionary, but was not dictionary.");
		}
		ci.load(input);
		this.triplesDictionary.load(input, ci, listener);
		ci.load(input);
		this.graphDictionary.load(input, ci, listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#mapFromFile(org.rdfhdt.hdt.util.io.CountInputStream, java.io.File, org.rdfhdt.hdt.listener.ProgressListener)
	 */
	@Override
	public void mapFromFile(final CountInputStream in, final File f, final ProgressListener listener) throws IOException {
		final ControlInformation ci = new ControlInformation();
		ci.load(in);
		if (ci.getType() != ControlInfo.Type.DICTIONARY) {
			throw new IllegalFormatException("Trying to read a dictionary, but was not dictionary.");
		}
		this.triplesDictionary.mapFromFile(in, f, listener);
		// ci.load(in);
		this.graphDictionary.mapFromFile(in, f, listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#load(org.rdfhdt.hdt.dictionary.TempDictionary, org.rdfhdt.hdt.listener.ProgressListener)
	 */
	@Override
	public void load(final Dictionary other, final ProgressListener listener) {
		if (other instanceof CompositeDictionary) {
			this.triplesDictionary.load(((CompositeDictionary) other).getTriplesDictionary(), listener);
			this.graphDictionary.load(((CompositeDictionary) other).getGraphsDictionary(), listener);
		} else {
			if (other instanceof TriplesDictionary) {
				this.triplesDictionary.load(other, listener);
			}
			if (other instanceof GraphsDictionary) {
				this.graphDictionary.load(other, listener);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#save(java.io.OutputStream, org.rdfhdt.hdt.options.ControlInfo, org.rdfhdt.hdt.listener.ProgressListener)
	 */
	@Override
	public void save(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {
		ci.clear();
		ci.setType(Type.DICTIONARY);
		ci.setFormat(HDTVocabulary.DICTIONARY_TYPE_REIFICATION);
		ci.save(output);
		this.triplesDictionary.save(output, ci, listener);
		this.graphDictionary.save(output, ci, listener);
	}

}