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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.iterator.SequentialSearchIteratorTripleID;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TempTriples;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleIDComparator;
import org.rdfhdt.hdt.triples.TripleTranslatorImpl;
import org.rdfhdt.hdt.triples.Triples;
import org.rdfhdt.hdt.util.RDFInfo;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.io.IOUtil;
import org.rdfhdt.hdt.util.listener.ListenerUtil;


/**
 * Implementation of TempTriples using a List of TripleID.
 *
 */
public class TriplesList extends TripleTranslatorImpl implements TempTriples {

	/** The array to hold the triples */
	ArrayList<TripleID> arrayOfTriples;

	/** The order of the triples */
	private TripleComponentOrder order;
	protected long		 numValidTriples;

	protected boolean		 sorted;

	/**
	 * Constructor, given an order to sort by
	 *
	 * @param order
	 *            The order to sort by
	 */
	public TriplesList(final HDTOptions specification) {

		//precise allocation of the array (minimal memory wasting)
		long numTriples = RDFInfo.getTriples(specification);
		numTriples = (numTriples>0)?numTriples:100;
		this.arrayOfTriples = new ArrayList<>((int)numTriples);

		//choosing starting(or default) component order
		String orderStr = specification.get("triplesOrder");
		if(orderStr==null) {
			orderStr = "SPO";
		}
		this.order = TripleComponentOrder.valueOf(orderStr);

		this.numValidTriples = 0;
	}

