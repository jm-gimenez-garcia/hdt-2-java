/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/iface/org/rdfhdt/hdt/triples/TripleID.java $
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



/**
 * TripleID holds a triple as integers
 *
 */
public class TripleID implements Comparable<TripleID> {

	private int	subject		= 0;
	private int	predicate	= 0;
	private int	object		= 0;

	/**
	 * Basic constructor
	 */
	public TripleID() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param subject
	 *            The subject
	 * @param predicate
	 *            The predicate
	 * @param object
	 *            The object
	 */
	public TripleID(final int subject, final int predicate, final int object) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	/**
	 * Build a TripleID as a copy of another one.
	 * @param other
	 */
	public TripleID(final TripleID other) {
		super();
		this.subject = other.subject;
		this.predicate = other.predicate;
		this.object = other.object;
	}

	/**
	 * @return the subject
	 */
	public int getSubject() {
		return this.subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(final int subject) {
		this.subject = subject;
	}

	/**
	 * @return the object
	 */
	public int getObject() {
		return this.object;
	}

	/**
	 * @param object
	 *            the object to set
	 */
	public void setObject(final int object) {
		this.object = object;
	}

	/**
	 * @return the predicate
	 */
	public int getPredicate() {
		return this.predicate;
	}

	/**
	 * @param predicate
	 *            the predicate to set
	 */
	public void setPredicate(final int predicate) {
		this.predicate = predicate;
	}

	/**
	 * Replace all components of a TripleID at once. Useful to reuse existing objects.
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public void setAll(final int subject, final int predicate, final int object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	public void assign(final TripleID replacement) {
		this.subject = replacement.getSubject();
		this.object = replacement.getObject();
		this.predicate = replacement.getPredicate();
	}

	/**
	 * Set all components to zero.
	 */
	public void clear() {
		this.subject = this.predicate = this.object = 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(this.subject) + " " + this.predicate + " " + this.object;
	}

	public boolean equals(final TripleID other) {
		return !( this.subject!=other.subject || this.predicate!=other.predicate || this.object!=other.object );
	}

	/**
	 * Compare TripleID to another one using SPO Order.
	 * To compare using other orders use {@link TripleStringComparator}
	 */
	@Override
	public int compareTo(final TripleID other) {
		int result = this.subject - other.subject;

		if(result==0) {
			result = this.predicate - other.predicate;
			if(result==0) {
				return this.object - other.object;
			} else {
				return result;
			}
		} else {
			return result;
		}

	}

	/**
	 * Check whether this triple matches a pattern of TripleID. 0 acts as a wildcard
	 *
	 * @param pattern
	 *            The pattern to match against
	 * @return boolean
	 */
	public boolean match(final TripleID pattern) {

		// get the components of the pattern
		final int subjectPattern = pattern.getSubject();
		final int predicatePattern = pattern.getPredicate();
		final int objectPattern = pattern.getObject();

		/* Remember that 0 acts as a wildcard */
		if (subjectPattern == 0 || this.subject == subjectPattern) {
			if (predicatePattern == 0 || this.predicate == predicatePattern) {
				if (objectPattern == 0 || this.object == objectPattern) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check whether all the components of the triple are empty (zero).
	 * @return
	 */
	public boolean isEmpty() {
		return !(this.subject != 0 || this.predicate != 0 || this.object != 0);
	}

	/**
	 * Check whether none of the components of the triple are empty.
	 * @return
	 */
	public boolean isValid() {
		return this.subject>0 && this.predicate>0 && this.object>0;
	}

	/**
	 * Get the pattern of the triple as String, such as "SP?".
	 * @return
	 */
	public String getPatternString() {
		return "" +
				(this.subject==0   ? '?' : 'S') +
				(this.predicate==0 ? '?' : 'P') +
				(this.object==0    ? '?' : 'O');
	}

	/** size of one TripleID in memory */
	public static int size(){
		return 24;
	}

}
