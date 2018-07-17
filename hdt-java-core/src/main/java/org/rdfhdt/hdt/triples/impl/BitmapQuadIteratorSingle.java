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
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.TripleID;

/**
 * @author José M. Giménez-García
 *
 */
public class BitmapQuadIteratorSingle implements IteratorTripleID {

	protected BitmapQuads		quads;
	protected Permutation		permutation;
	protected final QuadID	pattern;
	protected int			x, y, z;
	protected boolean		hasNext	= false, hasPrevious = false;

	public BitmapQuadIteratorSingle(final BitmapQuads bitmapQuads, final QuadID pattern) {
		this.quads = bitmapQuads;
		this.permutation = bitmapQuads.permutation;
		this.pattern = pattern;
		goToStart();
	}

	public BitmapQuadIteratorSingle(final BitmapQuads bitmapQuads, final QuadID pattern, final BiFunction<Integer, TripleComponentRole, Integer> toRoleID) {
		this.quads = bitmapQuads;
		this.permutation = bitmapQuads.permutation;
		this.pattern = pattern;
		goToStart();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return this.hasNext;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public TripleID next() {
		final QuadID returnQuad = new QuadID(this.x, this.y, this.z, this.pattern.getGraph());
		TripleOrderConvert.swapComponentOrder(returnQuad, this.quads.order, TripleComponentOrder.SPO);
		this.hasNext = false;
		this.hasPrevious = true;
		return returnQuad;
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#hasPrevious()
	 */
	@Override
	public boolean hasPrevious() {
		return this.hasPrevious;
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#previous()
	 */
	@Override
	public TripleID previous() {
		final QuadID returnQuad = new QuadID(this.x, this.y, this.z, this.pattern.getGraph());
		TripleOrderConvert.swapComponentOrder(returnQuad, this.quads.order, TripleComponentOrder.SPO);
		this.hasNext = true;
		this.hasPrevious = false;
		return returnQuad;
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#goToStart()
	 */
	@Override
	public void goToStart() {
		final int patX = this.pattern.getSubject();
		final int patY = this.pattern.getPredicate();
		final int patZ = this.pattern.getObject();
		final int patG = this.pattern.getGraph();

		final AdjacencyList adjY = this.quads.adjY;
		final AdjacencyList adjZ = this.quads.adjZ;

		final long posZ = this.permutation.revpi(patG) - 1; // The position of the object is the position of the triple
		this.z = (int) adjZ.get(posZ);

		if (patZ != 0 && patZ != this.z) {
			// Item not found in list, no results.
			this.x = this.y = this.z = 0;
			this.hasNext = false;
		} else {
			final long posY = adjZ.findListIndex(posZ);
			this.y = (int) adjY.get(posY);
			if (patY != 0 && patY != this.y) {
				// Item not found in list, no results.
				this.x = this.y = this.z = 0;
				this.hasNext = false;
			} else {

				this.x = (int) adjY.findListIndex(posY) + 1;
				if (patX != 0 && patX != this.x) {
					// Item not found in list, no results.
					this.x = this.y = this.z = 0;
					this.hasNext = false;
				} else {
					// Item found
					this.hasNext = true;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#canGoTo()
	 */
	@Override
	public boolean canGoTo() {
		return this.pattern.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#goTo(long)
	 */
	@Override
	public void goTo(final long pos) {
		if (!canGoTo()) {
			throw new IllegalAccessError("Cannot goto on this bitmaptriples pattern");
		}
		if (pos != 1) {
			throw new ArrayIndexOutOfBoundsException("Cannot goTo beyond last triple");
		}
		goToStart();
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#estimatedNumResults()
	 */
	@Override
	public long estimatedNumResults() {
		return this.hasNext || this.hasPrevious ? 1 : 0;
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#numResultEstimation()
	 */
	@Override
	public ResultEstimationType numResultEstimation() {
		return ResultEstimationType.EXACT;
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.IteratorTripleID#getOrder()
	 */
	@Override
	public TripleComponentOrder getOrder() {
		return this.quads.order;
	}

}
