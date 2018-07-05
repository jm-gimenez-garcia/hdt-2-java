package org.rdfhdt.hdt.compact.permutation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rdfhdt.hdt.compact.permutation.PermutationMRRR;

/**
 * @author javi
 *
 */
public class PermutationMRRRTest {

	private List<Long> getTestInput(boolean IDsStartsAtOne){
		List<Long> input = new ArrayList<Long>(); 
		if (!IDsStartsAtOne){
			input.add(0L);
		}
		input.add(2L);
		input.add(3L);
		input.add(1L);
		input.add(5L);
		input.add(4L);
		input.add(9L);
		input.add(7L);
		input.add(6L);
		input.add(8L);
		return input;
	}
	private List<Long> getSelfTestInput(boolean IDsStartsAtOne){
		List<Long> input = new ArrayList<Long>(); 
		if (!IDsStartsAtOne){
			input.add(0L);
		}
		input.add(1L);
		input.add(2L);
		input.add(3L);
		input.add(4L);
		input.add(5L);
		input.add(6L);
		input.add(7L);
		input.add(8L);
		input.add(9L);
		return input;
	}
	@Test 
	public void testLength_step1_IDStart0() throws IOException {
		List<Long> input = getTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 1);
		assertEquals(input.size(), perm.getLength());
	}
	@Test 
	public void testLength_step1_IDStart1() throws IOException {
		List<Long> input = getTestInput(true);
		PermutationMRRR perm = new PermutationMRRR(input, 1,true);
		assertEquals(input.size(), perm.getLength());
	}
	@Test 
	public void testPi_all_step1_IDStart0() throws IOException {
		List<Long> input = getTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 1);
		
		for (int i=0;i<input.size();i++){
			assertEquals(input.get(i),(Long)perm.pi(i));
			// System.out.println(perm.pi(i));
		}	
	}
	@Test 
	public void testPi_all_step1_IDStart1() throws IOException {
		List<Long> input = getTestInput(true);
		PermutationMRRR perm = new PermutationMRRR(input, 1,true);
		
		for (int i=0;i<input.size();i++){
			assertEquals(input.get(i),(Long)perm.pi(i));
		}	
	}
	@Test 
	public void testPi_all_step4_IDStart0() throws IOException {
		List<Long> input = getTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 4);
		
		for (int i=0;i<input.size();i++){
			assertEquals(input.get(i),(Long)perm.pi(i));
			// System.out.println(perm.pi(i));
		}	
	}
	@Test 
	public void testPi_all_step4_IDStart1() throws IOException {
		List<Long> input = getTestInput(true);
		PermutationMRRR perm = new PermutationMRRR(input, 4,true);
		
		for (int i=0;i<input.size();i++){
			assertEquals(input.get(i),(Long)perm.pi(i));
			// System.out.println(perm.pi(i));
		}	
	}
	@Test 
	public void testRevPi_all_step1_IDStart0() throws IOException {
		List<Long> input = getTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 1);
		
		// test rev perm
		
		assertEquals(0L,perm.revpi(0));
		assertEquals(3L,perm.revpi(1));
		assertEquals(1L,perm.revpi(2));
		assertEquals(2L,perm.revpi(3));
		assertEquals(5L,perm.revpi(4));
		assertEquals(4L,perm.revpi(5));
		assertEquals(8L,perm.revpi(6));
		assertEquals(7L,perm.revpi(7));
		assertEquals(9L,perm.revpi(8));
		assertEquals(6L,perm.revpi(9));
	}
	@Test 
	public void testRevPi_all_step1_IDStart1() throws IOException {
		List<Long> input = getTestInput(true);
		PermutationMRRR perm = new PermutationMRRR(input, 1,true);
		
		// test rev perm
		
		assertEquals(3L,perm.revpi(1));
		assertEquals(1L,perm.revpi(2));
		assertEquals(2L,perm.revpi(3));
		assertEquals(5L,perm.revpi(4));
		assertEquals(4L,perm.revpi(5));
		assertEquals(8L,perm.revpi(6));
		assertEquals(7L,perm.revpi(7));
		assertEquals(9L,perm.revpi(8));
		assertEquals(6L,perm.revpi(9));
	}
	@Test 
	public void testRevPi_all_step1_IDStart0_step4() throws IOException {
		List<Long> input = getTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 4);
		
		// test rev perm
		
		assertEquals(0L,perm.revpi(0));
		assertEquals(3L,perm.revpi(1));
		assertEquals(1L,perm.revpi(2));
		assertEquals(2L,perm.revpi(3));
		assertEquals(5L,perm.revpi(4));
		assertEquals(4L,perm.revpi(5));
		assertEquals(8L,perm.revpi(6));
		assertEquals(7L,perm.revpi(7));
		assertEquals(9L,perm.revpi(8));
		assertEquals(6L,perm.revpi(9));
	}
	@Test 
	public void testRevPi_all_step1_IDStart1_step4() throws IOException {
		List<Long> input = getTestInput(true);
		PermutationMRRR perm = new PermutationMRRR(input, 4,true);
		
		// test rev perm
		
		assertEquals(3L,perm.revpi(1));
		assertEquals(1L,perm.revpi(2));
		assertEquals(2L,perm.revpi(3));
		assertEquals(5L,perm.revpi(4));
		assertEquals(4L,perm.revpi(5));
		assertEquals(8L,perm.revpi(6));
		assertEquals(7L,perm.revpi(7));
		assertEquals(9L,perm.revpi(8));
		assertEquals(6L,perm.revpi(9));
	}
	
	@Test 
	public void testPi_selfPerm_step1_IDStart0() throws IOException {
		List<Long> input = getSelfTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 1);
		for (int i=0;i<input.size();i++){
			assertEquals(input.get(i),(Long)perm.pi(i));
			// System.out.println(perm.pi(i));
		}	
		
	}
	@Test 
	public void testPi_selfPerm_step1_IDStart1() throws IOException {
		List<Long> input = getSelfTestInput(true);
		PermutationMRRR perm = new PermutationMRRR(input, 1,true);
		for (int i=0;i<input.size();i++){
			assertEquals(input.get(i),(Long)perm.pi(i));
			// System.out.println(perm.pi(i));
		}	
		
	}
		@Test 
		public void testRevPi_selfPerm_step1_IDStart0() throws IOException {
			List<Long> input = getSelfTestInput(false);
			PermutationMRRR perm = new PermutationMRRR(input, 1);
		// test rev perm
		
		assertEquals(0L,perm.revpi(0));	
		assertEquals(1L,perm.revpi(1));
		assertEquals(2L,perm.revpi(2));
		assertEquals(3L,perm.revpi(3));
		assertEquals(4L,perm.revpi(4));
		assertEquals(5L,perm.revpi(5));
		assertEquals(6L,perm.revpi(6));
		assertEquals(7L,perm.revpi(7));
		assertEquals(8L,perm.revpi(8));
		assertEquals(9L,perm.revpi(9));
	}
		
		/**
		 * @throws IOException
		 */
		@Test 
		public void testRevPi_selfPerm_step1_IDStart1() throws IOException {
			List<Long> input = getSelfTestInput(true);
			PermutationMRRR perm = new PermutationMRRR(input, 1,true);
		// test rev perm
		
		assertEquals(1L,perm.revpi(1));
		assertEquals(2L,perm.revpi(2));
		assertEquals(3L,perm.revpi(3));
		assertEquals(4L,perm.revpi(4));
		assertEquals(5L,perm.revpi(5));
		assertEquals(6L,perm.revpi(6));
		assertEquals(7L,perm.revpi(7));
		assertEquals(8L,perm.revpi(8));
		assertEquals(9L,perm.revpi(9));
	}
		
		
		@Test
		public void testLoadSave() {
			List<Long> input = getSelfTestInput(true);
			PermutationMRRR perm = new PermutationMRRR(input, 4,true);
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				perm.save(out, null);
				
				ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
				
				PermutationMRRR loaded = new PermutationMRRR();
				loaded.load(in, null);
				

				for(int i=0;i<perm.getLength();i++) {
					assertEquals(perm.pi(i),loaded.pi(i));
					assertEquals(perm.revpi(i),loaded.revpi(i));
				}
			} catch (IOException e) {
				fail("Exception thrown: "+e);
			}
			
		}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testZeroPi() throws IOException {
		List<Long> input = getTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 1);
		perm.pi(-1);
		fail("Exception not thrown");
	}
	@Test(expected=IndexOutOfBoundsException.class)
	public void testZeroRevPi() throws IOException {
		List<Long> input = getTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 1);
		perm.revpi(-1);
		fail("Exception not thrown");
	}
	@Test(expected=IndexOutOfBoundsException.class)
	public void testOverflowPi() throws IndexOutOfBoundsException {
		List<Long> input = getTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 1);
		perm.pi(input.size());
		fail("Exception not thrown");
	}
	@Test(expected=IndexOutOfBoundsException.class)
	public void testOverflowReVPi() throws IndexOutOfBoundsException {
		List<Long> input = getTestInput(false);
		PermutationMRRR perm = new PermutationMRRR(input, 1);
		perm.revpi(input.size());
		fail("Exception not thrown");
	}
}
