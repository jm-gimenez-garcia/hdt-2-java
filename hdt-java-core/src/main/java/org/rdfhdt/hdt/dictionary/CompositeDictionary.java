/**
 *
 */
package org.rdfhdt.hdt.dictionary;

/**
 * @author José M. Giménez-García
 *
 */
public interface CompositeDictionary extends TriplesDictionary, GraphsDictionary {

    TriplesDictionary getTriplesDictionary();

    GraphsDictionary getGraphsDictionary();
}
