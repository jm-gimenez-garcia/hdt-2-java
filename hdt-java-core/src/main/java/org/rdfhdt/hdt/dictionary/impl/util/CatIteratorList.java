package org.rdfhdt.hdt.dictionary.impl.util;

import org.apache.commons.math3.util.Pair;
import org.rdfhdt.hdt.util.string.CharSequenceComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Ali Haidar
 *
 */
public class CatIteratorList implements Iterator<IterElement> {
	ArrayList<IteratorPlusElement> list;
	private List<Iterator<IterElement>> listIters;

	public CatIteratorList(List<Iterator<IterElement>> listIters) {

		list = new ArrayList<IteratorPlusElement>();
		this.listIters = new ArrayList<>(listIters);
		int count = 1;
		for (Iterator<IterElement> iter : listIters) {
			if (iter.hasNext()) {
				list.add(new IteratorPlusElement(count, iter.next()));
			}
			count++;
		}

	}

	@Override
	public boolean hasNext() {
		if (list.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public IterElement next() {
		IterElement triple = null;
		Collections.sort(list, new ScoreComparator());
		for (int i = 1; i <= listIters.size(); i++) {
			if (hasNext()) {
				if (list.get(0).iterator == i) {
					triple = list.get(0).iterElement;
					if (listIters.get(i - 1).hasNext()) {
						list.set(0, new IteratorPlusElement(i, listIters.get(i - 1).next()));
					} else {
						list.remove(0);
					}
					break;
				}
			}
		}

		return triple;
	}


	@Override
	public void remove() {
		try {
			throw new Exception("Not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class IteratorPlusElement {
		public int iterator;
		public IterElement iterElement;

		public IteratorPlusElement(int iterator, IterElement iterElement) {
			this.iterator = iterator;
			this.iterElement = iterElement;

		}
	}

	public class ScoreComparator implements Comparator<IteratorPlusElement> {

		public int compare(IteratorPlusElement a, IteratorPlusElement b) {
			if (a.iterElement.getPair().getKey() > b.iterElement.getPair().getKey()) {
				return 1;
			} else if (a.iterElement.getPair().getKey() < b.iterElement.getPair().getKey()) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
