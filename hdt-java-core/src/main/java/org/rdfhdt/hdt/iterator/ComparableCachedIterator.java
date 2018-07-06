package org.rdfhdt.hdt.iterator;

import java.util.Iterator;

public class ComparableCachedIterator<E extends Comparable<E>> extends CachedIterator<E> implements Comparable<ComparableCachedIterator<E>> {

    public ComparableCachedIterator(final Iterator<E> iterator) {
	super(iterator);
    }

    @Override
    public int compareTo(final ComparableCachedIterator<E> other) {
	return this.getNext().compareTo(other.getNext());
    }

}
