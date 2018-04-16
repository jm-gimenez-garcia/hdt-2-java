/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/util/string/CompactString.java $
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
 */

package org.rdfhdt.hdt.util.string;

import java.io.Serializable;
import java.util.Arrays;

import org.rdfhdt.hdt.exceptions.NotImplementedException;

/**
 * Implementation of CharSequence that uses only one byte per character to save memory.
 * The String length is defined by the buffer size.
 * 
 * @author mario.arias
 *
 */
public class CompactString implements CharSequence, Serializable, Comparable<CompactString> {

    private static final long	      serialVersionUID = 6789858615261959413L;

    // String buffer as bytes.
    final byte[]		      data;

    // Cached hash value.
    private int			      hash;

    public static final CompactString EMPTY	       = new CompactString();

    private CompactString() {
	this.data = new byte[0];
    }

    public CompactString(final ReplazableString str) {
	this.data = Arrays.copyOf(str.buffer, str.used);
    }

    public CompactString(final CompactString other) {
	this.data = Arrays.copyOf(other.data, other.data.length);
    }

    public CompactString(final String other) {
	this.data = other.getBytes(ByteStringUtil.STRING_ENCODING);
    }

    public CompactString(final CharSequence other) {
	this.data = other.toString().getBytes(ByteStringUtil.STRING_ENCODING);
    }

    public byte[] getData() {
	return this.data;
    }

    private CompactString(final byte[] data) {
	this.data = data;
    }

    @Override
    public char charAt(final int index) {
	final int ix = index;
	if (ix >= this.data.length) { throw new StringIndexOutOfBoundsException("Invalid index " + index + " length " + this.length()); }
	return (char) (this.data[ix] & 0xff);
    }

    @Override
    public int length() {
	return this.data.length;
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
	if (start < 0 || end > (this.length()) || (end - start) < 0) { throw new IllegalArgumentException("Illegal range " +
		start + "-" + end + " for sequence of length " + this.length()); }
	final byte[] newdata = new byte[end - start];
	System.arraycopy(this.data, start, newdata, 0, end - start);
	return new CompactString(newdata);
    }

    @Override
    public String toString() {
	return new String(this.data, 0, this.data.length, ByteStringUtil.STRING_ENCODING);
    }

    @Override
    public int hashCode() {
	// FNV Hash function: http://isthe.com/chongo/tech/comp/fnv/
	if (this.hash == 0) {
	    this.hash = (int) 2166136261L;
	    int i = this.data.length;

	    while (i-- != 0) {
		this.hash = (this.hash * 16777619) ^ this.data[i];
	    }
	}
	return this.hash;
    }

    @Override
    public boolean equals(final Object o) {
	if (o == null) { return false; }
	if (this == o) { return true; }
	if (o instanceof CompactString) {
	    final CompactString cmp = (CompactString) o;
	    if (this.data.length != cmp.data.length) { return false; }

	    // Byte by byte comparison
	    int i = this.data.length;
	    while (i-- != 0) {
		if (this.data[i] != cmp.data[i]) { return false; }
	    }
	    return true;
	} else if (o instanceof CharSequence) {
	    final CharSequence other = (CharSequence) o;
	    return this.length() == other.length() && CharSequenceComparator.getInstance().compare(this, other) == 0;
	}
	throw new NotImplementedException();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final CompactString other) {
	final int n = Math.min(this.data.length, other.data.length);

	int k = 0;
	while (k < n) {
	    final int c1 = this.data[k] & 0xFF;
	    final int c2 = other.data[k] & 0xFF;
	    if (c1 != c2) { return c1 - c2; }
	    k++;
	}
	return this.data.length - other.data.length;
    }

    public CharSequence getDelayed() {
	return new DelayedString(this);
    }
}
