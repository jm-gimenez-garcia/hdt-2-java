/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/triples/impl/BitmapTriplesIterator.java $
 * Revision: $Rev: 191 $
 * Last modified: $Date: 2013-03-03 11:41:43 +0000 (dom, 03 mar 2013) $
 * Last modified by: $Author: mario.arias $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contacting the authors:
 *   Mario Arias:               mario.arias@deri.org
 *   Javier D. Fernandez:       jfergar@infor.uva.es
 *   Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 *   Alejandro Andres:          fuzzy.alej@gmail.com
 */

package org.rdfhdt.hdt.triples.impl;

import org.rdfhdt.hdt.compact.bitmap.AdjacencyList;
import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;

/**
 * @author mario.arias
 *
 */
public class BitmapTriplesIterator implements IteratorTripleID {

	protected final BitmapTriples	triples;
	protected final TripleID		pattern, returnTriple;
	protected int					patX, patY, patZ;

	protected AdjacencyList			adjY, adjZ;
	protected long					posY, posZ, minY, minZ, maxY, maxZ;
	protected long					nextY, nextZ;
	protected int					x, y, z;

	BitmapTriplesIterator(final BitmapTriples triples, final TripleID pattern) {
		this.triples = triples;
		this.returnTriple = new TripleID();
		this.pattern = new TripleID();
		this.newSearch(pattern);
	}

	public void newSearch(final TripleID pattern) {
		this.pattern.assign(pattern);

		TripleOrderConvert.swapComponentOrder(this.pattern, TripleComponentOrder.SPO, this.triples.order);
		this.patX = this.pattern.getSubject();
		this.patY = this.pattern.getPredicate();
		this.patZ = this.pattern.getObject();

		this.adjY = this.triples.adjY;
		this.adjZ = this.triples.adjZ;

		//		((BitSequence375)triples.bitmapZ).dump();

		this.findRange();
		this.goToStart();
	}

	private void updateOutput() {
		this.returnTriple.setAll(this.x, this.y, this.z);
		TripleOrderConvert.swapComponentOrder(this.returnTriple, this.triples.order, TripleComponentOrder.SPO);
	}

	protected void findRange() {
		if(this.patX!=0) {
			// S X X
			if(this.patY!=0) {
				// S P X
				try {
					this.minY = this.adjY.find(this.patX-1, this.patY);
					this.maxY = this.minY+1;
					if(this.patZ!=0) {
						// S P O
						this.minZ = this.adjZ.find(this.minY,this.patZ);
						this.maxZ = this.minZ+1;
					} else {
						// S P ?
						this.minZ = this.adjZ.find(this.minY);
						this.maxZ = this.adjZ.last(this.minY)+1;
					}
				} catch (final NotFoundException e) {
					// Item not found in list, no results.
					this.minY = this.minZ = this.maxY = this.maxZ = 0;
				}
			} else {
				// S ? X
				this.minY = this.adjY.find(this.patX-1);
				this.minZ = this.adjZ.find(this.minY);
				this.maxY = this.adjY.last(this.patX-1)+1;
				this.maxZ = this.adjZ.find(this.maxY);
			}
			this.x = this.patX;
		} else {
			// ? X X
			this.minY=0;
			this.minZ=0;
			this.maxY = this.adjY.getNumberOfElements();
			this.maxZ = this.adjZ.getNumberOfElements();
		}
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return this.posZ<this.maxZ;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#next()
	 */
	@Override
	public TripleID next() {
		this.z = (int) this.adjZ.get(this.posZ);
		if(this.posZ==this.nextZ) {
			this.posY++;
			this.y = (int) this.adjY.get(this.posY);
			//			nextZ = adjZ.find(posY+1);
			this.nextZ = this.adjZ.findNext(this.nextZ)+1;

			if(this.posY==this.nextY) {
				this.x++;
				//				nextY = adjY.find(x);
				this.nextY = this.adjY.findNext(this.nextY)+1;
			}
		}

		this.posZ++;

		this.updateOutput();

		return this.returnTriple;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#hasPrevious()
	 */
	@Override
	public boolean hasPrevious() {
		return this.posZ>this.minZ;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#previous()
	 */
	@Override
	public TripleID previous() {
		this.posZ--;

		this.posY = this.adjZ.findListIndex(this.posZ);

		this.z = (int) this.adjZ.get(this.posZ);
		this.y = (int) this.adjY.get(this.posY);
		this.x = (int) this.adjY.findListIndex(this.posY)+1;

		this.nextY = this.adjY.last(this.x-1)+1;
		this.nextZ = this.adjZ.last(this.posY)+1;

		this.updateOutput();

		return this.returnTriple;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#goToStart()
	 */
	@Override
	public void goToStart() {
		this.posZ = this.minZ;
		this.posY = this.adjZ.findListIndex(this.posZ);

		this.z = (int) this.adjZ.get(this.posZ);
		this.y = (int) this.adjY.get(this.posY);
		this.x = (int) this.adjY.findListIndex(this.posY)+1;

		this.nextY = this.adjY.last(this.x-1)+1;
		this.nextZ = this.adjZ.last(this.posY)+1;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#estimatedNumResults()
	 */
	@Override
	public long estimatedNumResults() {
		return this.maxZ-this.minZ;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#numResultEstimation()
	 */
	@Override
	public ResultEstimationType numResultEstimation() {
		if(this.patX!=0 && this.patY==0 && this.patZ!=0) {
			return ResultEstimationType.UP_TO;
		}
		return ResultEstimationType.EXACT;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#canGoTo()
	 */
	@Override
	public boolean canGoTo() {
		return this.pattern.isEmpty();
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#goTo(int)
	 */
	@Override
	public void goTo(final long pos) {
		if(!this.canGoTo()) {
			throw new IllegalAccessError("Cannot goto on this bitmaptriples pattern");
		}

		if(pos>=this.adjZ.getNumberOfElements()) {
			throw new ArrayIndexOutOfBoundsException("Cannot goTo beyond last triple");
		}

		this.posZ = pos;
		this.posY = this.adjZ.findListIndex(this.posZ);

		this.z = (int) this.adjZ.get(this.posZ);
		this.y = (int) this.adjY.get(this.posY);
		this.x = (int) this.adjY.findListIndex(this.posY)+1;

		this.nextY = this.adjY.last(this.x-1)+1;
		this.nextZ = this.adjZ.last(this.posY)+1;
	}

	/* (non-Javadoc)
	 * @see hdt.iterator.IteratorTripleID#getOrder()
	 */
	@Override
	public TripleComponentOrder getOrder() {
		return this.triples.order;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
