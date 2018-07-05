package org.rdfhdt.hdt.hdt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.rdf.RDFAccess;

/**
 * HDT Operations that are using internally from the implementation.
 * @author mario.arias
 *
 */

public interface HDTPrivate extends HDT, RDFAccess {
    /**
     * Loads a HDT file
     *
     * @param input
     *            InputStream to read from
     */
    void loadFromHDT(InputStream input, ProgressListener listener) throws IOException;

    /**
     * Loads a HDT file
     *
     * @param input
     *            InputStream to read from
     */
    void loadFromHDT(String fileName, ProgressListener listener) throws IOException;

    void mapFromHDT(File f, long offset, ProgressListener listener) throws IOException;

    /**
     * Generates any additional index needed to solve all triple patterns more efficiently
     *
     * @param listener A listener to be notified of the progress.
     */
    void loadOrCreateIndex(ProgressListener listener);

    void populateHeaderStructure(String baseUri);

    /**
     * Saves to OutputStream in HDT format
     *
     * @param output
     *            The OutputStream to save to
     */
    void saveToHDT(OutputStream output, ProgressListener listener) throws IOException;

    /**
     * Saves to a file in HDT format
     *
     * @param output
     *            The OutputStream to save to
     */
    void saveToHDT(String fileName, ProgressListener listener) throws IOException;

    /**
     * Returns the size of the Data Structure in bytes.
     *
     * @return
     */
    long size();
}
