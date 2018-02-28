package org.rdfhdt.hdt.dictionary.impl;

import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.enums.DictionarySectionRole;
import org.rdfhdt.hdt.enums.TripleComponentRole;

public interface DictionaryWrapper<T extends Dictionary> {

	void setInternal(T dictionary);
	
	void getInternal(T dictionary;
	
	static DictionarySectionRole translate(DictionarySectionRole position) {
		if (position == DictionarySectionRole.GRAPH) {
			position = DictionarySectionRole.PREDICATE;
		}
		return position;
	}
	
	static TripleComponentRole translate(TripleComponentRole position) {
		if (position == TripleComponentRole.GRAPH) {
			position = TripleComponentRole.PREDICATE;
		}
		return position;
	}
	
}
