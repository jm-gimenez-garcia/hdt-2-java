package org.rdfhdt.hdt.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.text.Utilities;

import org.rdfhdt.hdt.dictionary.CompositeDictionary;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
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
	
	/*public void cat(String quadFile1,String quadFile2) {
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
				
				Utility.printMappings(dictionaryCat);
			
				BitmapQuadIteratorCat it = new BitmapQuadIteratorCat(hdt1.getTriples(),hdt2.getTriples(),dictionaryCat);

				BitmapQuadsCat bitmapTriplesCat = new BitmapQuadsCat(location);
				bitmapTriplesCat.cat(it,this);
				
				CountInputStream fis2 = new CountInputStream(new BufferedInputStream(new FileInputStream(location + "triples")));
				ci2 = new ControlInformation();
				ci2.clear();
				fis2.mark(1024);
				ci2.load(fis2);
				fis2.reset();
				TriplesPrivate triples = TriplesFactory.createTriples(ci2);
				triples.mapFromFile(fis2,new File(location + "triples"),null);
				deleteMappings(location);
				//dictionaryCat.close();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	public void deleteMappings(String location) {
		try {
			Files.delete(Paths.get(location+"P1"));
			Files.delete(Paths.get(location+"P1"+"Types"));
			Files.delete(Paths.get(location+"P2"));
			Files.delete(Paths.get(location+"P2"+"Types"));
	        Files.delete(Paths.get(location+"SH1"));
	        Files.delete(Paths.get(location+"SH1"+"Types"));
	        Files.delete(Paths.get(location+"SH2"));
	        Files.delete(Paths.get(location+"SH2"+"Types"));
			Files.delete(Paths.get(location+"S1"));
			Files.delete(Paths.get(location+"S1"+"Types"));
			Files.delete(Paths.get(location+"S2"));
			Files.delete(Paths.get(location+"S2"+"Types"));
			Files.delete(Paths.get(location+"O1"));
			Files.delete(Paths.get(location+"O1"+"Types"));
			Files.delete(Paths.get(location+"O2"));
			Files.delete(Paths.get(location+"O2"+"Types"));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		  File file = new File("out");
	        File theDir = new File(file.getAbsolutePath()+"_tmp");
	        theDir.mkdirs();
	        String location = theDir.getAbsolutePath()+"/";
	        try {
				HDT hdt = HDTManager.catHDT(location,"/home/alyhdr/Desktop/hdt/test_1.hdt", "/home/alyhdr/Desktop/hdt/test_2.hdt" , new HDTSpecification(),null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	@Override
	public void notifyProgress(float level, String message) {
		// TODO Auto-generated method stub
		
	}
}
