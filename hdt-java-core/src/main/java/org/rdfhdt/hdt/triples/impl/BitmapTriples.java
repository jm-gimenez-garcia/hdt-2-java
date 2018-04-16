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
import java.util.ArrayList;
import java.util.Collections;

import org.rdfhdt.hdt.compact.bitmap.AdjacencyList;
import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.compact.bitmap.Bitmap375;
import org.rdfhdt.hdt.compact.bitmap.BitmapFactory;
import org.rdfhdt.hdt.compact.bitmap.ModifiableBitmap;
import org.rdfhdt.hdt.compact.sequence.Sequence;
import org.rdfhdt.hdt.compact.sequence.SequenceFactory;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64Big;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.iterator.SequentialSearchIteratorTripleID;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TempTriples;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TriplesPrivate;
import org.rdfhdt.hdt.util.BitUtil;
import org.rdfhdt.hdt.util.StopWatch;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.listener.IntermediateListener;
import org.rdfhdt.hdt.util.listener.ListenerUtil;

/**
 * @author mario.arias
 *
 */
public class BitmapTriples implements TriplesPrivate {
    protected TripleComponentOrder order = TripleComponentOrder.SPO;

    public Sequence		   seqY;

    public Sequence		   seqZ;

    protected Sequence		   indexZ;

    protected Sequence		   predicateCount;
    public Bitmap		   bitmapY;

    public Bitmap		   bitmapZ;

    protected Bitmap		   bitmapIndexZ;

    protected AdjacencyList	   adjY, adjZ, adjIndex;

    // Index for Y
    PredicateIndex		   predicateIndex;

    public BitmapTriples() {
	this(new HDTSpecification());
    }

    public BitmapTriples(final HDTOptions spec) {
	final String orderStr = spec.get("triplesOrder");
	if (orderStr != null) {
	    this.order = TripleComponentOrder.valueOf(orderStr);
	}

	this.bitmapY = BitmapFactory.createBitmap(spec.get("bitmap.y"));
	this.bitmapZ = BitmapFactory.createBitmap(spec.get("bitmap.z"));

	this.seqY = SequenceFactory.createStream(spec.get("seq.y"));
	this.seqZ = SequenceFactory.createStream(spec.get("seq.z"));

	this.adjY = new AdjacencyList(this.seqY, this.bitmapY);
	this.adjZ = new AdjacencyList(this.seqZ, this.bitmapZ);
    }

    public BitmapTriples(final Sequence seqY, final Sequence seqZ, final Bitmap bitY, final Bitmap bitZ, final TripleComponentOrder order) {
	this.seqY = seqY;
	this.seqZ = seqZ;
	this.bitmapY = bitY;
	this.bitmapZ = bitZ;
	this.order = order;

	this.adjY = new AdjacencyList(seqY, this.bitmapY);
	this.adjZ = new AdjacencyList(seqZ, this.bitmapZ);
    }

