/**
 *
 */
package org.rdfhdt.hdt.dictionary;

import org.rdfhdt.hdt.enums.DictionarySectionRole;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.triples.TripleTranslatorImpl;
import org.rdfhdt.hdt.util.string.CompactString;

/**
 * @author José M. Giménez-García
 *
 */
public abstract class BaseDictionary extends TripleTranslatorImpl implements DictionaryPrivate {

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

	protected abstract DictionarySectionPrivate getSection(int id, TripleComponentRole position);

	/*
	 * (non-Javadoc)
	 * @see org.rdfhdt.hdt.dictionary.Dictionary#idToString(int, org.rdfhdt.hdt.enums.TripleComponentRole)
	 */
	@Override
	public CharSequence idToString(final int id, final TripleComponentRole position) {
		final DictionarySectionPrivate section = getSection(id, position);
		final int localId = getLocalId(id, position);
		return section.extract(localId);
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
				if (ret != 0) { return getGlobalId(ret, DictionarySectionRole.SHARED); }
				ret = this.subjects.locate(cs);
				if (ret != 0) { return getGlobalId(ret, DictionarySectionRole.SUBJECT); }
				return -1;
			case OBJECT:
				if (cs.charAt(0) != '"') {
					ret = this.shared.locate(cs);
					if (ret != 0) { return getGlobalId(ret, DictionarySectionRole.SHARED); }
				}
				ret = this.objects.locate(cs);
				if (ret != 0) { return getGlobalId(ret, DictionarySectionRole.OBJECT); }
				return -1;
			default:
				return -1;
		}
	}
}
