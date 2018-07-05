package org.rdfhdt.hdt.dictionary;
/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/iface/org/rdfhdt/hdt/dictionary/Dictionary.java $
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

/**
 * Interface that specifies the basic methods for any Dictionary implementation
 *
 * @author José M. Giménez-García, mario.arias, Eugen
 *
 */
public interface GraphsDictionary extends Dictionary {

    long getNsubjects();

    /**
     * Returns the number of subjects in the dictionary.
     */
    long getNgraphs();

    /**
     * Returns the number of objects in the dictionary.
     */
    long getNobjects();

    /**
     * Returns the number of subjects/objects in the dictionary.
     */
    long getNshared();

    DictionarySection getGraphs();

}
