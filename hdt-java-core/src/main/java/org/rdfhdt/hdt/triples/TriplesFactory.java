/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/triples/TriplesFactory.java $
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

package org.rdfhdt.hdt.triples;

import org.rdfhdt.hdt.hdt.HDTFactory;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.impl.BitmapQuads;
import org.rdfhdt.hdt.triples.impl.BitmapTriples;
import org.rdfhdt.hdt.triples.impl.QuadsList;
import org.rdfhdt.hdt.triples.impl.TriplesList;

/**
 * Factory that creates Triples objects
 *
 */
public class TriplesFactory {

	public static final String TEMP_TRIPLES_IMPL_LIST = "list";

	private TriplesFactory() {
	}

	/**
	 * Creates a new TempTriples (writable triples structure)
	 *
	 * @return TempTriples
	 */
	static public TempTriples createTempTriples(final HDTOptions spec, final boolean reif) {
		final String triplesImpl = spec.get("tempTriples.impl");

		// Implementations available in the Core
		if (triplesImpl == null || triplesImpl.equals("") || TEMP_TRIPLES_IMPL_LIST.equals(triplesImpl)) {
			return reif ? new QuadsList(spec) : new TriplesList(spec);
		}

		// Implementations available in the HDT-Disk module.
		return HDTFactory.getTempFactory().getTriples(spec);
	}

	/**
	 * Creates a new Triples based on an HDTOptions
	 *
	 * @param specification
	 *            The HDTOptions to read
	 * @return Triples
	 */
	static public TriplesPrivate createTriples(final HDTOptions spec, final boolean reif) {
		final String type = spec.get("triples.format");

		if (type == null) {
			return reif ? new BitmapQuads(spec) : new BitmapTriples(spec);
		} else if (HDTVocabulary.TRIPLES_TYPE_TRIPLESLIST.equals(type)) {
			return reif ? new QuadsList(spec) : new TriplesList(spec);
		} else if (HDTVocabulary.TRIPLES_TYPE_BITMAP.equals(type)) {
			return reif ? new BitmapQuads(spec) : new BitmapTriples(spec);
		} else {
			return reif ? new BitmapQuads(spec) : new BitmapTriples(spec);
		}
	}

	/**
	 * Creates a new Triples based on a ControlInformation
	 *
	 * @param specification
	 *            The HDTOptions to read
	 * @return Triples
	 */
	public static TriplesPrivate createTriples(final ControlInfo ci) {
		final String format = ci.getFormat();

		if (HDTVocabulary.TRIPLES_TYPE_TRIPLESLIST.equals(format)) {
			return new TriplesList(new HDTSpecification());
		} else if (HDTVocabulary.TRIPLES_TYPE_BITMAP.equals(format)) {
			return new BitmapTriples();
		} else if (HDTVocabulary.QUADS_TYPE_BITMAP.equals(format)) {
			return new BitmapQuads();
		} else {
			throw new IllegalArgumentException("No implementation for Triples type: " + format);
		}
	}

}
