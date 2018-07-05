/**
 *
 */
package org.rdfhdt.hdt.triples;

/**
 * @author José M. Giménez-García
 *
 */
public interface TempQuads extends TempTriples {

    boolean insert(int subject, int predicate, int object, int graph);

    boolean update(TripleID triple, int subj, int pred, int obj, int graph);

}
