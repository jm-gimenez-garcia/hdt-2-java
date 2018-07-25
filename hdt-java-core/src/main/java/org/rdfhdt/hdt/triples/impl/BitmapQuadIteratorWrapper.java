/**
 *
 */
package org.rdfhdt.hdt.triples.impl;

import org.rdfhdt.hdt.compact.bitmap.AdjacencyList;
import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.compact.permutation.Permutation;
import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
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
	protected BitmapQuads		bitmapQuads;
	protected AdjacencyList		adjY, adjZ;
	protected Permutation	permutation;
	protected Bitmap			bitmapPermutation;
	protected TripleID			previousTriple;
	protected long				previousPosition;

	public BitmapQuadIteratorWrapper(final BitmapQuads bitmapQuads, final IteratorTripleID iteratorTripleID) {
		this.iteratorTripleID = iteratorTripleID;
		this.bitmapQuads = bitmapQuads;
		this.adjY = bitmapQuads.adjY;
		this.adjZ = bitmapQuads.adjZ;
		this.permutation = bitmapQuads.permutation;
		this.bitmapPermutation = bitmapQuads.bitmapG;
		this.previousTriple = new TripleID();
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
		long graphID;
		if (this.previousTriple.equals(triple)) {
			graphID = getGraphIDNextTriple(triple);
		} else {
			graphID = getGraphIDNewTriple(triple);
		}
		return graphID;
	}

	protected long getGraphIDNextTriple(final TripleID triple) {
		this.previousPosition++;
		return this.bitmapPermutation.access(this.previousPosition) ? (int) this.permutation.pi(this.bitmapPermutation.rank1(this.previousPosition)) : 0;
	}

	protected long getGraphIDNewTriple(final TripleID triple) {
		long positionPredicate, positionObject;
		int graphID = 0;
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

		// It is possible that more than one reified triple with the same SPO exists
		// We need to go to the first one
		if (positionObject != 0) {
			TripleID firstTriple;
			do {
				firstTriple = this.bitmapQuads.get(--positionObject);
			} while (firstTriple.getSubject() == triple.getSubject()
					&& firstTriple.getPredicate() == triple.getPredicate()
					&& firstTriple.getObject() == triple.getObject());
			positionObject++;
		}

		if (this.bitmapPermutation.access(positionObject)) {
			graphID = (int) this.permutation.pi(this.bitmapPermutation.rank1(positionObject));
		} else {
			graphID = 0;
		}

		// this.previousTriple = triple; // This does not work well. WTF?
		this.previousTriple = new TripleID(triple);
		this.previousPosition = positionObject;

		return graphID;
	}
}
