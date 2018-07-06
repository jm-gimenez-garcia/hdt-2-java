/**
 *
 */
package org.rdfhdt.hdt.dictionary;

import java.io.IOException;

import org.junit.Test;
import org.rdfhdt.hdt.dictionary.impl.GraphsFourSectionDictionary;
import org.rdfhdt.hdt.dictionary.impl.HashGraphsDictionary;
import org.rdfhdt.hdt.dictionary.impl.HashTriplesDictionary;
import org.rdfhdt.hdt.dictionary.impl.ReificationDictionary;
import org.rdfhdt.hdt.dictionary.impl.ReificationTempDictionary;
import org.rdfhdt.hdt.dictionary.impl.TriplesFourSectionDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTSpecification;

/**
 * @author José M. Giménez-García
 *
 */
public class TestDictionaries {

    @Test
    public void testReificationDictionary() throws IOException {
	final HDTSpecification spec = new HDTSpecification();
	final ReificationTempDictionary dictionaryTemp = new ReificationTempDictionary(new HashTriplesDictionary(spec), new HashGraphsDictionary(spec));
	final ReificationDictionary dictionaryPrivate = new ReificationDictionary(new TriplesFourSectionDictionary(spec), new GraphsFourSectionDictionary(spec));
	final ProgressListener listener = (level, message) -> System.out.print("\r" + message + "\t" + Float.toString(level) + "                            \r");

	dictionaryTemp.insert("SOG1", TripleComponentRole.SUBJECT);
	dictionaryTemp.insert("SOG1", TripleComponentRole.OBJECT);
	dictionaryTemp.insert("SOG1", TripleComponentRole.GRAPH);
	dictionaryTemp.insert("SG1", TripleComponentRole.SUBJECT);
	dictionaryTemp.insert("SG1", TripleComponentRole.GRAPH);
	dictionaryTemp.insert("OG1", TripleComponentRole.OBJECT);
	dictionaryTemp.insert("OG1", TripleComponentRole.GRAPH);
	dictionaryTemp.insert("G1", TripleComponentRole.GRAPH);
	dictionaryTemp.insert("SO1", TripleComponentRole.SUBJECT);
	dictionaryTemp.insert("SO1", TripleComponentRole.OBJECT);
	dictionaryTemp.insert("S1", TripleComponentRole.SUBJECT);
	dictionaryTemp.insert("P1", TripleComponentRole.PREDICATE);
	dictionaryTemp.insert("O1", TripleComponentRole.OBJECT);

	dictionaryTemp.reorganize();

	dictionaryPrivate.load(dictionaryTemp, listener);

	this.showResults(dictionaryPrivate);

	dictionaryTemp.close();
	dictionaryPrivate.close();
    }

    @Test
    public void testTriplesDictionary() throws IOException {
	final HDTSpecification spec = new HDTSpecification();
	final TriplesTempDictionary dictionaryTemp = new HashTriplesDictionary(spec);
	final TriplesDictionaryPrivate dictionaryPrivate = new TriplesFourSectionDictionary(spec);
	final ProgressListener listener = (level, message) -> System.out.print("\r" + message + "\t" + Float.toString(level) + "                            \r");

	dictionaryTemp.insert("SO1", TripleComponentRole.SUBJECT);
	dictionaryTemp.insert("SO1", TripleComponentRole.OBJECT);
	dictionaryTemp.insert("S1", TripleComponentRole.SUBJECT);
	dictionaryTemp.insert("P1", TripleComponentRole.PREDICATE);
	dictionaryTemp.insert("O1", TripleComponentRole.OBJECT);

	dictionaryTemp.reorganize();

	dictionaryPrivate.load(dictionaryTemp, listener);

	this.showResults(dictionaryPrivate);

	dictionaryTemp.close();
	dictionaryPrivate.close();
    }

    @Test
    public void testGraphsDictionary() throws IOException {
	final HDTSpecification spec = new HDTSpecification();
	final GraphsTempDictionary dictionaryTemp = new HashGraphsDictionary(spec);
	final GraphsDictionaryPrivate dictionaryPrivate = new GraphsFourSectionDictionary(spec);
	final ProgressListener listener = (level, message) -> System.out.print("\r" + message + "\t" + Float.toString(level) + "                            \r");

	dictionaryTemp.insert("SO1", TripleComponentRole.SUBJECT);
	dictionaryTemp.insert("SO1", TripleComponentRole.OBJECT);
	dictionaryTemp.insert("S1", TripleComponentRole.SUBJECT);
	dictionaryTemp.insert("O1", TripleComponentRole.OBJECT);
	dictionaryTemp.insert("G1", TripleComponentRole.GRAPH);

	dictionaryTemp.reorganize();

	dictionaryPrivate.load(dictionaryTemp, listener);

	this.showResults(dictionaryPrivate);

	dictionaryTemp.close();
	dictionaryPrivate.close();
    }

