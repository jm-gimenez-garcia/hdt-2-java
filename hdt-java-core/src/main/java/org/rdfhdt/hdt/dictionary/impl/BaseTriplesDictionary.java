/**
 * File: $HeadURL:
 * https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/dictionary/impl/BaseDictionary.java $
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * Contacting the authors:
 * Mario Arias: mario.arias@deri.org
 * Javier D. Fernandez: jfergar@infor.uva.es
 * Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 * Alejandro Andres: fuzzy.alej@gmail.com
 */

package org.rdfhdt.hdt.dictionary.impl;

import org.rdfhdt.hdt.dictionary.BaseDictionary;
import org.rdfhdt.hdt.dictionary.DictionarySection;
import org.rdfhdt.hdt.dictionary.DictionarySectionPrivate;
import org.rdfhdt.hdt.dictionary.TriplesDictionaryPrivate;
import org.rdfhdt.hdt.enums.DictionarySectionRole;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.util.string.CompactString;

/**
 *
 * This abstract clas implements all general methods that are the same
 * for every implementation of Dictionary.
 *
 * @author mario.arias, Eugen, José M. Giménez-García
 *
 */
public abstract class BaseTriplesDictionary extends BaseDictionary implements TriplesDictionaryPrivate {

	protected HDTOptions				spec;

	protected DictionarySectionPrivate subjects;
	protected DictionarySectionPrivate objects;
	protected DictionarySectionPrivate shared;
	protected DictionarySectionPrivate	predicates;

	public BaseTriplesDictionary(final HDTOptions spec) {
		super(spec);
	}

	@Override
	protected int getGlobalId(final int id, final DictionarySectionRole position) {
		final int globalId = super.getGlobalId(id, position);
		if (globalId > 0) {
			return globalId;
		} else {
			switch (position) {
				case PREDICATE:
					return id;
				default:
					throw new IllegalArgumentException();
			}
		}
	}

	@Override
	protected int getLocalId(final int id, final TripleComponentRole position) {
		final int localId = super.getLocalId(id, position);
		if (localId > 0) {
			return localId;
		} else {
			switch (position) {
				case PREDICATE:
					return id;
				default:
					throw new IllegalArgumentException();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see hdt.dictionary.Dictionary#stringToId(java.lang.CharSequence, datatypes.TripleComponentRole)
	 */
	@Override
	public int stringToId(CharSequence str, final TripleComponentRole position) {

		if (str == null || str.length() == 0) {
			return 0;
		}

		if (str instanceof String) {
			// CompactString is more efficient for the binary search.
			str = new CompactString(str);
		}

		int ret = 0;
		switch (position) {
			case SUBJECT:
				ret = this.shared.locate(str);
				if (ret != 0) {
					return this.getGlobalId(ret, DictionarySectionRole.SHARED);
				}
				ret = this.subjects.locate(str);
				if (ret != 0) {
					return this.getGlobalId(ret, DictionarySectionRole.SUBJECT);
				}
				return -1;
			case PREDICATE:
				ret = this.predicates.locate(str);
				if (ret != 0) {
					return this.getGlobalId(ret, DictionarySectionRole.PREDICATE);
				}
				return -1;
			case OBJECT:
				if (str.charAt(0) != '"') {
					ret = this.shared.locate(str);
					if (ret != 0) {
						return this.getGlobalId(ret, DictionarySectionRole.SHARED);
					}
				}
				ret = this.objects.locate(str);
				if (ret != 0) {
					return this.getGlobalId(ret, DictionarySectionRole.OBJECT);
				}
				return -1;
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public long getNumberOfElements() {
		return this.subjects.getNumberOfElements() + this.predicates.getNumberOfElements()
		+ this.objects.getNumberOfElements() + this.shared.getNumberOfElements();
	}

	@Override
	public long size() {
		return this.subjects.size() + this.predicates.size() + this.objects.size() + this.shared.size();
	}

	@Override
	public long getNsubjects() {
		return this.subjects.getNumberOfElements() + this.shared.getNumberOfElements();
	}

	@Override
	public long getNpredicates() {
		return this.predicates.getNumberOfElements();
	}

	@Override
	public long getNobjects() {
		return this.objects.getNumberOfElements() + this.shared.getNumberOfElements();
	}

	@Override
	public long getNshared() {
		return this.shared.getNumberOfElements();
	}

	@Override
	public DictionarySection getSubjects() {
		return this.subjects;
	}

	@Override
	public DictionarySection getPredicates() {
		return this.predicates;
	}

	@Override
	public DictionarySection getObjects() {
		return this.objects;
	}

	@Override
	public DictionarySection getShared() {
		return this.shared;
	}

	protected DictionarySectionPrivate getSection(final int id, final TripleComponentRole role) {
		switch (role) {
			case SUBJECT:
				if (id <= this.shared.getNumberOfElements()) {
					return this.shared;
				} else {
					return this.subjects;
				}
			case PREDICATE:
				return this.predicates;
			case OBJECT:
				if (id <= this.shared.getNumberOfElements()) {
					return this.shared;
				} else {
					return this.objects;
				}
			default:
				throw new IllegalArgumentException();
		}
	}

}