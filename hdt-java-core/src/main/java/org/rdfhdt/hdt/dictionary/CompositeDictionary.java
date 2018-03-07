/**
 *
 */
package org.rdfhdt.hdt.dictionary;

/**
 * @author José M. Giménez-García
 *
 */
public interface CompositeDictionary extends TriplesDictionary, GraphDictionary {

    TriplesDictionary getTriplesDictionary();

    GraphDictionary getGraphDictionary();
}
