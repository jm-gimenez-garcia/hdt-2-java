/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/FourSectionDictionary.java $
 * Revision: $Rev: 191 $
 * Last modified: $Date: 2013-03-03 11:41:43 +0000 (dom, 03 mar 2013) $
 * Last modified by: $Author: mario.arias $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contacting the authors:
 *   Dennis Diefenbach:         dennis.diefenbach@googlemail.com
 *   Jose Gimenez Garcia:       jose.gimenez.garcia@univ-st-etienne.fr
 */

package org.rdfhdt.hdt.dictionary.impl;

import org.apache.commons.math3.util.Pair;
import org.rdfhdt.hdt.compact.integer.VByte;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64BigDisk;
import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.dictionary.GraphsDictionary;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.dictionary.impl.util.CatCommon;
import org.rdfhdt.hdt.dictionary.impl.util.CatIteratorList;
import org.rdfhdt.hdt.dictionary.impl.util.CatIteratorList2;
import org.rdfhdt.hdt.dictionary.impl.util.CatMapping;
import org.rdfhdt.hdt.dictionary.impl.util.CatMappingBack;
import org.rdfhdt.hdt.dictionary.impl.util.IterElement;
import org.rdfhdt.hdt.dictionary.impl.util.IteratorPlusString;
import org.rdfhdt.hdt.dictionary.impl.util.ScoreComparator;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.util.crc.CRC32;
import org.rdfhdt.hdt.util.crc.CRC8;
import org.rdfhdt.hdt.util.crc.CRCOutputStream;
import org.rdfhdt.hdt.util.io.IOUtil;
import org.rdfhdt.hdt.util.string.ByteStringUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author alyhdr
 */
public class DictionaryCat {

	private String location;
	private int DEFAULT_BLOCK_SIZE = 16;
	private int BLOCK_PER_BUFFER = 100000;


	private CatMappingBack mappingS;

	public final String M_SH_1 = "SH1";
	public final String M_SH_2 = "SH2";

	public final String M_S_1 = "S1";
	public final String M_S_2 = "S2";

	public final String M_O_1 = "O1";
	public final String M_O_2 = "O2";

	public final String M_P_1 = "P1";
	public final String M_P_2 = "P2";

	public final String M_GSH_1 = "GSH1";
	public final String M_GSH_2 = "GSH2";

	public final String M_GS_1 = "GS1";
	public final String M_GS_2 = "GS2";

	public final String M_GO_1 = "GO1";
	public final String M_GO_2 = "GO2";

	public final String M_GU_1 = "GU1";
	public final String M_GU_2 = "GU2";
	
	public int numPredicates;
	public int numSubjects;
	public int numObjects;
	public int numShared;
	public int numSharedGraphs;
	public int numSubjectGraphs;
	public int numObjectGraphs;
	public int numUnusedGraphs;
	
	
	private HashMap<String, CatMapping> mappings;

	private HDTOptions spec;

	public DictionaryCat(String location) {

		this.location = location;
		this.mappings = new HashMap<String, CatMapping>();
	}

