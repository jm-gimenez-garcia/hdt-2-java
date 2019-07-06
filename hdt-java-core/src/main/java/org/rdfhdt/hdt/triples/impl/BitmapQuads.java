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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.rdfhdt.hdt.compact.bitmap.AdjacencyList;
import org.rdfhdt.hdt.compact.bitmap.Bitmap;
import org.rdfhdt.hdt.compact.bitmap.Bitmap375;
import org.rdfhdt.hdt.compact.bitmap.BitmapFactory;
import org.rdfhdt.hdt.compact.bitmap.ModifiableBitmap;
import org.rdfhdt.hdt.compact.permutation.Permutation;
import org.rdfhdt.hdt.compact.permutation.PermutationFactory;
import org.rdfhdt.hdt.compact.sequence.Sequence;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64Big;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64BigDisk;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.util.BitUtil;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.listener.IntermediateListener;
import org.rdfhdt.hdt.util.listener.ListenerUtil;

/**
 * @author mario.arias
 *
 */
public class BitmapQuads extends BitmapTriples {

	protected Permutation permutation;
	protected Bitmap bitmapG;

	public BitmapQuads() {
		this(new HDTSpecification());
	}

	public BitmapQuads(final HDTOptions spec) {
		super(spec);
	}

	public BitmapQuads(final Sequence seqY, final Sequence seqZ, final Bitmap bitY, final Bitmap bitZ, final TripleComponentOrder order) {
		super(seqY, seqZ, bitY, bitZ, order);
	}

	@Override
	public TripleID get(final long pos) {
		final long posY = this.adjZ.findListIndex(pos);

		final int g = this.bitmapG.access(pos) ? (int) this.permutation.pi(this.bitmapG.rank1(pos)) : 0;
		final int z = (int) this.adjZ.get(pos);
		final int y = (int) this.adjY.get(posY);
		final int x = (int) (this.adjY.findListIndex(posY) + 1);

		return g == 0 ? new TripleID(x, y, z) : new QuadID(x, y, z, g);
	}

