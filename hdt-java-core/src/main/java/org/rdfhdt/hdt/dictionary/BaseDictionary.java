/**
 *
 */
package org.rdfhdt.hdt.dictionary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rdfhdt.hdt.enums.DictionarySectionRole;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdt.util.string.CompactString;

/**
 * @author José M. Giménez-García
 *
 */
public abstract class BaseDictionary<T extends Dictionary> implements DictionaryPrivate<T> {

    protected HDTOptions	       spec;

    protected DictionarySectionPrivate subjects;
    protected DictionarySectionPrivate objects;
    protected DictionarySectionPrivate shared;

    public BaseDictionary(final HDTOptions spec) {
	this.spec = spec;
    }

    protected int getGlobalId(final int id, final DictionarySectionRole position) {
	switch (position) {
	    case SUBJECT:
	    case OBJECT:
		return this.shared.getNumberOfElements() + id;
	    case SHARED:
		return id;
	    default:
		return 0;
	}
    }

    protected int getLocalId(final int id, final TripleComponentRole position) {
	switch (position) {
	    case SUBJECT:
	    case OBJECT:
		if (id <= this.shared.getNumberOfElements()) {
		    return id;
		} else {
		    return id - this.shared.getNumberOfElements();
		}
	    default:
		return 0;
	}
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#idToString(int, org.rdfhdt.hdt.enums.TripleComponentRole)
     */
    @Override
    public CharSequence idToString(final int id, final TripleComponentRole position) {
	// TODO Auto-generated method stub
	return null;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#stringToId(java.lang.CharSequence, org.rdfhdt.hdt.enums.TripleComponentRole)
     */
    @Override
    public int stringToId(final CharSequence str, final TripleComponentRole position) {

	if (str == null || str.length() == 0) { return 0; }

	final CharSequence cs = str instanceof CompactString ? str : new CompactString(str);

	int ret = 0;
	switch (position) {
	    case SUBJECT:
		ret = this.shared.locate(cs);
		if (ret != 0) { return this.getGlobalId(ret, DictionarySectionRole.SHARED); }
		ret = this.subjects.locate(cs);
		if (ret != 0) { return this.getGlobalId(ret, DictionarySectionRole.SUBJECT); }
		return -1;
	    case OBJECT:
		if (cs.charAt(0) != '"') {
		    ret = this.shared.locate(cs);
		    if (ret != 0) { return this.getGlobalId(ret, DictionarySectionRole.SHARED); }
		}
		ret = this.objects.locate(cs);
		if (ret != 0) { return this.getGlobalId(ret, DictionarySectionRole.OBJECT); }
		return -1;
	    default:
		return -1;
	}
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#getNumberOfElements()
     */
    @Override
    public long getNumberOfElements() {
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.Dictionary#size()
     */
    @Override
    public long size() {
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#load(java.io.InputStream, org.rdfhdt.hdt.options.ControlInfo, org.rdfhdt.hdt.listener.ProgressListener)
     */
    @Override
    public void load(final InputStream input, final ControlInfo ci, final ProgressListener listener) throws IOException {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#mapFromFile(org.rdfhdt.hdt.util.io.CountInputStream, java.io.File, org.rdfhdt.hdt.listener.ProgressListener)
     */
    @Override
    public void mapFromFile(final CountInputStream in, final File f, final ProgressListener listener) throws IOException {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#load(org.rdfhdt.hdt.dictionary.Dictionary, org.rdfhdt.hdt.listener.ProgressListener)
     */
    @Override
    public void load(final Dictionary other, final ProgressListener listener) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#save(java.io.OutputStream, org.rdfhdt.hdt.options.ControlInfo, org.rdfhdt.hdt.listener.ProgressListener)
     */
    @Override
    public void save(final OutputStream output, final ControlInfo ci, final ProgressListener listener) throws IOException {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#populateHeader(org.rdfhdt.hdt.header.Header, java.lang.String)
     */
    @Override
    public void populateHeader(final Header header, final String rootNode) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.rdfhdt.hdt.dictionary.DictionaryPrivate#getType()
     */
    @Override
    public String getType() {
	// TODO Auto-generated method stub
	return null;
    }

}
