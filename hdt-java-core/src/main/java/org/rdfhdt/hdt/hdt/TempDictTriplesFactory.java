package org.rdfhdt.hdt.hdt;

import org.rdfhdt.hdt.dictionary.TriplesTempDictionary;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.TempTriples;

public interface TempDictTriplesFactory {

    void checkTwoPass(HDTOptions spec);

    TriplesTempDictionary getDictionary(HDTOptions options);

    TempTriples getTriples(HDTOptions options);
}
