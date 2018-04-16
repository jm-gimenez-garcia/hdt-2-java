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

import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.triples.TempTriples;

/**
 * @author José M. Giménez-García
 *
 */
public class ReificationTempDictionary extends BaseReificationDictionary<BaseTempTriplesDictionary, BaseTempGraphDictionary> implements TempDictionary {

    public ReificationTempDictionary(final BaseTempTriplesDictionary td, final BaseTempGraphDictionary gd) {
	super(td, gd);
    }

    @Override
    public void reorganize() {

	// If a resource is at S and G, or O and G, then move it to the Graph Dictionary
	this.graphDictionary.getGraphs().getEntries().forEachRemaining(G -> {
	    int graphId = this.triplesDictionary.getSubjects().locate(G);
	    if (graphId > 0) {
		this.graphDictionary.getSubjects().add(G);
		this.triplesDictionary.getSubjects().remove(G);
	    }
	    graphId = this.triplesDictionary.getObjects().locate(G);
	    if (graphId > 0) {
		this.graphDictionary.getObjects().add(G);
		this.triplesDictionary.getObjects().remove(G);
	    }
	});

	// Then organize each section separately
	this.triplesDictionary.reorganize();
	this.graphDictionary.reorganize();
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.Dictionary#reorganize(hdt.triples.TempTriples)
     * The memory footprint of this method is horrible, please try to use reorganize() instead.
     */
    @Override
    public void reorganize(final TempTriples triples) {

	final DictionaryIDMapping mapSubjects = new DictionaryIDMapping(this.triplesDictionary.subjects.getNumberOfElements());
	final DictionaryIDMapping mapPredicates = new DictionaryIDMapping(this.triplesDictionary.predicates.getNumberOfElements());
	final DictionaryIDMapping mapObjects = new DictionaryIDMapping(this.triplesDictionary.objects.getNumberOfElements());
	final DictionaryIDMapping mapGraphs = new DictionaryIDMapping(this.graphDictionary.objects.getNumberOfElements());

	// Get the old IDs
	this.triplesDictionary.getSubjects().getEntries().forEachRemaining(S -> mapSubjects.add(S));
	this.triplesDictionary.getPredicates().getEntries().forEachRemaining(P -> mapPredicates.add(P));
	this.triplesDictionary.getObjects().getEntries().forEachRemaining(O -> mapObjects.add(O));
	this.graphDictionary.getGraphs().getEntries().forEachRemaining(G -> mapGraphs.add(G));

	// Reorganize
	this.reorganize();

	// Map old IDs to new IDs
	mapSubjects.getEntries().forEachRemaining(S -> S.setNewId(this.stringToId(S.getStr(), TripleComponentRole.SUBJECT)));
	mapPredicates.getEntries().forEachRemaining(P -> P.setNewId(this.stringToId(P.getStr(), TripleComponentRole.PREDICATE)));
	mapObjects.getEntries().forEachRemaining(O -> O.setNewId(this.stringToId(O.getStr(), TripleComponentRole.OBJECT)));
	mapGraphs.getEntries().forEachRemaining(G -> G.setNewId(this.stringToId(G.getStr(), TripleComponentRole.GRAPH)));

	// Change IDs in Triples
	triples.searchAll().forEachRemaining(T -> T.setAll(mapSubjects.getNewID(T.getSubject()), mapPredicates.getNewID(T.getPredicate()), mapObjects.getNewID(T.getObject())));
    }

    @Override
    public void close() throws IOException {
	this.triplesDictionary.close();
	this.graphDictionary.close();
    }

    @Override
    public int insert(final CharSequence str, final TripleComponentRole position) {
	if (position == TripleComponentRole.GRAPH) {
	    return this.graphDictionary.insert(str, position);
	} else {
	    return this.triplesDictionary.insert(str, position);
	}
    }

    @Override
    public boolean isOrganized() {
	return this.triplesDictionary.isOrganized() && this.graphDictionary.isOrganized();
    }

    @Override
    public void startProcessing() {
	this.triplesDictionary.startProcessing();
	this.graphDictionary.startProcessing();
    }

    @Override
    public void endProcessing() {
	this.triplesDictionary.endProcessing();
	this.graphDictionary.endProcessing();
    }

    @Override
    public void clear() {
	this.triplesDictionary.clear();
	this.graphDictionary.clear();
    }

}
