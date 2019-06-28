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

import org.rdfhdt.hdt.compact.bitmap.Bitmap375;
import org.rdfhdt.hdt.compact.bitmap.ModifiableBitmap;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64Big;
import org.rdfhdt.hdt.compact.sequence.SequenceLog64BigDisk;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.QuadID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.util.BitUtil;
import org.rdfhdt.hdt.util.listener.IntermediateListener;
import org.rdfhdt.hdt.util.listener.ListenerUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class BitmapQuadsCat extends BitmapTriples {

    private String location;

    public BitmapQuadsCat(String location){
        this.location = location;
    }

    public void cat(IteratorTripleID it, ProgressListener listener){
        try {
            long number = it.estimatedNumResults();
            SequenceLog64BigDisk vectorY = new SequenceLog64BigDisk(location + "vectorY", BitUtil.log2(number), number);
            SequenceLog64BigDisk vectorZ = new SequenceLog64BigDisk(location + "vectorZ",BitUtil.log2(number), number);
            ModifiableBitmap bitY = new Bitmap375();//Disk(location + "bitY",number);
            ModifiableBitmap bitZ = new Bitmap375();//Disk(location + "bitZ",number);
            
            final SequenceLog64BigDisk vectorG = new SequenceLog64BigDisk(location + "vectorG",BitUtil.log2(number), number);
            
    		final ModifiableBitmap bitG = new Bitmap375(number);
            
    		long lastX=0, lastY=0, lastZ=0;
            long x, y, z;
            long numTriples=0;
            long g;
            while(it.hasNext()) {
                TripleID triple = it.next();
                TripleOrderConvert.swapComponentOrder(triple, TripleComponentOrder.SPO, TripleComponentOrder.SPO);

                x = triple.getSubject();
                y = triple.getPredicate();
                z = triple.getObject();
    			g = triple instanceof QuadID ? toRoleID(((QuadID) triple).getGraph(), TripleComponentRole.GRAPH) : 0;

                if(x==0 || y==0 || z==0) {
                    throw new IllegalFormatException("None of the components of a triple can be null");
                }

                if(numTriples==0) {
                    // First triple
                    vectorY.append(y);
                    vectorZ.append(z);
                } else if(x!=lastX) {
                    if(x!=lastX+1) {
                        throw new IllegalFormatException("Upper level must be increasing and correlative.");
                    }
                    // X changed
                    bitY.append(true);
                    vectorY.append(y);

                    bitZ.append(true);
                    vectorZ.append(z);
                } else if(y!=lastY) {
                    if(y<lastY) {
                        throw new IllegalFormatException("Middle level must be increasing for each parent.");
                    }

                    // Y changed
                    bitY.append(false);
                    vectorY.append(y);

                    bitZ.append(true);
                    vectorZ.append(z);
                } else {
                    if(z<lastZ) {
                        throw new IllegalFormatException("Lower level must be increasing for each parent.");
                    }

                    // Z changed
                    bitZ.append(false);
                    vectorZ.append(z);
                }
                if (g == 0) {
    				bitG.append(false);
    			} else
    			{
    				bitG.append(true);
    				vectorG.append(g);
    			}

                lastX = x;
                lastY = y;
                lastZ = z;

                ListenerUtil.notifyCond(listener, "Converting to BitmapTriples", numTriples, numTriples, number);
                numTriples++;
            }

            if(numTriples>0) {
                bitY.append(true);
                bitZ.append(true);
            }

            vectorY.aggressiveTrimToSize();
            vectorZ.trimToSize();

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(location + "triples"));
            ControlInfo ci = new ControlInformation();
            ci.setType(ControlInfo.Type.TRIPLES);
            ci.setFormat(HDTVocabulary.TRIPLES_TYPE_BITMAP);
            ci.setInt("order", TripleComponentOrder.SPO.ordinal());
            ci.setType(ControlInfo.Type.TRIPLES);
            ci.save(bos);
            
            IntermediateListener iListener = new IntermediateListener(listener);
            
            bitY.save(bos, iListener);
            bitZ.save(bos, iListener);
            bitG.save(bos, iListener);
            
            vectorY.save(bos, iListener);
            vectorZ.save(bos, iListener);
            vectorG.save(bos, iListener);
            
            Files.delete(Paths.get(location + "vectorY"));
            Files.delete(Paths.get(location + "vectorZ"));
            Files.delete(Paths.get(location + "vectorG"));
            
            //Files.delete(Paths.get(location + "bitY"));
            //Files.delete(Paths.get(location + "bitZ"));
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}