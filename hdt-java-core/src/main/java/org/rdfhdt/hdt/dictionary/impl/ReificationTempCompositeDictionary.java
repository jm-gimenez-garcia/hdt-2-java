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

package org.rdfhdt.hdt.dictionary.impl;

import java.io.IOException;
import java.util.Iterator;

import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.GraphDictionaryPrivate;
import org.rdfhdt.hdt.dictionary.GraphTempDictionary;
import org.rdfhdt.hdt.dictionary.CompositeTempDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionarySection;
import org.rdfhdt.hdt.dictionary.impl.section.HashDictionarySection;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.TempTriples;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.util.StopWatch;

/**
 * @author José M. Giménez-García
 *
 */
public class ReificationTempCompositeDictionary implements CompositeTempDictionary {

	protected TempDictionary bd;
	protected GraphTempDictionary gd;
	
	public ReificationTempCompositeDictionary(TempDictionary bd, GraphTempDictionary gd) {
		this.bd = bd;
		this.gd = gd;
	}
	
	public ReificationTempCompositeDictionary(TempDictionary bd, TempDictionary gd) {
		this(bd, new GraphTempDictionaryWrapper(gd));
	}
	
	/* (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#reorganize(hdt.triples.TempTriples)
	 */
	@Override
	public void reorganize(TempTriples triples) {
		bd.reorganize();
		gd.reorganize();
	}
	
	@Override
	public void startProcessing() {
		bd.startProcessing();
		gd.startProcessing();
	}
	
	@Override
	public void endProcessing() {
		bd.startProcessing();
		gd.startProcessing();
	}

	@Override
	public void close() throws IOException {
		bd.close();
		gd.close();
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
	public int insert(CharSequence str, TripleComponentRole position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reorganize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOrganized() {
		return bd.isOrganized() && gd.isOrganized();
	}

	@Override
	public void clear() {
		bd.clear();
		gd.clear();
	}

	@Override
	public int stringToId(CharSequence subject, TripleComponentRole role) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TempDictionarySection getGraphs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TempDictionary getNonReified() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphTempDictionary getReified() {
		// TODO Auto-generated method stub
		return null;
	}
}
