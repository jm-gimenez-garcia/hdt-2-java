/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/util/string/ReplazableString.java $
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
 */

package org.rdfhdt.hdt.util.string;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.rdfhdt.hdt.exceptions.NotImplementedException;


/**
 * CharSequence implementation suitable for appending or replacing the suffix of the string.
 * It grows as necessary but it never returns that size back.
 *
 * @author mario.arias
 *
 */
public final class ReplazableString implements ComparableCharSequence {

    byte [] buffer;
    int used;
    /**
     *
     */
    public ReplazableString() {
	this.buffer = new byte[16*1024];
	this.used=0;
    }

    private ReplazableString(final byte [] buffer) {
	this.buffer = buffer;
	this.used = buffer.length;
    }

    public byte [] getBuffer() {
	return this.buffer;
    }

    private void ensureSize(final int size) {
	if(size>this.buffer.length) {
	    this.buffer = Arrays.copyOf(this.buffer, size);
	}
    }

    public void append(final byte [] data, final int offset, final int len) {
	this.replace(this.used, data, offset, len);
    }

    public void append(final CharSequence other) {
	this.ensureSize(this.used+other.length());
	for(int i=0;i<other.length();i++) {
	    this.buffer[this.used+i] = (byte) other.charAt(i);
	}
	this.used+=other.length();
    }


    public void replace(final int pos, final byte [] data, final int offset, final int len) {
	this.ensureSize(pos+len);
	System.arraycopy(data, offset, this.buffer, pos, len);
	this.used = pos+len;
    }

    public void replace(final InputStream in, final int pos, final int len) throws IOException {
	this.ensureSize(pos+len);
	in.read(this.buffer, pos, len);
	this.used = pos+len;
    }

    public void replace(final ByteBuffer in, final int pos, final int len) throws IOException {
	this.ensureSize(pos+len);
	in.get(this.buffer, pos, len);
	this.used = pos+len;
    }

    public void replace2(final InputStream in, final int pos) throws IOException {
	this.used = pos;

	while(true) {
	    final int value = in.read();
	    if(value==-1) {
		throw new IllegalArgumentException("Was reading a string but stream ended before finding the null terminator");
	    }
	    if(value==0) {
		break;
	    }
	    if(this.used>=this.buffer.length) {
		this.buffer = Arrays.copyOf(this.buffer, this.buffer.length*2);
	    }
	    this.buffer[this.used++] = (byte)(value&0xFF);
	}
    }

    private static final int READ_AHEAD = 1024;

    public void replace(final InputStream in, final int pos) throws IOException {

	if(!in.markSupported()) {
	    this.replace2(in,pos);
	    return;
	}
	this.used = pos;


	while(true) {
	    if(this.used+READ_AHEAD>this.buffer.length) {
		this.buffer = Arrays.copyOf(this.buffer, Math.max(this.buffer.length*2, this.used+READ_AHEAD));
	    }
	    in.mark(READ_AHEAD);
	    final int numread = in.read(this.buffer, this.used, READ_AHEAD);
	    if(numread==-1){
		throw new IllegalArgumentException("Was reading a string but stream ended before finding the null terminator");
	    }

	    int i=0;
	    while(i<numread) {
		//				System.out.println("Char: "+buffer[used+i]+"/"+(char)buffer[used+i]);
		if(this.buffer[this.used+i]==0) {
		    in.reset();
		    in.skip(i+1);
		    this.used+=i;
		    return;
		}
		i++;
	    }
	    this.used+=numread;
	}
    }

    public void replace(final ByteBuffer in, final int pos) throws IOException {
	this.used = pos;

	int n = in.capacity()-in.position();
	while(n-- != 0) {
	    final byte value = in.get();
	    if(value==0) {
		return;
	    }
	    if(this.used>=this.buffer.length) {
		this.buffer = Arrays.copyOf(this.buffer, this.buffer.length*2);
	    }
	    this.buffer[this.used++] = value;
	}
	throw new IllegalArgumentException("Was reading a string but stream ended before finding the null terminator");
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#charAt(int)
     */
    @Override
    public char charAt(final int index) {
	return (char)(this.buffer[index] & 0xFF);
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#length()
     */
    @Override
    public int length() {
	return this.used;
    }

    @Override
    public int hashCode() {
	// FNV Hash function: http://isthe.com/chongo/tech/comp/fnv/
	int hash = (int) 2166136261L;
	int i = this.used;

	while(i-- != 0) {
	    hash = 	(hash * 16777619) ^ this.buffer[i];
	}

	return hash;
    }

    @Override
    public boolean equals(final Object o) {
	if(o==null) {
	    return false;
	}
	if(this==o) {
	    return true;
	}
	if(o instanceof CompactString) {
	    final CompactString cmp = (CompactString) o;
	    if(this.buffer.length!=cmp.data.length) {
		return false;
	    }

	    // Byte by byte comparison
	    int i = this.buffer.length;
	    while(i-- != 0) {
		if(this.buffer[i]!=cmp.data[i]) {
		    return false;
		}
	    }
	    return true;
	} else if(o instanceof ReplazableString) {
	    final ReplazableString cmp = (ReplazableString) o;
	    if(this.used!=cmp.used) {
		return false;
	    }

	    // Byte by byte comparison
	    int i = this.used;
	    while(i-- != 0) {
		if(this.buffer[i]!=cmp.buffer[i]) {
		    return false;
		}
	    }
	    return true;
	} else if (o instanceof CharSequence) {
	    final CharSequence other = (CharSequence) o;
	    return this.length()==other.length() && CharSequenceComparator.getInstance().compare(this, other)==0;
	}
	throw new NotImplementedException();
    }


    /* (non-Javadoc)
     * @see java.lang.CharSequence#subSequence(int, int)
     */
    @Override
    public CharSequence subSequence(final int start, final int end) {
	if (start < 0 || end > (this.length()) || (end-start)<0) {
	    throw new IllegalArgumentException("Illegal range " +
		    start + "-" + end + " for sequence of length " + this.length());
	}
	final byte [] newdata = new byte[end-start];
	System.arraycopy(this.buffer, start, newdata, 0, end-start);
	return new ReplazableString(newdata);
    }

    @Override
    public String toString() {
	return new String(this.buffer, 0, this.used, ByteStringUtil.STRING_ENCODING);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final ComparableCharSequence other) {

	final ReplazableString otherReplazableString = other instanceof ReplazableString ? (ReplazableString) other : new ReplazableString(other.toString().getBytes());

	final int n = Math.min(this.used, otherReplazableString.used);

	int k = 0;
	while (k < n) {
	    final int c1 = this.buffer[k] & 0xFF;
	    final int c2 = otherReplazableString.buffer[k] & 0xFF;
	    if (c1 != c2) {
		return c1 - c2;
	    }
	    k++;
	}
	return this.used - otherReplazableString.used;
    }

    public CharSequence getDelayed() {
	return new DelayedString(this);
    }
}
