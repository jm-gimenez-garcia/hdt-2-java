/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/iface/org/rdfhdt/hdt/triples/TripleString.java $
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

package org.rdfhdt.hdt.triples;

import java.io.IOException;

import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.util.UnicodeEscape;

/**
 * TripleString holds a triple as Strings
 */
public class TripleString {

	protected CharSequence	subject;
	protected CharSequence	predicate;
	protected CharSequence	object;

	public TripleString() {
		this.subject = this.predicate = this.object = null;
	}

	/**
	 * Basic constructor
	 *
	 * @param subject
	 *            The subject
	 * @param predicate
	 *            The predicate
	 * @param object
	 *            The object
	 */
	public TripleString(final CharSequence subject, final CharSequence predicate, final CharSequence object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	/**
	 * Copy constructor
	 */
	public TripleString(final TripleString other) {
		this.subject = other.subject;
		this.predicate = other.predicate;
		this.object = other.object;
	}

	/**
	 * @return the subject
	 */
	public CharSequence getSubject() {
		return this.subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(final CharSequence subject) {
		this.subject = subject;
	}

	/**
	 * @return the predicate
	 */
	public CharSequence getPredicate() {
		return this.predicate;
	}

	/**
	 * @param predicate
	 *            the predicate to set
	 */
	public void setPredicate(final CharSequence predicate) {
		this.predicate = predicate;
	}

	/**
	 * @return the object
	 */
	public CharSequence getObject() {
		return this.object;
	}

	/**
	 * @param object
	 *            the object to set
	 */
	public void setObject(final CharSequence object) {
		this.object = object;
	}

	/**
	 * Sets all components at once. Useful to reuse existing object instead of creating new ones for performance.
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public void setAll(final CharSequence subject, final CharSequence predicate, final CharSequence object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	public boolean equals(final TripleString other) {
		return !( !this.subject.equals(other.subject) || !this.predicate.equals(other.predicate) || !this.object.equals(other.object) );
	}

	/**
	 * Check whether this triple matches a pattern. A pattern is just a TripleString where each empty component means <em>any</em>.
	 * @param pattern
	 * @return
	 */
	public boolean match(final TripleString pattern) {
		if (pattern.getSubject() == "" || pattern.getSubject().equals(this.subject)) {
			if (pattern.getPredicate() == "" || pattern.getPredicate().equals(this.predicate)) {
				if (pattern.getObject() == "" || pattern.getObject().equals(this.object)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Set all components to ""
	 */
	public void clear() {
		this.subject = this.predicate = this.object = "";
	}

	/**
	 * Checks wether all components are empty.
	 * @return
	 */
	public boolean isEmpty() {
		return this.subject.length()==0 && this.predicate.length()==0 && this.object.length()==0;
	}

	/**
	 * Checks wether any component is empty.
	 * @return
	 */
	public boolean hasEmpty() {
		return this.subject.length()==0 || this.predicate.length()==0 || this.object.length()==0;
	}

	/**
	 * Read from a line, where each component is separated by space.
	 * @param line
	 */
	public void read(String line) throws ParserException {
		int split, posa, posb;
		clear();

		line = line.replaceAll("\\t"," ");

		// SET SUBJECT
		posa = 0;
		posb = split = line.indexOf(' ', posa);

		if(posb==-1)
		{
			return;					// Not found, error.
		}
		if(line.charAt(posa)=='<')
		{
			posa++;		// Remove <
		}
		if(line.charAt(posb-1)=='>')
		{
			posb--;	// Remove >
		}

		setSubject(line.substring(posa, posb));

		// SET PREDICATE
		posa = split+1;
		posb = split = line.indexOf(' ', posa);

		if(posb==-1) {
			return;
		}
		if(line.charAt(posa)=='<') {
			posa++;
		}
		if(posb>posa && line.charAt(posb-1)=='>') {
			posb--;
		}

		setPredicate(line.substring(posa, posb));

		// SET OBJECT
		posa = split+1;
		posb = line.length();

		if(line.charAt(posb-1)=='.')
		{
			posb--;	// Remove trailing <space> <dot> from NTRIPLES.
		}
		if(line.charAt(posb-1)==' ') {
			posb--;
		}

		if(line.charAt(posa)=='<') {
			posa++;

			// Remove trailing > only if < appears, so "some"^^<http://datatype> is kept as-is.
			if(posb>posa && line.charAt(posb-1)=='>') {
				posb--;
			}
		}

		setObject(UnicodeEscape.unescapeString(line.substring(posa, posb)));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.subject + " " + this.predicate + " " + this.object;
	}

	/** Convert TripleString to NTriple */
	public CharSequence asNtriple() throws IOException {
		final StringBuilder str = new StringBuilder();
		dumpNtriple(str);
		return str;
	}

	public void dumpNtriple(final Appendable out) throws IOException {
		final char s0 = this.subject.charAt(0);
		if(s0=='_' || s0=='<') {
			out.append(this.subject);
		} else {
			out.append('<').append(this.subject).append('>');
		}

		final char p0 = this.predicate.charAt(0);
		if(p0=='<') {
			out.append(' ').append(this.predicate).append(' ');
		} else {
			out.append(" <").append(this.predicate).append("> ");
		}

		final char o0 = this.object.charAt(0);
		if(o0=='"') {
			UnicodeEscape.escapeString(this.object.toString(), out);
			out.append(" .\n");
		} else if(o0=='_' ||o0=='<' ) {
			out.append(this.object).append(" .\n");
		} else {
			out.append('<').append(this.object).append("> .\n");
		}
	}
}