	public void cat(ReificationDictionary dictionary1, ReificationDictionary dictionary2){
		
		

		//get the dictionaries of both files
		TriplesDictionary triples_dictionary_1 = dictionary1.getTriplesDictionary();
		TriplesDictionary triples_dictionary_2 = dictionary2.getTriplesDictionary();
		
		GraphsDictionary graphs_dictionary_1 = dictionary1.getGraphsDictionary();
		GraphsDictionary graphs_dictionary_2 = dictionary2.getGraphsDictionary();
		
		
        mappings.put(M_P_1,new CatMapping(location,M_P_1,(int)triples_dictionary_1.getPredicates().getNumberOfElements()));
        mappings.put(M_P_2,new CatMapping(location,M_P_2,(int)triples_dictionary_2.getPredicates().getNumberOfElements()));
        
        mappings.put(M_S_1,new CatMapping(location,M_S_1,(int)triples_dictionary_1.getSubjects().getNumberOfElements()));
        mappings.put(M_S_2,new CatMapping(location,M_S_2,(int)triples_dictionary_2.getSubjects().getNumberOfElements()));
       
        mappings.put(M_O_1,new CatMapping(location,M_O_1,(int)triples_dictionary_1.getObjects().getNumberOfElements()));
        mappings.put(M_O_2,new CatMapping(location,M_O_2,(int)triples_dictionary_2.getObjects().getNumberOfElements()));
       
        mappings.put(M_SH_1,new CatMapping(location,M_SH_1,(int)triples_dictionary_1.getShared().getNumberOfElements()));
        mappings.put(M_SH_2,new CatMapping(location,M_SH_2,(int)triples_dictionary_2.getShared().getNumberOfElements()));
       
        mappings.put(M_GSH_1,new CatMapping(location,M_GSH_1,(int)graphs_dictionary_1.getShared().getNumberOfElements()));
        mappings.put(M_GSH_2,new CatMapping(location,M_GSH_2,(int)graphs_dictionary_2.getShared().getNumberOfElements()));
       
        mappings.put(M_GS_1,new CatMapping(location,M_GS_1,(int)graphs_dictionary_1.getSubjects().getNumberOfElements()));
        mappings.put(M_GS_2,new CatMapping(location,M_GS_2,(int)graphs_dictionary_2.getSubjects().getNumberOfElements()));
        
        mappings.put(M_GO_1,new CatMapping(location,M_GO_1,(int)graphs_dictionary_1.getObjects().getNumberOfElements()));
        mappings.put(M_GO_2,new CatMapping(location,M_GO_2,(int)graphs_dictionary_2.getObjects().getNumberOfElements()));
      
        mappings.put(M_GU_1,new CatMapping(location,M_GU_1,(int)graphs_dictionary_1.getGraphs().getNumberOfElements()));
        mappings.put(M_GU_2,new CatMapping(location,M_GU_2,(int)graphs_dictionary_2.getGraphs().getNumberOfElements()));
      
        //join the predicates first because it is easy
        System.out.println("PREDICATES-------------------");

        
        
        int numCommonPredicates = 0;
        Iterator<IterElement> commonP1P2 = new CatCommon(getIterator(triples_dictionary_1.getPredicates()),getIterator(triples_dictionary_2.getPredicates()));
        while (commonP1P2.hasNext()){
            commonP1P2.next();
            numCommonPredicates++;
        }
        this.numPredicates = triples_dictionary_1.getPredicates().getNumberOfElements()+ triples_dictionary_2.getPredicates().getNumberOfElements()-numCommonPredicates;
        catAnySection(numPredicates,triples_dictionary_1.getPredicates(),triples_dictionary_2.getPredicates(),
        		Collections.<IterElement>emptyList().iterator(), Collections.<IterElement>emptyList().iterator(),
        		Collections.<IterElement>emptyList().iterator(),new ArrayList<CatCommon>(),
        		mappings.get(M_P_1),mappings.get(M_P_2),4);
        
       /* this.triplesDictionary.predicates = dictionarySectionCatPredicates;
        Iterator it = this.triplesDictionary.predicates.getSortedEntries();
        while(it.hasNext()) {
        	System.out.println(it.next());
        }*/
		//subjects
        
        System.out.println("SUBJECTS-------------------");
        
        ArrayList<Iterator<IterElement>> iters1 = new ArrayList<>();
        
        //add iters on triples part
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getShared())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getObjects())));
        
        //add iters on graphs part
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getShared())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getSubjects())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getObjects())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getGraphs())));
        
		CatIteratorList commonSubject1 = new CatIteratorList(iters1);
		int numCommonSubject1Hdt2 = 0;
        while (commonSubject1.hasNext()){
        	commonSubject1.next();
            numCommonSubject1Hdt2++;
        }
        
        ArrayList<Iterator<IterElement>> iters2 = new ArrayList<>();
        
        //add iters on triples part
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(triples_dictionary_1.getShared())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(triples_dictionary_1.getObjects())));
        
        //add iters on graphs part
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getShared())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getSubjects())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getObjects())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getGraphs())));
        
        CatIteratorList commonSubject2 = new CatIteratorList(iters2);
        int numCommonSubject2Hdt1 = 0;
        while (commonSubject2.hasNext()){
            commonSubject2.next();
            numCommonSubject2Hdt1++;
        }
		int numCommonSubjects = 0;
        Iterator<IterElement> commonS1S2 = new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getSubjects()));
        while (commonS1S2.hasNext()){
            commonS1S2.next();
            numCommonSubjects++;
        }
        iters1 = new ArrayList<>();
        
        //add iters on triples part
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getShared())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getObjects())));
        
        //add iters on graphs part
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getShared())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getSubjects())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getObjects())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getGraphs())));
        
        iters2 = new ArrayList<>();
        
        //add iters on triples part
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(triples_dictionary_1.getShared())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(triples_dictionary_1.getObjects())));
        
        //add iters on graphs part
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getShared())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getSubjects())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getObjects())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getGraphs())));
        
        commonSubject1 = new CatIteratorList(iters1);
        commonSubject2 = new CatIteratorList(iters2);
        numSubjects = triples_dictionary_1.getSubjects().getNumberOfElements() + triples_dictionary_2.getSubjects().getNumberOfElements()-numCommonSubjects - numCommonSubject1Hdt2 - numCommonSubject2Hdt1;
        
        catAnySection(numSubjects,triples_dictionary_1.getSubjects(),triples_dictionary_2.getSubjects(), commonSubject1, commonSubject2,Collections.<IterElement>emptyList().iterator(),new ArrayList<CatCommon>(),mappings.get(M_S_1),mappings.get(M_S_2),2);
		
        /*this.triplesDictionary.subjects = dictionarySectionCatSubjects;
		Iterator it2 = this.triplesDictionary.subjects.getSortedEntries();
		while(it2.hasNext()) {
			System.out.println(it2.next());
		}*/
		
        //objects
		System.out.println("OBJECTS-------------------");
        
		iters1 = new ArrayList<>();
        
        //add iters on triples part
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getShared())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getSubjects())));
        
        //add iters on graphs part
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getShared())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getSubjects())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getObjects())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getGraphs())));
        
		CatIteratorList commonObject1 = new CatIteratorList(iters1);

		int numCommonObject1Hdt2 = 0;
        while (commonObject1.hasNext()){
            commonObject1.next();
            numCommonObject1Hdt2++;
        }
        iters2 = new ArrayList<>();
        
        //add iters on triples part
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(triples_dictionary_1.getShared())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(triples_dictionary_1.getSubjects())));
        
        //add iters on graphs part
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getShared())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getSubjects())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getObjects())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getGraphs())));
        
        CatIteratorList commonObject2 = new CatIteratorList(iters2);
        int numCommonObject2Hdt1 = 0;
        while (commonObject2.hasNext()){
            commonObject2.next();
            numCommonObject2Hdt1++;
        }
        int numCommonObjects = 0;
        Iterator<IterElement> commonO1O2 = new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getObjects()));
        while (commonO1O2.hasNext()){
            commonO1O2.next();
            numCommonObjects++;
        }
        
        iters1 = new ArrayList<>();
        
        //add iters on triples part
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getShared())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getSubjects())));
        
        //add iters on graphs part
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getShared())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getSubjects())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getObjects())));
        iters1.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getGraphs())));
        
		commonObject1 = new CatIteratorList(iters1);
		
		iters2 = new ArrayList<>();
        
        //add iters on triples part
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(triples_dictionary_1.getShared())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(triples_dictionary_1.getSubjects())));
        
        //add iters on graphs part
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getShared())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getSubjects())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getObjects())));
        iters2.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getGraphs())));
        
        commonObject2 = new CatIteratorList(iters2);
        
        
        numObjects = triples_dictionary_1.getObjects().getNumberOfElements()+triples_dictionary_2.getObjects().getNumberOfElements()-numCommonObjects-numCommonObject1Hdt2-numCommonObject2Hdt1;
        catAnySection(numObjects,triples_dictionary_1.getObjects(),triples_dictionary_2.getObjects(), commonObject1, commonObject2,Collections.<IterElement>emptyList().iterator(),new ArrayList<CatCommon>(),mappings.get(M_O_1),mappings.get(M_O_2),3);
		
        /*this.triplesDictionary.objects = dictionarySectionCatObjects;

		Iterator it_o = this.triplesDictionary.objects.getSortedEntries();
		while(it_o.hasNext()) {
			System.out.println(i.next());
		}*/
		
		
		
		System.out.println("SHARED-------------------");
		Iterator<IterElement> i2 = new CatCommon(getIterator(triples_dictionary_1.getSubjects()), getIterator(triples_dictionary_2.getObjects()));
	      int numCommonS1O2=0;
	      while (i2.hasNext()){
	          i2.next();
	          numCommonS1O2++;
	      }
	      i2 = new CatCommon(getIterator(triples_dictionary_1.getObjects()), getIterator(triples_dictionary_2.getSubjects()));
	      int numCommonO1S2=0;
	      while (i2.hasNext()){
	          i2.next();
	          numCommonO1S2++;
	      }
	      i2 = new CatCommon(getIterator(triples_dictionary_1.getShared()), getIterator(triples_dictionary_2.getShared()));
	      int numCommonSh1Sh2=0;
	      while (i2.hasNext()){
	          i2.next();
	          numCommonSh1Sh2++;
	      }
	      
	      iters1 = new ArrayList<>();
	      iters1.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getShared())));
	      iters1.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getSubjects())));
	      iters1.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getObjects())));
	      iters1.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getGraphs())));
	      
	      CatIteratorList iterCommonSharedGraphsHdt1 = new CatIteratorList(iters1);
			int numCommonSharedGraphsHdt1 = 0;
	      while (iterCommonSharedGraphsHdt1.hasNext()){
	          iterCommonSharedGraphsHdt1.next();
	          numCommonSharedGraphsHdt1++;
	      }
	      iters2 = new ArrayList<>();
	      iters2.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getShared())));
	      iters2.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getSubjects())));
	      iters2.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getObjects())));
	      iters2.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getGraphs())));
	      
	      CatIteratorList iterCommonSharedGraphsHdt2 = new CatIteratorList(iters2);
			int numCommonSharedGraphsHdt2 = 0;
	    while (iterCommonSharedGraphsHdt2.hasNext()){
	        iterCommonSharedGraphsHdt2.next();
	        numCommonSharedGraphsHdt2++;
	    }
		  numShared = triples_dictionary_1.getShared().getNumberOfElements()+triples_dictionary_2.getShared().getNumberOfElements()-numCommonSh1Sh2 -numCommonSharedGraphsHdt1 - numCommonSharedGraphsHdt2 +numCommonS1O2+numCommonO1S2;
		
		
		
		  iters1 = new ArrayList<>();
	      iters1.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getShared())));
	      iters1.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getSubjects())));
	      iters1.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getObjects())));
	      iters1.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getGraphs())));
	      
	      iterCommonSharedGraphsHdt1 = new CatIteratorList(iters1);
	      
	      iters2 = new ArrayList<>();
	      iters2.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getShared())));
	      iters2.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getSubjects())));
	      iters2.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getObjects())));
	      iters2.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getGraphs())));
	      
	      iterCommonSharedGraphsHdt2 = new CatIteratorList(iters2);
		
	      ArrayList<Iterator<IterElement>> iters3 = new ArrayList<>();
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getObjects()),M_S_1,M_O_2));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getSubjects()),M_O_1,M_S_2));
	      
	      
	      ArrayList<CatCommon> iterMappings = new ArrayList<>();
	      
	      iterMappings.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(triples_dictionary_2.getSubjects()),M_SH_1,M_S_2));
	      iterMappings.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(triples_dictionary_2.getObjects()),M_SH_1,M_O_2));
	      iterMappings.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(triples_dictionary_1.getSubjects()),M_SH_2,M_S_1));
	      iterMappings.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(triples_dictionary_1.getObjects()),M_SH_2,M_O_1));
	
	      

	      CatIteratorList iterCommonSO = new CatIteratorList(iters3);
	      
	       
	     catAnySection(numShared,triples_dictionary_1.getShared(),triples_dictionary_2.getShared(), iterCommonSharedGraphsHdt1, iterCommonSharedGraphsHdt2,iterCommonSO,iterMappings,mappings.get(M_SH_1),mappings.get(M_SH_2),1);
		/*System.out.println("The shared: ");
		Iterator it3 = this.triplesDictionary.shared.getSortedEntries();
		while(it3.hasNext()) {
			System.out.println(it3.next());
		}*/
		
		
		  
		System.out.println("SHARED GRAPHS-------------------");
	      
		
	      iters3 = new ArrayList<>();
	      
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getSubjects())));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getObjects())));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getGraphs())));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getSubjects())));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getObjects())));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getGraphs())));
	      
	      //subject1 graphs2
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getObjects())));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getObjects())));
	      
	    //subject1 graphs2
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getSubjects())));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getSubjects())));
	      
	      //graphs_subjects1 
	      
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getObjects())));
	      
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getObjects())));
	      
	      //graphs_objects1
	      
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getSubjects())));
	      
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getSubjects())));
	      
	      //graphs_unused1
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getShared())));
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(triples_dictionary_1.getShared())));
	      

	      int num_common = 0;
	      for(Iterator iter:iters3) {
	    	  while(iter.hasNext()) {
	    		  iter.next();
	    		  num_common++;
	    	  }
	  		
	      }
	      
	      i2 = new CatCommon(getIterator(graphs_dictionary_1.getShared()), getIterator(graphs_dictionary_2.getShared()));
		  int numCommonGraphSh1GraphSh2 = 0;
	      while(i2.hasNext()) {
			  i2.next();
			  numCommonGraphSh1GraphSh2++;
		  }
			
	      numSharedGraphs = graphs_dictionary_1.getShared().getNumberOfElements() + graphs_dictionary_2.getShared().getNumberOfElements() - numCommonGraphSh1GraphSh2 + num_common;
	      //System.out.println("number shared graphs: "+numentries);
	      iters3 = new ArrayList<>();
	      
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getSubjects()),M_SH_1,M_GS_2));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getObjects()),M_SH_1,M_GO_2));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getGraphs()),M_SH_1,M_GU_2));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getSubjects()),M_SH_2,M_GS_1));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getObjects()),M_SH_2,M_GO_1));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getGraphs()),M_SH_2,M_GU_1));
	      
	      //subject1 graphs2
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getObjects()),M_S_1,M_GO_2));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getObjects()),M_S_2,M_GO_1));
	      
	    //subject1 graphs2
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getSubjects()),M_O_1,M_GS_2));
	      iters3.add(new CatCommon(getIterator(triples_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getSubjects()),M_O_2,M_GS_1));
	      
	      //graphs_subjects1 
	      
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getObjects()),M_S_1,M_GO_2));
	      
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getObjects()),M_S_2,M_GO_1));
	      
	      //graphs_objects1
	      
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getSubjects()),M_GO_1,M_GS_2));
	      
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getSubjects()),M_GO_2,M_GS_1));
	      
	      //graphs_unused1
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getShared()),M_GU_1,M_SH_2));
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(triples_dictionary_1.getShared()),M_GU_2,M_SH_1));
	      
	      CatIteratorList iterCommonSharedGraphs = new CatIteratorList(iters3);
	      
	      
	      iterMappings = new ArrayList<>();
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getShared()),getIterator(triples_dictionary_2.getShared()),M_GSH_1,M_SH_2));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getShared()),getIterator(triples_dictionary_2.getSubjects()),M_GSH_1,M_S_2));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getShared()),getIterator(triples_dictionary_2.getObjects()),M_GSH_1,M_O_2));
	      
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getSubjects()),M_GSH_1,M_GS_2));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getObjects()),M_GSH_1,M_GO_2));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getShared()),getIterator(graphs_dictionary_2.getShared()),M_GSH_1,M_GU_2));
	      
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getShared()),getIterator(triples_dictionary_1.getShared()),M_GSH_2,M_SH_1));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getShared()),getIterator(triples_dictionary_1.getSubjects()),M_GSH_2,M_S_1));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getShared()),getIterator(triples_dictionary_1.getObjects()),M_GSH_2,M_O_1));
	      
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getSubjects()),M_GSH_2,M_GS_1));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getObjects()),M_GSH_2,M_GO_1));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getShared()),getIterator(graphs_dictionary_1.getShared()),M_GSH_2,M_GU_1));
	            
	
	     catAnySection(numSharedGraphs,graphs_dictionary_1.getShared(),graphs_dictionary_2.getShared(), Collections.<IterElement>emptyList().iterator(), Collections.<IterElement>emptyList().iterator(),iterCommonSharedGraphs,iterMappings,mappings.get(M_GSH_1),mappings.get(M_GSH_2),5);
		/*Iterator it4 = this.graphDictionary.shared.getSortedEntries();
		while(it4.hasNext()) {
			System.out.println(it4.next());
		}*/
		System.out.println("SUBJECT GRAPHS-------------------");
	      
		
		//skipped entries
		  iters1 = new ArrayList<>();
	      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getShared())));
	      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getObjects())));
	      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getShared())));
	      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getObjects())));
	      
		  iters2 = new ArrayList<>();
	      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(triples_dictionary_1.getShared())));
	      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(triples_dictionary_1.getObjects())));
	      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getShared())));
	      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getObjects())));
	      CatIteratorList iterSkip1 = new CatIteratorList(iters1);
	      CatIteratorList iterSkip2 = new CatIteratorList(iters2);
	      
		//added entries 
	      iters3 = new ArrayList<>();
	      
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getGraphs())));
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getSubjects())));
	      
	      //common unused graphs
	      i2 = new CatCommon(getIterator(graphs_dictionary_1.getSubjects()), getIterator(graphs_dictionary_2.getSubjects()));
			 
	      num_common = 0;
	      for(Iterator iter:iters3) {
	    	  while(iter.hasNext()) {
	    		  iter.next();
	    		  num_common++;
	    	  }
	  		
	      }
	      
	      int numCommonGraphs1Graphs2 = 0;
	      while(i2.hasNext()) {
			  i2.next();
			  numCommonGraphs1Graphs2++;
		  }
	      
	      int numskip1 = 0;
	      while(iterSkip1.hasNext()) {
			  iterSkip1.next();
			  numskip1++;
		  }
	      int numskip2 = 0;
	      while(iterSkip2.hasNext()) {
			  iterSkip2.next();
			  numskip2++;
		  }
	      
	      numSubjectGraphs = graphs_dictionary_1.getSubjects().getNumberOfElements() + graphs_dictionary_2.getSubjects().getNumberOfElements() - numCommonGraphs1Graphs2 - numskip1 - numskip2 + num_common;
	      //System.out.println("number shared subject graphs: "+numentries);
	      
	      iters1 = new ArrayList<>();
	      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getShared())));
	      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getObjects())));
	      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getShared())));
	      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getObjects())));
	      
		  iters2 = new ArrayList<>();
	      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(triples_dictionary_1.getShared())));
	      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(triples_dictionary_1.getObjects())));
	      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getShared())));
	      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getObjects())));
	      iterSkip1 = new CatIteratorList(iters1);
	      iterSkip2 = new CatIteratorList(iters2);
	      
	      
	      
	      iters3 = new ArrayList<>();
	      
	      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getGraphs()),M_S_1,M_GU_2));
	      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getSubjects()),M_GU_1,M_S_2));
	      
	      CatIteratorList iterCommonSharedSubjectGraphs = new CatIteratorList(iters3);
	      
	      iterMappings = new ArrayList<>();
	      
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(graphs_dictionary_2.getGraphs()),M_GS_1,M_GU_2));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getSubjects()),getIterator(triples_dictionary_2.getSubjects()),M_GS_1,M_S_2));
	      
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(graphs_dictionary_1.getGraphs()),M_GS_2,M_GU_1));
	      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getSubjects()),getIterator(triples_dictionary_1.getSubjects()),M_GS_2,M_S_1));
 
	     catAnySection(numSubjectGraphs,graphs_dictionary_1.getSubjects(),graphs_dictionary_2.getSubjects(), iterSkip1, iterSkip2,iterCommonSharedSubjectGraphs,iterMappings,mappings.get(M_GS_1),mappings.get(M_GS_2),6);
	     /*it4 = this.graphDictionary.subjects.getSortedEntries();
	     while(it4.hasNext()) {
	    	 System.out.println(it4.next());
	     }*/
	     
	     System.out.println("OBJECT GRAPHS-------------------");
	      
			
			//skipped entries
			  iters1 = new ArrayList<>();
		      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getShared())));
		      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getSubjects())));
		      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getShared())));
		      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getSubjects())));
		      
			  iters2 = new ArrayList<>();
		      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(triples_dictionary_1.getShared())));
		      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(triples_dictionary_1.getSubjects())));
		      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getShared())));
		      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getSubjects())));
		      iterSkip1 = new CatIteratorList(iters1);
		      iterSkip2 = new CatIteratorList(iters2);
		      
			//added entries 
		      iters3 = new ArrayList<>();
		      
		      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getGraphs())));
		      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getObjects())));
		      
		      //common unused graphs
		      i2 = new CatCommon(getIterator(graphs_dictionary_1.getObjects()), getIterator(graphs_dictionary_2.getObjects()));
				 
		      num_common = 0;
		      for(Iterator iter:iters3) {
		    	  while(iter.hasNext()) {
		    		  iter.next();
		    		  num_common++;
		    	  }
		  		
		      }
		      
		      numCommonGraphs1Graphs2 = 0;
		      while(i2.hasNext()) {
				  i2.next();
				  numCommonGraphs1Graphs2++;
			  }
		      
		      numskip1 = 0;
		      while(iterSkip1.hasNext()) {
				  iterSkip1.next();
				  numskip1++;
			  }
		      numskip2 = 0;
		      while(iterSkip2.hasNext()) {
				  iterSkip2.next();
				  numskip2++;
			  }
		      
		      numObjectGraphs = graphs_dictionary_1.getObjects().getNumberOfElements() + graphs_dictionary_2.getObjects().getNumberOfElements() - numCommonGraphs1Graphs2 - numskip1 - numskip2 + num_common;
		      //System.out.println("number shared object graphs: "+);
		      
		      iters1 = new ArrayList<>();
		      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getShared())));
		      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getSubjects())));
		      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getShared())));
		      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getSubjects())));
		      
			  iters2 = new ArrayList<>();
		      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(triples_dictionary_1.getShared())));
		      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(triples_dictionary_1.getSubjects())));
		      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getShared())));
		      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getSubjects())));
		      iterSkip1 = new CatIteratorList(iters1);
		      iterSkip2 = new CatIteratorList(iters2);
		      
		      
		      
		      iters3 = new ArrayList<>();
		      
		      iters3.add(new CatCommon(getIterator(triples_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getGraphs()),M_O_1,M_GU_2));
		      iters3.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getObjects()),M_GU_1,M_O_2));
		      
		      iterMappings = new ArrayList<>();
		      
		      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(graphs_dictionary_2.getGraphs()),M_GO_1,M_GU_2));
		      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_1.getObjects()),getIterator(triples_dictionary_2.getObjects()),M_GO_1,M_O_2));
		      
		      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(graphs_dictionary_1.getGraphs()),M_GO_2,M_GU_1));
		      iterMappings.add(new CatCommon(getIterator(graphs_dictionary_2.getObjects()),getIterator(triples_dictionary_1.getObjects()),M_GO_2,M_O_1));

		      CatIteratorList iterCommonSharedObjectGraphs = new CatIteratorList(iters3);

		     catAnySection(numObjectGraphs,graphs_dictionary_1.getObjects(),graphs_dictionary_2.getObjects(), iterSkip1, iterSkip2,iterCommonSharedObjectGraphs,iterMappings,mappings.get(M_GO_1),mappings.get(M_GO_2),7);
		     
		     /*it4 = this.graphDictionary.objects.getSortedEntries();
		     while(it4.hasNext()) {
		    	 System.out.println(it4.next());
		     }*/
		     System.out.println("UNUSED GRAPHS-------------------");
		      
				
				//skipped entries
				  iters1 = new ArrayList<>();
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getShared())));
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getSubjects())));
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getObjects())));
			      
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(graphs_dictionary_2.getShared())));
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(graphs_dictionary_2.getSubjects())));
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(graphs_dictionary_2.getObjects())));
			      
			      iters2 = new ArrayList<>();
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(triples_dictionary_1.getShared())));
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(triples_dictionary_1.getSubjects())));
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(triples_dictionary_1.getObjects())));
			      
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(graphs_dictionary_1.getShared())));
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(graphs_dictionary_1.getSubjects())));
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(graphs_dictionary_1.getObjects())));
			      
			      
			      iterSkip1 = new CatIteratorList(iters1);
			      iterSkip2 = new CatIteratorList(iters2);
			      
			      //common unused graphs
			      i2 = new CatCommon(getIterator(graphs_dictionary_1.getGraphs()), getIterator(graphs_dictionary_2.getGraphs()));
					 
			      
			      numCommonGraphs1Graphs2 = 0;
			      while(i2.hasNext()) {
					  i2.next();
					  numCommonGraphs1Graphs2++;
				  }
			      
			      numskip1 = 0;
			      while(iterSkip1.hasNext()) {
					  iterSkip1.next();
					  numskip1++;
				  }
			      numskip2 = 0;
			      while(iterSkip2.hasNext()) {
					  iterSkip2.next();
					  numskip2++;
				  }
			      
			      numUnusedGraphs = graphs_dictionary_1.getGraphs().getNumberOfElements() + graphs_dictionary_2.getGraphs().getNumberOfElements() - numCommonGraphs1Graphs2 - numskip1 - numskip2;
			      //System.out.println("number unused graphs: "+numentries);
			      
			      iters1 = new ArrayList<>();
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getShared())));
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getSubjects())));
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(triples_dictionary_2.getObjects())));
			      
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(graphs_dictionary_2.getShared())));
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(graphs_dictionary_2.getSubjects())));
			      iters1.add(new CatCommon(getIterator(graphs_dictionary_1.getGraphs()),getIterator(graphs_dictionary_2.getObjects())));
			      
			      iters2 = new ArrayList<>();
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(triples_dictionary_1.getShared())));
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(triples_dictionary_1.getSubjects())));
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(triples_dictionary_1.getObjects())));
			      
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(graphs_dictionary_1.getShared())));
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(graphs_dictionary_1.getSubjects())));
			      iters2.add(new CatCommon(getIterator(graphs_dictionary_2.getGraphs()),getIterator(graphs_dictionary_1.getObjects())));
			      
			      
			      iterSkip1 = new CatIteratorList(iters1);
			      iterSkip2 = new CatIteratorList(iters2);
			       
			     catAnySection(numUnusedGraphs,graphs_dictionary_1.getGraphs(),graphs_dictionary_2.getGraphs(), iterSkip1, iterSkip2,Collections.<IterElement>emptyList().iterator(),new ArrayList<CatCommon>(),mappings.get(M_GU_1),mappings.get(M_GU_2),8);
			     
			     /*it4 = this.graphDictionary.graphs.getSortedEntries();
			     while(it4.hasNext()) {
			    	 System.out.println(it4.next());
			     }*/
			     writeTriplesDictionary(numPredicates + numSubjects + numObjects + numShared);
			     writeGraphsDictionary(numSharedGraphs + numSubjectGraphs + numObjectGraphs + numUnusedGraphs);
			     
		//calculate the inverse mapping for the subjects, i.e. from the new dictionary subject section to the old ones
        mappingS = new CatMappingBack(location,numSubjects + numShared + numSubjectGraphs + numSharedGraphs);

        for (int i=0; i<mappings.get(M_SH_1).getSize(); i++){
            mappingS.set(mappings.get(M_SH_1).getMapping(i),i+1,1);
        }
        for (int i=0; i<mappings.get(M_GSH_1).getSize(); i++){
            mappingS.set(mappings.get(M_GSH_1).getMapping(i) + numShared ,i + (int)triples_dictionary_1.getShared().getNumberOfElements() + 1,1);
        }
        for (int i=0; i<mappings.get(M_SH_2).getSize(); i++){
            mappingS.set(mappings.get(M_SH_2).getMapping(i),i+1,2);
        }
        for (int i=0; i<mappings.get(M_GSH_2).getSize(); i++){
            mappingS.set(mappings.get(M_GSH_2).getMapping(i) + numShared,i+ (int)triples_dictionary_1.getShared().getNumberOfElements() + 1,2);
        }

        int totalSharedNew = numShared + numSharedGraphs;
        int totalSharedOld1 = (int)triples_dictionary_1.getShared().getNumberOfElements() + (int)graphs_dictionary_1.getShared().getNumberOfElements();
        int totalSharedOld2 = (int)triples_dictionary_2.getShared().getNumberOfElements() + (int)graphs_dictionary_2.getShared().getNumberOfElements();

        /* probably buggy must consider other cases nut just (type = 1 )for triples subjects and (type =5) for graphs subjects 
	some entries could also went to other sections not just *Shared*.
	*/
        for (int i=0; i<mappings.get(M_S_1).getSize(); i++){

        	//if the element was added to the mapping in the shared section (type = 1)
            if (mappings.get(M_S_1).getType(i)==1){
                mappingS.set(mappings.get(M_S_1).getMapping(i) ,(i+1+totalSharedOld1),1);
            } else {
               mappingS.set(mappings.get(M_S_1).getMapping(i)+totalSharedNew,(i+1+totalSharedOld1),1);
            }

        }

        for (int i=0; i<mappings.get(M_S_2).getSize(); i++){
        	//if the element was added to the mapping in the shared section (type = 1)
            if (mappings.get(M_S_2).getType(i)==1){
                mappingS.set(mappings.get(M_S_2).getMapping(i), (i + 1 + totalSharedOld2), 2);
            } else {
                mappingS.set(mappings.get(M_S_2).getMapping(i) + totalSharedNew , (i + 1 + totalSharedOld2), 2);
            }

        }


        for (int i=0; i<mappings.get(M_GS_1).getSize(); i++){

        	//if the element was added to the mapping in the graph shared section (type = 5)
            if (mappings.get(M_GS_1).getType(i)==5){
              mappingS.set(mappings.get(M_GS_1).getMapping(i) + numShared ,(i + 1+totalSharedOld1 + (int)triples_dictionary_1.getSubjects().getNumberOfElements()),1);
            } else {
               mappingS.set(mappings.get(M_GS_1).getMapping(i)+totalSharedNew + numSubjects,(i+1+totalSharedOld1 + (int)triples_dictionary_1.getSubjects().getNumberOfElements()),1);
            }

        }


        for (int i=0; i<mappings.get(M_GS_2).getSize(); i++){
            if (mappings.get(M_GS_2).getType(i)==5){
                mappingS.set(mappings.get(M_GS_2).getMapping(i) + numShared, (i + 1 + totalSharedOld2 + (int)triples_dictionary_2.getSubjects().getNumberOfElements()), 2);
            } else {
                mappingS.set(mappings.get(M_GS_2).getMapping(i) + totalSharedNew + numSubjects, (i + 1 + totalSharedOld2 + (int)triples_dictionary_2.getSubjects().getNumberOfElements()), 2);
            }
        }
	
    }
	public void printMappingBack() {
        for(int i=1;i<mappingS.size();i++) {
        	System.out.println("Mapping: "+mappingS.getMapping(i)+" Type: "+mappingS.getType(i));
        }
	}
	public void writeTriplesDictionary(int numElements) {
		ControlInfo ci = new ControlInformation();
        ci.setType(ControlInfo.Type.DICTIONARY);
        ci.setFormat(HDTVocabulary.DICTIONARY_TYPE_FOUR_SECTION);
        ci.setInt("elements",numElements);
      try {
            ci.save(new FileOutputStream(location + "dictionary_t"));
            FileOutputStream outFinal = new FileOutputStream(location + "dictionary_t",true);
            byte[] buf = new byte[100000];
            for (int i = 1; i <= 4; i++) {
                int j = i;
                if (i == 4){
                    j = 3;
                } else if (j == 3){
                    j = 4;
                }
                try {
                    InputStream in = new FileInputStream(location + "section" + j);
                    int b = 0;
                    while ((b = in.read(buf)) >= 0) {
                        outFinal.write(buf, 0, b);
                        outFinal.flush();
                    }
                    in.close();
                    Files.delete(Paths.get(location + "section" + j));
                } catch (FileNotFoundException e ){
                    e.printStackTrace();
                }
            }
            outFinal.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	public void writeGraphsDictionary(int numElements) {

		ControlInfo ci = new ControlInformation();
        ci.setType(ControlInfo.Type.DICTIONARY);
        ci.setFormat(HDTVocabulary.DICTIONARY_TYPE_FOUR_SECTION);
        ci.setInt("elements",numElements);
      try {
            ci.save(new FileOutputStream(location + "dictionary_g"));
            FileOutputStream outFinal = new FileOutputStream(location + "dictionary_g",true);
            byte[] buf = new byte[100000];
            for (int i = 5; i <= 8; i++) {
                
                try {
                    InputStream in = new FileInputStream(location + "section" + i);
                    int b = 0;
                    while ((b = in.read(buf)) >= 0) {
                        outFinal.write(buf, 0, b);
                        outFinal.flush();
                    }
                    in.close();
                    Files.delete(Paths.get(location + "section" + i));
                } catch (FileNotFoundException e ){
                    e.printStackTrace();
                }
            }
            outFinal.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	public void catAnySection(long numEntries, DictionarySection dictionarySectionHdt1,
			DictionarySection dictionarySectionHdt2,Iterator<IterElement> itSkip1, 
			Iterator<IterElement> itSkip2, Iterator<IterElement> itAdd,
			ArrayList<CatCommon> itMappings, CatMapping mapping1, CatMapping mapping2, int type) {
		
		CRCOutputStream out_buffer = null;
		try {
			out_buffer = new CRCOutputStream(new FileOutputStream(location + "section_buffer_" + type), new CRC32());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		 int count1 = 0;
         int count2 = 0;
         int skipSection1 = -1;
         int skipSection2 = -1;
         long storedBuffersSize = 0;
         long numBlocks = 0;
         long numberElements = 0;
         SequenceLog64BigDisk blocks = new SequenceLog64BigDisk(location+"SequenceLog64BigDisk"+type,64, numEntries/16);
         ByteArrayOutputStream byteOut = new ByteArrayOutputStream(16*1024);
			
		try {
         if (numEntries > 0) {
			CharSequence previousStr = null;
			Iterator<? extends CharSequence> it1 = getIterator(dictionarySectionHdt1);
			Iterator<? extends CharSequence> it2 = getIterator(dictionarySectionHdt2);

				ArrayList<IteratorPlusString> list = new ArrayList<IteratorPlusString>();
				ArrayList<IterElement> commonForMapping = new ArrayList<IterElement>();
				IterElement nextToAdd = new IterElement(new Pair(-1,-1), "defaut", "", "");
				if (it1.hasNext()) {
					list.add(new IteratorPlusString(1, it1.next()));
				}
				if (it2.hasNext()) {
					list.add(new IteratorPlusString(2, it2.next()));
				}
				if (itAdd.hasNext()) {
					 nextToAdd = itAdd.next();
					list.add(new IteratorPlusString(3, nextToAdd.getEntity()));
				}
				if (itSkip1.hasNext()) {
					skipSection1 = itSkip1.next().getPair().getKey();
				}
				if (itSkip2.hasNext()) {
					skipSection2 = itSkip2.next().getPair().getKey();
				}
				for(CatCommon iter:itMappings) {
					if(iter.hasNext()) {
						commonForMapping.add(iter.next());
					}else {
						commonForMapping.add(new IterElement(new Pair(-1,-1), "defaut", "", ""));
					}
				}
				while (list.size() != 0) {
					Collections.sort(list, new ScoreComparator());
					boolean skip = false;
					if (list.get(0).iterator == 1) {
						if (count1 == skipSection1) {
							skip = true;
							if (it1.hasNext()) {
								list.set(0, new IteratorPlusString(1, it1.next()));
								count1++;
							} else {
								list.remove(0);
							}
							if (itSkip1.hasNext()) {
								skipSection1 = itSkip1.next().getPair().getKey();
							}
						}
					} else if (list.get(0).iterator == 2) { // there are only two cases
						if (count2 == skipSection2) {
							skip = true;
							if (it2.hasNext()) {
								list.set(0, new IteratorPlusString(2, it2.next()));
								count2++;
							} else {
								list.remove(0);
							}
							if (itSkip2.hasNext()) {
								skipSection2 = itSkip2.next().getPair().getKey();
							}
						}
					}
					if (skip == false) {
						String str = list.get(0).value.toString();
                        if (numberElements % DEFAULT_BLOCK_SIZE == 0) {
                            blocks.append(storedBuffersSize + byteOut.size());
                            numBlocks++;

                            // if a buffer is filled, flush the byteOut and store it
                            if (((numBlocks - 1) % BLOCK_PER_BUFFER == 0) && ((numBlocks - 1) / BLOCK_PER_BUFFER != 0)) {
                            	System.out.println("Writing buffer.......................................");
                                storedBuffersSize += byteOut.size();
                                byteOut.flush();
                                IOUtil.writeBuffer(out_buffer, byteOut.toByteArray(), 0, byteOut.toByteArray().length, null);
                                byteOut.close();
                                byteOut = new ByteArrayOutputStream(16 * 1024);
                            }

                            // Copy full string
                            ByteStringUtil.append(byteOut, str, 0);
                        } else {
                            // Find common part.
                            int delta = ByteStringUtil.longestCommonPrefix(previousStr, str);
                            // Write Delta in VByte
                            VByte.encode(byteOut, delta);
                            // Write remaining
                            ByteStringUtil.append(byteOut, str, delta);
                        }
                        byteOut.write(0); // End of string
                        previousStr = str;
						if (list.size() >= 2 && list.get(0).value.toString().equals(list.get(1).value.toString())) {
							
							for (IterElement element : commonForMapping) {
								String mappingName = element.getSecondIterName();
								
								//if the common entry is in a section from first file then check count2 
								if(mappingName.endsWith("1")) {
									if (count2 == element.getPair().getKey()) {
										mappings.get(mappingName).set(element.getPair().getValue(),
												numberElements + 1, type);
									}
								}else if(mappingName.endsWith("2")){
									//else check count1 with sections of the second files
									if (count1 == element.getPair().getKey()) {
										mappings.get(mappingName).set(element.getPair().getValue(),
												numberElements + 1, type);
									}
								}
								
							}
							
							boolean removed = false;
							mapping1.set(count1, numberElements + 1, type);
							count1++;
							mapping2.set(count2, numberElements + 1, type);
							count2++;
							if (it1.hasNext()) {

								list.set(0, new IteratorPlusString(1, it1.next()));
								int index = 0;
								for (CatCommon iter : itMappings) {
									IterElement element = commonForMapping.get(index);
									String mappingName = element.getSecondIterName();
									
									//if the common entry is in a section from first file then check count2 
									if(mappingName.endsWith("2")) {
										if (count1 > element.getPair().getKey() && iter.hasNext()) {
											commonForMapping.set(index, iter.next());
										}
									}
									index++;
								}
								
							} else {
								list.remove(0);
								removed = true;
							}
							if (it2.hasNext()) {

								if (removed == true) {
									list.set(0, new IteratorPlusString(2, it2.next()));
								} else {
									list.set(1, new IteratorPlusString(2, it2.next()));
								}
								int index = 0;
								for (CatCommon iter : itMappings) {
									IterElement element = commonForMapping.get(index);
									String mappingName = element.getSecondIterName();
									
									//if the common entry is in a section from first file then check count2 
									if(mappingName.endsWith("1")) {
										if (count2 > element.getPair().getKey() && iter.hasNext()) {
											commonForMapping.set(index, iter.next());
										}
									}
									index++;
								}
							} else {
								if (removed == true) {
									list.remove(0);
								} else {
									list.remove(1);
								}
							}
						} else if (list.get(0).iterator == 1) {
							mapping1.set(count1, numberElements + 1, type);

							for (IterElement element : commonForMapping) {
								String mappingName = element.getSecondIterName();
								
								//if the common entry is in a section from first file then check count2 
								if(mappingName.endsWith("2")) {
									if (count1 == element.getPair().getKey()) {
										mappings.get(mappingName).set(element.getPair().getValue(),
												numberElements + 1, type);
									}
								}
								
							}

							count1++;
							if (it1.hasNext()) {

								list.set(0, new IteratorPlusString(1, it1.next()));
								int index = 0;
								for (CatCommon iter : itMappings) {
									IterElement element = commonForMapping.get(index);
									String mappingName = element.getSecondIterName();
									
									//if the common entry is in a section from first file then check count2 
									if(mappingName.endsWith("2")) {
										if (count1 > element.getPair().getKey() && iter.hasNext()) {
											commonForMapping.set(index, iter.next());
										}
									}
									index++;
								}
							} else {
								list.remove(0);
							}
						} else if (list.get(0).iterator == 2) {
							
							mapping2.set(count2, numberElements + 1, type);
							
							for (IterElement element : commonForMapping) {
								String mappingName = element.getSecondIterName();
								
								//if the common entry is in a section from first file then check count2 
								if(mappingName.endsWith("1")) {
									if (count2 == element.getPair().getKey()) {
										mappings.get(mappingName).set(element.getPair().getValue(),
												numberElements + 1, type);
									}
								}
								
							}

							count2++;
							if (it2.hasNext()) {

								list.set(0, new IteratorPlusString(2, it2.next()));
								
								int index = 0;
								for (CatCommon iter : itMappings) {
									IterElement element = commonForMapping.get(index);
									String mappingName = element.getSecondIterName();
									
									//if the common entry is in a section from first file then check count2 
									if(mappingName.endsWith("1")) {
										if (count2 > element.getPair().getKey() && iter.hasNext()) {
											commonForMapping.set(index, iter.next());
										}
									}
									index++;
								}
								
							} else {
								list.remove(0);
							}
						} else if (list.get(0).iterator == 3) {
							String mapping_name_1 = nextToAdd.getFirstIterName();
							String mapping_name_2 = nextToAdd.getSecondIterName();
							mappings.get(mapping_name_1).set(nextToAdd.getPair().getKey(), numberElements + 1, type);
							mappings.get(mapping_name_2).set(nextToAdd.getPair().getValue(), numberElements + 1, type);
							
							if (itAdd.hasNext()) {
								nextToAdd = itAdd.next();
								list.set(0, new IteratorPlusString(3, nextToAdd.getEntity()));
							} else {
								list.remove(0);
							}
						}
						numberElements += 1;
					}

				}
         }
         // Ending block pointer.
         blocks.append(storedBuffersSize+byteOut.size());
         // Trim text/blocks
         blocks.aggressiveTrimToSize();
         byteOut.flush();
         
         //section.addBuffer(buffer, byteOut.toByteArray());
         IOUtil.writeBuffer(out_buffer, byteOut.toByteArray(), 0, byteOut.toByteArray().length, null);
         out_buffer.writeCRC();
         out_buffer.close();
         //Save the section conforming to the HDT format
         CRCOutputStream out = new CRCOutputStream(new FileOutputStream(location+"section"+type), new CRC8());
         //write the index type
         out.write(2);
         //write the number of strings
         VByte.encode(out, numberElements);
         //write the datasize
         VByte.encode(out, storedBuffersSize+byteOut.size());
         //wirte the blocksize
         VByte.encode(out, DEFAULT_BLOCK_SIZE);
         //write CRC
         out.writeCRC();
         //write the blocks
         blocks.save(out, null);	// Write blocks directly to output, they have their own CRC check.
         blocks.close();
         //write out_buffer
         byte[] buf = new byte[100000];
         InputStream in = new FileInputStream(location+"section_buffer_"+type);
         int b = 0;
         while ( (b = in.read(buf)) >= 0) {
             out.write(buf, 0, b);
             out.flush();
         }
         out.close();
         Files.delete(Paths.get(location+"section_buffer_"+type));
         Files.delete(Paths.get(location+"SequenceLog64BigDisk"+type));
     } catch (IOException e) {
         e.printStackTrace();
     }
	}

	public CatMappingBack getMappingS() {
		return mappingS;
	}
	public HashMap<String, CatMapping> getMappings() {
		return mappings;
	}
	// This method is a bug fix. There are cases where an HDT file does not contain
	// elements in a section. When getSorted is called then an error occurs
	// This should not happen. To still make the code work we use this method.
	private Iterator<? extends CharSequence> getIterator(DictionarySection section) {
		try {
			return section.getSortedEntries();
		} catch (NullPointerException e) {
			return Collections.emptyIterator();
		}
	}

}
