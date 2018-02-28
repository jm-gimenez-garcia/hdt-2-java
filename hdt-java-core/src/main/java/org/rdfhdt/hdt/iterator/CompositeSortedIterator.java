package org.rdfhdt.hdt.iterator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class CompositeSortedIterator<E extends Comparable<E>> extends CompositeIterator<E> {

	List<ComparableCachedIterator<E>> iterators;

	public CompositeSortedIterator(List<Iterator<E>> iterators) {
		this.iterators = new ArrayList<ComparableCachedIterator<E>>();
		iterators.forEach(I -> this.iterators.add((new ComparableCachedIterator<E>(I))));
	}
	
	public CompositeSortedIterator(Iterator<E>... iterators) {
		this(Arrays.asList(iterators));
	}

	@Override
	public E next() {
		Collections.sort(iterators);
		return super.next();
	}

}