    public Sequence getPredicateCount() {
	return this.predicateCount;
    }

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
    public void load(final TempTriples triples, final ProgressListener listener) {
	triples.setOrder(this.order);
	triples.sort(listener);

	final IteratorTripleID it = triples.searchAll();
	this.load(it, listener);
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#search(hdt.triples.TripleID)
     */
    @Override
    public IteratorTripleID search(final TripleID pattern) {
	final TripleID reorderedPat = new TripleID(pattern);
	TripleOrderConvert.swapComponentOrder(reorderedPat, TripleComponentOrder.SPO, this.order);
	final String patternString = reorderedPat.getPatternString();

	if (patternString.equals("?P?")) {
	    if (this.predicateIndex != null) {
		return new BitmapTriplesIteratorYFOQ(this, pattern);
	    } else {
		return new BitmapTriplesIteratorY(this, pattern);
	    }
	}

	if (this.indexZ != null && this.bitmapIndexZ != null) {
	    // USE FOQ
	    if (patternString.equals("?PO") || patternString.equals("??O")) { return new BitmapTriplesIteratorZFOQ(this, pattern); }
	} else {
	    if (patternString.equals("?PO")) { return new SequentialSearchIteratorTripleID(pattern, new BitmapTriplesIteratorZ(this, pattern)); }

	    if (patternString.equals("??O")) { return new BitmapTriplesIteratorZ(this, pattern); }
	}

	final IteratorTripleID bitIt = new BitmapTriplesIterator(this, pattern);
	if (patternString.equals("???") || patternString.equals("S??") || patternString.equals("SP?") || patternString.equals("SPO")) {
	    return bitIt;
	} else {
	    return new SequentialSearchIteratorTripleID(pattern, bitIt);
	}

    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#searchAll()
     */
    @Override
    public IteratorTripleID searchAll() {
	return this.search(new TripleID());
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#getNumberOfElements()
     */
    @Override
    public long getNumberOfElements() {
	return this.seqZ.getNumberOfElements();
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#size()
     */
    @Override
    public long size() {
	return this.seqY.size() + this.seqZ.size() + this.bitmapY.getSizeBytes() + this.bitmapZ.getSizeBytes();
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#save(java.io.OutputStream, hdt.ControlInfo, hdt.ProgressListener)
     */
    @Override
    public void save(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {
	ci.clear();
	ci.setFormat(this.getType());
	ci.setInt("order", this.order.ordinal());
	ci.setType(ControlInfo.Type.TRIPLES);
	ci.save(output);

	final IntermediateListener iListener = new IntermediateListener(listener);
	this.bitmapY.save(output, iListener);
	this.bitmapZ.save(output, iListener);
	this.seqY.save(output, iListener);
	this.seqZ.save(output, iListener);
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#load(java.io.InputStream, hdt.ControlInfo, hdt.ProgressListener)
     */
    @Override
    public void load(final InputStream input, final ControlInfo ci, final ProgressListener listener) throws IOException {

	if (ci.getType() != ControlInfo.Type.TRIPLES) { throw new IllegalFormatException("Trying to read a triples section, but was not triples."); }

	if (!ci.getFormat().equals(this.getType())) { throw new IllegalFormatException("Trying to read BitmapTriples, but the data does not seem to be BitmapTriples"); }

	this.order = TripleComponentOrder.values()[(int) ci.getInt("order")];

	final IntermediateListener iListener = new IntermediateListener(listener);

	this.bitmapY = BitmapFactory.createBitmap(input);
	this.bitmapY.load(input, iListener);

	this.bitmapZ = BitmapFactory.createBitmap(input);
	this.bitmapZ.load(input, iListener);

	this.seqY = SequenceFactory.createStream(input);
	this.seqY.load(input, iListener);

	this.seqZ = SequenceFactory.createStream(input);
	this.seqZ.load(input, iListener);

	this.adjY = new AdjacencyList(this.seqY, this.bitmapY);
	this.adjZ = new AdjacencyList(this.seqZ, this.bitmapZ);
    }

    @Override
    public void mapFromFile(final CountInputStream input, final File f, final ProgressListener listener) throws IOException {

	final ControlInformation ci = new ControlInformation();
	ci.load(input);
	if (ci.getType() != ControlInfo.Type.TRIPLES) { throw new IllegalFormatException("Trying to read a triples section, but was not triples."); }

	if (!ci.getFormat().equals(this.getType())) { throw new IllegalFormatException("Trying to read BitmapTriples, but the data does not seem to be BitmapTriples"); }

	this.order = TripleComponentOrder.values()[(int) ci.getInt("order")];

	final IntermediateListener iListener = new IntermediateListener(listener);

	this.bitmapY = BitmapFactory.createBitmap(input);
	this.bitmapY.load(input, iListener);

	this.bitmapZ = BitmapFactory.createBitmap(input);
	this.bitmapZ.load(input, iListener);

	this.seqY = SequenceFactory.createStream(input, f);
	this.seqZ = SequenceFactory.createStream(input, f);

	this.adjY = new AdjacencyList(this.seqY, this.bitmapY);
	this.adjZ = new AdjacencyList(this.seqZ, this.bitmapZ);

    }

    private void createIndexObjectMemoryEfficient() {
	final StopWatch global = new StopWatch();
	final StopWatch st = new StopWatch();

	// Count the number of appearances of each object
	long maxCount = 0;
	long numDifferentObjects = 0;
	long numReservedObjects = 8192;
	SequenceLog64 objectCount = new SequenceLog64(BitUtil.log2(this.seqZ.getNumberOfElements()), numReservedObjects, true);
	for (long i = 0; i < this.seqZ.getNumberOfElements(); i++) {
	    final long val = this.seqZ.get(i);
	    if (val == 0) { throw new RuntimeException("ERROR: There is a zero value in the Z level."); }
	    if (numReservedObjects < val) {
		while (numReservedObjects < val) {
		    numReservedObjects <<= 1;
		}
		objectCount.resize(numReservedObjects);
	    }
	    if (numDifferentObjects < val) {
		numDifferentObjects = val;
	    }

	    final long count = objectCount.get(val - 1) + 1;
	    maxCount = count > maxCount ? count : maxCount;
	    objectCount.set(val - 1, count);
	}
	System.out.println("Count Objects in " + st.stopAndShow() + " Max was: " + maxCount);
	st.reset();

	// Calculate bitmap that separates each object sublist.
	final Bitmap375 bitmapIndex = new Bitmap375(this.seqZ.getNumberOfElements());
	long tmpCount = 0;
	for (long i = 0; i < numDifferentObjects; i++) {
	    tmpCount += objectCount.get(i);
	    bitmapIndex.set(tmpCount - 1, true);
	}
	bitmapIndex.set(this.seqZ.getNumberOfElements() - 1, true);
	System.out.println("Bitmap in " + st.stopAndShow());
	objectCount = null;
	st.reset();

	// Copy each object reference to its position
	SequenceLog64 objectInsertedCount = new SequenceLog64(BitUtil.log2(maxCount), numDifferentObjects);
	objectInsertedCount.resize(numDifferentObjects);

	final SequenceLog64 objectArray = new SequenceLog64(BitUtil.log2(this.seqY.getNumberOfElements()), this.seqZ.getNumberOfElements());
	objectArray.resize(this.seqZ.getNumberOfElements());

	for (long i = 0; i < this.seqZ.getNumberOfElements(); i++) {
	    final long objectValue = this.seqZ.get(i);
	    final long posY = i > 0 ? this.bitmapZ.rank1(i - 1) : 0;

	    final long insertBase = objectValue == 1 ? 0 : bitmapIndex.select1(objectValue - 1) + 1;
	    final long insertOffset = objectInsertedCount.get(objectValue - 1);
	    objectInsertedCount.set(objectValue - 1, insertOffset + 1);

	    objectArray.set(insertBase + insertOffset, posY);
	}
	System.out.println("Object references in " + st.stopAndShow());
	objectInsertedCount = null;
	st.reset();

	long object = 1;
	long first = 0;
	long last = bitmapIndex.select1(object) + 1;
	do {
	    final long listLen = last - first;

	    // Sublists of one element do not need to be sorted.

	    // Hard-coded size-2 for speed (They are quite common).
	    if (listLen == 2) {
		final long aPos = objectArray.get(first);
		final long a = this.seqY.get(aPos);
		final long bPos = objectArray.get(first + 1);
		final long b = this.seqY.get(bPos);
		if (a > b) {
		    objectArray.set(first, bPos);
		    objectArray.set(first + 1, aPos);
		}
	    } else if (listLen > 2) {
		class Pair {
		    int	valueY;
		    int	positionY;
		}
		;

		// FIXME: Sort directly without copying?
		final ArrayList<Pair> list = new ArrayList<>((int) listLen);

		// Create temporary list of (position, predicate)
		for (long i = first; i < last; i++) {
		    final Pair p = new Pair();
		    p.positionY = (int) objectArray.get(i);
		    p.valueY = (int) this.seqY.get(p.positionY);
		    list.add(p);
		}

		// Sort
		Collections.sort(list, (o1, o2) -> {
		    if (o1.valueY == o2.valueY) { return o1.positionY - o2.positionY; }
		    return o1.valueY - o2.valueY;
		});

		// Copy back
		for (long i = first; i < last; i++) {
		    objectArray.set(i, list.get((int) (i - first)).positionY);
		}
	    }

	    first = last;
	    last = bitmapIndex.select1(object) + 1;
	    object++;
	} while (object <= numDifferentObjects);

	System.out.println("Sort object sublists in " + st.stopAndShow());
	st.reset();

	// Count predicates
	final SequenceLog64 predCount = new SequenceLog64(BitUtil.log2(this.seqY.getNumberOfElements()));
	for (long i = 0; i < this.seqY.getNumberOfElements(); i++) {
	    // Read value
	    final long val = this.seqY.get(i);

	    // Grow if necessary
	    if (predCount.getNumberOfElements() < val) {
		predCount.resize(val);
	    }

	    // Increment
	    predCount.set(val - 1, predCount.get(val - 1) + 1);
	}
	predCount.trimToSize();
	System.out.println("Count predicates in " + st.stopAndShow());
	this.predicateCount = predCount;
	st.reset();

	// Save Object Index
	this.indexZ = objectArray;
	this.bitmapIndexZ = bitmapIndex;
	this.adjIndex = new AdjacencyList(this.indexZ, this.bitmapIndexZ);

	System.out.println("Index generated in " + global.stopAndShow());
    }

    @Override
    public void generateIndex(final ProgressListener listener) {
	this.predicateIndex = new PredicateIndexArray(this);
	this.predicateIndex.generate(listener);

	// createIndexObjects();
	this.createIndexObjectMemoryEfficient();
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#populateHeader(hdt.header.Header, java.lang.String)
     */
    @Override
    public void populateHeader(final Header header, final String rootNode) {
	header.insert(rootNode, HDTVocabulary.TRIPLES_TYPE, this.getType());
	header.insert(rootNode, HDTVocabulary.TRIPLES_NUM_TRIPLES, this.getNumberOfElements());
	header.insert(rootNode, HDTVocabulary.TRIPLES_ORDER, this.order.toString());
	// header.insert(rootNode, HDTVocabulary.TRIPLES_SEQY_TYPE, seqY.getType() );
	// header.insert(rootNode, HDTVocabulary.TRIPLES_SEQZ_TYPE, seqZ.getType() );
	// header.insert(rootNode, HDTVocabulary.TRIPLES_SEQY_SIZE, seqY.size() );
	// header.insert(rootNode, HDTVocabulary.TRIPLES_SEQZ_SIZE, seqZ.size() );
	// if(bitmapY!=null) {
	// header.insert(rootNode, HDTVocabulary.TRIPLES_BITMAPY_SIZE, bitmapY.getSizeBytes() );
	// }
	// if(bitmapZ!=null) {
	// header.insert(rootNode, HDTVocabulary.TRIPLES_BITMAPZ_SIZE, bitmapZ.getSizeBytes() );
	// }
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#getType()
     */
    @Override
    public String getType() {
	return HDTVocabulary.TRIPLES_TYPE_BITMAP;
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#saveIndex(java.io.OutputStream, hdt.options.ControlInfo, hdt.listener.ProgressListener)
     */
    @Override
    public void saveIndex(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {
	final IntermediateListener iListener = new IntermediateListener(listener);

	ci.clear();
	ci.setType(ControlInfo.Type.INDEX);
	ci.setInt("numTriples", this.getNumberOfElements());
	ci.setInt("order", this.order.ordinal());
	ci.setFormat(HDTVocabulary.INDEX_TYPE_FOQ);
	ci.save(output);

	this.bitmapIndexZ.save(output, iListener);
	this.indexZ.save(output, iListener);

	this.predicateIndex.save(output);

	this.predicateCount.save(output, iListener);
    }

    /*
     * (non-Javadoc)
     * @see hdt.triples.Triples#loadIndex(java.io.InputStream, hdt.options.ControlInfo, hdt.listener.ProgressListener)
     */
    @Override
    public void loadIndex(final InputStream input, final ControlInfo ci, final ProgressListener listener) throws IOException {
	final IntermediateListener iListener = new IntermediateListener(listener);

	if (ci.getType() != ControlInfo.Type.INDEX) { throw new IllegalFormatException("Trying to read an Index Section but it was not an Index."); }

	if (!HDTVocabulary.INDEX_TYPE_FOQ
		.equals(ci.getFormat())) { throw new IllegalFormatException("Trying to read wrong format of Index. Remove the .hdt.index file and let the app regenerate it."); }

	final long numTriples = ci.getInt("numTriples");
	if (this.getNumberOfElements() != numTriples) { throw new IllegalFormatException("This index is not associated to the HDT file"); }

	final TripleComponentOrder indexOrder = TripleComponentOrder.values()[(int) ci.getInt("order")];
	if (indexOrder != this.order) { throw new IllegalFormatException("The order of the triples is not the same of the index."); }

	this.bitmapIndexZ = BitmapFactory.createBitmap(input);
	this.bitmapIndexZ.load(input, iListener);

	this.indexZ = SequenceFactory.createStream(input);
	this.indexZ.load(input, iListener);

	this.predicateIndex = new PredicateIndexArray(this);
	this.predicateIndex.load(input);

	this.predicateCount = SequenceFactory.createStream(input);
	this.predicateCount.load(input, iListener);

	this.adjIndex = new AdjacencyList(this.indexZ, this.bitmapIndexZ);
    }

    @Override
    public void mapIndex(final CountInputStream input, final File f, final ControlInfo ci, final ProgressListener listener) throws IOException {
	final IntermediateListener iListener = new IntermediateListener(listener);

	if (ci.getType() != ControlInfo.Type.INDEX) { throw new IllegalFormatException("Trying to read an Index Section but it was not an Index."); }

	if (!HDTVocabulary.INDEX_TYPE_FOQ
		.equals(ci.getFormat())) { throw new IllegalFormatException("Trying to read wrong format of Index. Remove the .hdt.index file and let the app regenerate it."); }

	final long numTriples = ci.getInt("numTriples");
	if (this.getNumberOfElements() != numTriples) { throw new IllegalFormatException("This index is not associated to the HDT file"); }

	final TripleComponentOrder indexOrder = TripleComponentOrder.values()[(int) ci.getInt("order")];
	if (indexOrder != this.order) { throw new IllegalFormatException("The order of the triples is not the same of the index."); }

	this.bitmapIndexZ = BitmapFactory.createBitmap(input);
	this.bitmapIndexZ.load(input, iListener);

	this.indexZ = SequenceFactory.createStream(input, f);

	this.predicateIndex = new PredicateIndexArray(this);
	this.predicateIndex.mapIndex(input, f, iListener);

	this.predicateCount = SequenceFactory.createStream(input, f);

	this.adjIndex = new AdjacencyList(this.indexZ, this.bitmapIndexZ);
    }

    @Override
    public void close() throws IOException {
	this.seqY.close();
	this.seqY = null;
	this.seqZ.close();
	this.seqZ = null;
	if (this.indexZ != null) {
	    this.indexZ.close();
	    this.indexZ = null;
	}
	if (this.predicateCount != null) {
	    this.predicateCount.close();
	    this.predicateCount = null;
	}
	if (this.predicateIndex != null) {
	    this.predicateIndex.close();
	    this.predicateIndex = null;
	}
    }

    public TripleComponentOrder getOrder() {
	return this.order;
    }
}
