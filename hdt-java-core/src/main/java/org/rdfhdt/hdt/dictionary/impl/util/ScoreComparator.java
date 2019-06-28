package org.rdfhdt.hdt.dictionary.impl.util;

import org.rdfhdt.hdt.dictionary.impl.util.IteratorPlusString;
import org.rdfhdt.hdt.util.string.CharSequenceComparator;

import java.util.Comparator;

/**
 * @author Dennis Diefenbach & Jose Gimenez Garcia
 *
 */
public class ScoreComparator implements Comparator<IteratorPlusString> {

    public int compare(IteratorPlusString a, IteratorPlusString b) {
        CharSequenceComparator comparator = new CharSequenceComparator();
        int c = comparator.compare(a.value,b.value);
        if (c>0){
            return 1;
        } else if (c<0) {
            return -1;
        } else {
            return 0;
        }
    }
}
