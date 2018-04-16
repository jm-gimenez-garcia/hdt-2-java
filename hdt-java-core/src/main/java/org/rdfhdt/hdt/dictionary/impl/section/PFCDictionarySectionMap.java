/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/section/PFCDictionarySection.java $
 * Revision: $Rev: 94 $
 * Last modified: $Date: 2012-11-20 23:44:36 +0000 (mar, 20 nov 2012) $
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

package org.rdfhdt.hdt.dictionary.impl.section;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.util.Iterator;

import org.rdfhdt.hdt.compact.integer.VByte;
import org.rdfhdt.hdt.compact.sequence.Sequence;
import org.rdfhdt.hdt.compact.sequence.SequenceFactory;
import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.dictionary.DictionarySectionPrivate;
import org.rdfhdt.hdt.exceptions.CRCException;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.util.crc.CRC8;
import org.rdfhdt.hdt.util.crc.CRCInputStream;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.io.IOUtil;
import org.rdfhdt.hdt.util.string.ByteStringUtil;
import org.rdfhdt.hdt.util.string.CompactString;
import org.rdfhdt.hdt.util.string.ReplazableString;

/**
 * @author mario.arias
 *
 */
public class PFCDictionarySectionMap implements DictionarySectionPrivate, Closeable {
    public static final int   TYPE_INDEX	    = 2;
    public static final int   DEFAULT_BLOCK_SIZE    = 16;

    private static final int  BLOCKS_PER_BYTEBUFFER = 50000;
    protected FileChannel     ch;
    protected ByteBuffer[]    buffers;			    // Encoded sequence
    long[]		      posFirst;			    // Global byte position of the start of each buffer
    protected int	      blocksize;
    protected int	      numstrings;
    protected Sequence	      blocks;
    protected FileInputStream fis;
    protected long	      dataSize;

    private final File	      f;
    private final long	      startOffset, endOffset;

    public PFCDictionarySectionMap(final CountInputStream input, final File f) throws IOException {
	this.f = f;
	this.startOffset = input.getTotalBytes();

	final CRCInputStream crcin = new CRCInputStream(input, new CRC8());

	// Read type
	final int type = crcin.read();
	if (type != TYPE_INDEX) { throw new IllegalFormatException("Trying to read a DictionarySectionPFC from data that is not of the suitable type"); }

	// Read vars
	this.numstrings = (int) VByte.decode(crcin);
	this.dataSize = VByte.decode(crcin);
	this.blocksize = (int) VByte.decode(crcin);

	if (!crcin.readCRCAndCheck()) { throw new CRCException("CRC Error while reading Dictionary Section Plain Front Coding Header."); }

	// Read blocks
	this.blocks = SequenceFactory.createStream(input, f);
	// blocks = SequenceFactory.createStream(input);
	// blocks.load(input, null);

	final long base = input.getTotalBytes();
	IOUtil.skip(crcin, this.dataSize + 4); // Including CRC32

	this.endOffset = input.getTotalBytes();

	// Read packed data
	this.ch = FileChannel.open(Paths.get(f.toString()));
	int block = 0;
	int buffer = 0;
	final long numBlocks = this.blocks.getNumberOfElements();
	long bytePos = 0;
	final long numBuffers = 1 + numBlocks / BLOCKS_PER_BYTEBUFFER;
	this.buffers = new ByteBuffer[(int) numBuffers];
	this.posFirst = new long[(int) numBuffers];

	// System.out.println("Buffers "+buffers.length);
	while (block < numBlocks - 1) {
	    final int nextBlock = (int) Math.min(numBlocks - 1, block + BLOCKS_PER_BYTEBUFFER);
	    final long nextBytePos = this.blocks.get(nextBlock);

	    // System.out.println("From block "+block+" to "+nextBlock);
	    // System.out.println("From pos "+ bytePos+" to "+nextBytePos);
	    // System.out.println("Total size: "+ (nextBytePos-bytePos));
	    this.buffers[buffer] = this.ch.map(MapMode.READ_ONLY, base + bytePos, nextBytePos - bytePos);
	    this.buffers[buffer].order(ByteOrder.LITTLE_ENDIAN);

	    this.posFirst[buffer] = bytePos;

	    bytePos = nextBytePos;
	    block += BLOCKS_PER_BYTEBUFFER;
	    buffer++;
	}
    }

