package org.rdfhdt.hdt.hdt;

import java.io.IOException;
import java.io.InputStream;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.IteratorTripleString;

public abstract class HDTManager {

    private static HDTManager instance;

    private static HDTManager getInstance() {
	if (instance == null) {
	    try {
		final Class<?> managerImplClass = Class.forName("org.rdfhdt.hdt.hdt.HDTManagerImpl");
		instance = (HDTManager) managerImplClass.newInstance();
	    } catch (final ClassNotFoundException e) {
		throw new RuntimeException("Class org.rdfhdt.hdt.hdt.HDTManagerImpl not found. Did you include the HDT implementation jar?");
	    } catch (final InstantiationException e) {
		throw new RuntimeException("Cannot create implementation for HDTManager. Does the class org.rdfhdt.hdt.hdt.HDTManagerImpl inherit from HDTManager?");
	    } catch (final IllegalAccessException e) {
		throw new RuntimeException(e);
	    }
	}
	return instance;
    }

    public static HDTOptions readOptions(final String file) throws IOException {
	return HDTManager.getInstance().doReadOptions(file);
    }

    /**
     * Load an HDT file into memory to use it. NOTE: Use this method to go through all elements. If you plan
     * to do queries, use loadIndexedHDT() instead.
     *
     * @param hdtFileName
     * @param listener
     *            Listener to get notified of loading progress. Can be null if no notifications needed.
     * @return
     * @throws IOException
     */
    public static HDT loadHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
	return HDTManager.getInstance().doLoadHDT(hdtFileName, listener);
    }

    /**
     * Map an HDT file into memory to use it. This method does not load the whole file into memory,
     * it lets the OS to handle memory pages as desired. Therefore it uses less memory but can be slower
     * for querying because it needs to load those blocks from disk.
     * NOTE: Use this method to go through all elements. If you plan to do queries, use mapIndexedHDT() instead.
     *
     * @param hdtFileName
     * @param listener
     *            Listener to get notified of loading progress. Can be null if no notifications needed.
     * @return
     * @throws IOException
     */
    public static HDTPrivate mapHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
	return HDTManager.getInstance().doMapHDT(hdtFileName, listener);
    }

    /**
     * Load an HDT from an InputStream (File, socket...). NOTE: Use this method to go through all elements. If you plan
     * to do queries, use loadIndexedHDT() instead.
     *
     * @param hdtFile
     * @param listener
     *            Listener to get notified of loading progress. Can be null if no notifications needed.
     * @return
     * @throws IOException
     */
    public static HDT loadHDT(final InputStream hdtFile, final ProgressListener listener) throws IOException {
	return HDTManager.getInstance().doLoadHDT(hdtFile, listener);
    }

    /**
     * Load an HDT File, and load/create additional indexes to support all kind of queries efficiently.
     *
     * @param hdtFileName
     * @param listener
     *            Listener to get notified of loading progress. Can be null if no notifications needed.
     * @return
     * @throws IOException
     */
    public static HDTPrivate loadIndexedHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
	return HDTManager.getInstance().doLoadIndexedHDT(hdtFileName, listener);
    }

    /**
     * Maps an HDT File into virtual memory, and load/create additional indexes to support all kind of queries efficiently.
     *
     * @param hdtFileName
     * @param listener
     *            Listener to get notified of loading progress. Can be null if no notifications needed.
     * @return
     * @throws IOException
     */
    public static HDTPrivate mapIndexedHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
	return HDTManager.getInstance().doMapIndexedHDT(hdtFileName, listener);
    }

    /**
     * Load an HDT file from InputStream, and create additional indexes to support all kind of queries efficiently.
     *
     * @param hdtFileName
     * @param listener
     *            Listener to get notified of loading progress. Can be null if no notifications needed.
     * @return
     * @throws IOException
     */
    public static HDT loadIndexedHDT(final InputStream hdtFileName, final ProgressListener listener) throws IOException {
	return HDTManager.getInstance().doLoadIndexedHDT(hdtFileName, listener);
    }

    /**
     * Return an indexed HDT that is efficient for all kind of queries, given a not indexed HDT.
     *
     * @param hdt
     * @param listener
     *            Listener to get notified of loading progress. Can be null if no notifications needed.
     * @return
     */
    public static HDTPrivate indexedHDT(final HDTPrivate hdt, final ProgressListener listener) {
	return HDTManager.getInstance().doIndexedHDT(hdt, listener);
    }

    /**
     * Create an HDT file from an RDF file.
     *
     * @param rdfFileName
     *            File name.
     * @param baseURI
     *            Base URI for the dataset.
     * @param rdfNotation
     *            Format of the source RDF File (NTriples, N3, RDF-XML...)
     * @param hdtFormat
     *            Parameters to tune the generated HDT.
     * @param reif
     *            Whether use statement reification or not
     * @param listener
     *            Listener to get notified of loading progress. Can be null if no notifications needed.
     * @return
     * @throws IOException
     * @throws ParserException
     */
    public static HDTPrivate generateHDT(final String rdfFileName, final String baseURI, final RDFNotation rdfNotation, final HDTOptions hdtFormat, final boolean reif, final ProgressListener listener)
	    throws IOException, ParserException {
	return HDTManager.getInstance().doGenerateHDT(rdfFileName, baseURI, rdfNotation, hdtFormat, reif, listener);
    }

    /**
     * Create an HDT file from an RDF file.
     *
     * @param iterator
     *            A provider of triples. Must implement hasNext(), next() and estimatedNumResults.
     * @param baseURI
     *            Base URI for the dataset.
     * @param hdtFormat
     *            Parameters to tune the generated HDT.
     * @param listener
     *            Listener to get notified of loading progress. Can be null if no notifications needed.
     * @return
     * @throws IOException
     */
    public static HDT generateHDT(final IteratorTripleString iterator, final String baseURI, final HDTOptions hdtFormat, final boolean reif, final ProgressListener listener)
	    throws IOException, ParserException {
	return HDTManager.getInstance().doGenerateHDT(iterator, baseURI, hdtFormat, reif, listener);
    }
    
	public static HDT catHDT(String location, String hdtFileName1, String hdtFileName2, HDTOptions hdtFormat, ProgressListener listener) throws IOException {
		return HDTManager.getInstance().doHDTCat(location, hdtFileName1, hdtFileName2, hdtFormat, listener);
	}

    // Abstract methods for the current implementation
    protected abstract HDTOptions doReadOptions(String file) throws IOException;

    protected abstract HDT doLoadHDT(String hdtFileName, ProgressListener listener) throws IOException;

    protected abstract HDT doLoadHDT(InputStream hdtFile, ProgressListener listener) throws IOException;

    protected abstract HDTPrivate doMapHDT(String hdtFileName, ProgressListener listener) throws IOException;

    protected abstract HDTPrivate doLoadIndexedHDT(String hdtFileName, ProgressListener listener) throws IOException;

    protected abstract HDT doLoadIndexedHDT(InputStream hdtFileName, ProgressListener listener) throws IOException;

    protected abstract HDTPrivate doMapIndexedHDT(String hdtFileName, ProgressListener listener) throws IOException;

    protected abstract HDTPrivate doIndexedHDT(HDTPrivate hdt, ProgressListener listener);

    protected abstract HDTPrivate doGenerateHDT(String rdfFileName, String baseURI, RDFNotation rdfNotation, HDTOptions hdtFormat, boolean reif, ProgressListener listener)
	    throws IOException, ParserException;

    protected abstract HDT doGenerateHDT(IteratorTripleString iterator, String baseURI, HDTOptions hdtFormat, boolean reif, ProgressListener listener) throws IOException;
	protected abstract HDT doHDTCat(String location, String hdtFileName1, String hdtFileName2, HDTOptions hdtFormat, ProgressListener listener) throws IOException;

}