	@Override
	public void load(final IteratorTripleID it, final ProgressListener listener) {

		final long number = it.estimatedNumResults();

		final SequenceLog64Big vectorY = new SequenceLog64Big(BitUtil.log2(number), number);
		final SequenceLog64Big vectorZ = new SequenceLog64Big(BitUtil.log2(number), number);
		final ModifiableBitmap bitY = new Bitmap375(number);
		final ModifiableBitmap bitZ = new Bitmap375(number);

		final List<Long> vectorG = new LinkedList<>();
		final ModifiableBitmap bitG = new Bitmap375(number);

		int lastX = 0;
		int lastY = 0;
		int lastZ = 0;
		int x, y, z;
		long g;
		int numTriples = 0;

		while (it.hasNext()) {
			final TripleID triple = it.next();
			TripleOrderConvert.swapComponentOrder(triple, TripleComponentOrder.SPO, this.order);

			x = toRoleID(triple.getSubject(), TripleComponentRole.SUBJECT);
			y = toRoleID(triple.getPredicate(), TripleComponentRole.PREDICATE);
			z = toRoleID(triple.getObject(), TripleComponentRole.OBJECT);
			g = triple instanceof QuadID ? toRoleID(((QuadID) triple).getGraph(), TripleComponentRole.GRAPH) : 0;
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

			if (g == 0) {
				bitG.append(false);
			} else
			{
				bitG.append(true);
				vectorG.add(g);
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
		this.bitmapG = bitG;

		this.adjY = new AdjacencyList(this.seqY, this.bitmapY);
		this.adjZ = new AdjacencyList(this.seqZ, this.bitmapZ);
		this.permutation = PermutationFactory.createPermutation(vectorG, PermutationFactory.PERMUTATION_MRRR_DEFAULT_STEP);

		// DEBUG
		// adjY.dump();
		// adjZ.dump();
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.triples.Triples#search(hdt.triples.TripleID)
	 */
	@Override
	public IteratorTripleID search(final TripleID pattern) {
		IteratorTripleID iterator;
		final TripleID patternLocal = toRoleIDs(pattern);
		if (patternLocal instanceof QuadID) {
			if (((QuadID) patternLocal).getGraph() == 0) {
				// if the quad is not given, use a normal tripleID iterator and wrap it to get the Quad ID
				iterator = new BitmapQuadIteratorWrapper(this, super.search(patternLocal));
			} else {
				// if the quad id is given, we need to return an iterator with a single element
				iterator = new BitmapQuadIteratorSingle(this, (QuadID) patternLocal);
			}
		} else {
			iterator = super.search(patternLocal);
		}
		return new TripleTranslatorIteratorWrapper(this, iterator);
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.triples.Triples#searchAll()
	 */
	@Override
	public IteratorTripleID searchAll() {
		return search(new QuadID());
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.triples.Triples#size()
	 */
	@Override
	public long size() {
		return super.size() + this.permutation.getSize();
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.triples.Triples#save(java.io.OutputStream, hdt.ControlInfo, hdt.ProgressListener)
	 */
	@Override
	public void save(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {
		super.save(output, ci, listener);
		final IntermediateListener iListener = new IntermediateListener(listener);
		this.bitmapG.save(output, iListener);
		this.permutation.save(output, iListener);
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.triples.Triples#load(java.io.InputStream, hdt.ControlInfo, hdt.ProgressListener)
	 */
	@Override
	public void load(final InputStream input, final ControlInfo ci, final ProgressListener listener) throws IOException {
		super.load(input, ci, listener);
		final IntermediateListener iListener = new IntermediateListener(listener);
		this.bitmapG = BitmapFactory.createBitmap(input);
		this.bitmapG.load(input, iListener);
		this.permutation = PermutationFactory.createPermutation();
		this.permutation.load(input, iListener);
	}

	@Override
	public void mapFromFile(final CountInputStream input, final File f, final ProgressListener listener) throws IOException {
		super.mapFromFile(input, f, listener);
		final IntermediateListener iListener = new IntermediateListener(listener);
		this.bitmapG = BitmapFactory.createBitmap(input);
		this.bitmapG.load(input, iListener);
		this.permutation = PermutationFactory.createPermutation();
		this.permutation.load(input, listener);
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
		this.permutation = null;
	}
	 public void cat(String location,IteratorTripleID it, ProgressListener listener){
	        try {
	            long number = it.estimatedNumResults();
	            SequenceLog64BigDisk vectorY = new SequenceLog64BigDisk(location + "vectorY", BitUtil.log2(number), number);
	            SequenceLog64BigDisk vectorZ = new SequenceLog64BigDisk(location + "vectorZ",BitUtil.log2(number), number);
	            ModifiableBitmap bitY = new Bitmap375();//Disk(location + "bitY",number);
	            ModifiableBitmap bitZ = new Bitmap375();//Disk(location + "bitZ",number);
	            

	    		final List<Long> vectorG = new LinkedList<>();
	    		final ModifiableBitmap bitG = new Bitmap375(number);

	    		int lastX = 0;
	    		int lastY = 0;
	    		int lastZ = 0;
	    		int x, y, z;
	    		long g;
	    		int numTriples = 0;

	    		while (it.hasNext()) {
	    			final TripleID triple = it.next();
	    			TripleOrderConvert.swapComponentOrder(triple, TripleComponentOrder.SPO, this.order);

	    			x = toRoleID(triple.getSubject(), TripleComponentRole.SUBJECT);
	    			y = toRoleID(triple.getPredicate(), TripleComponentRole.PREDICATE);
	    			z = toRoleID(triple.getObject(), TripleComponentRole.OBJECT);
	    			g = triple instanceof QuadID ? toRoleID(((QuadID) triple).getGraph(), TripleComponentRole.GRAPH) : 0;
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

	    			if (g == 0) {
	    				bitG.append(false);
	    			} else
	    			{
	    				bitG.append(true);
	    				vectorG.add(g);
	    			}

	    			lastX = x;
	    			lastY = y;
	    			lastZ = z;

	    			ListenerUtil.notifyCond(listener, "Converting to BitmapTriples", numTriples, numTriples, number);
	    			numTriples++;
	    		}

	    		if(numTriples>0) {
	    			bitY.append(true);
	    			bitZ.append(true);
	    		}
	    		vectorY.aggressiveTrimToSize();
	    		vectorZ.trimToSize();

	            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(location + "triples"));
	            ControlInfo ci = new ControlInformation();
	            ci.setType(ControlInfo.Type.TRIPLES);
	            ci.setFormat(HDTVocabulary.QUADS_TYPE_BITMAP);
	            ci.setInt("order", TripleComponentOrder.SPO.ordinal());
	            ci.save(bos);
	            
	            IntermediateListener iListener = new IntermediateListener(listener);
	            
	            bitY.save(bos, iListener);
	            bitZ.save(bos, iListener);
	            
	            vectorY.save(bos, iListener);
	            vectorZ.save(bos, iListener);
	            
	            bitG.save(bos, iListener);
	            Permutation permutation = PermutationFactory.createPermutation(vectorG, PermutationFactory.PERMUTATION_MRRR_DEFAULT_STEP);
	            permutation.save(bos, iListener);
	            
	            Files.delete(Paths.get(location + "vectorY"));
	            Files.delete(Paths.get(location + "vectorZ"));
	            
	            //Files.delete(Paths.get(location + "bitY"));
	            //Files.delete(Paths.get(location + "bitZ"));
	            bos.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
}
