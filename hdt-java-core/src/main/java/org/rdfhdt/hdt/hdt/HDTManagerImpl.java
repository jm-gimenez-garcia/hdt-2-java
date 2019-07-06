package org.rdfhdt.hdt.hdt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.rdfhdt.hdt.dictionary.CompositeDictionary;
import org.rdfhdt.hdt.dictionary.GraphsDictionary;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.dictionary.impl.BaseReificationDictionary;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.enums.TripleComponentRole;
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
import org.rdfhdt.hdt.triples.QuadID;
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
	protected HDTPrivate doMapHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
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
	public HDTPrivate doLoadIndexedHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
		final HDTPrivate hdt = new HDTImpl(new HDTSpecification());
		hdt.loadFromHDT(hdtFileName, listener);
		hdt.loadOrCreateIndex(listener);
		return hdt;
	}

	@Override
	protected HDTPrivate doMapIndexedHDT(final String hdtFileName, final ProgressListener listener) throws IOException {
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
	public HDTPrivate doIndexedHDT(final HDTPrivate hdt, final ProgressListener listener) {
		hdt.loadOrCreateIndex(listener);
		return hdt;
	}

	@Override
	public HDTPrivate doGenerateHDT(final String rdfFileName, final String baseURI, final RDFNotation rdfNotation, final HDTOptions spec, final boolean reif, final ProgressListener listener)
			throws IOException, ParserException {

		// Let implementations override the one/two pass.
		try {
			HDTFactory.getTempFactory().checkTwoPass(spec);
		} catch (final Exception e) {

		}

		// choose the importer
		final String loaderType = spec.get("loader.type");
		TempHDTImporter loader;

		// If doing reification we do only two-pass for now
		if (reif) {
			loader = new TempHDTImporterTwoPass(reif);
		} else {
			if ("two-pass".equals(loaderType)) {
				loader = new TempHDTImporterTwoPass(false);
			} else {
				loader = new TempHDTImporterOnePass();
			}
		}

		final StopWatch st = new StopWatch();

		// Create TempHDT
		final TempHDT modHdt = loader.loadFromRDF(spec, rdfFileName, baseURI, rdfNotation, listener);

		// Show Basic stats
		/*System.out.println("----------------------------------------");
		System.out.println("Modifiable (temporal) dictionary created");
		System.out.println("The HDT is a " + modHdt.getClass().getName());
		System.out.println("The dictionary is a " + modHdt.getDictionary().getClass().getName());
		System.out.println("The triples is a " + modHdt.getTriples().getClass().getName());
		System.out.println("Total Triples: " + modHdt.getTriples().getNumberOfElements());
		if (modHdt.getDictionary() instanceof CompositeDictionary) {
			final CompositeDictionary rd = (CompositeDictionary) modHdt.getDictionary();
			final TriplesDictionary td = rd.getTriplesDictionary();
			final GraphsDictionary gd = rd.getGraphsDictionary();
			System.out.println("Different subjects: " + rd.getNsubjects());
			System.out.println("Different predicates: " + rd.getNpredicates());
			System.out.println("Different objects: " + rd.getNobjects());
			System.out.println("Different graphs:" + rd.getNgraphs());
			System.out.println("Common Subject/Object in triples dictionary:" + td.getNshared());
			System.out.println("Different subjects in triples dictionary: " + td.getNsubjects());
			System.out.println("Different predicates in triples dictionary: " + td.getNpredicates());
			System.out.println("Different objects in triples dictionary: " + td.getNobjects());
			System.out.println("Common Subject/Object in graphs dictionary:" + gd.getNshared());
			System.out.println("Different subjects in graphs dictionary: " + gd.getNsubjects());
			System.out.println("Different objects in graphs dictionary: " + gd.getNobjects());
			System.out.println("Unused Graphs in graphs dictionary:" + gd.getNgraphs());
			td.getShared().getSortedEntries().forEachRemaining(
					X -> System.out.println("Triples Shared = " + X + " [" + rd.stringToId(X, TripleComponentRole.SUBJECT) + "]" + " [" + td.stringToId(X, TripleComponentRole.SUBJECT) + "]"));
			gd.getShared().getSortedEntries().forEachRemaining(
					X -> System.out.println("Graphs Shared = " + X + " [" + rd.stringToId(X, TripleComponentRole.SUBJECT) + "]" + " [" + gd.stringToId(X, TripleComponentRole.SUBJECT) + "]"));
			td.getSubjects().getSortedEntries().forEachRemaining(
					X -> System.out.println("Triples Subjects = " + X + " [" + rd.stringToId(X, TripleComponentRole.SUBJECT) + "]" + " [" + td.stringToId(X, TripleComponentRole.SUBJECT) + "]"));
			gd.getSubjects().getSortedEntries().forEachRemaining(
					X -> System.out.println("Graphs Subjects = " + X + " [" + rd.stringToId(X, TripleComponentRole.SUBJECT) + "]" + " [" + gd.stringToId(X, TripleComponentRole.SUBJECT) + "]"));
			td.getObjects().getSortedEntries().forEachRemaining(
					X -> System.out.println("Triples Objects = " + X + " [" + rd.stringToId(X, TripleComponentRole.OBJECT) + "]" + " [" + td.stringToId(X, TripleComponentRole.OBJECT) + "]"));
			gd.getObjects().getSortedEntries().forEachRemaining(
					X -> System.out.println("Graphs Objects = " + X + " [" + rd.stringToId(X, TripleComponentRole.OBJECT) + "]" + " [" + gd.stringToId(X, TripleComponentRole.OBJECT) + "]"));
			td.getPredicates().getSortedEntries().forEachRemaining(
					X -> System.out.println("Triples Predicates = " + X + " [" + rd.stringToId(X, TripleComponentRole.PREDICATE) + "]" + " [" + td.stringToId(X, TripleComponentRole.PREDICATE) + "]"));
			gd.getGraphs().getSortedEntries().forEachRemaining(
					X -> System.out.println("Graphs Unused = " + X + " [" + rd.stringToId(X, TripleComponentRole.GRAPH) + "]" + " [" + gd.stringToId(X, TripleComponentRole.GRAPH) + "]"));
		} else if (modHdt.getDictionary() instanceof TriplesDictionary) {
			final TriplesDictionary td = (TriplesDictionary) modHdt.getDictionary();
			System.out.println("Common Subject/Object:" + td.getNshared());
			System.out.println("Different subjects: " + td.getNsubjects());
			System.out.println("Different predicates: " + td.getNpredicates());
			System.out.println("Different objects: " + td.getNobjects());
			td.getShared().getSortedEntries().forEachRemaining(X -> System.out.println("Triples Shared = " + X));
			td.getSubjects().getSortedEntries().forEachRemaining(X -> System.out.println("Triples Subjects = " + X));
			td.getObjects().getSortedEntries().forEachRemaining(X -> System.out.println("Triples Objects = " + X));
			td.getPredicates().getSortedEntries().forEachRemaining(X -> System.out.println("Triples Predicates = " + X));
		} else if (modHdt.getDictionary() instanceof GraphsDictionary) {
			final GraphsDictionary gd = (GraphsDictionary) modHdt.getDictionary();
			System.out.println("Unused Graphs:" + gd.getNgraphs());
			System.out.println("Different subjects: " + gd.getNsubjects());
			System.out.println("Different objects: " + gd.getNobjects());
			System.out.println("Common Subject/Object:" + gd.getNshared());
			gd.getShared().getSortedEntries().forEachRemaining(X -> System.out.println("Graphs Shared = " + X));
			gd.getSubjects().getSortedEntries().forEachRemaining(X -> System.out.println("Graphs Subjects = " + X));
			gd.getObjects().getSortedEntries().forEachRemaining(X -> System.out.println("Graphs Objects = " + X));
			gd.getGraphs().getSortedEntries().forEachRemaining(X -> System.out.println("Graphs Unused = " + X));
		}
		System.out.println("----------------------------------------");
		modHdt.getTriples().searchAll().forEachRemaining(X -> System.out.println(X));
		System.out.println("----------------------------------------");
		modHdt.getTriples().searchAll().forEachRemaining(X -> System.out.println(((BaseReificationDictionary<TriplesDictionary, GraphsDictionary>) modHdt.getDictionary()).toRoleIDs(X)));
		System.out.println("----------------------------------------");
		modHdt.getTriples().searchAll().forEachRemaining(X -> System.out.println(((BaseReificationDictionary<TriplesDictionary, GraphsDictionary>) modHdt.getDictionary())
				.toGlobalIDs(((BaseReificationDictionary<TriplesDictionary, GraphsDictionary>) modHdt.getDictionary()).toRoleIDs(X))));
		System.out.println("----------------------------------------");
		modHdt.getTriples().searchAll().forEachRemaining(X -> System.out.println(modHdt.getDictionary().idToString(X.getSubject(), TripleComponentRole.SUBJECT) + " " +
				modHdt.getDictionary().idToString(X.getPredicate(), TripleComponentRole.PREDICATE) + " " +
				modHdt.getDictionary().idToString(X.getObject(), TripleComponentRole.OBJECT) + " " +
				(X instanceof QuadID ? modHdt.getDictionary().idToString(((QuadID) X).getGraph(), TripleComponentRole.GRAPH) : "")));*/

		// Convert to HDT
		final HDTImpl hdt = new HDTImpl(spec, reif);
		hdt.loadFromHDT(modHdt, listener);
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
	public HDT doGenerateHDT(final IteratorTripleString triples, final String baseURI, final HDTOptions spec, final boolean reif, final ProgressListener listener) throws IOException {
		// choose the importer
		final TempHDTImporterOnePass loader = new TempHDTImporterOnePass();

		final StopWatch st = new StopWatch();

		// Create TempHDT
		final TempHDT modHdt = loader.loadFromTriples(spec, triples, baseURI, reif, listener);

		// Convert to HDT
		final HDTImpl hdt = new HDTImpl(spec);
		hdt.loadFromHDT(modHdt, listener);
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
	public HDTPrivate doHDTCat(String location, String hdtFileName1, String hdtFileName2, HDTOptions hdtFormat, ProgressListener listener) throws IOException {
		StopWatch st = new StopWatch();
	
		HDT hdt1 = doMapHDT(hdtFileName1, listener);
		HDT hdt2 = doMapHDT(hdtFileName2, listener);
		HDTImpl hdt = new HDTImpl(new HDTSpecification(),true);
		hdt.cat(location, hdt1, hdt2, listener);
		return hdt;
	}
	protected class LambdaCounter {
		long counter = 0;

		public LambdaCounter(final long i) {
			set(i);
		}

		public long set(final long i) {
			return this.counter = i;
		}

		public long get() {
			return this.counter;
		}

		public long increment() {
			return this.counter++;
		}
	}

}
