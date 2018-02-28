package org.rdfhdt.hdt.iterator;

import java.util.Iterator;

public class ComparableCachedIterator<E extends Comparable<E>> extends CachedIterator<E> implements Comparable<ComparableCachedIterator<E>> {

	public ComparableCachedIterator(Iterator<E> iterator) {
		super(iterator);
	}

	@Override
	public int compareTo(ComparableCachedIterator<E> other) {
		return this.current().compareTo(other.current());
	}

}
