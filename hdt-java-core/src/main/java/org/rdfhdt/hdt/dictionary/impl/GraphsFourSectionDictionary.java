/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/FourSectionDictionary.java $
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

import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.dictionary.DictionarySectionPrivate;
import org.rdfhdt.hdt.dictionary.GraphsDictionary;
import org.rdfhdt.hdt.dictionary.impl.section.DictionarySectionFactory;
import org.rdfhdt.hdt.dictionary.impl.section.PFCDictionarySection;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInfo.Type;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.listener.IntermediateListener;

/**
 * @author mario.arias, José M. Giménez-García
 *
 */
public class GraphsFourSectionDictionary extends BaseGraphsDictionary {

	public GraphsFourSectionDictionary(final HDTOptions spec,
			final DictionarySectionPrivate s, final DictionarySectionPrivate o, final DictionarySectionPrivate sh, final DictionarySectionPrivate g) {
		super(spec);
		this.subjects = s;
		this.objects = o;
		this.shared = sh;
		this.graphs = g;
	}

	public GraphsFourSectionDictionary(final HDTOptions spec) {
		super(spec);
		// FIXME: Read type from spec.
		this.subjects = new PFCDictionarySection(spec);
		this.objects = new PFCDictionarySection(spec);
		this.shared = new PFCDictionarySection(spec);
		this.graphs = new PFCDictionarySection(spec);
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#load(hdt.dictionary.Dictionary)
	 */
	@Override
	public void load(final Dictionary other, final ProgressListener listener) {
		final IntermediateListener iListener = new IntermediateListener(listener);
		this.subjects.load(other.getSubjects(), iListener);
		this.objects.load(other.getObjects(), iListener);
		this.shared.load(other.getShared(), iListener);
		if (other instanceof GraphsDictionary) {
			this.graphs.load(((GraphsDictionary) other).getGraphs(), iListener);
		} else {
			System.out.println("WARNING: Trying to load a non-graphs dictionary into a graphs dictionary");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#save(java.io.OutputStream, hdt.ControlInformation, hdt.ProgressListener)
	 */
	@Override
	public void save(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {
		ci.clear();
		ci.setType(Type.DICTIONARY);
		ci.setFormat(HDTVocabulary.DICTIONARY_TYPE_FOUR_SECTION);
		ci.setInt("elements", getNumberOfElements());
		ci.save(output);

		final IntermediateListener iListener = new IntermediateListener(listener);
		this.shared.save(output, iListener);
		this.subjects.save(output, iListener);
		this.objects.save(output, iListener);
		this.graphs.save(output, iListener);
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#load(java.io.InputStream)
	 */
	@Override
	public void load(final InputStream input, final ControlInfo ci, final ProgressListener listener) throws IOException {
		if (ci.getType() != ControlInfo.Type.DICTIONARY) { throw new IllegalFormatException("Trying to read a dictionary section, but was not dictionary."); }

		final IntermediateListener iListener = new IntermediateListener(listener);

		this.shared = DictionarySectionFactory.loadFrom(input, iListener);
		this.subjects = DictionarySectionFactory.loadFrom(input, iListener);
		this.objects = DictionarySectionFactory.loadFrom(input, iListener);
		this.graphs = DictionarySectionFactory.loadFrom(input, iListener);
	}

	@Override
	public void mapFromFile(final CountInputStream in, final File f, final ProgressListener listener) throws IOException {
		final ControlInformation ci = new ControlInformation();
		ci.load(in);
		if (ci.getType() != ControlInfo.Type.DICTIONARY) { throw new IllegalFormatException("Trying to read a dictionary section, but was not dictionary."); }

		final IntermediateListener iListener = new IntermediateListener(listener);
		this.shared = DictionarySectionFactory.loadFrom(in, f, iListener);
		this.subjects = DictionarySectionFactory.loadFrom(in, f, iListener);
		this.objects = DictionarySectionFactory.loadFrom(in, f, iListener);
		this.graphs = DictionarySectionFactory.loadFrom(in, f, iListener);

		// Use cache only for predicates. Preload only up to 100K predicates.
		// FIXME: DISABLED
		// predicates = new DictionarySectionCacheAll(predicates, predicates.getNumberOfElements()<100000);
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#populateHeader(hdt.header.Header, java.lang.String)
	 */
	@Override
	public void populateHeader(final Header header, final String rootNode) {
		header.insert(rootNode, HDTVocabulary.DICTIONARY_TYPE, HDTVocabulary.DICTIONARY_TYPE_FOUR_SECTION);
		header.insert(rootNode, HDTVocabulary.DICTIONARY_NUMSUBJECTS, getNsubjects());
		header.insert(rootNode, HDTVocabulary.DICTIONARY_NUMOBJECTS, getNobjects());
		header.insert(rootNode, HDTVocabulary.DICTIONARY_NUMSHARED, getNshared());
		header.insert(rootNode, HDTVocabulary.DICTIONARY_NUMGRAPHS, getNgraphs());
		// header.insert(rootNode, HDTVocabulary.DICTIONARY_MAXSUBJECTID, getMaxSubjectID());
		// header.insert(rootNode, HDTVocabulary.DICTIONARY_MAXPREDICATEID, getMaxPredicateID());
		// header.insert(rootNode, HDTVocabulary.DICTIONARY_MAXOBJECTTID, getMaxObjectID());
		header.insert(rootNode, HDTVocabulary.DICTIONARY_SIZE_STRINGS, size());
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#getType()
	 */
	@Override
	public String getType() {
		return HDTVocabulary.DICTIONARY_TYPE_FOUR_SECTION;
	}

	@Override
	public void close() throws IOException {
		this.shared.close();
		this.subjects.close();
		this.objects.close();
		this.graphs.close();
	}
}
