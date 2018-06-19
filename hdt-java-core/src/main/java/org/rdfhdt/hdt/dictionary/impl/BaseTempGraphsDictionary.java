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

import java.util.Iterator;

import org.rdfhdt.hdt.dictionary.GraphsDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionarySection;
import org.rdfhdt.hdt.enums.DictionarySectionRole;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.TempTriples;

/**
 * This abstract class implements all methods that have implementation
 * common to all modifiable dictionaries (or could apply to)
 *
 * @author José M. Giménez-García, Eugen
 *
 */
public abstract class BaseTempGraphsDictionary implements TempDictionary, GraphsDictionary {

    HDTOptions			    spec;
    protected boolean		    isOrganized;

    protected TempDictionarySection subjects;
    protected TempDictionarySection graphs;
    protected TempDictionarySection objects;
    protected TempDictionarySection shared;

    public BaseTempGraphsDictionary(final HDTOptions spec) {
	this.spec = spec;
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.Dictionary#insert(java.lang.String, datatypes.TripleComponentRole)
     */
    @Override
    public int insert(final CharSequence str, final TripleComponentRole position) {
	int returnValue;
	switch (position) {
	    case SUBJECT:
		returnValue = this.subjects.add(str);
		this.isOrganized &= this.subjects.isSorted();
		break;
	    case OBJECT:
		returnValue = this.objects.add(str);
		this.isOrganized &= this.objects.isSorted();
		break;
	    case GRAPH:
		returnValue = this.graphs.add(str);
		this.isOrganized &= this.graphs.isSorted();
		break;
	    default:
		throw new IllegalArgumentException();
	}
	return returnValue;
    }

    @Override
    public void reorganize() {

	// Generate shared
	final Iterator<? extends CharSequence> itSubj = this.subjects.getEntries();
	while (itSubj.hasNext()) {
	    final CharSequence str = itSubj.next();

	    // FIXME: These checks really needed?
	    if (str.length() > 0 && str.charAt(0) != '"' && this.objects.locate(str) != 0) {
		this.shared.add(str);
	    }
	}

	// Remove shared from subjects and objects
	final Iterator<? extends CharSequence> itShared = this.shared.getEntries();
	while (itShared.hasNext()) {
	    final CharSequence sharedStr = itShared.next();
	    this.subjects.remove(sharedStr);
	    this.objects.remove(sharedStr);
	}

	// Sort sections individually
	this.shared.sort();
	this.subjects.sort();
	this.objects.sort();
	this.graphs.sort();

	this.isOrganized = true;

    }

    /**
     * This method is used in the one-pass way of working in which case it
     * should not be used with a disk-backed dictionary because remapping
     * requires practically a copy of the dictionary which is very bad...
     * (it is ok for in-memory and they should override and write implementation)
     */
    @Override
    public void reorganize(final TempTriples triples) {
	throw new NotImplementedException();
    }

    @Override
    public boolean isOrganized() {
	return this.isOrganized;
    }

    @Override
    public void clear() {
	this.subjects.clear();
	this.graphs.clear();
	this.shared.clear();
	this.objects.clear();
    }

    @Override
    public TempDictionarySection getSubjects() {
	return this.subjects;
    }

    @Override
    public TempDictionarySection getGraphs() {
	return this.graphs;
    }

    @Override
    public TempDictionarySection getObjects() {
	return this.objects;
    }

    @Override
    public TempDictionarySection getShared() {
	return this.shared;
    }

    protected int getGlobalId(final int id, final DictionarySectionRole position) {
	switch (position) {
	    case SUBJECT:
	    case OBJECT:
		return this.shared.getNumberOfElements() + id;

	    case GRAPH:
	    case SHARED:
		return id;
	    default:
		throw new IllegalArgumentException();
	}
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.Dictionary#stringToId(java.lang.CharSequence, datatypes.TripleComponentRole)
     */
    @Override
    public int stringToId(final CharSequence str, final TripleComponentRole position) {

	if (str == null || str.length() == 0) { return 0; }

	int ret = 0;
	switch (position) {
	    case SUBJECT:
		ret = this.shared.locate(str);
		if (ret != 0) { return this.getGlobalId(ret, DictionarySectionRole.SHARED); }
		ret = this.subjects.locate(str);
		if (ret != 0) { return this.getGlobalId(ret, DictionarySectionRole.SUBJECT); }
		return -1;
	    case GRAPH:
		ret = this.graphs.locate(str);
		if (ret != 0) { return this.getGlobalId(ret, DictionarySectionRole.PREDICATE); }
		return -1;
	    case OBJECT:
		ret = this.shared.locate(str);
		if (ret != 0) { return this.getGlobalId(ret, DictionarySectionRole.SHARED); }
		ret = this.objects.locate(str);
		if (ret != 0) { return this.getGlobalId(ret, DictionarySectionRole.OBJECT); }
		return -1;
	    default:
		throw new IllegalArgumentException();
	}
    }
}
