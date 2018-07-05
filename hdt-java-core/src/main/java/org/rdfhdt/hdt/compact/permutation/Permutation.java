package org.rdfhdt.hdt.compact.permutation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.listener.ProgressListener;

/**
 * The permutation interface represents a compact permutation of integers.
 * 
 */

public interface Permutation {


	/**
	 * Compute the value at the given position
	 * 
	 * @param position
	 *            The position of the element to be returned
	 * @return int
	 */
	long pi(long position);

	/**
	 * Gets the position where the value i appears
	 * 
	 * @return int
	 */
	long revpi(long i);
	
	/**
	 * Return the size of the data structure in bytes
	 * 
	 * @return
	 */
	long getSize();
	
	/**
	 * Return the length of this permutation
	 * 
	 * @return
	 */
	long getLength();

	/**
	 * Saves the permutation to an OutputStream
	 * 
	 * @param output
	 *            The OutputStream to be saved to
	 * @throws IOException
	 */
	void save(OutputStream output, ProgressListener listener) throws IOException;

	/**
	 * Loads a permutation from an InputStream
	 * 
	 * @param input
	 *            The InputStream to load from
	 */
	void load(InputStream input, ProgressListener listener) throws IOException;
	
	/**
	 * Type identifier of this Permutation.
	 * @return
	 */
	String getType();
}
