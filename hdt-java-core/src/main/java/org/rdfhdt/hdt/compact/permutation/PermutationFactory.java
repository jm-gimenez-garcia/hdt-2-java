

package org.rdfhdt.hdt.compact.permutation;

import java.util.List;

/**
 * @author mario.arias
 *
 */
public class PermutationFactory {
	
	private PermutationFactory() {}
	
	public static final byte TYPE_PERMUTATION_MRRR= 1;
	
	
	public static Permutation createPermutation(String type,List<Long> elements, long step) {
		return new PermutationMRRR(elements, step);
	}
	
	
}
