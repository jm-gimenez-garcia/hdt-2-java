package org.rdfhdt.hdt.iterator;

import java.util.Iterator;
import java.util.Optional;

public class CachedIterator<E> implements Iterator<E> {

	Iterator<E> iterator;
	Optional<E> cachedValue = Optional.empty();
	
	public CachedIterator(Iterator<E> iterator) {
		this.iterator = iterator;
		initialize();
	}
	
	@Override
	public boolean hasNext() {
		return cachedValue.isPresent();
	}

	@Override
	public E next() {
		E next = cachedValue.get();
		cachedValue = iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
		return next;
	}
	
	public E current() {
		return cachedValue.get();
	}
	
	protected void initialize() {
		if (iterator.hasNext()) {
			cachedValue = Optional.of(iterator.next());
		}
	}

}
