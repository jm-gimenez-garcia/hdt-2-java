/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/section/HashDictionarySection.java $
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

package org.rdfhdt.hdt.dictionary.impl.section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.rdfhdt.hdt.dictionary.TempDictionarySection;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.util.string.CharSequenceComparator;
import org.rdfhdt.hdt.util.string.CompactString;
import org.rdfhdt.hdt.util.string.ComparableCharSequence;

/**
 * @author mario.arias
 *
 */
public class HashDictionarySection implements TempDictionarySection {
    public static final int TYPE_INDEX = 1;

    private HashMap<ComparableCharSequence, Integer> map;
    private List<ComparableCharSequence>   list;
    private int size;
    boolean sorted;

    /**
     *
     */
    public HashDictionarySection() {
	this(new HDTSpecification());
    }

    public HashDictionarySection(final HDTOptions spec) {
	this.map = new HashMap<>();
	this.list = new ArrayList<>();
	this.size=0;
    }

    /* (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#locate(java.lang.CharSequence)
     */
    @Override
    public int locate(final CharSequence s) {
	final CompactString compact = new CompactString(s);
	final Integer val = this.map.get(compact);
	if(val==null) {
	    return 0;
	}
	return val.intValue();
    }

    /* (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#extract(int)
     */
    @Override
    public ComparableCharSequence extract(final int pos) {
	if(pos<=0) {
	    return null;
	}
	return this.list.get(pos-1);
    }

    /* (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#size()
     */
    @Override
    public long size() {
	return this.size;
    }

    /* (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#getNumberOfElements()
     */
    @Override
    public int getNumberOfElements() {
	return this.list.size();
    }

    /* (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#getEntries()
     */
    @Override
    public Iterator<ComparableCharSequence> getSortedEntries() {
	if(!this.sorted) {
	    return null;
	}
	return this.list.iterator();
    }

    @Override
    public Iterator<ComparableCharSequence> getEntries() {
	return this.list.iterator();
    }

    @Override
    public int add(final CharSequence entry) {
	final ComparableCharSequence compact = new CompactString(entry);
	final Integer pos = this.map.get(compact);
	if(pos!=null) {
	    // Found return existing ID.
	    return pos;
	}

	// Not found, insert new
	this.list.add(compact);
	this.map.put(compact, this.list.size());

	this.size+=compact.length();
	this.sorted = false;

	return this.list.size();
    }

    @Override
    public void remove(final CharSequence seq) {
	this.map.remove(seq);
	this.sorted = false;
    }

    @Override
    public void sort() {
	// Update list.
	this.list = new ArrayList<>(this.map.size());
	for (final ComparableCharSequence str : this.map.keySet()) {
	    this.list.add(str);
	}

	// Sort list
	Collections.sort(this.list, new CharSequenceComparator());

	// Update map indexes
	for(int i=1;i<=this.getNumberOfElements();i++) {
	    this.map.put(this.extract(i), i);
	}

	this.sorted = true;
    }

    @Override
    public boolean isSorted() {
	return this.sorted;
    }

    @Override
    public void clear() {
	this.list.clear();
	this.map.clear();
	this.size=0;
	this.sorted = false; //because if sorted won't be anymore
    }

    @Override
    public void close() throws IOException {
	this.map=null;
	this.list=null;
    }
}
