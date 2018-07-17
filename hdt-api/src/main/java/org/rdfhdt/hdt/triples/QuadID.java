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
public final class QuadID extends TripleID {

	protected int graph;

	public QuadID() {
		super();
	}

	public QuadID(final int subject, final int predicate, final int object, final int graph) {
		super(subject, predicate, object);
		this.graph = graph;
	}

	public QuadID(final TripleID other, final int graph) {
		super(other);
		this.graph = graph;
	}

	public QuadID(final TripleID other) {
		super(other);
		this.graph = other instanceof QuadID ? ((QuadID) other).getGraph() : 0;
	}

	public int getGraph() {
		return this.graph;
	}

	public void setGraph(final int graph) {
		this.graph = graph;
	}

	public void setAll(final int subject, final int predicate, final int object, final int graph) {
		super.setAll(subject, predicate, object);
		this.graph = graph;
	}

	@Override
	public void assign(final TripleID replacement) {
		super.assign(replacement);
		this.graph = replacement instanceof QuadID ? ((QuadID) replacement).getGraph() : 0;
	}

	/**
	 * Set all components to zero.
	 */
	@Override
	public void clear() {
		super.clear();
		this.graph = 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " " + this.graph;
	}

	@Override
	public boolean equals(final TripleID other) {
		return (super.equals(other) && other instanceof QuadID ? (this.graph == ((QuadID) other).getGraph()) : true);
	}

	@Override
	public int compareTo(final TripleID other) {
		int result = super.compareTo(other);
		if (result == 0 && other instanceof QuadID) {
			result = this.graph - ((QuadID) other).getGraph();
		}
		return result;
	}

	@Override
	public boolean match(final TripleID pattern) {
		boolean result = true;

		if (pattern instanceof QuadID) {
			final int graphPattern = ((QuadID) pattern).getGraph();
			result = (graphPattern == 0 || getGraph() == graphPattern);
		}

		return result && super.match(pattern);
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && this.graph == 0;
	}

	@Override
	public boolean isValid() {
		return super.isValid() && this.graph > 0;
	}

	@Override
	public String getPatternString() {
		return super.getPatternString() + (this.graph == 0 ? '?' : 'G');
	}

	/** size of one TripleID in memory */
	public static int size(){
		return 24;
	}

}
