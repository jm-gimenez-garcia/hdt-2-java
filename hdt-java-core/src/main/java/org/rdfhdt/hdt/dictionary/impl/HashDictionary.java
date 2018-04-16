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
import java.util.Iterator;

import org.rdfhdt.hdt.dictionary.impl.section.HashDictionarySection;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.TempTriples;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.util.StopWatch;

/**
 * @author mario.arias, Eugen
 *
 */
public class HashDictionary extends BaseTempTriplesDictionary {

    public HashDictionary(final HDTOptions spec) {
	super(spec);

	// FIXME: Read types from spec
	this.subjects = new HashDictionarySection();
	this.predicates = new HashDictionarySection();
	this.objects = new HashDictionarySection();
	this.shared = new HashDictionarySection();
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.Dictionary#reorganize(hdt.triples.TempTriples)
     */
    @Override
    public void reorganize(final TempTriples triples) {
	final DictionaryIDMapping mapSubj = new DictionaryIDMapping(this.subjects.getNumberOfElements());
	final DictionaryIDMapping mapPred = new DictionaryIDMapping(this.predicates.getNumberOfElements());
	final DictionaryIDMapping mapObj = new DictionaryIDMapping(this.objects.getNumberOfElements());

	final StopWatch st = new StopWatch();

	// Generate old subject mapping
	final Iterator<? extends CharSequence> itSubj = this.subjects.getEntries();
	while (itSubj.hasNext()) {
	    final CharSequence str = itSubj.next();
	    mapSubj.add(str);

	    // GENERATE SHARED at the same time
	    if (str.length() > 0 && str.charAt(0) != '"' && this.objects.locate(str) != 0) {
		this.shared.add(str);
	    }
	}
	// System.out.println("Num shared: "+shared.getNumberOfElements()+" in "+st.stopAndShow());

	// Generate old predicate mapping
	st.reset();
	final Iterator<? extends CharSequence> itPred = this.predicates.getEntries();
	while (itPred.hasNext()) {
	    final CharSequence str = itPred.next();
	    mapPred.add(str);
	}

	// Generate old object mapping
	final Iterator<? extends CharSequence> itObj = this.objects.getEntries();
	while (itObj.hasNext()) {
	    final CharSequence str = itObj.next();
	    mapObj.add(str);
	}

	// Remove shared from subjects and objects
	final Iterator<? extends CharSequence> itShared = this.shared.getEntries();
	while (itShared.hasNext()) {
	    final CharSequence sharedStr = itShared.next();
	    this.subjects.remove(sharedStr);
	    this.objects.remove(sharedStr);
	}
	// System.out.println("Mapping generated in "+st.stopAndShow());

	// Sort sections individually
	st.reset();
	this.subjects.sort();
	this.predicates.sort();
	this.objects.sort();
	this.shared.sort();
	// System.out.println("Sections sorted in "+ st.stopAndShow());

	// Update mappings with new IDs
	st.reset();
	for (int j = 0; j < mapSubj.size(); j++) {
	    mapSubj.setNewID(j, this.stringToId(mapSubj.getString(j), TripleComponentRole.SUBJECT));
	    // System.out.print("Subj Old id: "+(j+1) + " New id: "+ mapSubj.getNewID(j)+ " STR: "+mapSubj.getString(j));
	}

	for (int j = 0; j < mapPred.size(); j++) {
	    mapPred.setNewID(j, this.stringToId(mapPred.getString(j), TripleComponentRole.PREDICATE));
	    // System.out.print("Pred Old id: "+(j+1) + " New id: "+ mapPred.getNewID(j)+ " STR: "+mapPred.getString(j));
	}

	for (int j = 0; j < mapObj.size(); j++) {
	    mapObj.setNewID(j, this.stringToId(mapObj.getString(j), TripleComponentRole.OBJECT));
	    // System.out.print("Obj Old id: "+(j+1) + " New id: "+ mapObj.getNewID(j)+ " STR: "+mapObj.getString(j));
	}
	// System.out.println("Update mappings in "+st.stopAndShow());

	// Replace old IDs with news
	st.reset();
	final Iterator<TripleID> iteratorTriples = triples.searchAll();
	while (iteratorTriples.hasNext()) {
	    final TripleID triple = iteratorTriples.next();
	    triples.update(triple,
		    mapSubj.getNewID(triple.getSubject() - 1),
		    mapPred.getNewID(triple.getPredicate() - 1),
		    mapObj.getNewID(triple.getObject() - 1));
	}
	// System.out.println("Replace IDs in "+st.stopAndShow());

	this.isOrganized = true;
    }

    @Override
    public void startProcessing() {
	// Do nothing.
    }

    @Override
    public void endProcessing() {
	// Do nothing.
    }

    @Override
    public void close() throws IOException {
	// Do nothing.
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNsubjects()
     */

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#size()
     */
    @Override
    public long size() {
	return this.subjects.size() + this.predicates.size() + this.objects.size() + this.shared.size();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNsubjects()
     */
    @Override
    public long getNsubjects() {
	return this.subjects.getNumberOfElements() + this.shared.getNumberOfElements();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNpredicates()
     */
    @Override
    public long getNpredicates() {
	return this.predicates.getNumberOfElements();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNobjects()
     */
    @Override
    public long getNobjects() {
	return this.objects.getNumberOfElements() + this.shared.getNumberOfElements();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.TriplesDictionary#getNshared()
     */
    @Override
    public long getNshared() {
	// TODO Auto-generated method stub
	return this.shared.getNumberOfElements();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#idToString(int, org.rdfhdt.hdt.enums.TripleComponentRole)
     */
    @Override
    public CharSequence idToString(final int id, final TripleComponentRole position) {
	// TODO Auto-generated method stub
	throw new NotImplementedException();
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#getNumberOfElements()
     */
    @Override
    public long getNumberOfElements() {
	return this.subjects.getNumberOfElements() + this.predicates.getNumberOfElements() + this.objects.getNumberOfElements() + this.shared.getNumberOfElements();
    }

}
