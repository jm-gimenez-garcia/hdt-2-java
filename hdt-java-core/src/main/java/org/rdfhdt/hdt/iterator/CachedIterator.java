package org.rdfhdt.hdt.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CachedIterator<E> implements Iterator<E> {

    Iterator<E>	iterator;
    Optional<E>	currentValue = Optional.empty();
    Optional<E>	nextValue    = Optional.empty();

    public CachedIterator(final Iterator<E> iterator) {
	this.iterator = iterator;
	this.initialize();
    }

    @Override
    public boolean hasNext() {
	return this.nextValue.isPresent();
    }

    @Override
    public E next() {
	this.currentValue = Optional.of(this.nextValue.get());
	this.nextValue = this.iterator.hasNext() ? Optional.of(this.iterator.next()) : Optional.empty();
	return this.currentValue.orElseThrow(NoSuchElementException::new);
    }

    public E getCurrent() {
	return this.currentValue.get();
    }

    public E getNext() {
	return this.nextValue.orElseThrow(NoSuchElementException::new);
    }

    protected void initialize() {
	if (this.iterator.hasNext()) {
	    this.nextValue = Optional.of(this.iterator.next());
	}
    }

}
