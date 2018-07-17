/**
 *
 */
package org.rdfhdt.hdt.triples.impl;

import java.util.function.BiFunction;

import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleTranslatorImpl;

/**
 * @author José M. Giménez-García
 *
 */
public class TripleTranslatorIteratorWrapper extends TripleTranslatorImpl implements IteratorTripleID {

	IteratorTripleID iteratorTripleID;

	public TripleTranslatorIteratorWrapper(final IteratorTripleID iteratorTripleID) {
		this.iteratorTripleID = iteratorTripleID;
	}

	public TripleTranslatorIteratorWrapper(final IteratorTripleID iteratorTripleID, final BiFunction<Integer, TripleComponentRole, Integer> toGlobalID) {
		this(iteratorTripleID);
		this.toGlobalID = toGlobalID;
	}

	public TripleTranslatorIteratorWrapper(final BitmapTriples bitmapTriples, final IteratorTripleID iteratorTripleID) {
		this(iteratorTripleID);
		this.toGlobalID = bitmapTriples.getToGlobalIDFunction();
	}

	@Override
	public boolean hasNext() {
		return this.iteratorTripleID.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public TripleID next() {
		return toGlobalIDs(this.iteratorTripleID.next());
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#hasPrevious()
	 */
	@Override
	public boolean hasPrevious() {
		return this.iteratorTripleID.hasPrevious();
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#previous()
	 */
	@Override
	public TripleID previous() {
		return toGlobalIDs(this.iteratorTripleID.previous());
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#goToStart()
	 */
	@Override
	public void goToStart() {
		this.iteratorTripleID.goToStart();
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#canGoTo()
	 */
	@Override
	public boolean canGoTo() {
		return this.iteratorTripleID.canGoTo();
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#goTo(long)
	 */
	@Override
	public void goTo(final long pos) {
		this.iteratorTripleID.goTo(pos);
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#estimatedNumResults()
	 */
	@Override
	public long estimatedNumResults() {
		return this.iteratorTripleID.estimatedNumResults();
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#numResultEstimation()
	 */
	@Override
	public ResultEstimationType numResultEstimation() {
		return this.iteratorTripleID.numResultEstimation();
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#getOrder()
	 */
	@Override
	public TripleComponentOrder getOrder() {
		return this.iteratorTripleID.getOrder();
	}

}
