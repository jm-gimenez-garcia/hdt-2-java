/**
 *
 */
package org.rdfhdt.hdt.triples;

import java.util.Comparator;

import org.rdfhdt.hdt.enums.TripleComponentOrder;

/**
 * @author José M. Giménez-García
 *
 */
public class QuadIDComparator extends TripleIDComparator {

	private static final long serialVersionUID = 8813466257382580352L;

	protected Comparator<TripleID>	tripleIDcomparator	= null;

	public static Comparator<TripleID> getComparator(final TripleComponentOrder order) {
		return new QuadIDComparator(order);
	}

	protected QuadIDComparator(final TripleComponentOrder order) {
		super(order);
		this.tripleIDcomparator = TripleIDComparator.getComparator(order);
	}

	@Override
	public int compare(final TripleID o1, final TripleID o2) {
		int result = this.tripleIDcomparator.compare(o1, o2);
		if (result == 0) {
			final int g1 = o1 instanceof QuadID ? ((QuadID) o1).getGraph() : 0;
			final int g2 = o2 instanceof QuadID ? ((QuadID) o2).getGraph() : 0;
			result = g1 - g2;
		}
		return result;
	}
}
