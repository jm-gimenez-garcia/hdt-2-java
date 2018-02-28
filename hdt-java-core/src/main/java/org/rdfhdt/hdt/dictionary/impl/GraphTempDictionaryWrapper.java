/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/BaseTempDictionary.java $
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

import org.rdfhdt.hdt.dictionary.GraphTempDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionarySection;
import org.rdfhdt.hdt.enums.DictionarySectionRole;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.TempTriples;

//import static org.rdfhdt.hdt.dictionary.impl.DictionaryWrapper.*;

/**
 * This abstract class implements all methods that have implementation
 * common to all modifiable dictionaries (or could apply to)
 * 
 * @author Eugen
 *
 */
public class GraphTempDictionaryWrapper implements GraphTempDictionary, DictionaryWrapper<TempDictionary> {
	
	protected TempDictionary bd;

	public GraphTempDictionaryWrapper(TempDictionary bd) {
		this.bd = bd;
	}
	
	/* (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#insert(java.lang.String, datatypes.TripleComponentRole)
	 */
	@Override
	public int insert(CharSequence str, TripleComponentRole position) {
		return bd.insert(str, translate(position));
	}

	@Override
	public void reorganize() {
		bd.reorganize();
	}
	
	@Override
	public void reorganize(TempTriples triples) {
		bd.reorganize(triples);
	}
	
	@Override
	public boolean isOrganized() {
		return bd.isOrganized();
	}

	@Override
	public void clear() {
		bd.clear();
	}
	
	@Override
	public TempDictionarySection getSubjects() {
		return bd.getSubjects();
	}
	
	@Override
	public TempDictionarySection getGraphs() {
		return bd.getPredicates();
	}

	@Override
	public TempDictionarySection getObjects() {
		return bd.getObjects();
	}

	@Override
	public TempDictionarySection getShared() {
		return bd.getShared();
	}
	
//	protected int getGlobalId(int id, DictionarySectionRole position) {
//		return bd.getGlobalId(id, translate(position));
//	}
	
	@Override
	public int stringToId(CharSequence str, TripleComponentRole position) {
		return bd.stringToId(str, translate(position));
	}

	@Override
	public void close() throws IOException {
		bd.close();
	}

	@Override
	public void startProcessing() {
		bd.startProcessing();
	}

	@Override
	public void endProcessing() {
		bd.endProcessing();
	}	
}
