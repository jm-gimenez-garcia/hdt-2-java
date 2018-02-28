package org.rdfhdt.hdt.dictionary.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.dictionary.DictionarySectionPrivate;
import org.rdfhdt.hdt.dictionary.GraphDictionary;
import org.rdfhdt.hdt.dictionary.GraphDictionaryPrivate;
import org.rdfhdt.hdt.dictionary.GraphTempDictionary;
import org.rdfhdt.hdt.dictionary.CompositeDictionaryPrivate;
import org.rdfhdt.hdt.dictionary.CompositeTempDictionary;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.impl.section.CompositeDictionarySection;
import org.rdfhdt.hdt.enums.DictionarySectionRole;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.string.CompactString;


public class ReificationCompositeDictionary implements CompositeDictionaryPrivate{

	protected DictionaryPrivate bd;
	protected GraphDictionaryPrivate gd;
	
	protected CompositeDictionarySection shared = null;
	protected CompositeDictionarySection subjects = null;
	protected CompositeDictionarySection objects = null;
	protected CompositeDictionarySection graphs = null;
	
	protected long maxId = -1;
	
	public ReificationCompositeDictionary(DictionaryPrivate bd, GraphDictionaryPrivate gd) {
		this.bd = bd;
		this.gd = gd;
	}
	
	public ReificationCompositeDictionary(DictionaryPrivate bd, DictionaryPrivate gd) {
		this(bd, new GraphDictionaryPrivateWrapper(gd));
	}
	
	protected long getMaxId() {
		if (maxId == -1) {
			maxId = Math.max(bd.getNsubjects(), bd.getNobjects());
		}
		return maxId;
	}
	
	@Override
	public CharSequence idToString(int id, TripleComponentRole position) {
		CharSequence string;
		if (id <= getMaxId()) {
			string = bd.idToString(id, position);
		} else {
			string = gd.idToString(id, position);
		}
		return string;
	}
	
	@Override
	public int stringToId(CharSequence str, TripleComponentRole position) {
		int ret = -1;
		if(str==null || str.length()==0) {
			ret = 0;
		} else if ((ret = bd.stringToId(str, position)) == -1) {
			ret = gd.stringToId(str, position);
		}
		return ret;
	}

	@Override
	public long getNumberOfElements() {
		return bd.getNumberOfElements() + gd.getNumberOfElements();
	}

	@Override
	public long size() {
		return bd.size() + gd.size();
	}

	@Override
	public long getNsubjects() {
		return bd.getNsubjects() + gd.getNsubjects();
	}

	@Override
	public long getNpredicates() {
		return bd.getNpredicates();
	}

	@Override
	public long getNobjects() {
		return bd.getNobjects() + gd.getNobjects();
	}

	@Override
	public long getNshared() {
		return bd.getNshared() + gd.getNshared();
	}

	@Override
	public DictionarySection getSubjects() {
		if (subjects == null) {
			subjects = new CompositeDictionarySection(bd.getSubjects(), gd.getSubjects());
		}
		return subjects;
	}

	@Override
	public DictionarySection getPredicates() {
		return bd.getPredicates();
	}

	@Override
	public DictionarySection getObjects() {
		if (objects == null) {
			objects = new CompositeDictionarySection(bd.getObjects(), gd.getObjects());
		}
		return objects;
	}

	@Override
	public DictionarySection getShared() {
		if (shared == null) {
			shared = new CompositeDictionarySection(bd.getShared(), gd.getShared());
		}
		return shared;
	}

	@Override
	public void populateHeader(Header header, String rootNode) {
		// TODO implement
		throw new NotImplementedException();
	}

	@Override
	public long getNgraphs() {
		return gd.getNgraphs();
	}

	@Override
	public DictionarySection getGraphs() {
		if (graphs == null) {
			graphs = new CompositeDictionarySection(gd.getShared(), gd.getSubjects(), gd.getObjects(), gd.getGraphs());
		}
		return graphs;
	}
	
	@Override
	public String getType() {
		return HDTVocabulary.DICTIONARY_TYPE_REIFICATION;
	}

	@Override
	public void close() throws IOException {
		bd.close();
		gd.close();
	}

	@Override
	public void load(InputStream input, ControlInfo ci, ProgressListener listener) throws IOException {
		bd.load(input, ci, listener);
		gd.load(input, ci, listener);
	}

	@Override
	public void mapFromFile(CountInputStream in, File f, ProgressListener listener) throws IOException {
		bd.mapFromFile(in, f, listener);
		gd.mapFromFile(in, f, listener);
	}

	public void load(CompositeTempDictionary other, ProgressListener listener) {
		load(other.getNonReified(), listener);
		load(other.getReified(), listener);
	}
	
	@Override
	public void load(TempDictionary other, ProgressListener listener) {
		bd.load(other, listener);
	}
	
	@Override
	public void load(GraphTempDictionary other, ProgressListener listener) {
		gd.load(other, listener);
	}

	@Override
	public void save(OutputStream output, ControlInfo ci, ProgressListener listener) throws IOException {
		bd.save(output, ci, listener);
		gd.save(output, ci, listener);
	}

	

	
	
}
