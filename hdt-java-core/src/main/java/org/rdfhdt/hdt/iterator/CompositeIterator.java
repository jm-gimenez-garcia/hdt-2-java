package org.rdfhdt.hdt.iterator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class CompositeIterator<E> implements Iterator<E> {

	List<Iterator<E>> iterators;

	public CompositeIterator(List<Iterator<E>> iterators) {
		this.iterators = iterators;
	}
	
	public CompositeIterator(Iterator<E>... iterators) {
		this(Arrays.asList(iterators));
	}
	
	@Override
	public boolean hasNext() {
		boolean hasNext = false;
		for (Iterator<E> iterator : iterators) {
			if ((hasNext = iterator.hasNext()) == true) break;
		}
		return hasNext;
	}

	@Override
	public E next() {
		E next;
		if (iterators.get(0).hasNext()) {
			next = iterators.get(0).next();
		} else {
			iterators.remove(0);
			next = next();
		}
		return next;
	}

}
