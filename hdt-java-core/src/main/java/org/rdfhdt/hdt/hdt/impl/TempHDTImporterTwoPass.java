/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/hdt/impl/TempHDTImporterTwoPass.java $
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

import java.io.File;
import java.io.IOException;

import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.TempHDT;
import org.rdfhdt.hdt.hdt.TempHDTImporter;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.rdf.RDFParserCallback;
import org.rdfhdt.hdt.rdf.RDFParserCallback.RDFCallback;
import org.rdfhdt.hdt.rdf.RDFParserFactory;
import org.rdfhdt.hdt.triples.QuadString;
import org.rdfhdt.hdt.triples.TempTriples;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.triples.TriplesFactory;
import org.rdfhdt.hdt.triples.impl.QuadsList;
import org.rdfhdt.hdt.util.RDFInfo;
import org.rdfhdt.hdt.util.listener.ListenerUtil;

public class TempHDTImporterTwoPass implements TempHDTImporter {

	class DictionaryAppender implements RDFCallback {

		TempDictionary	 dict;
		ProgressListener listener;
		long		 count;

		DictionaryAppender(final TempDictionary dict, final ProgressListener listener) {
			this.dict = dict;
			this.listener = listener;
		}

		@Override
		public void processTriple(final TripleString triple, final long pos) {
			System.out.println("processing triple [" + triple.toString() + "]  to append to dictionary");
			this.dict.insert(triple.getSubject(), TripleComponentRole.SUBJECT);
			this.dict.insert(triple.getPredicate(), TripleComponentRole.PREDICATE);
			this.dict.insert(triple.getObject(), TripleComponentRole.OBJECT);
			this.count++;
			ListenerUtil.notifyCond(this.listener, "Generating dictionary " + this.count + " triples processed.", this.count, 0, 100);
		}

		@Override
		public void processQuad(final QuadString quad, final long pos) {
			System.out.println("processing quad [" + quad.toString() + "]  to append to dictionary");
			processTriple(quad, pos);
			this.dict.insert(quad.getGraph(), TripleComponentRole.GRAPH);
		}

		public long getCount() {
			return this.count;
		}
	};

	/**
	 * Warning: different from HDTConverterOnePass$TripleAppender
	 * This one uses dict.stringToID, the other uses dict.insert
	 *
	 * @author mario.arias
	 *
	 */
	class TripleAppender2 implements RDFCallback {
		TempDictionary	 dict;
		TempTriples	 triples;
		ProgressListener listener;
		long		 count;

		public TripleAppender2(final TempDictionary dict, final TempTriples triples, final ProgressListener listener) {
			this.dict = dict;
			this.triples = triples;
			this.listener = listener;
		}

		@Override
		public void processTriple(final TripleString triple, final long pos) {
			System.out.println("processing triple [" + triple.toString() + "] to append to triples");
			final int s = this.dict.stringToId(triple.getSubject(), TripleComponentRole.SUBJECT);
			final int p = this.dict.stringToId(triple.getPredicate(), TripleComponentRole.PREDICATE);
			final int o = this.dict.stringToId(triple.getObject(), TripleComponentRole.OBJECT);
			this.triples.insert(s, p, o);
			this.count++;
			ListenerUtil.notifyCond(this.listener, "Generating triples " + this.count + " triples processed.", this.count, 0, 100);
		}

		@Override
		public void processQuad(final QuadString quad, final long pos) {
			System.out.println("processing quad [" + quad.toString() + "] to append to triples");
			final int s =  this.dict.stringToId(quad.getSubject(), TripleComponentRole.SUBJECT);
			final int p = this.dict.stringToId(quad.getPredicate(), TripleComponentRole.PREDICATE);
			final int o = this.dict.stringToId(quad.getObject(), TripleComponentRole.OBJECT);
			final int g = this.dict.stringToId(quad.getGraph(), TripleComponentRole.GRAPH);
			((QuadsList) this.triples).insert(s, p, o, g);
			this.count++;
			ListenerUtil.notifyCond(this.listener, "Generating triples " + this.count + " triples processed.", this.count, 0, 100);
		}
	}

	protected boolean reif;;

	/**
	 * @param reif
	 */
	public TempHDTImporterTwoPass(final boolean reif) {
		this.reif = reif;
	}

	@Override
	public TempHDT loadFromRDF(final HDTOptions specs, final String filename, final String baseUri, final RDFNotation notation, final ProgressListener listener)
			throws IOException, ParserException {

		final RDFParserCallback parser = RDFParserFactory.getParserCallback(notation);

		// Fill the specs with missing properties
		if (!RDFInfo.triplesSet(specs) &&
				TriplesFactory.TEMP_TRIPLES_IMPL_LIST.equals(specs.get("tempTriples.impl"))) {
			// count lines if not user-set and if triples in-mem (otherwise not important info)
			RDFInfo.setTriples(RDFInfo.countLines(filename, parser, notation), specs);
			// FIXME setting numberOfLines costs (counting them) but saves memory... what to do??
			// especially because in two-pass they are counter by DictionaryAppender (but triples object
			// is instantiated earlier)
		}
		RDFInfo.setSizeInBytes(new File(filename).length(), specs); // else just get sizeOfRDF

		// Create Modifiable Instance and parser
		final TempHDT modHDT = new TempHDTImpl(specs, baseUri, ModeOfLoading.TWO_PASS, this.reif);
		final TempDictionary dictionary = modHDT.getDictionary();
		final TempTriples triples = modHDT.getTriples();

		// Load RDF in the dictionary
		dictionary.startProcessing();
		parser.doParse(filename, baseUri, notation, new DictionaryAppender(dictionary, listener));
		dictionary.endProcessing();

		System.out.println("Temp dictionary created succesfully");

		// Reorganize IDs before loading triples
		modHDT.reorganizeDictionary(listener);

		// Load triples (second pass)
		parser.doParse(filename, baseUri, notation, new TripleAppender2(dictionary, triples, listener));

		// reorganize HDT
		modHDT.reorganizeTriples(listener);

		System.out.println("Temp triples created succesfully");

		return modHDT;
	}
}
