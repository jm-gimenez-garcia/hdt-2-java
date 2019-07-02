package org.rdfhdt.hdt.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.text.Utilities;

import org.rdfhdt.hdt.dictionary.CompositeDictionary;
import org.rdfhdt.hdt.dictionary.GraphsDictionary;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.dictionary.impl.ReificationDictionary;
import org.rdfhdt.hdt.dictionary.impl.DictionaryCat;
import org.rdfhdt.hdt.dictionary.impl.GraphsFourSectionDictionaryBig;
import org.rdfhdt.hdt.dictionary.impl.TriplesFourSectionDictionary;
import org.rdfhdt.hdt.dictionary.impl.TriplesFourSectionDictionaryBig;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.hdt.HDTPrivate;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.TriplesFactory;
import org.rdfhdt.hdt.triples.TriplesPrivate;
import org.rdfhdt.hdt.triples.impl.BitmapQuadIteratorCat;
import org.rdfhdt.hdt.triples.impl.BitmapQuadsCat;
import org.rdfhdt.hdt.triples.impl.BitmapTriplesCat;
import org.rdfhdt.hdt.util.Utility;
import org.rdfhdt.hdt.util.io.CountInputStream;

public class Test_Cat implements ProgressListener{
	
	public void read(String hdtInput ) {
		HDTPrivate hdt;
		try {
			hdt = HDTManager.mapHDT(hdtInput, this);
			if (hdt.getDictionary() instanceof TriplesFourSectionDictionary) {
				System.out.println("Hello");
				final ReificationDictionary rd = (ReificationDictionary) hdt.getDictionary();
			}
			/*CompositeDictionary cs = (CompositeDictionary)hdt.getDictionary();
			TriplesDictionary td = cs.getTriplesDictionary();
			GraphsDictionary gd = cs.getGraphsDictionary();*/
			
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testGenerate(String quadFile) {
		try {
			HDTPrivate hdt = HDTManager.generateHDT(quadFile, "file://" + quadFile, RDFNotation.NQUADS, new HDTSpecification(), true, this);
			if (hdt.getDictionary() instanceof ReificationDictionary) {
				final ReificationDictionary rd = (ReificationDictionary) hdt.getDictionary();
				TriplesDictionary td = rd.getTriplesDictionary();
				GraphsDictionary gd = rd.getGraphsDictionary();
				//HDT hdt_cat = HDTManager.catHDT(hdtInput1, hdtInput2 , spec,this);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void cat(String quadFile1,String quadFile2) {
		try {
			HDTPrivate hdt1 = HDTManager.generateHDT(quadFile1, "file://" + quadFile1, RDFNotation.NQUADS, new HDTSpecification(), true, this);
			HDTPrivate hdt2 = HDTManager.generateHDT(quadFile2, "file://" + quadFile2, RDFNotation.NQUADS, new HDTSpecification(), true, this);

			
			if (hdt1.getDictionary() instanceof ReificationDictionary && hdt2.getDictionary() instanceof ReificationDictionary) {
				File file = new File("out");
		        File theDir = new File(file.getAbsolutePath()+"_tmp");
		        theDir.mkdirs();
		        String location = theDir.getAbsolutePath()+"/";
				
				ReificationDictionary rd1 = (ReificationDictionary) hdt1.getDictionary();
				ReificationDictionary rd2 = (ReificationDictionary) hdt2.getDictionary();
				
				DictionaryCat dictionaryCat = new DictionaryCat(location);
				dictionaryCat.cat(rd1,rd2);
				
				ControlInfo ci1 = new ControlInformation();
				CountInputStream fis_t = new CountInputStream(new BufferedInputStream(new FileInputStream(location + "dictionary_t")));
				
				ControlInfo ci2 = new ControlInformation();
				CountInputStream fis_g = new CountInputStream(new BufferedInputStream(new FileInputStream(location + "dictionary_g")));
				
				HDTSpecification spec = new HDTSpecification();
				
				TriplesFourSectionDictionaryBig t_dictionary = new TriplesFourSectionDictionaryBig(spec);
				
				fis_t.mark(1024);
				ci1.load(fis_t);
				fis_t.reset();
				t_dictionary.mapFromFile(fis_t, new File(location + "dictionary_t"),null);
				
				GraphsFourSectionDictionaryBig g_dictionary = new GraphsFourSectionDictionaryBig(spec);
				
				fis_g.mark(1024);
				ci2.load(fis_g);
				fis_g.reset();
				g_dictionary.mapFromFile(fis_g, new File(location + "dictionary_g"),null);
				
				Utility.printIDs(hdt1);
				System.out.println("-----------------------------------------");
				Utility.printIDs(hdt2);
				
				Utility.printTriplesDictionary(t_dictionary);
				System.out.println("-----------------------------------------");
				Utility.printGraphsDictionary(g_dictionary);

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
				
				//BitmapQuadIteratorCat it = new BitmapQuadIteratorCat(hdt1.getTriples(),hdt2.getTriples(),dictionaryCat);
				/*while(it.hasNext()) {
					System.out.println("nexttt: "+it.next());
				}*/
				/*BitmapQuadsCat bitmapTriplesCat = new BitmapQuadsCat(location);
				bitmapTriplesCat.cat(it,this);
				
				CountInputStream fis2 = new CountInputStream(new BufferedInputStream(new FileInputStream(location + "triples")));
				ci2 = new ControlInformation();
				ci2.clear();
				fis2.mark(1024);
				ci2.load(fis2);
				fis2.reset();
				TriplesPrivate triples = TriplesFactory.createTriples(ci2);
				triples.mapFromFile(fis2,new File(location + "triples"),null);*/
				
				//dictionaryCat.close();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		Test_Cat readHDT = new Test_Cat();
		readHDT.cat("/home/alyhdr/Desktop/hdt/test_1.nq","/home/alyhdr/Desktop/hdt/test_2.nq");
	}

	@Override
	public void notifyProgress(float level, String message) {
		// TODO Auto-generated method stub
		
	}
}
