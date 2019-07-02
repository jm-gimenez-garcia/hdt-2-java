/**
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
 *   Dennis Diefenbach:         dennis.diefenbach@univ-st-etienne.fr
 */

package org.rdfhdt.hdt.triples.impl;

import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.dictionary.impl.util.CatMapping;
import org.rdfhdt.hdt.dictionary.impl.DictionaryCat;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleIDComparator;
import org.rdfhdt.hdt.triples.Triples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BitmapQuadIteratorCat implements IteratorTripleID {

    int count = 1;

    Triples hdt1;
    Triples hdt2;
    Iterator<TripleID> list;
    DictionaryCat dictionaryCat;
    TripleIDComparator tripleIDComparator = new TripleIDComparator(TripleComponentOrder.SPO);

    public BitmapQuadIteratorCat(Triples hdt1, Triples hdt2, DictionaryCat dictionaryCat){

        this.dictionaryCat = dictionaryCat;
        this.hdt1 = hdt1;
        this.hdt2 = hdt2;

        list = getTripleID(1).listIterator();
        count++;

    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public TripleID previous() {
        return null;
    }

    @Override
    public void goToStart() {

    }

    @Override
    public boolean canGoTo() {
        return false;
    }

    @Override
    public void goTo(long pos) {

    }

    @Override
    public long estimatedNumResults() {
        return hdt1.searchAll().estimatedNumResults()+hdt2.searchAll().estimatedNumResults();
    }

    @Override
    public ResultEstimationType numResultEstimation() {
        return null;
    }

    @Override
    public TripleComponentOrder getOrder() {
        return null;
    }

    @Override
    public boolean hasNext() {
        if (count<dictionaryCat.getMappingS().size()){
            return true;
        } else {
            if (list.hasNext()){
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public TripleID next() {
        if (list.hasNext()){
            return list.next();
        } else {

            list = getTripleID(count).listIterator();
            count ++;
            if (count%100000==0){
                System.out.println(count);
            }
            return list.next();
        }
    }

    @Override
    public void remove() {

    }

	private List<TripleID> getTripleID(int count) {
		Set<TripleID> set = new HashSet<>();
		ArrayList<Long> mapping = null;
		ArrayList<Integer> mappingType = null;
		mapping = dictionaryCat.getMappingS().getMapping(count);
		mappingType = dictionaryCat.getMappingS().getType(count);

		for (int i = 0; i < mapping.size(); i++) {
			IteratorTripleID it;
			if (mappingType.get(i) == 1) {
				it = hdt1.search(new QuadID((int) (long) mapping.get(i), 0, 0, 0));
			} else {
				it = hdt1.search(new QuadID((int) (long) mapping.get(i), 0, 0, 0));
			}
			while (it.hasNext()) {
				TripleID temp = it.next();
				if (temp instanceof QuadID) {
					if (((QuadID) temp).getGraph() != 0) {
						//System.out.println(((QuadID) temp).getGraph());
						set.add(temp);
					} else {
						System.out.println("triplee");
						set.add(temp);
					}
				}
			}
		}
		ArrayList<TripleID> triples = new ArrayList<TripleID>(set);
		Collections.sort(triples, tripleIDComparator);
		return triples;
	}
    /*public TripleID mapTriple(TripleID tripleID, int num){
        if (num == 1){
            long new_subject1 = mapIdSection(tripleID.getSubject(), dictionaryCat.getMappings().get(dictionaryCat.M_SH_1),dictionaryCat.getMappings().get(dictionaryCat.M_S_1));
            long new_predicate1 = mapIdPredicate(tripleID.getPredicate(), dictionaryCat.getMappings().get(dictionaryCat.M_P_1));
            long new_object1 = mapIdSection(tripleID.getObject(), dictionaryCat.getMappings().get(dictionaryCat.M_SH_1),dictionaryCat.getMappings().get(dictionaryCat.M_O_1));
            
            return new QuadID(new_subject1, new_predicate1, new_object1);
        } else {
            long new_subject2 = mapIdSection(tripleID.getSubject(), dictionaryCat.getMappingSh2(),dictionaryCat.getMappingS2());
            long new_predicate2 = mapIdPredicate(tripleID.getPredicate(), dictionaryCat.getMappingP2());
            long new_object2 = mapIdSection(tripleID.getObject(), dictionaryCat.getMappingSh2(),dictionaryCat.getMappingO2());
            return new TripleID(new_subject2, new_predicate2, new_object2);
        }
    }*/
    /*public TripleID mapTriple(TripleID tripleID, int num){
        if (num == 1){
            long new_subject1 = mapIdSection(tripleID.getSubject(), dictionaryCat.getMappingSh1(),dictionaryCat.getMappingS1());
            long new_predicate1 = mapIdPredicate(tripleID.getPredicate(), dictionaryCat.getMappingP1());
            long new_object1 = mapIdSection(tripleID.getObject(), dictionaryCat.getMappingSh1(),dictionaryCat.getMappingO1());
            return new TripleID(new_subject1, new_predicate1, new_object1);
        } else {
            long new_subject2 = mapIdSection(tripleID.getSubject(), dictionaryCat.getMappingSh2(),dictionaryCat.getMappingS2());
            long new_predicate2 = mapIdPredicate(tripleID.getPredicate(), dictionaryCat.getMappingP2());
            long new_object2 = mapIdSection(tripleID.getObject(), dictionaryCat.getMappingSh2(),dictionaryCat.getMappingO2());
            return new TripleID(new_subject2, new_predicate2, new_object2);
        }
    }*/

    /*private long mapIdSection(long id, CatMapping catMappingShared, CatMapping catMapping){
        if (id <= catMappingShared.getSize()){
            return catMappingShared.getMapping(id-1);
        } else {
            if (catMapping.getType(id-catMappingShared.getSize()-1)==1){
                return catMapping.getMapping(id-catMappingShared.getSize()-1);
            } else {
                return catMapping.getMapping(id - catMappingShared.getSize()-1) + dictionaryCat.getNumEntriesShared();
            }
        }
    }*/

    private long mapIdPredicate(long id, CatMapping catMapping){
        return catMapping.getMapping(id-1);
    }


}