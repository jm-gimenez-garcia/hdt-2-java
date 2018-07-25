/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/triples/impl/TriplesList.java $
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

package org.rdfhdt.hdt.triples.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.iterator.SequentialSearchIteratorTripleID;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.QuadIDComparator;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.util.io.IOUtil;
import org.rdfhdt.hdt.util.listener.ListenerUtil;


/**
 * Implementation of TempTriples using a List of TripleID.
 *
 */
public class QuadsList extends TriplesList {

	public QuadsList(final HDTOptions specification) {
		super(specification);
	}

	@Override
	public IteratorTripleID search(final TripleID pattern) {
		final String patternStr = pattern.getPatternString();
		if (patternStr.equals("???") || patternStr.equals("????")) {
			return new TriplesListIterator(this);
		} else {
			return new SequentialSearchIteratorTripleID(pattern, new TriplesListIterator(this));
		}
	}

	@Override
	public IteratorTripleID searchAll() {
		final QuadID all = new QuadID(0, 0, 0, 0);
		return search(all);
	}

	@Override
	public long size() {
		return getNumberOfElements() * QuadID.size();
	}


	@Override
	public void save(final OutputStream output, final ControlInfo controlInformation, final ProgressListener listener) throws IOException {
		controlInformation.clear();
		controlInformation.setInt("numTriples", getNumberOfElements());
		controlInformation.setFormat(HDTVocabulary.QUADS_TYPE_TRIPLESLIST);
		controlInformation.setInt("order", getOrder().ordinal());
		controlInformation.save(output);

		final DataOutputStream dout = new DataOutputStream(output);
		int count = 0;
		for (final TripleID triple : this.arrayOfTriples) {
			if(triple.isValid()) {
				dout.writeInt(triple.getSubject());
				dout.writeInt(triple.getPredicate());
				dout.writeInt(triple.getObject());
				dout.writeInt(triple instanceof QuadID ? ((QuadID) triple).getGraph() : 0);
				ListenerUtil.notifyCond(listener, "Saving TriplesList", count, this.arrayOfTriples.size());
			}
			count++;
		}
	}

	@Override
	public void load(final InputStream input, final ControlInfo controlInformation, final ProgressListener listener) throws IOException {
		setOrder(TripleComponentOrder.values()[(int) controlInformation.getInt("order")]);
		final long totalTriples = controlInformation.getInt("numTriples");

		int numRead=0;

		while(numRead<totalTriples) {
			this.arrayOfTriples.add(new QuadID(IOUtil.readInt(input), IOUtil.readInt(input), IOUtil.readInt(input), IOUtil.readInt(input)));
			numRead++;
			this.numValidTriples++;
			ListenerUtil.notifyCond(listener, "Loading TriplesList", numRead, totalTriples);
		}

		this.sorted = false;
	}

	public boolean insert(final int subject, final int predicate, final int object, final int graph) {
		this.arrayOfTriples.add(new QuadID(subject, predicate, object, graph));
		this.numValidTriples++;
		this.sorted = false;
		return true;
	}

	public boolean update(final QuadID quad, final int subj, final int pred, final int obj, final int graph) {
		if (quad == null) {
			return false;
		}
		quad.setAll(subj, pred, obj, graph);
		this.sorted = false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hdt.triples.TempTriples#sort(datatypes.TripleComponentOrder)
	 */
	@Override
	public void sort(final ProgressListener listener) {
		if(!this.sorted) {
			Collections.sort(this.arrayOfTriples, QuadIDComparator.getComparator(getOrder()));
		}
		this.sorted = true;
	}

	@Override
	public void removeDuplicates(final ProgressListener listener) {
		if (this.arrayOfTriples.size() <= 1 || !this.sorted) {
			return;
		}

		if (this.order == TripleComponentOrder.Unknown || !this.sorted) {
			throw new IllegalArgumentException("Cannot remove duplicates unless sorted");
		}

		int j = 0;

		for (int i = 1; i < this.arrayOfTriples.size(); i++) {
			final TripleID ti = this.arrayOfTriples.get(i);
			final TripleID tj = this.arrayOfTriples.get(j);
			if ((tj instanceof QuadID ? tj.compareTo(ti) : ti.compareTo(tj)) != 0) {
				j++;
				this.arrayOfTriples.set(j, this.arrayOfTriples.get(i));
			}
			ListenerUtil.notifyCond(listener, "Removing duplicate triples", i, this.arrayOfTriples.size());
		}

		while (this.arrayOfTriples.size() > j + 1) {
			this.arrayOfTriples.remove(this.arrayOfTriples.size() - 1);
		}
		this.arrayOfTriples.trimToSize();
		this.numValidTriples = j + 1;
	}

	@Override
	public void populateHeader(final Header header, final String rootNode) {
		header.insert(rootNode, HDTVocabulary.TRIPLES_TYPE, HDTVocabulary.QUADS_TYPE_TRIPLESLIST);
		header.insert(rootNode, HDTVocabulary.TRIPLES_NUM_TRIPLES, getNumberOfElements() );
		header.insert(rootNode, HDTVocabulary.TRIPLES_ORDER, getOrder().ordinal());
	}

}
