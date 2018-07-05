/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/triples/impl/BitmapTriples.java $
 * Revision: $Rev: 203 $
 * Last modified: $Date: 2013-05-24 10:48:53 +0100 (vie, 24 may 2013) $
 * Last modified by: $Author: mario.arias $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * Contacting the authors:
 * Mario Arias: mario.arias@deri.org
 * Javier D. Fernandez: jfergar@infor.uva.es
 * Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 * Alejandro Andres: fuzzy.alej@gmail.com
 */

package org.rdfhdt.hdt.triples.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.compact.bitmap.AdjacencyList;
import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.compact.bitmap.Bitmap375;
import org.rdfhdt.hdt.compact.bitmap.ModifiableBitmap;
import org.rdfhdt.hdt.compact.sequence.Sequence;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64Big;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.TempQuads;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.Triples;
import org.rdfhdt.hdt.util.BitUtil;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.listener.ListenerUtil;

/**
 * @author mario.arias
 *
 */
public class BitmapQuads extends BitmapTriples {

    public BitmapQuads() {
	this(new HDTSpecification());
    }

    public BitmapQuads(final HDTOptions spec) {
	super(spec);
	// TODO: Create permutation
    }

    public BitmapQuads(final Sequence seqY, final Sequence seqZ, final Bitmap bitY, final Bitmap bitZ, final TripleComponentOrder order) {
	super(seqY, seqZ, bitY, bitZ, order);
	// TODO: Create permutation
    }

    @Override
    public void load(final IteratorTripleID it, final ProgressListener listener) {

	final long number = it.estimatedNumResults();

	final SequenceLog64Big vectorY = new SequenceLog64Big(BitUtil.log2(number), number);
	final SequenceLog64Big vectorZ = new SequenceLog64Big(BitUtil.log2(number), number);
	final ModifiableBitmap bitY = new Bitmap375(number);
	final ModifiableBitmap bitZ = new Bitmap375(number);

	int lastX = 0, lastY = 0, lastZ = 0;
	int x, y, z;
	int numTriples = 0;

	while (it.hasNext()) {
	    final TripleID triple = it.next();
	    TripleOrderConvert.swapComponentOrder(triple, TripleComponentOrder.SPO, this.order);

	    x = triple.getSubject();
	    y = triple.getPredicate();
	    z = triple.getObject();
	    if (x == 0 || y == 0 || z == 0) { throw new IllegalFormatException("None of the components of a triple can be null"); }

	    if (numTriples == 0) {
		// First triple
		vectorY.append(y);
		vectorZ.append(z);
	    } else if (x != lastX) {
		if (x != lastX + 1) { throw new IllegalFormatException("Upper level must be increasing and correlative."); }
		// X changed
		bitY.append(true);
		vectorY.append(y);

		bitZ.append(true);
		vectorZ.append(z);
	    } else if (y != lastY) {
		if (y < lastY) { throw new IllegalFormatException("Middle level must be increasing for each parent."); }

		// Y changed
		bitY.append(false);
		vectorY.append(y);

		bitZ.append(true);
		vectorZ.append(z);
	    } else {
		if (z < lastZ) { throw new IllegalFormatException("Lower level must be increasing for each parent."); }

		// Z changed
		bitZ.append(false);
		vectorZ.append(z);
	    }

	    lastX = x;
	    lastY = y;
	    lastZ = z;

	    ListenerUtil.notifyCond(listener, "Converting to BitmapTriples", numTriples, numTriples, number);
	    numTriples++;
	}

	bitY.append(true);
	bitZ.append(true);

	vectorY.aggresiveTrimToSize();
	vectorZ.trimToSize();

	// Assign local variables to BitmapTriples Object
	this.seqY = vectorY;
	this.seqZ = vectorZ;
	this.bitmapY = bitY;
	this.bitmapZ = bitZ;

	this.adjY = new AdjacencyList(this.seqY, this.bitmapY);
	this.adjZ = new AdjacencyList(this.seqZ, this.bitmapZ);

	// DEBUG
	// adjY.dump();
	// adjZ.dump();
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#load(hdt.triples.TempTriples, hdt.ProgressListener)
     */
    @Override
    public void load(final Triples triples, final ProgressListener listener) {
	if (triples instanceof TempQuads) {
	    // TODO: Load quads
	} else {
	    super.load(triples, listener);
	}
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#search(hdt.triples.TripleID)
     */
    @Override
    public IteratorTripleID search(final TripleID pattern) {
	if (pattern instanceof QuadID) {
	    // TODO: Compare using quads
	    throw new NotImplementedException();
	} else {
	    return super.search(pattern);
	}
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#size()
     */
    @Override
    public long size() {
	// TODO: Add the size of the permutation
	return super.size() + 0;
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#save(java.io.OutputStream, hdt.ControlInfo, hdt.ProgressListener)
     */
    @Override
    public void save(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {
	super.save(output, ci, listener);
	// TODO: Save permutation
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#load(java.io.InputStream, hdt.ControlInfo, hdt.ProgressListener)
     */
    @Override
    public void load(final InputStream input, final ControlInfo ci, final ProgressListener listener) throws IOException {
	super.load(input, ci, listener);
	// TODO: Load permutation
    }

    @Override
    public void mapFromFile(final CountInputStream input, final File f, final ProgressListener listener) throws IOException {
	super.mapFromFile(input, f, listener);
	// TODO: Load permutation
    }

    @Override
    public void populateHeader(final Header header, final String rootNode) {
	super.populateHeader(header, rootNode);
	// TODO: Insert data about quads
    }

    @Override
    public String getType() {
	return HDTVocabulary.QUADS_TYPE_BITMAP;
    }

    @Override
    public void close() throws IOException {
	super.close();
	// TODO: close permutation
    }

}
