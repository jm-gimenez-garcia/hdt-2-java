package org.rdfhdt.hdt.dictionary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.util.io.CountInputStream;

/**
 * @author mario.arias, Eugen, José M. Giménez-García
 *
 */
public interface DictionaryPrivate<T extends Dictionary> extends Dictionary {
    /**
     * Loads a dictionary from a InputStream
     *
     * @param input
     *            InputStream to load the dictionary from
     * @throws IOException
     */
    void load(InputStream input, ControlInfo ci, ProgressListener listener) throws IOException;

    void mapFromFile(CountInputStream in, File f, ProgressListener listener) throws IOException;

    /**
     * Loads all information from another dictionary into this dictionary.
     */
    void load(T other, ProgressListener listener);

    /**
     * Saves the dictionary to a OutputStream
     */
    void save(OutputStream output, ControlInfo ci, ProgressListener listener) throws IOException;

    /**
     * Fills the header with information from the dictionary
     */
    void populateHeader(Header header, String rootNode);

    /**
     * Returns the type of the dictionary (the way it is written onto file/held in memory)
     *
     * @return
     */
    String getType();
}
