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

import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.dictionary.TempDictionarySection;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.iterator.CompositeIterator;
import org.rdfhdt.hdt.iterator.CompositeSortedIterator;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.util.string.CharSequenceComparator;
import org.rdfhdt.hdt.util.string.CompactString;
import org.rdfhdt.hdt.util.string.ComparableCharSequence;

/**
 * @author José M. Giménez-García
 *
 */
public class CompositeTempDictionarySection extends CompositeDictionarySection implements TempDictionarySection {

	public CompositeTempDictionarySection(DictionarySection... sections) {
		for (DictionarySection section : sections) {
			if (!(section instanceof TempDictionarySection)) {
				throw new IllegalFormatException("Trying to create a Composite Temporary Dictionary Section from non-temporary section(s) ");
			}
		}
		this.sections = sections;
	}
	
	@Override
	public int add(CharSequence str) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void remove(CharSequence str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sort() {
		for (DictionarySection section : sections) {
			((TempDictionarySection) section).sort();
		}
	}

	@Override
	public void clear() {
		for (DictionarySection section : sections) {
			((TempDictionarySection) section).clear();
		}
	}

	@Override
	public boolean isSorted() {
		boolean isSorted = true;
		for (DictionarySection section : sections) {
			if (!((TempDictionarySection) section).isSorted()) {
				isSorted = false;
				break;
			}
		}
		return isSorted;
	}

	@Override
	public Iterator<CharSequence> getEntries() {
		Iterator<CharSequence>[] iterators = new Iterator[sections.length];
		for (int i = 0; i < sections.length; i++) {
			iterators[i] = ((TempDictionarySection) sections[i]).getEntries();
		}
		return new CompositeIterator<CharSequence>(iterators);
	}
	
	
}
