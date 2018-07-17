/**
 *
 */
package org.rdfhdt.hdt.triples;

import java.util.function.BiFunction;

import org.rdfhdt.hdt.enums.TripleComponentRole;

/**
 * @author José M. Giménez-García
 *
 */
public interface TripleTranslator {

	void setToRoleIDFunction(BiFunction<Integer, TripleComponentRole, Integer> function);

	void setToGlobalIDFunction(BiFunction<Integer, TripleComponentRole, Integer> function);

	BiFunction<Integer, TripleComponentRole, Integer> getToRoleIDFunction();

	BiFunction<Integer, TripleComponentRole, Integer> getToGlobalIDFunction();

}