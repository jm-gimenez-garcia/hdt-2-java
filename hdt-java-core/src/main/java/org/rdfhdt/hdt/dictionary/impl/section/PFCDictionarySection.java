/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/section/PFCDictionarySection.java $
 * Revision: $Rev: 201 $
 * Last modified: $Date: 2013-04-17 23:40:20 +0100 (mié, 17 abr 2013) $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.rdfhdt.hdt.compact.integer.VByte;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64;
import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.dictionary.DictionarySectionPrivate;
import org.rdfhdt.hdt.exceptions.CRCException;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.util.BitUtil;
import org.rdfhdt.hdt.util.Mutable;
import org.rdfhdt.hdt.util.crc.CRC32;
import org.rdfhdt.hdt.util.crc.CRC8;
import org.rdfhdt.hdt.util.crc.CRCInputStream;
import org.rdfhdt.hdt.util.crc.CRCOutputStream;
import org.rdfhdt.hdt.util.io.IOUtil;
import org.rdfhdt.hdt.util.string.ByteStringUtil;
import org.rdfhdt.hdt.util.string.CompactString;
import org.rdfhdt.hdt.util.string.ReplazableString;

/**
 * @author mario.arias
 *
 */
public class PFCDictionarySection implements DictionarySectionPrivate {
    public static final int TYPE_INDEX	       = 2;
    public static final int DEFAULT_BLOCK_SIZE = 16;

    // FIXME: Due to java array indexes being int, only 2GB can be addressed per dictionary section.
    protected byte[]	    text;		    // Encoded sequence
    protected int	    blocksize;
    protected int	    numstrings;
    protected SequenceLog64 blocks;

