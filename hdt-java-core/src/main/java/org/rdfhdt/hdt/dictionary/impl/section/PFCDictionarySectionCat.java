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

package org.rdfhdt.hdt.dictionary.impl.section;

import org.rdfhdt.hdt.compact.sequence.SequenceLog64Big;
import org.rdfhdt.hdt.options.HDTOptions;

/**
 * @author Dennis Diefenbach & Jose Gimenez Garcia
 *
 */
public class PFCDictionarySectionCat extends PFCDictionarySectionBig {

	long storedBuffersSize;

	public PFCDictionarySectionCat(HDTOptions spec, int numentries) {
		super(spec);
		this.blocks = new SequenceLog64Big(64, numentries/blocksize);
		this.numstrings = 0;
		this.storedBuffersSize = 0;
		int numBlocks = (int) Math.ceil((double)numentries/blocksize);
		int numBuffers =  (int)Math.ceil((double)numBlocks/BLOCK_PER_BUFFER);
		data = new byte[(int)numBuffers][];
		posFirst = new long[(int)numBuffers];
	}

	public long getStoredBuffersSize() {
		return storedBuffersSize;
	}

	public void setStoredBuffersSize(long storedBuffersSize) {
		this.storedBuffersSize = storedBuffersSize;
	}
	
	public void addToStoredBuffersSize(long toAdd) {
		this.storedBuffersSize += toAdd;
	}

	public void setBlocks(SequenceLog64Big blocks) {
		this.blocks = blocks;
	}

	public int getBlockSize() {
		return this.blocksize;
	}

	public SequenceLog64Big getBlocks() {
		return this.blocks;
	}

	public void setData(byte[][] data) {
		this.data = data;
	}
	
	public void addBuffer(int buffer, byte[] byteArray) {
		this.data[buffer] = byteArray;
	}
	
	public int getNumBuffers() {
		return this.data.length;
	}

	public void append(int size) {
		this.blocks.append(size);
	}

	public int getNumentries(){
		return this.numstrings;
	}

	public void setNumentries(int numstrings){
		this.numstrings = numstrings;
	}
	
	public void setPosFirst(int numBuffer, long position) {
		this.posFirst[numBuffer] = position;
	}

}
