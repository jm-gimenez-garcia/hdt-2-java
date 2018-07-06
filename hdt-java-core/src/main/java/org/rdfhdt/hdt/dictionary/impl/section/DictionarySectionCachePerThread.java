/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/section/DictionarySectionCachePerThread.java $
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

package org.rdfhdt.hdt.dictionary.impl.section;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.dictionary.DictionarySectionPrivate;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.util.string.ComparableCharSequence;

/**
 * DictionarySection that caches results returned by a child DictionarySection to increase performance.
 * This one uses one cache per thread, to avoid waits on mutex when having concurrent queries.
 *
 * @author mario.arias
 *
 */
public class DictionarySectionCachePerThread implements DictionarySectionPrivate {

    final int					    CACHE_ENTRIES = 128;
    private final DictionarySectionPrivate	    child;

    private ThreadLocal<Map<CharSequence, Integer>> cacheString	  = new ThreadLocal<Map<CharSequence, Integer>>() {
	@SuppressWarnings("serial")
	@Override
	protected java.util.Map<CharSequence, Integer> initialValue() {
	    return new LinkedHashMap<CharSequence, Integer>(DictionarySectionCachePerThread.this.CACHE_ENTRIES + 1, .75F, true) {
		// This method is called just after a new entry has been
		// added
		@Override
		public boolean removeEldestEntry(
			final Map.Entry<CharSequence, Integer> eldest) {
		    return this
			    .size() > DictionarySectionCachePerThread.this.CACHE_ENTRIES;
		}
	    };
	};
    };

    private ThreadLocal<Map<Integer, CharSequence>> cacheID	  = new ThreadLocal<Map<Integer, CharSequence>>() {
	@SuppressWarnings("serial")
	@Override
	protected java.util.Map<Integer, CharSequence> initialValue() {
	    return new LinkedHashMap<Integer, CharSequence>(DictionarySectionCachePerThread.this.CACHE_ENTRIES + 1, .75F, true) {
		// This method is called just after a new entry has been
		// added
		@Override
		public boolean removeEldestEntry(
			final Map.Entry<Integer, CharSequence> eldest) {
		    return this
			    .size() > DictionarySectionCachePerThread.this.CACHE_ENTRIES;
		}
	    };
	};
    };

    public DictionarySectionCachePerThread(final DictionarySectionPrivate child) {
	this.child = child;
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#locate(java.lang.CharSequence)
     */
    @Override
    public int locate(final CharSequence s) {
	final Map<CharSequence, Integer> map = this.cacheString.get();
	Integer o = map.get(s);
	if (o == null) {
	    o = this.child.locate(s);
	    map.put(s, o);
	}
	return o;
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#extract(int)
     */
    @Override
    public CharSequence extract(final int pos) {
	final Map<Integer, CharSequence> map = this.cacheID.get();
	CharSequence o = map.get(pos);
	if (o == null) {
	    o = this.child.extract(pos);
	    map.put(pos, o);
	    // cacheString.put(o, pos);
	}
	return o;
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#size()
     */
    @Override
    public long size() {
	return this.child.size();
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#getNumberOfElements()
     */
    @Override
    public int getNumberOfElements() {
	return this.child.getNumberOfElements();
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#getEntries()
     */
    @Override
    public Iterator<ComparableCharSequence> getSortedEntries() {
	return this.child.getSortedEntries();
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#save(java.io.OutputStream, hdt.listener.ProgressListener)
     */
    @Override
    public void save(final OutputStream output, final ProgressListener listener)
	    throws IOException {
	this.child.save(output, listener);
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#load(java.io.InputStream, hdt.listener.ProgressListener)
     */
    @Override
    public void load(final InputStream input, final ProgressListener listener)
	    throws IOException {
	this.child.load(input, listener);

    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#load(hdt.dictionary.DictionarySection, hdt.listener.ProgressListener)
     */
    @Override
    public void load(final DictionarySection other, final ProgressListener listener) {
	this.child.load(other, listener);
    }

    @Override
    public void close() throws IOException {
	this.cacheString = null;
	this.cacheID = null;
	this.child.close();
    }
}