    private void showResults(final DictionaryPrivate dictionary) {

	if (dictionary instanceof ReificationDictionary) {
	    final ReificationDictionary reificationDictionary = (ReificationDictionary) dictionary;
	    System.out.println("--- REIFICATION DICTIONARY ---");
	    System.out.println("#Shared: " + reificationDictionary.getNshared());
	    System.out.println("#Subjects: " + reificationDictionary.getNsubjects());
	    System.out.println("#Objects: " + reificationDictionary.getNobjects());
	    System.out.println("#Predicates: " + reificationDictionary.getNpredicates());
	    System.out.println("#Graphs: " + reificationDictionary.getNgraphs());
	    System.out.println();
	    System.out.println("#SHARED: " + reificationDictionary.getShared().getNumberOfElements());
	    System.out.println("#SUBJECTS: " + reificationDictionary.getSubjects().getNumberOfElements());
	    System.out.println("#OBJECTS: " + reificationDictionary.getObjects().getNumberOfElements());
	    System.out.println("#PREDICATES: " + reificationDictionary.getPredicates().getNumberOfElements());
	    System.out.println("#GRAPHS: " + reificationDictionary.getGraphs().getNumberOfElements());
	    System.out.println();
	    reificationDictionary.getShared().getSortedEntries().forEachRemaining(V -> System.out.println("SHARED: " + V));
	    reificationDictionary.getSubjects().getSortedEntries().forEachRemaining(V -> System.out.println("SUBJECT: " + V));
	    reificationDictionary.getObjects().getSortedEntries().forEachRemaining(V -> System.out.println("OBJECT: " + V));
	    reificationDictionary.getPredicates().getSortedEntries().forEachRemaining(V -> System.out.println("PREDICATE: " + V));
	    reificationDictionary.getGraphs().getSortedEntries().forEachRemaining(V -> System.out.println("GRAPH: " + V));
	    System.out.println();
	    reificationDictionary.getTriplesDictionary().getShared().getSortedEntries().forEachRemaining(V -> System.out.println("TRIPLES SHARED: " + V));
	    reificationDictionary.getTriplesDictionary().getSubjects().getSortedEntries().forEachRemaining(V -> System.out.println("TRIPLES SUBJECT: " + V));
	    reificationDictionary.getTriplesDictionary().getObjects().getSortedEntries().forEachRemaining(V -> System.out.println("TRIPLES OBJECT: " + V));
	    reificationDictionary.getTriplesDictionary().getPredicates().getSortedEntries().forEachRemaining(V -> System.out.println("TRIPLES PREDICATE: " + V));
	    System.out.println();
	    reificationDictionary.getGraphsDictionary().getShared().getSortedEntries().forEachRemaining(V -> System.out.println("GRPAHS SHARED: " + V));
	    reificationDictionary.getGraphsDictionary().getSubjects().getSortedEntries().forEachRemaining(V -> System.out.println("GRPAHS SUBJECT: " + V));
	    reificationDictionary.getGraphsDictionary().getObjects().getSortedEntries().forEachRemaining(V -> System.out.println("GRPAHS OBJECT: " + V));
	    reificationDictionary.getGraphsDictionary().getGraphs().getSortedEntries().forEachRemaining(V -> System.out.println("GRPAHS UNUSED: " + V));
	}

	if (dictionary instanceof TriplesDictionaryPrivate) {
	    final TriplesDictionaryPrivate triplesDictionary = (TriplesDictionaryPrivate) dictionary;
	    System.out.println("--- TRIPLES DICTIONARY ---");
	    System.out.println("#Shared: " + triplesDictionary.getNshared());
	    System.out.println("#Subjects: " + triplesDictionary.getNsubjects());
	    System.out.println("#Objects: " + triplesDictionary.getNobjects());
	    System.out.println("#Predicates: " + triplesDictionary.getNpredicates());
	    System.out.println();
	    System.out.println("#SHARED: " + triplesDictionary.getShared().getNumberOfElements());
	    System.out.println("#SUBJECTS: " + triplesDictionary.getSubjects().getNumberOfElements());
	    System.out.println("#OBJECTS: " + triplesDictionary.getObjects().getNumberOfElements());
	    System.out.println("#PREDICATES: " + triplesDictionary.getPredicates().getNumberOfElements());
	    System.out.println();
	    triplesDictionary.getShared().getSortedEntries().forEachRemaining(V -> System.out.println("SHARED: " + V));
	    triplesDictionary.getSubjects().getSortedEntries().forEachRemaining(V -> System.out.println("SUBJECT: " + V));
	    triplesDictionary.getObjects().getSortedEntries().forEachRemaining(V -> System.out.println("OBJECT: " + V));
	    triplesDictionary.getPredicates().getSortedEntries().forEachRemaining(V -> System.out.println("PREDICATE: " + V));
	}

	if (dictionary instanceof GraphsDictionaryPrivate) {
	    final GraphsDictionaryPrivate graphsDictionary = (GraphsDictionaryPrivate) dictionary;
	    System.out.println("--- GRAPHS DICTIONARY ---");
	    System.out.println("#Shared: " + graphsDictionary.getNshared());
	    System.out.println("#Subjects: " + graphsDictionary.getNsubjects());
	    System.out.println("#Objects: " + graphsDictionary.getNobjects());
	    System.out.println("#Graphs: " + graphsDictionary.getNgraphs());
	    System.out.println();
	    System.out.println("#SHARED: " + graphsDictionary.getShared().getNumberOfElements());
	    System.out.println("#SUBJECTS: " + graphsDictionary.getSubjects().getNumberOfElements());
	    System.out.println("#OBJECTS: " + graphsDictionary.getObjects().getNumberOfElements());
	    System.out.println("#GRAPHS: " + graphsDictionary.getGraphs().getNumberOfElements());
	    System.out.println();
	    graphsDictionary.getShared().getSortedEntries().forEachRemaining(V -> System.out.println("SHARED: " + V));
	    graphsDictionary.getSubjects().getSortedEntries().forEachRemaining(V -> System.out.println("SUBJECT: " + V));
	    graphsDictionary.getObjects().getSortedEntries().forEachRemaining(V -> System.out.println("OBJECT: " + V));
	    graphsDictionary.getGraphs().getSortedEntries().forEachRemaining(V -> System.out.println("GRAPH: " + V));
	}
    }
}