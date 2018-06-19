package org.rdfhdt.hdt.hdt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.impl.HDTImpl;
import org.rdfhdt.hdt.hdt.impl.TempHDTImporterOnePass;
import org.rdfhdt.hdt.hdt.impl.TempHDTImporterTwoPass;
import org.rdfhdt.hdt.header.HeaderUtil;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.util.StopWatch;

public class HDTManagerImpl extends HDTManager {

    @Override
    public HDTOptions doReadOptions(final String file) throws IOException {
	return new HDTSpecification(file);
    }

    @Override
    public HDT doLoadHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
	final HDTPrivate hdt = new HDTImpl(new HDTSpecification());
	hdt.loadFromHDT(hdtFileName, listener);
	return hdt;
    }

    @Override
    protected HDT doMapHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
	final HDTPrivate hdt = new HDTImpl(new HDTSpecification());
	hdt.mapFromHDT(new File(hdtFileName), 0, listener);
	return hdt;
    }

    @Override
    public HDT doLoadHDT(final InputStream hdtFile, final ProgressListener listener) throws IOException {
	final HDTPrivate hdt = new HDTImpl(new HDTSpecification());
	hdt.loadFromHDT(hdtFile, listener);
	return hdt;
    }

    @Override
    public HDT doLoadIndexedHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
	final HDTPrivate hdt = new HDTImpl(new HDTSpecification());
	hdt.loadFromHDT(hdtFileName, listener);
	hdt.loadOrCreateIndex(listener);
	return hdt;
    }

    @Override
    protected HDT doMapIndexedHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
	final HDTPrivate hdt = new HDTImpl(new HDTSpecification());
	hdt.mapFromHDT(new File(hdtFileName), 0, listener);
	hdt.loadOrCreateIndex(listener);
	return hdt;
    }

    @Override
    public HDT doLoadIndexedHDT(final InputStream hdtFile, final ProgressListener listener) throws IOException {
	final HDTPrivate hdt = new HDTImpl(new HDTSpecification());
	hdt.loadFromHDT(hdtFile, listener);
	hdt.loadOrCreateIndex(listener);
	return hdt;
    }

    @Override
    public HDT doIndexedHDT(final HDT hdt, final ProgressListener listener) {
	((HDTPrivate) hdt).loadOrCreateIndex(listener);
	return hdt;
    }

    @Override
    public HDT doGenerateHDT(final String rdfFileName, final String baseURI, final RDFNotation rdfNotation, final HDTOptions spec, final boolean reif, final ProgressListener listener)
	    throws IOException, ParserException {

	// Let implementations override the one/two pass.
	try {
	    HDTFactory.getTempFactory().checkTwoPass(spec);
	} catch (final Exception e) {

	}

	// choose the importer
	final String loaderType = spec.get("loader.type");
	TempHDTImporter loader;
	if ("two-pass".equals(loaderType)) {
	    loader = new TempHDTImporterTwoPass();
	} else {
	    loader = new TempHDTImporterOnePass();
	}

	final StopWatch st = new StopWatch();

	// Create TempHDT
	final TempHDT modHdt = loader.loadFromRDF(spec, rdfFileName, baseURI, rdfNotation, reif, listener);

	// Convert to HDT
	final HDTImpl hdt = new HDTImpl(spec);
	hdt.loadFromModifiableHDT(modHdt, listener);
	hdt.populateHeaderStructure(modHdt.getBaseURI());

	// Add file size to Header
	try {
	    final long originalSize = HeaderUtil.getPropertyLong(modHdt.getHeader(), "_:statistics", HDTVocabulary.ORIGINAL_SIZE);
	    hdt.getHeader().insert("_:statistics", HDTVocabulary.ORIGINAL_SIZE, originalSize);
	} catch (final NotFoundException e) {
	}

	System.out.println("File converted in: " + st.stopAndShow());

	modHdt.close();

	return hdt;
    }

    @Override
    public HDT doGenerateHDT(final IteratorTripleString triples, final String baseURI, final HDTOptions spec, final ProgressListener listener) throws IOException {
	// choose the importer
	final TempHDTImporterOnePass loader = new TempHDTImporterOnePass();

	final StopWatch st = new StopWatch();

	// Create TempHDT
	final TempHDT modHdt = loader.loadFromTriples(spec, triples, baseURI, listener);

	// Convert to HDT
	final HDTImpl hdt = new HDTImpl(spec);
	hdt.loadFromModifiableHDT(modHdt, listener);
	hdt.populateHeaderStructure(modHdt.getBaseURI());

	// Add file size to Header
	try {
	    final long originalSize = HeaderUtil.getPropertyLong(modHdt.getHeader(), "_:statistics", HDTVocabulary.ORIGINAL_SIZE);
	    hdt.getHeader().insert("_:statistics", HDTVocabulary.ORIGINAL_SIZE, originalSize);
	} catch (final NotFoundException e) {
	}

	System.out.println("File converted in: " + st.stopAndShow());

	modHdt.close();

	return hdt;
    }

}
