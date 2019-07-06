package org.rdfhdt.hdt.dictionary.impl.util;

import org.apache.commons.math3.util.Pair;
/**
 * @author alyhdr
 */
public class IterElement {
	private final Pair<Integer, Integer> pair;
	private final String entity;
	private String firstIterName;
	private String secondIterName;
	
	public IterElement(Pair<Integer,Integer> pair,String entity,String firstIterName,String secondIterName) {
		this.pair = pair;
		this.entity = entity;
		this.firstIterName = firstIterName;
		this.secondIterName = secondIterName;
	}
	public Pair<Integer, Integer> getPair() {
		return pair;
	}
	public String getEntity() {
		return entity;
	}
	public String getFirstIterName() {
		return firstIterName;
	}
	public String getSecondIterName() {
		return secondIterName;
	}
}
