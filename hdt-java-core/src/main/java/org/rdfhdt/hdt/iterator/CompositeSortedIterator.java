package org.rdfhdt.hdt.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CompositeSortedIterator<E extends Comparable<E>> extends CompositeIterator<E> {

    // List<ComparableCachedIterator<E>> iterators;

    public CompositeSortedIterator(final List<Iterator<E>> iterators) {
	this.iterators = new ArrayList<>();
	iterators.forEach(I -> this.iterators.add((new ComparableCachedIterator<>(I))));
    }

    public CompositeSortedIterator(final Iterator<E>... iterators) {
	this(Arrays.asList(iterators));
    }

    @Override
    public E next() {
	this.iterators.sort(null);
	return super.next();
    }

}
