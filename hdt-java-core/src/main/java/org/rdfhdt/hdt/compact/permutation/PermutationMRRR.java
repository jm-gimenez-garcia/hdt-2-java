package org.rdfhdt.hdt.compact.permutation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.rdfhdt.hdt.compact.bitmap.Bitmap375;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64;
import org.rdfhdt.hdt.exceptions.CRCException;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.util.BitUtil;
import org.rdfhdt.hdt.util.crc.CRC8;
import org.rdfhdt.hdt.util.crc.CRCInputStream;
import org.rdfhdt.hdt.util.crc.CRCOutputStream;

/**
 * @author Javier D. Fernández
 *
 * Implementation of a Permutation MRRR
 * It assumes the IDs and positions starts at 0. 
 * If IDs start with 1, use the specific creator to add a fake ID=0 in the beginning
 */
public class PermutationMRRR implements Permutation {

	
	SequenceLog64 sequence; // elements of the permutation
	Bitmap375 bitmap; // bitmap to mark the backward pointers
	SequenceLog64 backwardPointers; // backward pointers
	long step; // the sampling step for backward pointers
	boolean IDsStartsAtOne=false; 
	
	/**
	 * @param elements
	 * @param step sets how often backward pointers should be created
	 */
	public PermutationMRRR(List<Long> elements,long step, boolean IDsStartsAtOne) {
		this.IDsStartsAtOne = IDsStartsAtOne;
		createPermutationMRRR(elements,step);
	}
	public PermutationMRRR(List<Long> elements,long step) {
		createPermutationMRRR(elements,step);
	}
	public PermutationMRRR() {
		sequence= new SequenceLog64();
		bitmap = new Bitmap375();
		backwardPointers = new SequenceLog64();
	}
	/**
	 * @param elements
	 * @param step sets how often backward pointers should be created
	 */
	private void createPermutationMRRR(List<Long> elements,long step) {
		this.step = step;
		if (IDsStartsAtOne==true){
			elements.add(0, 0L); //insert a fakeID=0
		}
		sequence= new SequenceLog64(BitUtil.log2(elements.size()), elements.size());
		
		//Auxiliary structures
	    TreeMap<Long,Long> aux_backwardPointers = new TreeMap<Long,Long>();; //auxiliary for backward pointers (key=position, value=element value), sorted by key
	   
		if (step==1){ // if the step is one, create the sequence from the list and add all backward pointers. In this case we don't need the bitmap as all values are present
			
			backwardPointers =  new SequenceLog64(BitUtil.log2(elements.size()), elements.size());
			
			bitmap = new Bitmap375(); //bitmap will be empty but we need to create it in order to save it afterwards
			
			Iterator<Long> els = elements.iterator();
			long bitIndex=0; 
			while (els.hasNext()){
				long value = els.next(); // get the element value
				sequence.append(value); // insert the value in the final sequence
				aux_backwardPointers.put(value,bitIndex); // insert the reverse in the backward pointers
				bitIndex++;
			}
			// we iterate in order of keys in order to add the aux pointers to the final pointers (it could be done with the add function of the sequence, but it has a bug 
			// backwardPointers.add(aux_backwardPointers.values().iterator()); // this has a bug
			Iterator<Long> it= aux_backwardPointers.values().iterator();
			while (it.hasNext()){
				backwardPointers.append(it.next());
			}
		}
		else{ // implement permutation with a step
			
			Bitmap375 aux_markVisited = new Bitmap375(elements.size()); // bitmap to mark the visited elements
			bitmap = new Bitmap375(elements.size());
			for (int i=0;i<elements.size();i++){ 
				if (aux_markVisited.access(i)==false){ // if not visited
					aux_markVisited.set(i, true); // mark as visited
					long aux=0;
					long nextElement = i; 
					long j=i; // j is the index of the iteration of the cycle. 
					while(nextElement!=elements.get((int) j)){
						j=elements.get((int) j); // get next element in the cycle
						aux_markVisited.set(j, true); // mark as visited
						aux++;
						// mark the future backward pointer if it's in accordance with the step
						if (aux>=step){
							bitmap.set(j,true);
							aux=0; // restart he count, it is like in theory doing the sample every given "steps"
						}
					}
					
				}
			}
			// in the second iteration we store the sequence and the backward pointers
			aux_markVisited = new Bitmap375(elements.size()); // restart the visited nodes
			for (int i=0;i<elements.size();i++){
				long value = elements.get(i);
				long pointer=0;
				sequence.append(value); // insert the value in the final sequence
				if (aux_markVisited.access(i)==false){ // if not visited
					
					aux_markVisited.set(i, true); // mark as visited
					pointer=i; //prepare pointer
					long aux=0;
					long nextElement = i; 
					long j=i; // j is the index of the iteration of the cycle. 
					long cyclesize=0; // number of total elements in the cycle
					while(nextElement!=elements.get((int) j)){
						j=elements.get((int) j); // get next element in the cycle
						aux_markVisited.set(j, true); // mark as visited
						aux++;
						if (aux>=step){
							aux_backwardPointers.put((Long)j,pointer); // insert the reverse in the backward pointers
							pointer = j; // prepare the pointer for the next step
							aux=0; // restart he count, it is like in theory doing the sample every given "steps"
						}
						cyclesize++;
					}
					// add the last pointer
					
					if (cyclesize>=step){
						aux_backwardPointers.put((Long)nextElement,pointer); // insert the reverse in the backward pointers
					}
				}
			}
			
			
			backwardPointers =  new SequenceLog64(BitUtil.log2(elements.size()-1), aux_backwardPointers.size()); //-1 because one of them is 0	
					
			// we iterate in order of keys in order to add the aux pointers to the final pointers (it could be done with the add function of the sequence, but it has a bug 
			// backwardPointers.add(aux_backwardPointers.values().iterator()); // this has a bug
			Iterator<Long> it= aux_backwardPointers.values().iterator();
			while (it.hasNext()){
				Long pointer = it.next();
				backwardPointers.append(pointer);
			}
						
			bitmap.trimToSize();
		}
		
		sequence.trimToSize();
		backwardPointers.trimToSize();
		
		
	}
	
