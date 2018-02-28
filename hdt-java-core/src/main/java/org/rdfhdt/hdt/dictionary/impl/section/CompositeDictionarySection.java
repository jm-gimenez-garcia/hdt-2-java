package org.rdfhdt.hdt.dictionary.impl.section;

import java.io.IOException;
import java.util.Iterator;

import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.iterator.CompositeSortedIterator;
import org.rdfhdt.hdt.util.string.ComparableCharSequence;

public class CompositeDictionarySection implements DictionarySection {

	protected DictionarySection[] sections;
	
	public CompositeDictionarySection(DictionarySection... sections) {
		this.sections = sections;
	}
	
	@Override
	public int locate(CharSequence s) {
		int ret = 0;
		for (DictionarySection section : sections) {
			ret = section.locate(s);
			if (ret != 0) break;
		}
		return ret;
	}

	@Override
	public CharSequence extract(int pos) {
		CharSequence ret = null;
		int maxPos = 0;
		for (DictionarySection section : sections) {
			maxPos += section.getNumberOfElements();
			if (maxPos >= pos) {
				ret = section.extract(pos);
				break;
			}
		}
		return ret;
	}

	@Override
	public long size() {
		int ret = 0;
		for (DictionarySection section : sections) {
			ret += section.size();
		}
		return ret;
	}

	@Override
	public int getNumberOfElements() {
		int ret = 0;
		for (DictionarySection section : sections) {
			ret += section.getNumberOfElements();
		}
		return ret;
	}

	@Override
	public Iterator<ComparableCharSequence> getSortedEntries() {
		@SuppressWarnings("unchecked")
		Iterator<ComparableCharSequence>[] iterators = new Iterator[sections.length];
		return new CompositeSortedIterator<ComparableCharSequence>(iterators);
	}

	@Override
	public void close() throws IOException {
		for (DictionarySection section : sections) {
			section.close();
		}
	}

}