    public PFCDictionarySection(final HDTOptions spec) {
	this.blocksize = (int) spec.getInt("pfc.blocksize");
	if (this.blocksize == 0) {
	    this.blocksize = DEFAULT_BLOCK_SIZE;
	}
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#load(hdt.dictionary.DictionarySection)
     */
    @Override
    public void load(final DictionarySection other, final ProgressListener listener) {
	this.blocks = new SequenceLog64(BitUtil.log2(other.size()), other.getNumberOfElements() / this.blocksize);
	final Iterator<? extends CharSequence> it = other.getSortedEntries();
	this.load((Iterator<CharSequence>) it, other.getNumberOfElements(), listener);
    }

    public void load(final Iterator<CharSequence> it, final long numentries, final ProgressListener listener) {
	this.blocks = new SequenceLog64(32, numentries / this.blocksize);
	this.numstrings = 0;

	final ByteArrayOutputStream byteOut = new ByteArrayOutputStream(16 * 1024);

	CharSequence previousStr = null;

	try {
	    while (it.hasNext()) {
		final CharSequence str = it.next();

		if (this.numstrings % this.blocksize == 0) {
		    // Add new block pointer
		    this.blocks.append(byteOut.size());

		    // Copy full string
		    ByteStringUtil.append(byteOut, str, 0);
		} else {
		    // Find common part.
		    final int delta = ByteStringUtil.longestCommonPrefix(previousStr, str);
		    // Write Delta in VByte
		    VByte.encode(byteOut, delta);
		    // Write remaining
		    ByteStringUtil.append(byteOut, str, delta);
		}
		byteOut.write(0); // End of string

		this.numstrings++;
		previousStr = str;
	    }

	    // Ending block pointer.
	    this.blocks.append(byteOut.size());

	    // Trim text/blocks
	    this.blocks.aggresiveTrimToSize();

	    byteOut.flush();
	    this.text = byteOut.toByteArray();

	    // DEBUG
	    // dumpAll();
	} catch (final IOException e) {
	    e.printStackTrace();
	}
    }

    protected int locateBlock(final CharSequence str) {
	if (this.blocks.getNumberOfElements() == 0) { return -1; }

	int low = 0;
	int high = (int) this.blocks.getNumberOfElements() - 1;
	final int max = high;

	while (low <= high) {
	    final int mid = (low + high) >>> 1;

	    int cmp;
	    if (mid == max) {
		cmp = -1;
	    } else {
		final int pos = (int) this.blocks.get(mid);
		cmp = ByteStringUtil.strcmp(str, this.text, pos);
		// System.out.println("Comparing against block: "+ mid + " which is "+ ByteStringUtil.asString(text, pos)+ " Result: "+cmp);
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
	if (this.text == null || this.blocks == null) { return 0; }

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

	int pos = (int) this.blocks.get(block);
	final ReplazableString tempString = new ReplazableString();

	final Mutable<Long> delta = new Mutable<>(0L);
	int idInBlock = 0;
	int cshared = 0;

	// dumpBlock(block);

	// Read the first string in the block
	int slen = ByteStringUtil.strlen(this.text, pos);
	tempString.append(this.text, pos, slen);
	pos += slen + 1;
	idInBlock++;

	while ((idInBlock < this.blocksize) && (pos < this.text.length)) {
	    // Decode prefix
	    pos += VByte.decode(this.text, pos, delta);

	    // Copy suffix
	    slen = ByteStringUtil.strlen(this.text, pos);
	    tempString.replace(delta.getValue().intValue(), this.text, pos, slen);

	    if (delta.getValue() >= cshared) {
		// Current delta value means that this string
		// has a larger long common prefix than the previous one
		cshared += ByteStringUtil.longestCommonPrefix(tempString, str, cshared);

		if ((cshared == str.length()) && (tempString.length() == str.length())) {
		    break;
		}
	    } else {
		// We have less common characters than before,
		// this string is bigger that what we are looking for.
		// i.e. Not found.
		idInBlock = 0;
		break;
	    }
	    pos += slen + 1;
	    idInBlock++;

	}

	if (pos >= this.text.length || idInBlock == this.blocksize) {
	    idInBlock = 0;
	}

	return idInBlock;
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#extract(int)
     */
    @Override
    public CharSequence extract(final int id) {
	if (this.text == null || this.blocks == null) { return null; }

	if (id < 1 || id > this.numstrings) { return null; }

	final int block = (id - 1) / this.blocksize;
	final int stringid = (id - 1) % this.blocksize;
	int pos = (int) this.blocks.get(block);
	int len = ByteStringUtil.strlen(this.text, pos);

	final Mutable<Long> delta = new Mutable<>(0L);
	final ReplazableString tempString = new ReplazableString();
	tempString.append(this.text, pos, len);

	for (int i = 0; i < stringid; i++) {
	    pos += len + 1;
	    pos += VByte.decode(this.text, pos, delta);
	    len = ByteStringUtil.strlen(this.text, pos);
	    tempString.replace(delta.getValue().intValue(), this.text, pos, len);
	}
	return new CompactString(tempString).getDelayed();
    }

    // private void dumpAll() {
    // for(int i=0;i<blocks.getNumberOfElements();i++) {
    // dumpBlock(i);
    // }
    // }
    //
    // private void dumpBlock(int block) {
    // if(text==null || blocks==null || block>=blocks.getNumberOfElements()) {
    // return;
    // }
    //
    // System.out.println("Dump block "+block);
    // ReplazableString tempString = new ReplazableString();
    // Mutable<Integer> delta = new Mutable<Integer>(0);
    // int idInBlock = 0;
    //
    // int pos = (int)blocks.get(block);
    //
    // // Copy first string
    // int len = ByteStringUtil.strlen(text, pos);
    // tempString.append(text, pos, len);
    // pos+=len+1;
    //
    // System.out.println((block*blocksize+idInBlock)+ " ("+idInBlock+") => "+ tempString);
    // idInBlock++;
    //
    // while( (idInBlock<blocksize) && (pos<text.length)) {
    // pos += VByte.decode(text, pos, delta);
    //
    // len = ByteStringUtil.strlen(text, pos);
    // tempString.replace(delta.getValue(), text, pos, len);
    //
    // System.out.println((block*blocksize+idInBlock)+ " ("+idInBlock+") => "+ tempString + " Delta="+delta.getValue()+ " Len="+len);
    //
    // pos+=len+1;
    // idInBlock++;
    // }
    // }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#size()
     */
    @Override
    public long size() {
	return this.text.length + this.blocks.size();
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
	    int		     pos;
	    Mutable<Long>    delta	= new Mutable<>(0L);
	    ReplazableString tempString	= new ReplazableString();

	    @Override
	    public boolean hasNext() {
		return this.id < PFCDictionarySection.this.getNumberOfElements();
	    }

	    @Override
	    public CharSequence next() {
		int len;
		if ((this.id % PFCDictionarySection.this.blocksize) == 0) {
		    len = ByteStringUtil.strlen(PFCDictionarySection.this.text, this.pos);
		    this.tempString.replace(0, PFCDictionarySection.this.text, this.pos, len);
		} else {
		    this.pos += VByte.decode(PFCDictionarySection.this.text, this.pos, this.delta);
		    len = ByteStringUtil.strlen(PFCDictionarySection.this.text, this.pos);
		    this.tempString.replace(this.delta.getValue().intValue(), PFCDictionarySection.this.text, this.pos, len);
		}
		this.pos += len + 1;
		this.id++;
		return new CompactString(this.tempString).getDelayed();
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	};
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#save(java.io.OutputStream, hdt.ProgressListener)
     */
    @Override
    public void save(final OutputStream output, final ProgressListener listener) throws IOException {
	final CRCOutputStream out = new CRCOutputStream(output, new CRC8());

	out.write(TYPE_INDEX);
	VByte.encode(out, this.numstrings);
	VByte.encode(out, this.text.length);
	VByte.encode(out, this.blocksize);

	out.writeCRC();

	this.blocks.save(output, listener); // Write blocks directly to output, they have their own CRC check.

	out.setCRC(new CRC32());
	IOUtil.writeBuffer(out, this.text, 0, this.text.length, listener);
	out.writeCRC();
    }

    /*
     * (non-Javadoc)
     * @see hdt.dictionary.DictionarySection#load(java.io.InputStream, hdt.ProgressListener)
     */
    @Override
    public void load(final InputStream input, final ProgressListener listener) throws IOException {
	final CRCInputStream in = new CRCInputStream(input, new CRC8());

	// Read type
	final int type = in.read();
	if (type != TYPE_INDEX) { throw new IllegalFormatException("Trying to read a DictionarySectionPFC from data that is not of the suitable type"); }

	// Read vars
	this.numstrings = (int) VByte.decode(in);
	final long bytes = VByte.decode(in);
	this.blocksize = (int) VByte.decode(in);

	if (!in.readCRCAndCheck()) { throw new CRCException("CRC Error while reading Dictionary Section Plain Front Coding Header."); }

	if (bytes > Integer.MAX_VALUE) {
	    input.reset();
	    throw new IllegalArgumentException("This class cannot process files with a packed buffer bigger than 2GB");
	}

	// Read blocks
	this.blocks = new SequenceLog64();
	this.blocks.load(input, listener); // Read blocks from input, they have their own CRC check.

	// Read packed data
	in.setCRC(new CRC32());
	this.text = IOUtil.readBuffer(in, (int) bytes, listener);
	if (!in.readCRCAndCheck()) { throw new CRCException("CRC Error while reading Dictionary Section Plain Front Coding Data."); }
    }

    @Override
    public void close() throws IOException {
	this.text = null;
	this.blocks.close();
	this.blocks = null;
    }
}
