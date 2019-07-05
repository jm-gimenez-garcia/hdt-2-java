package org.rdfhdt.hdt.util;

import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.dictionary.GraphsDictionary;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.dictionary.impl.DictionaryCat;
import org.rdfhdt.hdt.dictionary.impl.GraphsFourSectionDictionaryBig;
import org.rdfhdt.hdt.dictionary.impl.util.CatMapping;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.Triples;

import java.util.Iterator;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;

public class Utility {

    public static void printTriplesDictionary(TriplesDictionary d){
        Iterator i = d.getShared().getSortedEntries();
        int count = 0;
        System.out.println("SHARED");
        while (i.hasNext()){
            System.out.println(count +"---"+i.next().toString());
            count++;
        }
        System.out.println("SUBJECTS");
        count = 0;
        i = d.getSubjects().getSortedEntries();
        while (i.hasNext()){
            System.out.println(count +"---"+i.next().toString());
            count++;
        }
        System.out.println("OBJECTS");
        count = 0;
        i = d.getObjects().getSortedEntries();
        while (i.hasNext()){
            System.out.println(count +"---"+i.next().toString());
            count++;
        }
        System.out.println("PREDICATES");
        count = 0;
        i = d.getPredicates().getSortedEntries();
        while (i.hasNext()){
            System.out.println(count +"---"+i.next().toString());
            count++;
        }
    }
    public static void printGraphsDictionary(GraphsFourSectionDictionaryBig g){
        Iterator i = g.getShared().getSortedEntries();
        int count = 0;
        System.out.println("SHARED");
        while (i.hasNext()){
            System.out.println(count +"---"+i.next().toString());
            count++;
        }
        System.out.println("SUBJECTS");
        count = 0;
        if(g.getSubjects() == null) {
        	System.out.println("nullll");
        	return;
        }
        i = g.getSubjects().getSortedEntries();
        while (i.hasNext()){
            System.out.println(count +"---"+i.next().toString());
            count++;
        }
        System.out.println("OBJECTS");
        count = 0;
        i = g.getObjects().getSortedEntries();
        while (i.hasNext()){
            System.out.println(count +"---"+i.next().toString());
            count++;
        }
        System.out.println("UNUSED");
        count = 0;
        i = g.getGraphs().getSortedEntries();
        while (i.hasNext()){
            System.out.println(count +"---"+i.next().toString());
            count++;
        }
    }

   /* public static void compareDictionary(Dictionary d1, Dictionary d2){
        Iterator i1 = d1.getShared().getSortedEntries();
        Iterator i2 = d2.getShared().getSortedEntries();
        while (i1.hasNext()){
            if (i2.hasNext()!=true){
                assertFalse("The dictionaries have a different size",true);
            } else {
                assertEquals(i1.next().toString(),i2.next().toString());
            }
        }
        i1 = d1.getSubjects().getSortedEntries();
        i2 = d2.getSubjects().getSortedEntries();
        while (i1.hasNext()){
            if (i2.hasNext()!=true){
                assertFalse("The dictionaries have a different size",true);
            } else {
                assertEquals(i1.next().toString(),i2.next().toString());
            }
        }
        i1 = d1.getObjects().getSortedEntries();
        i2 = d2.getObjects().getSortedEntries();
        while (i1.hasNext()){
            if (i2.hasNext()!=true){
                assertFalse("The dictionaries have a different size",true);
            } else {
                assertEquals(i1.next().toString(),i2.next().toString());
            }
        }
        i1 = d1.getPredicates().getSortedEntries();
        i2 = d2.getPredicates().getSortedEntries();
        while (i1.hasNext()){
            if (i2.hasNext()!=true){
                assertFalse("The dictionaries have a different size",true);
            } else {
                assertEquals(i1.next().toString(),i2.next().toString());
            }
        }
    }*/

