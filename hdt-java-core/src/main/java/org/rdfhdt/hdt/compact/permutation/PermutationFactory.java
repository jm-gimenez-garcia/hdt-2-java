

package org.rdfhdt.hdt.compact.permutation;

import java.util.List;

/**
 * @author mario.arias
 *
 */
public class PermutationFactory {

	private PermutationFactory() {}

	public static final byte	TYPE_PERMUTATION_MRRR			= 1;
	public static final long	PERMUTATION_MRRR_DEFAULT_STEP	= 16l;

	public static Permutation createPermutation(final List<Long> elements, final long step) {
		return new PermutationMRRR(elements, step, true);
	}

	public static Permutation createPermutation() {
		return new PermutationMRRR();
	}


}
