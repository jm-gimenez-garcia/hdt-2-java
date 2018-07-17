/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/iterator/DictionaryTranslateIterator.java $
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

package org.rdfhdt.hdt.iterator;

import org.rdfhdt.hdt.dictionary.DictionaryUtil;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.QuadString;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleString;

/**
 * Iterator of TripleStrings based on IteratorTripleID
 *
 */
public class DictionaryTranslateIterator implements IteratorTripleString {

    /** The iterator of TripleID */
    IteratorTripleID iterator;
    /** The dictionary */
    TriplesDictionary dictionary;

    CharSequence      s, p, o, g;

    int		      lastSid, lastPid, lastOid, lastGid;
    CharSequence      lastSstr, lastPstr, lastOstr, lastGstr;

    /**
     * Basic constructor
     *
     * @param iteratorTripleID
     *            Iterator of TripleID to be used
     * @param dictionary
     *            The dictionary to be used
     */
    public DictionaryTranslateIterator(final IteratorTripleID iteratorTripleID, final TriplesDictionary dictionary) {
	this.iterator = iteratorTripleID;
	this.dictionary = dictionary;
    }

    /**
     * Basic constructor
     *
     * @param iteratorTripleID
     *            Iterator of TripleID to be used
     * @param dictionary
     *            The dictionary to be used
     */
    public DictionaryTranslateIterator(final IteratorTripleID iteratorTripleID, final TriplesDictionary dictionary, final CharSequence s, final CharSequence p, final CharSequence o) {
	this.iterator = iteratorTripleID;
	this.dictionary = dictionary;
	this.s = s==null ? "" : s;
	this.p = p==null ? "" : p;
	this.o = o==null ? "" : o;
    }

    public DictionaryTranslateIterator(final IteratorTripleID iteratorTripleID, final TriplesDictionary dictionary, final CharSequence s, final CharSequence p, final CharSequence o,
	    final CharSequence g) {
	this.iterator = iteratorTripleID;
	this.dictionary = dictionary;
	this.s = s == null ? "" : s;
	this.p = p == null ? "" : p;
	this.o = o == null ? "" : o;
	this.g = g == null ? "" : g;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
	return this.iterator.hasNext();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public TripleString next() {
	final TripleID triple = this.iterator.next();
	// convert the tripleID to TripleString

	if(this.s.length()!=0) {
	    this.lastSstr = this.s;
	} else if(triple.getSubject()!=this.lastSid) {
	    this.lastSid = triple.getSubject();
	    this.lastSstr = this.dictionary.idToString(this.lastSid, TripleComponentRole.SUBJECT);
	}

	if(this.p.length()!=0) {
	    this.lastPstr = this.p;
	} else if(triple.getPredicate()!=this.lastPid) {
	    this.lastPstr = this.dictionary.idToString(triple.getPredicate(), TripleComponentRole.PREDICATE);
	    this.lastPid = triple.getPredicate();
	}

	if(this.o.length()!=0) {
	    this.lastOstr = this.o;
	} else if(triple.getObject()!=this.lastOid) {
	    this.lastOstr = this.dictionary.idToString(triple.getObject(), TripleComponentRole.OBJECT);
	    this.lastOid = triple.getObject();
	}

	if (triple instanceof QuadID) {
	    if (this.g.length() != 0) {
		this.lastGstr = this.g;
	    } else if (((QuadID) triple).getGraph() != this.lastGid) {
		this.lastGstr = this.dictionary.idToString(((QuadID) triple).getGraph(), TripleComponentRole.GRAPH);
		this.lastGid = ((QuadID) triple).getGraph();
	    }

	    return new QuadString(this.lastSstr, this.lastPstr, this.lastOstr, this.lastGstr);
	}

	return new TripleString(this.lastSstr, this.lastPstr, this.lastOstr);
	//		return DictionaryUtil.tripleIDtoTripleString(dictionary, triple);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
	this.iterator.remove();
    }

    /* (non-Javadoc)
     * @see hdt.iterator.IteratorTripleString#hasPrevious()
     */
    @Override
    public boolean hasPrevious() {
	return this.iterator.hasPrevious();
    }

    /* (non-Javadoc)
     * @see hdt.iterator.IteratorTripleString#previous()
     */
    @Override
    public TripleString previous() {
	final TripleID triple = this.iterator.previous();
	return DictionaryUtil.tripleIDtoTripleString(this.dictionary, triple);
    }

    /* (non-Javadoc)
     * @see hdt.iterator.IteratorTripleString#goToStart()
     */
    @Override
    public void goToStart() {
	this.iterator.goToStart();
    }

    @Override
    public long estimatedNumResults() {
	return this.iterator.estimatedNumResults();
    }

    @Override
    public ResultEstimationType numResultEstimation() {
	return this.iterator.numResultEstimation();
    }

}