    public static void printTriples(HDT hdt){
        IteratorTripleID it = hdt.getTriples().searchAll();
        while (it.hasNext()){
            TripleID tripleIDOld = it.next();
            String subject = hdt.getDictionary().idToString(tripleIDOld.getSubject(), TripleComponentRole.SUBJECT).toString();
            String predicate = hdt.getDictionary().idToString(tripleIDOld.getPredicate(), TripleComponentRole.PREDICATE).toString();
            String object = hdt.getDictionary().idToString(tripleIDOld.getObject(), TripleComponentRole.OBJECT).toString();
            System.out.println(subject+"--"+predicate+"--"+object);
        }
    }
    public static void printQuads(HDT hdt){
        IteratorTripleID it = hdt.getTriples().searchAll();
        while (it.hasNext()){
        	TripleID tripleIDOld = it.next();

        	String subject = hdt.getDictionary().idToString(tripleIDOld.getSubject(), TripleComponentRole.SUBJECT).toString();
            String predicate = hdt.getDictionary().idToString(tripleIDOld.getPredicate(), TripleComponentRole.PREDICATE).toString();
            String object = hdt.getDictionary().idToString(tripleIDOld.getObject(), TripleComponentRole.OBJECT).toString();
            
			if (tripleIDOld instanceof QuadID) {
				QuadID quad = (QuadID) tripleIDOld;
				if (quad.getGraph() != 0) {
					String graph = hdt.getDictionary().idToString(quad.getGraph(), TripleComponentRole.GRAPH)
							.toString();
					System.out.println(subject + "--" + predicate + "--" + object + "--" + graph);
				}else {
					System.out.println(subject + "--" + predicate + "--" + object);
				}
			}
        }
    }
    public static void printIDs(HDT hdt) {
    	IteratorTripleID it = hdt.getTriples().searchAll();
        while (it.hasNext()){
        	TripleID tripleIDOld = it.next();

        	int subject = tripleIDOld.getSubject();
            int predicate = tripleIDOld.getPredicate();
            int object = tripleIDOld.getObject();
            
            if(tripleIDOld instanceof QuadID) {
            	QuadID quad = (QuadID)tripleIDOld;
            	int graph = quad.getGraph();
            	String subject_st = hdt.getDictionary().idToString(tripleIDOld.getSubject(), TripleComponentRole.SUBJECT).toString();
                String predicate_st = hdt.getDictionary().idToString(tripleIDOld.getPredicate(), TripleComponentRole.PREDICATE).toString();
                String object_st = hdt.getDictionary().idToString(tripleIDOld.getObject(), TripleComponentRole.OBJECT).toString();
                String graph_st="";
                if(graph != 0 )
                	graph_st = hdt.getDictionary().idToString(quad.getGraph(), TripleComponentRole.GRAPH).toString();
                System.out.println(subject+" "+subject_st+"--"+predicate+" "+predicate_st+"--"+object+" "+object_st+"--"+graph+" "+graph_st);
            }
        }
    }
    public static void compareTriples(HDT hdt1, HDT hdt2){
        IteratorTripleID itOld = hdt1.getTriples().searchAll();
        IteratorTripleID itNew = hdt2.getTriples().searchAll();
        while (itOld.hasNext()){
            if (itNew.hasNext()!=true){
                assertFalse("The number of triples is different",true);
            }
            TripleID tripleIDOld = itOld.next();
            String subjectOld = hdt1.getDictionary().idToString(tripleIDOld.getSubject(), TripleComponentRole.SUBJECT).toString();
            String predicateOld = hdt1.getDictionary().idToString(tripleIDOld.getPredicate(), TripleComponentRole.PREDICATE).toString();
            String objectOld = hdt1.getDictionary().idToString(tripleIDOld.getObject(), TripleComponentRole.OBJECT).toString();
            TripleID tripleIDNew = itNew.next();
            String subjectNew = hdt2.getDictionary().idToString(tripleIDNew.getSubject(), TripleComponentRole.SUBJECT).toString();
            String predicateNew = hdt2.getDictionary().idToString(tripleIDNew.getPredicate(), TripleComponentRole.PREDICATE).toString();
            String objectNew = hdt2.getDictionary().idToString(tripleIDNew.getObject(), TripleComponentRole.OBJECT).toString();
            assertEquals(subjectOld,subjectNew);
            assertEquals(predicateOld,predicateNew);
            assertEquals(objectOld,objectNew);

        }
    }
    public static void printMapping(CatMapping mapping,String name) {
    	System.out.println("name: "+name );
    	for (int i = 0; i < mapping.getSize(); i++) {
			System.out.println("NEW ID: "+mapping.getMapping(i)+" TYPE: "+mapping.getType(i));
		}
    }
    public static void printMappings(DictionaryCat dictionaryCat) {
    	Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_S_1),dictionaryCat.M_S_1);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_S_2),dictionaryCat.M_S_2);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_O_1),dictionaryCat.M_O_1);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_O_2),dictionaryCat.M_O_2);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_P_1),dictionaryCat.M_P_1);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_P_2),dictionaryCat.M_P_2);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_SH_1),dictionaryCat.M_SH_1);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_SH_2),dictionaryCat.M_SH_2);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_GSH_1),dictionaryCat.M_GSH_1);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_GSH_2),dictionaryCat.M_GSH_2);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_GS_1),dictionaryCat.M_GS_1);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_GS_2),dictionaryCat.M_GS_2);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_GO_1),dictionaryCat.M_GO_1);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_GO_2),dictionaryCat.M_GO_2);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_GU_1),dictionaryCat.M_GU_1);
		System.out.println("-----------------------------------------");
		Utility.printMapping(dictionaryCat.getMappings().get(dictionaryCat.M_GU_2),dictionaryCat.M_GU_2);
		System.out.println("-----------------------------------------");
		
    }

}
