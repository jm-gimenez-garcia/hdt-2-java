/**
 *
 */
package org.rdfhdt.hdt.triples.impl;

import java.util.function.BiFunction;

import org.rdfhdt.hdt.compact.bitmap.AdjacencyList;
import org.rdfhdt.hdt.compact.permutation.Permutation;
import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.TripleID;

/**
 * @author José M. Giménez-García
 *
 */
public class BitmapQuadIteratorWrapper implements IteratorTripleID {

	protected IteratorTripleID	iteratorTripleID;
	protected Permutation	permutation;
	protected AdjacencyList		adjY, adjZ;

	public BitmapQuadIteratorWrapper(final BitmapQuads bitmapQuads, final IteratorTripleID iteratorTripleID) {
		this.iteratorTripleID = iteratorTripleID;
		this.permutation = bitmapQuads.permutation;
		this.adjY = bitmapQuads.adjY;
		this.adjZ = bitmapQuads.adjZ;
	}

	public BitmapQuadIteratorWrapper(final BitmapQuads bitmapQuads, final IteratorTripleID iteratorTripleID, final BiFunction<Integer, TripleComponentRole, Integer> toGlobalID) {
		this(bitmapQuads, iteratorTripleID);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return this.iteratorTripleID.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public TripleID next() {
		final TripleID triple = this.iteratorTripleID.next();
		return new QuadID(triple, (int) getGraphID(triple));
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#hasPrevious()
	 */
	@Override
	public boolean hasPrevious() {
		// TODO Auto-generated method stub
		return this.iteratorTripleID.hasPrevious();
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#previous()
	 */
	@Override
	public TripleID previous() {
		final TripleID triple = this.iteratorTripleID.previous();
		return new QuadID(triple, (int) getGraphID(triple));
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#goToStart()
	 */
	@Override
	public void goToStart() {
		this.iteratorTripleID.goToStart();
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#canGoTo()
	 */
	@Override
	public boolean canGoTo() {
		return this.iteratorTripleID.canGoTo();
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#goTo(long)
	 */
	@Override
	public void goTo(final long pos) {
		this.iteratorTripleID.goTo(pos);
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#estimatedNumResults()
	 */
	@Override
	public long estimatedNumResults() {
		return this.iteratorTripleID.estimatedNumResults();
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#numResultEstimation()
	 */
	@Override
	public ResultEstimationType numResultEstimation() {
		return this.iteratorTripleID.numResultEstimation();
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#getOrder()
	 */
	@Override
	public TripleComponentOrder getOrder() {
		return this.iteratorTripleID.getOrder();
	}

	protected long getGraphID(final TripleID triple) {
		long positionPredicate, positionObject;
		try {
			final int s = triple.getSubject();
			final int p = triple.getPredicate();
			final int o = triple.getObject();
			// positionPredicate = this.adjY.find(triple.getSubject() - 1, triple.getPredicate());
			positionPredicate = this.adjY.find(s - 1, p);
			positionObject = this.adjZ.find(positionPredicate, o);
		} catch (final NotFoundException e) {
			// No graph for triple
			positionObject = 0;
		}
		return (int) this.permutation.pi(positionObject + 1);
	}
}
