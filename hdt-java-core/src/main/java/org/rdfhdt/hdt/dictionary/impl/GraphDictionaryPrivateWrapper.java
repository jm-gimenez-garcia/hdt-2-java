package org.rdfhdt.hdt.dictionary.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.dictionary.DictionarySectionPrivate;
import org.rdfhdt.hdt.dictionary.GraphDictionaryPrivate;
import org.rdfhdt.hdt.dictionary.GraphTempDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.enums.DictionarySectionRole;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.string.CompactString;

import static org.rdfhdt.hdt.dictionary.impl.DictionaryWrapper.*;

/**
 * @author José M. Giménez-García
 * This class is a wrapper for BaseDictionary to implement a Reification dictionary
 * Section Property is substituted by section Graph (used to store graph id's not used in any other position)
 *
 */
public class GraphDictionaryPrivateWrapper implements GraphDictionaryPrivate {

	protected DictionaryPrivate bd;
	
	public GraphDictionaryPrivateWrapper(DictionaryPrivate bd) {
		this.bd = bd;
	}
	
//	protected int getGlobalId(int id, DictionarySectionRole position) {
//		return bd.getGlobalId(id, translate(position)); 
//	}
//
//	protected int getLocalId(int id, TripleComponentRole position) {
//		return bd.getLocalId(id, translate(position));
//	}
	
	/* (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#stringToId(java.lang.CharSequence, datatypes.TripleComponentRole)
	 */
	@Override
	public int stringToId(CharSequence str, TripleComponentRole position) {
		return bd.stringToId(str, translate(position));
	}	
	
	@Override
	public long getNumberOfElements() {
		return bd.getNumberOfElements();
	}

	@Override
	public long size() {
		return bd.size();
	}

	@Override
	public long getNsubjects() {
		return bd.getNsubjects();
	}

	public long getNgraphs() {
		return bd.getNpredicates();
	}

	@Override
	public long getNobjects() {
		return bd.getNobjects();
	}

	@Override
	public long getNshared() {
		return bd.getNshared();
	}

	@Override
	public DictionarySection getSubjects() {
		return bd.getSubjects();
	}
	
	public DictionarySection getGraphs() {
		return bd.getPredicates();
	}
	
	@Override
	public DictionarySection getObjects() {
		return bd.getObjects();
	}
	
	@Override
	public DictionarySection getShared() {
		return bd.getShared();
	}
	
//	protected DictionarySectionPrivate getSection(int id, TripleComponentRole role) {
//		return bd.getSection(id, translate(role));
//	}

	/* (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#idToString(int, datatypes.TripleComponentRole)
	 */
	@Override
	public CharSequence idToString(int id, TripleComponentRole role) {
		return bd.idToString(id, role);
	}

	@Override
	public void populateHeader(Header header, String rootNode) {
		bd.populateHeader(header, rootNode);
	}

	@Override
	public String getType() {
		return bd.getType();
	}

	@Override
	public void close() throws IOException {
		bd.close();
	}

	@Override
	public void load(InputStream input, ControlInfo ci, ProgressListener listener) throws IOException {
		bd.load(input, ci, listener);
	}

	@Override
	public void mapFromFile(CountInputStream in, File f, ProgressListener listener) throws IOException {
		bd.mapFromFile(in, f, listener);
	}

	@Override
	public void load(TempDictionary other, ProgressListener listener) {
		bd.load(other, listener);
	}

	@Override
	public void save(OutputStream output, ControlInfo ci, ProgressListener listener) throws IOException {
		bd.save(output, ci, listener);
	}

	@Override
	public void load(GraphTempDictionary other, ProgressListener listener) {
		if (other instanceof GraphDictionaryPrivateWrapper) {
			bd.load(other, listener);
		}
		
	}

}
