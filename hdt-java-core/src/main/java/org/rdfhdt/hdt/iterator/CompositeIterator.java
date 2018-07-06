package org.rdfhdt.hdt.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CompositeIterator<E> implements Iterator<E> {

    List<Iterator<E>> iterators;

    public CompositeIterator(final List<Iterator<E>> iterators) {
	this.iterators = iterators;
    }

    public CompositeIterator(final Iterator<E>... iterators) {
	this(new LinkedList<>(Arrays.asList(iterators)));
    }

    @Override
    public boolean hasNext() {
	boolean hasNext = false;
	for (final Iterator<E> iterator : this.iterators) {
	    if ((hasNext = iterator.hasNext()) == true) break;
	}
	return hasNext;
    }

    @Override
    public E next() {
	final E next = this.iterators.get(0).next();
	if (!this.iterators.get(0).hasNext()) {
	    this.iterators.remove(0);
	}
	return next;
    }

}
