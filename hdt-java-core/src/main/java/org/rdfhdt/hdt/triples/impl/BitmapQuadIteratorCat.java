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
 */

package org.rdfhdt.hdt.triples.impl;

import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.dictionary.impl.util.CatMapping;
import org.rdfhdt.hdt.dictionary.impl.DictionaryCat;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.QuadIDComparator;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleIDComparator;
import org.rdfhdt.hdt.triples.Triples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * @author alyhdr
 */
public class BitmapQuadIteratorCat extends BitmapTriples implements IteratorTripleID {

	int count = 1;

	Triples hdt1;
	Triples hdt2;
	Iterator<TripleID> list;
	DictionaryCat dictionaryCat;

	public BitmapQuadIteratorCat(Triples hdt1, Triples hdt2, DictionaryCat dictionaryCat) {

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
		return hdt1.searchAll().estimatedNumResults() + hdt2.searchAll().estimatedNumResults();
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
		if (count < dictionaryCat.getMappingS().size()) {
			return true;
		} else {
			if (list.hasNext()) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public TripleID next() {
		if (list.hasNext()) {
			return list.next();
		} else {

			list = getTripleID(count).listIterator();
			count++;
			if (count % 100000 == 0) {
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
				while (it.hasNext()) {
					TripleID temp = it.next();
					set.add(getNewQuad(temp, 1));
				}
			} else {
				it = hdt2.search(new QuadID((int) (long) mapping.get(i), 0, 0, 0));
				while (it.hasNext()) {
					TripleID temp = it.next();
					set.add(getNewQuad(temp, 2));
				}
			}
			
		}
		ArrayList<TripleID> triples = new ArrayList<TripleID>(set);
		Collections.sort(triples, QuadIDComparator.getComparator(TripleComponentOrder.SPO) );
		return triples;
	}
	
	private TripleID getNewQuad(TripleID temp,int type) {
		if (((QuadID) temp).getGraph() != 0) {
			long newP = mapIdPredicate(temp.getPredicate(), type);
			long newO = mapIdObject(temp.getObject(), type);
			long newG = mapIdGraph(((QuadID)temp).getGraph(), type);
			
			return new QuadID(count,(int)newP,(int)newO,(int)newG);
		} else {
			long newP = mapIdPredicate(temp.getPredicate(), type);
			long newO = mapIdObject(temp.getObject(), type);

			return new QuadID(count,(int)newP,(int)newO,0);
		}
	}
    /*private long mapIdsubject(long id,int type) {
    	CatMapping m_sh;
    	CatMapping m_gsh;
    	CatMapping m_s;
    	CatMapping m_gs;
    	
    	if(type == 1) {
    		m_sh = dictionaryCat.getMappings().get(dictionaryCat.M_SH_1);
    		m_gsh = dictionaryCat.getMappings().get(dictionaryCat.M_GSH_1);
    		m_s = dictionaryCat.getMappings().get(dictionaryCat.M_S_1);
    		m_gs = dictionaryCat.getMappings().get(dictionaryCat.M_GS_1);
    	} else {
    		m_sh = dictionaryCat.getMappings().get(dictionaryCat.M_SH_2);
    		m_gsh = dictionaryCat.getMappings().get(dictionaryCat.M_GSH_2);
    		m_s = dictionaryCat.getMappings().get(dictionaryCat.M_S_2);
    		m_gs = dictionaryCat.getMappings().get(dictionaryCat.M_GS_2);
    	}
    	if(id <= m_sh.getSize()) {
    		return m_sh.getMapping(id - 1);
    	}else if( id > m_sh.getSize() && id <= (m_sh.getSize() + m_gsh.getSize())) {
    		return m_gsh.getMapping(id - m_sh.getSize() - 1) + dictionaryCat.numShared;
    	}else if(id > (m_sh.getSize()+ m_gsh.getSize()) && id <= (m_sh.getSize() + m_gsh.getSize() + m_s.getSize())) {
    		return m_s.getMapping(id - m_gsh.getSize() - m_sh.getSize() -1) + dictionaryCat.numShared + dictionaryCat.numSharedGraphs;
    	}else {
    		return m_gs.getMapping(id - m_s.getSize() - m_gsh.getSize() - m_sh.getSize() -1)+ dictionaryCat.numShared +dictionaryCat.numSharedGraphs + dictionaryCat.numSubjects;
    	}
    }*/
	private long mapIdPredicate(long id, int type) {
		if(type == 1)
			return dictionaryCat.getMappings().get(dictionaryCat.M_P_1).getMapping(id - 1);
		else
			return dictionaryCat.getMappings().get(dictionaryCat.M_P_2).getMapping(id - 1);
	}
	private long mapIdObject(long id,int type) {
		CatMapping m_o;
		CatMapping m_go;
		CatMapping m_sh;
		CatMapping m_gsh;
		CatMapping m_s;
		CatMapping m_gs;
		if(type == 1) {
    		m_sh = dictionaryCat.getMappings().get(dictionaryCat.M_SH_1);
    		m_gsh = dictionaryCat.getMappings().get(dictionaryCat.M_GSH_1);
    		m_o = dictionaryCat.getMappings().get(dictionaryCat.M_O_1);
    		m_go = dictionaryCat.getMappings().get(dictionaryCat.M_GO_1);
    		m_s = dictionaryCat.getMappings().get(dictionaryCat.M_S_1);
    		m_gs = dictionaryCat.getMappings().get(dictionaryCat.M_GS_1);
    	} else {
    		m_sh = dictionaryCat.getMappings().get(dictionaryCat.M_SH_2);
    		m_gsh = dictionaryCat.getMappings().get(dictionaryCat.M_GSH_2);
    		m_o = dictionaryCat.getMappings().get(dictionaryCat.M_O_2);
    		m_go = dictionaryCat.getMappings().get(dictionaryCat.M_GO_2);
    		m_s = dictionaryCat.getMappings().get(dictionaryCat.M_S_2);
    		m_gs = dictionaryCat.getMappings().get(dictionaryCat.M_GS_2);
    	}
		if(m_sh == null || m_gsh == null) {
			System.out.println("nulllllllllllllllllllllllllllllllll");
		}
		long numberOfShared = m_sh.getSize() + m_gsh.getSize();
		long numberOfSubjects = m_s.getSize() + m_gs.getSize();
		long total = numberOfShared + numberOfSubjects;
		long totalNew = dictionaryCat.numShared + dictionaryCat.numSharedGraphs + dictionaryCat.numSubjects +dictionaryCat.numSubjectGraphs ;
		if(id <= m_sh.getSize()) {
			//if the id left to the shared graphs (type =5)
			if(m_sh.getType(id - 1)==5) {
				//return the new id in the graph shared section.
				return m_sh.getType(id -1) + dictionaryCat.numShared;
			} else {
				//return the id from the shared
				return m_sh.getMapping(id - 1);
			}
		} else if( id > m_sh.getSize() && id <= (m_sh.getSize() + m_gsh.getSize())) {
    		//return the id from the shared graphs were nothing could leave this section in merge
			return m_gsh.getMapping(id - m_sh.getSize() - 1) + dictionaryCat.numShared;
		}else if( id > total && id <= (total + m_o.getSize())) {
			//if the id left to the triples shared section (type =1)
			if(m_o.getType(id - total -1) == 1) {
				return m_o.getMapping(id - total -1);
			}else if(m_o.getType(id - total -1) == 5) {
				//if the id left to the shared graphs section (type =5)
				return m_o.getMapping(id - total -1) + dictionaryCat.numShared;
			}else if(m_o.getType(id - total -1) == 7) {
				//if the id left to the object graphs section (type =7)
				return m_o.getMapping(id - total -1) + totalNew + dictionaryCat.numObjects;
			}else {
				return m_o.getMapping(id - total -1) + totalNew;
			}
		}else {
			if(m_go.getType(id - total - m_o.getSize() -1) == 5) {
				//if id left to graphs shared section (type =5)
				return m_go.getMapping(id - total - m_o.getSize() -1) + dictionaryCat.numShared;
			}else {
				return m_go.getMapping(id - total - m_o.getSize() -1) + totalNew + dictionaryCat.numObjects;
			}
		}
	}
	private long mapIdGraph(long id,int type) {
		CatMapping m_gsh;
		CatMapping m_gs;
		CatMapping m_go;
		CatMapping m_gu;
		CatMapping m_sh;
		CatMapping m_s;
		CatMapping m_o;
		if(type == 1) {
    		m_sh = dictionaryCat.getMappings().get(dictionaryCat.M_SH_1);
    		m_s = dictionaryCat.getMappings().get(dictionaryCat.M_S_1);
    		m_o = dictionaryCat.getMappings().get(dictionaryCat.M_O_1);
    		m_gsh = dictionaryCat.getMappings().get(dictionaryCat.M_GSH_1);
    		m_gs = dictionaryCat.getMappings().get(dictionaryCat.M_GS_1);
    		m_go = dictionaryCat.getMappings().get(dictionaryCat.M_GO_1);
    		m_gu = dictionaryCat.getMappings().get(dictionaryCat.M_GU_1);

    	} else {
    		m_sh = dictionaryCat.getMappings().get(dictionaryCat.M_SH_2);
    		m_s = dictionaryCat.getMappings().get(dictionaryCat.M_S_2);
    		m_o = dictionaryCat.getMappings().get(dictionaryCat.M_O_2);
    		m_gsh = dictionaryCat.getMappings().get(dictionaryCat.M_GSH_2);
    		m_gs = dictionaryCat.getMappings().get(dictionaryCat.M_GS_2);
    		m_go = dictionaryCat.getMappings().get(dictionaryCat.M_GO_2);
    		m_gu = dictionaryCat.getMappings().get(dictionaryCat.M_GU_2);
    	}
		long total = m_sh.getSize() + m_gsh.getSize() + m_s.getSize() + m_gs.getSize();
		long totalNew = dictionaryCat.numShared + dictionaryCat.numSharedGraphs + dictionaryCat.numSubjects +dictionaryCat.numSubjectGraphs ;

		if(id > m_sh.getSize() && id <= (m_gsh.getSize() + m_sh.getSize())) {
			return m_gsh.getMapping(id - m_sh.getSize() - 1) + dictionaryCat.numShared;
		}else if(id > (m_gsh.getSize() + m_sh.getSize()) && id <= total){
			if(m_gs.getType(id - m_sh.getSize() - m_gsh.getSize() - m_s.getSize() -1) == 5) {
				return m_gs.getMapping(id - m_sh.getSize() - m_gsh.getSize() - m_s.getSize() -1) + dictionaryCat.numShared;
			}else {
				return m_gs.getMapping(id - m_sh.getSize() - m_gsh.getSize() - m_s.getSize() -1) + dictionaryCat.numShared + dictionaryCat.numSharedGraphs + dictionaryCat.numSubjects;
			}
		}else if( id > (total + m_o.getSize()) && id <= (total + m_o.getSize() + m_go.getSize())) {
			if(m_go.getType(id - total - m_o.getSize() -1) == 5) {
				//if id left to graphs shared section (type =5)
				return m_go.getMapping(id - total - m_o.getSize() -1) + dictionaryCat.numShared;
			}else {
				return m_go.getMapping(id - total - m_o.getSize() -1) + totalNew + dictionaryCat.numObjects;
			}
		}else {
			if(m_gu.getType(id - total -m_o.getSize() - m_go.getSize() -1) == 5) {
				return m_gu.getMapping(id - total -m_o.getSize() - m_go.getSize() -1) + dictionaryCat.numShared;
			}else if(m_gu.getType(id - total -m_o.getSize() - m_go.getSize() -1) == 6) {
				return m_gu.getMapping(id - total -m_o.getSize() - m_go.getSize() -1) + dictionaryCat.numShared +dictionaryCat.numSharedGraphs + dictionaryCat.numSubjects;
			}else if(m_gu.getType(id - total -m_o.getSize() - m_go.getSize() -1) == 7) {
				return m_gu.getMapping(id - total -m_o.getSize() - m_go.getSize() -1) + totalNew + dictionaryCat.numObjects;
			}
			else {
				return m_gu.getMapping(id - total -m_o.getSize() - m_go.getSize() -1) + totalNew + dictionaryCat.numObjects + dictionaryCat.numObjectGraphs;
			}
		}
	}
}