/**
 *
 */
package org.rdfhdt.hdt.triples;

import static org.rdfhdt.hdt.enums.TripleComponentRole.GRAPH;
import static org.rdfhdt.hdt.enums.TripleComponentRole.OBJECT;
import static org.rdfhdt.hdt.enums.TripleComponentRole.PREDICATE;
import static org.rdfhdt.hdt.enums.TripleComponentRole.SUBJECT;

import java.util.function.BiFunction;

import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.TripleID;

/**
 * @author José M. Giménez-García
 *
 */
public abstract class TripleTranslatorImpl implements TripleTranslator {

	protected BiFunction<Integer, TripleComponentRole, Integer>	toRoleID	= (id, role) -> id;
	protected BiFunction<Integer, TripleComponentRole, Integer>	toGlobalID	= (id, role) -> id;

	// protected boolean getRoleIDs;
	//
	// public TripleTranslator(final boolean getRoleIDs) {
	// this.getRoleIDs = getRoleIDs;
	// }


	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.TripleTranslator#setToRoleIDFunction(java.util.function.BiFunction)
	 */
	@Override
	public void setToRoleIDFunction(final BiFunction<Integer, TripleComponentRole, Integer> function) {
		this.toRoleID = function;
	};

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.TripleTranslator#getToRoleID()
	 */
	@Override
	public BiFunction<Integer, TripleComponentRole, Integer> getToRoleIDFunction() {
		return this.toRoleID;
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.TripleTranslator#getToGlobalID()
	 */
	@Override
	public BiFunction<Integer, TripleComponentRole, Integer> getToGlobalIDFunction() {
		return this.toGlobalID;
	}

	/* (non-Javadoc)
	 * @see org.rdfhdt.hdt.triples.TripleTranslator#setToGlobalIDFunction(java.util.function.BiFunction)
	 */
	@Override
	public void setToGlobalIDFunction(final BiFunction<Integer, TripleComponentRole, Integer> function) {
		this.toGlobalID = function;
	}

	protected int toRoleID(final int id, final TripleComponentRole role) {
		return this.toRoleID.apply(id, role);
	}

	protected int toGlobalID(final int id, final TripleComponentRole role) {
		return this.toGlobalID.apply(id, role);
	}

	protected TripleID toRoleIDs(final TripleID triple) {
		if (triple instanceof QuadID) {
			return new QuadID(toRoleID(triple.getSubject(), SUBJECT), toRoleID(triple.getPredicate(), PREDICATE), toRoleID(triple.getObject(), OBJECT),
					toRoleID(((QuadID) triple).getGraph(), GRAPH));
		} else {
			return new TripleID(toRoleID(triple.getSubject(), SUBJECT), toRoleID(triple.getPredicate(), PREDICATE), toRoleID(triple.getObject(), OBJECT));
		}
	}

	protected TripleID toGlobalIDs(final TripleID triple) {
		if (triple instanceof QuadID) {
			return new QuadID(toGlobalID(triple.getSubject(), SUBJECT), toGlobalID(triple.getPredicate(), PREDICATE), toGlobalID(triple.getObject(), OBJECT),
					this.toGlobalID.apply(((QuadID) triple).getGraph(), GRAPH));
		} else {
			return new TripleID(toGlobalID(triple.getSubject(), SUBJECT), toGlobalID(triple.getPredicate(), PREDICATE), toGlobalID(triple.getObject(), OBJECT));
		}
	}
}