    protected int locateBlock(final CharSequence str) {
	if (this.blocks.getNumberOfElements() == 0) { return -1; }

	int low = 0;
	int high = (int) this.blocks.getNumberOfElements() - 1;
	final int max = high;

	while (low <= high) {
	    final int mid = low + (high - low) / 2;

	    int cmp;
	    if (mid == max) {
		cmp = -1;
	    } else {
		final ByteBuffer buffer = this.buffers[mid / BLOCKS_PER_BYTEBUFFER];
		cmp = ByteStringUtil.strcmp(str, buffer, (int) (this.blocks.get(mid) - this.posFirst[mid / BLOCKS_PER_BYTEBUFFER]));
	    }
	    if (cmp < 0) {
		high = mid - 1;
	    } else if (cmp > 0) {
		low = mid + 1;
	    } else {
		return mid; // key found
	    }
	}
	return -(low + 1); // key not found.
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#locate(java.lang.CharSequence)
     */
    @Override
    public int locate(final CharSequence str) {
	if (this.buffers == null || this.blocks == null) { return 0; }

	int blocknum = this.locateBlock(str);
	if (blocknum >= 0) {
	    // Located exactly
	    return (blocknum * this.blocksize) + 1;
	} else {
	    // Not located exactly.
	    blocknum = -blocknum - 2;

	    if (blocknum >= 0) {
		final int idblock = this.locateInBlock(blocknum, str);

		if (idblock != 0) { return (blocknum * this.blocksize) + idblock + 1; }
	    }
	}

	return 0;
    }

    public int locateInBlock(final int block, final CharSequence str) {
	if (block >= this.blocks.getNumberOfElements()) { return 0; }

	final ReplazableString tempString = new ReplazableString();

	int idInBlock = 0;
	int cshared = 0;

	// dumpBlock(block);

	final ByteBuffer buffer = this.buffers[block / BLOCKS_PER_BYTEBUFFER].duplicate();
	buffer.position((int) (this.blocks.get(block) - this.posFirst[block / BLOCKS_PER_BYTEBUFFER]));

	try {
	    if (!buffer.hasRemaining()) { return 0; }

	    // Read the first string in the block
	    tempString.replace(buffer, 0);

	    idInBlock++;

	    while ((idInBlock < this.blocksize) && buffer.hasRemaining()) {
		// Decode prefix
		final long delta = VByte.decode(buffer);

		// Copy suffix
		tempString.replace(buffer, (int) delta);

		if (delta >= cshared) {
		    // Current delta value means that this string
		    // has a larger long common prefix than the previous one
		    cshared += ByteStringUtil.longestCommonPrefix(tempString, str, cshared);

		    if ((cshared == str.length()) && (tempString.length() == str.length())) { return idInBlock; }
		} else {
		    // We have less common characters than before,
		    // this string is bigger that what we are looking for.
		    // i.e. Not found.
		    return 0;
		}
		idInBlock++;
	    }
	    return 0;
	} catch (final IOException e) {
	    e.printStackTrace();
	    return 0;
	}
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#extract(int)
     */
    @Override
    public CharSequence extract(final int id) {
	if (this.buffers == null || this.blocks == null) { return null; }

	if (id < 1 || id > this.numstrings) { return null; }

	final int block = (id - 1) / this.blocksize;
	final ByteBuffer buffer = this.buffers[block / BLOCKS_PER_BYTEBUFFER].duplicate();
	buffer.position((int) (this.blocks.get(block) - this.posFirst[block / BLOCKS_PER_BYTEBUFFER]));

	try {
	    final ReplazableString tempString = new ReplazableString();
	    tempString.replace(buffer, 0);

	    final int stringid = (id - 1) % this.blocksize;
	    for (int i = 0; i < stringid; i++) {
		final long delta = VByte.decode(buffer);
		tempString.replace(buffer, (int) delta);
	    }
	    return new CompactString(tempString).getDelayed();
	} catch (final IOException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#size()
     */
    @Override
    public long size() {
	return this.dataSize + this.blocks.size();
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#getNumberOfElements()
     */
    @Override
    public int getNumberOfElements() {
	return this.numstrings;
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#getEntries()
     */
    @Override
    public Iterator<CharSequence> getSortedEntries() {
	return new Iterator<CharSequence>() {
	    int		     id;

	    ReplazableString tempString	= new ReplazableString();
	    int		     bytebufferIndex;
	    ByteBuffer	     buffer	= PFCDictionarySectionMap.this.buffers[0].duplicate();

	    @Override
	    public boolean hasNext() {
		return this.id < PFCDictionarySectionMap.this.getNumberOfElements();
	    }

	    @Override
	    public CharSequence next() {
		if (!this.buffer.hasRemaining()) {
		    this.buffer = PFCDictionarySectionMap.this.buffers[++this.bytebufferIndex].duplicate();
		    this.buffer.rewind();
		}
		try {
		    if ((this.id % PFCDictionarySectionMap.this.blocksize) == 0) {
			this.tempString.replace(this.buffer, 0);
		    } else {
			final long delta = VByte.decode(this.buffer);
			this.tempString.replace(this.buffer, (int) delta);
		    }
		    this.id++;
		    return new CompactString(this.tempString).getDelayed();
		    // return tempString.toString();
		} catch (final IOException e) {
		    throw new RuntimeException(e);
		}
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	};
    }

    @Override
    public void close() throws IOException {
	this.blocks.close();
	this.buffers = null;
	System.gc();
	this.ch.close();
    }

    @Override
    public void load(final DictionarySection other, final ProgressListener listener) {
	throw new NotImplementedException();
    }

    @Override
    public void save(final OutputStream output, final ProgressListener listener) throws IOException {
	final InputStream in = new BufferedInputStream(new FileInputStream(this.f));
	IOUtil.skip(in, this.startOffset);
	IOUtil.copyStream(in, output, this.endOffset - this.startOffset);
	in.close();
    }

    @Override
    public void load(final InputStream input, final ProgressListener listener)
	    throws IOException {
	throw new NotImplementedException();
    }
}