	@Override
	public long pi(long position) {
		if (position<0 || position>=sequence.getNumberOfElements()){
			throw new IndexOutOfBoundsException("The given position is bigger than the size of the permutation");
		}
		return sequence.get(position); // use -1 if we disregard the 0
	}

	@Override
	public long revpi(long i) {
		if (i<0 || i>=sequence.getNumberOfElements()){
			throw new IndexOutOfBoundsException("The gives value is bigger than the maximum permutation ID");
		}
		if (step==1){
			return backwardPointers.get(i); // use -1 if we disregard the 0
		}
		else{
			
			Long j=i;
			Long value=sequence.get(j);
			while ((bitmap.access(j)==false) && value!=i){ // follow the sequence until a pointer or a cycle is found
				j = value;
				value = sequence.get(j);
			}
			if (value!=i){ // follow backward pointer
				j = backwardPointers.get(bitmap.rank1(j-1)); //get the pointer for the current position, marked with the ranks of the bitmap
				value=sequence.get(j);
				while (value!=i){
					j = value;
					value = sequence.get(j);
				}
				
			}
			return j;
			
		}
		
	}

	@Override
	public long getSize() {
		return sequence.getRealSize()+bitmap.getSizeBytes()+backwardPointers.getRealSize();
	}

	@Override
	public long getLength() {
		return sequence.getNumberOfElements();
	}

	@Override
	public void save(OutputStream output, ProgressListener listener) throws IOException {

		CRCOutputStream out = new CRCOutputStream(output, new CRC8());
		
		out.write(PermutationFactory.TYPE_PERMUTATION_MRRR);
		out.write((int)step);
		out.writeCRC();
		
		sequence.save(output, listener);
		bitmap.save(output, listener);
		backwardPointers.save(output, listener);
				
	}

	/* (non-Javadoc)
	 * @see org.rdfdht.hdt.compact.permutation.Permutation#load(java.io.InputStream, org.rdfhdt.hdt.listener.ProgressListener)
	 */
	/* (non-Javadoc)
	 * @see org.rdfdht.hdt.compact.permutation.Permutation#load(java.io.InputStream, org.rdfhdt.hdt.listener.ProgressListener)
	 */
	@Override
	public void load(InputStream input, ProgressListener listener) throws IOException {

		CRCInputStream in = new CRCInputStream(input, new CRC8());
		
		int type = in.read();
		if(type!=PermutationFactory.TYPE_PERMUTATION_MRRR){
			throw new IllegalFormatException("Trying to read a PERMUTATION_MRRR but the data is not PERMUTATION_MRRR");
		}
		this.step = in.read();
		// Validate CRC
		if(!in.readCRCAndCheck()) {
			throw new CRCException("CRC Error while reading PERMUTATION_MRRR header.");
		}
		sequence.load(input, listener);
		bitmap.load(input, listener);
		backwardPointers.load(input, listener);
		
	}

	@Override
	public String getType() {
		return HDTVocabulary.PERM_TYPE_MRRR;
	}

}
