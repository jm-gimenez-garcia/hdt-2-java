package org.rdfhdt.hdt.dictionary.impl.section;

import java.io.IOException;
import java.util.Iterator;

import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.iterator.CompositeSortedIterator;
import org.rdfhdt.hdt.util.string.ComparableCharSequence;

/**
 * @author José M. Giménez-García
 *
 */
public class CompositeDictionarySection implements DictionarySection {

    protected DictionarySection[] sections;

    public CompositeDictionarySection(final DictionarySection... sections) {
	this.sections = sections;
    }

    @Override
    public int locate(final CharSequence s) {
	int ret = 0;
	for (final DictionarySection section : this.sections) {
	    ret = section.locate(s);
	    if (ret != 0) break;
	}
	return ret;
    }

    @Override
    public CharSequence extract(final int pos) {
	CharSequence ret = null;
	int maxPos = 0;
	for (final DictionarySection section : this.sections) {
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
	for (final DictionarySection section : this.sections) {
	    ret += section.size();
	}
	return ret;
    }

    @Override
    public int getNumberOfElements() {
	int ret = 0;
	for (final DictionarySection section : this.sections) {
	    ret += section.getNumberOfElements();
	}
	return ret;
    }

    @Override
    public Iterator<ComparableCharSequence> getSortedEntries() {
	@SuppressWarnings("unchecked")
	final Iterator<ComparableCharSequence>[] iterators = new Iterator[this.sections.length];
	for (int i = 0; i < this.sections.length; i++) {
	    iterators[i] = this.sections[i].getSortedEntries();
	}
	return new CompositeSortedIterator<>(iterators);
    }

    @Override
    public void close() throws IOException {
	for (final DictionarySection section : this.sections) {
	    section.close();
	}
    }

}