	/**
	 * A method for setting the size of the arrayList (so no reallocation occurs).
	 * If not empty does nothing and returns false.
	 */
	public boolean reallocateIfEmpty(final int numTriples){
		if (this.arrayOfTriples.isEmpty()) {
			this.arrayOfTriples = new ArrayList<>(numTriples);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hdt.triples.Triples#search(hdt.triples.TripleID)
	 */
	@Override
	public IteratorTripleID search(final TripleID pattern) {
		final String patternStr = pattern.getPatternString();
		if(patternStr.equals("???")) {
			return new TriplesListIterator(this);
		} else {
			return new SequentialSearchIteratorTripleID(pattern, new TriplesListIterator(this));
		}
	}

	/* (non-Javadoc)
	 * @see hdt.triples.Triples#searchAll()
	 */
	@Override
	public IteratorTripleID searchAll() {
		final TripleID all = new TripleID(0,0,0);
		return search(all);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hdt.triples.Triples#getNumberOfElements()
	 */
	@Override
	public long getNumberOfElements() {
		return this.numValidTriples;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hdt.triples.Triples#size()
	 */
	@Override
	public long size() {
		return getNumberOfElements()*TripleID.size();
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see hdt.triples.Triples#save(java.io.OutputStream)
	 */
	@Override
	public void save(final OutputStream output, final ControlInfo controlInformation, final ProgressListener listener) throws IOException {
		controlInformation.clear();
		controlInformation.setInt("numTriples", this.numValidTriples);
		controlInformation.setFormat(HDTVocabulary.TRIPLES_TYPE_TRIPLESLIST);
		controlInformation.setInt("order", this.order.ordinal());
		controlInformation.save(output);

		final DataOutputStream dout = new DataOutputStream(output);
		int count = 0;
		for (final TripleID triple : this.arrayOfTriples) {
			if(triple.isValid()) {
				dout.writeInt(triple.getSubject());
				dout.writeInt(triple.getPredicate());
				dout.writeInt(triple.getObject());
				ListenerUtil.notifyCond(listener, "Saving TriplesList", count, this.arrayOfTriples.size());
			}
			count++;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hdt.triples.Triples#load(java.io.InputStream)
	 */
	@Override
	public void load(final InputStream input, final ControlInfo controlInformation, final ProgressListener listener) throws IOException {
		this.order = TripleComponentOrder.values()[(int)controlInformation.getInt("order")];
		final long totalTriples = controlInformation.getInt("numTriples");

		int numRead=0;

		while(numRead<totalTriples) {
			this.arrayOfTriples.add(new TripleID(IOUtil.readInt(input), IOUtil.readInt(input), IOUtil.readInt(input)));
			numRead++;
			this.numValidTriples++;
			ListenerUtil.notifyCond(listener, "Loading TriplesList", numRead, totalTriples);
		}

		this.sorted = false;
	}

	/**
	 * @param order
	 *            the order to set
	 */
	@Override
	public void setOrder(final TripleComponentOrder order) {
		if (this.order.equals(order)) {
			return;
		}
		this.order = order;
		this.sorted = false;
	}

	@Override
	public TripleComponentOrder getOrder() {
		return this.order;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see hdt.triples.TempTriples#insert(hdt.triples.TripleID[])
	 */
	@Override
	public boolean insert(final TripleID... triples) {
		for (final TripleID triple : triples) {
			this.arrayOfTriples.add(new TripleID(triple));
			this.numValidTriples++;
		}
		this.sorted = false;
		return true;
	}

	/* (non-Javadoc)
	 * @see hdt.triples.TempTriples#insert(int, int, int)
	 */
	@Override
	public boolean insert(final int subject, final int predicate, final int object) {
		this.arrayOfTriples.add(new TripleID(subject,predicate,object));
		this.numValidTriples++;
		this.sorted = false;
		return true;
	}

	@Override
	public boolean update(final TripleID triple, final int subj, final int pred, final int obj) {
		if (triple==null) {
			return false;
		}

		triple.setAll(subj, pred, obj);
		this.sorted = false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hdt.triples.TempTriples#delete(hdt.triples.TripleID[])
	 */
	@Override
	public boolean remove(final TripleID... patterns) {
		boolean removed = false;
		for(final TripleID triple : this.arrayOfTriples){
			for(final TripleID pattern : patterns) {
				if(triple.match(pattern)) {
					triple.clear();
					removed = true;
					this.numValidTriples--;
					break;
				}
			}
		}

		return removed;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hdt.triples.TempTriples#sort(datatypes.TripleComponentOrder)
	 */
	@Override
	public void sort(final ProgressListener listener) {
		if(!this.sorted) {
			Collections.sort(this.arrayOfTriples, TripleIDComparator.getComparator(this.order));
		}
		this.sorted = true;
	}

	/**
	 * If called while triples not sorted nothing will happen!
	 */
	@Override
	public void removeDuplicates(final ProgressListener listener) {
		if(this.arrayOfTriples.size()<=1 || !this.sorted) {
			return;
		}

		if(this.order==TripleComponentOrder.Unknown || !this.sorted) {
			throw new IllegalArgumentException("Cannot remove duplicates unless sorted");
		}

		int j = 0;

		for(int i=1; i<this.arrayOfTriples.size(); i++) {
			if(this.arrayOfTriples.get(i).compareTo(this.arrayOfTriples.get(j))!=0) {
				j++;
				this.arrayOfTriples.set(j, this.arrayOfTriples.get(i));
			}
			ListenerUtil.notifyCond(listener, "Removing duplicate triples", i, this.arrayOfTriples.size());
		}

		while(this.arrayOfTriples.size()>j+1) {
			this.arrayOfTriples.remove(this.arrayOfTriples.size()-1);
		}
		this.arrayOfTriples.trimToSize();
		this.numValidTriples = j+1;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TriplesList [" + this.arrayOfTriples + "\n order=" + this.order + "]";
	}

	/* (non-Javadoc)
	 * @see hdt.triples.Triples#populateHeader(hdt.header.Header, java.lang.String)
	 */
	@Override
	public void populateHeader(final Header header, final String rootNode) {
		header.insert(rootNode, HDTVocabulary.TRIPLES_TYPE, HDTVocabulary.TRIPLES_TYPE_TRIPLESLIST);
		header.insert(rootNode, HDTVocabulary.TRIPLES_NUM_TRIPLES, getNumberOfElements() );
		header.insert(rootNode, HDTVocabulary.TRIPLES_ORDER, this.order.ordinal() );
	}

	@Override
	public String getType() {
		return HDTVocabulary.TRIPLES_TYPE_TRIPLESLIST;
	}

	@Override
	public void clear() {
		this.arrayOfTriples.clear();
		this.numValidTriples=0;
		this.order = TripleComponentOrder.Unknown;
		this.sorted = false;
	}

	@Override
	public void load(final Triples triples, final ProgressListener listener) {
		clear();
		final IteratorTripleID it = triples.searchAll();
		while(it.hasNext()) {
			final TripleID triple = it.next();
			this.insert(triple.getSubject(), triple.getPredicate(), triple.getObject());
		}
		this.sorted = false;
	}

	// @Override
	// public void load(final TempTriples input, final ProgressListener listener) {
	// final IteratorTripleID iterator = input.searchAll();
	// while (iterator.hasNext()) {
	// this.arrayOfTriples.add(iterator.next());
	// this.numValidTriples++;
	// }
	//
	// this.sorted = false;
	// }

	@Override
	public void close() throws IOException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.TriplesPrivate#mapFromFile(org.rdfhdt.hdt.util.io.CountInputStream, java.io.File, org.rdfhdt.hdt.listener.ProgressListener)
	 */
	@Override
	public void mapFromFile(final CountInputStream in, final File f, final ProgressListener listener) throws IOException {
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.TriplesPrivate#generateIndex(org.rdfhdt.hdt.listener.ProgressListener)
	 */
	@Override
	public void generateIndex(final ProgressListener listener) {
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.TriplesPrivate#loadIndex(java.io.InputStream, org.rdfhdt.hdt.options.ControlInfo, org.rdfhdt.hdt.listener.ProgressListener)
	 */
	@Override
	public void loadIndex(final InputStream input, final ControlInfo ci, final ProgressListener listener) throws IOException {
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.TriplesPrivate#mapIndex(org.rdfhdt.hdt.util.io.CountInputStream, java.io.File, org.rdfhdt.hdt.options.ControlInfo, org.rdfhdt.hdt.listener.ProgressListener)
	 */
	@Override
	public void mapIndex(final CountInputStream input, final File f, final ControlInfo ci, final ProgressListener listener) throws IOException {
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.TriplesPrivate#saveIndex(java.io.OutputStream, org.rdfhdt.hdt.options.ControlInfo, org.rdfhdt.hdt.listener.ProgressListener)
	 */
	@Override
	public void saveIndex(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {
		throw new NotImplementedException();
	}

}
