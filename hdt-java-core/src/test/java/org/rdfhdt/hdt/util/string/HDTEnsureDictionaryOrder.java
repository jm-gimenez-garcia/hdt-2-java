package org.rdfhdt.hdt.util.string;

import java.util.Comparator;
import java.util.Iterator;

import org.rdfhdt.hdt.dictionary.GraphsDictionary;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;


public class HDTEnsureDictionaryOrder {

    private static Comparator<CharSequence> comparator = CharSequenceComparator.getInstance();

    private static void print(final byte[] arr) {
	for (final byte b : arr) {
	    System.out.print(String.format("%02X ", b));
	}
	System.out.println();
    }

    private static void print(final CharSequence seq) {
	if(seq instanceof CompactString) {
	    final CompactString cs1 = (CompactString) seq;
	    print(cs1.data);
	}

	if(seq instanceof String) {
	    final String rs1 = (String) seq;
	    print(rs1.getBytes());
	}
    }

    public static void check(final Iterator<? extends CharSequence> it) {
	CharSequence lastCharseq = null;
	String lastStr =null;
	int cmp=0, cmp2=0;
	while (it.hasNext()) {
	    final CharSequence charSeq = it.next();
	    final String str = charSeq.toString();

	    if(lastCharseq!=null && ((cmp=comparator.compare(lastCharseq, charSeq))>0 )) {
		System.out.println("ERRA: "+lastCharseq+" / "+charSeq);
	    }

	    if(lastStr!=null && ((cmp2=lastStr.compareTo(str))>0)) {
		System.out.println("ERRB: "+lastStr+" / "+str);
	    }

	    if(Math.signum(cmp)!=Math.signum(cmp2)) {
		System.out.println("Not equal: "+cmp+" / "+cmp2);
		print(lastCharseq); print(charSeq);
		print(lastStr); print(str);
	    }

	    lastCharseq = charSeq;
	    lastStr = str;
	}
    }

    public static void main(final String[] args) throws Throwable {
	final HDT hdt = HDTManager.mapHDT(args[0], null);

	check(hdt.getDictionary().getSubjects().getSortedEntries());
	check(hdt.getDictionary().getObjects().getSortedEntries());
	check(hdt.getDictionary().getShared().getSortedEntries());

	if (hdt.getDictionary() instanceof TriplesDictionary) {
	    check(((TriplesDictionary) hdt.getDictionary()).getPredicates().getSortedEntries());
	}

	if (hdt.getDictionary() instanceof GraphsDictionary) {
	    check(((GraphsDictionary) hdt.getDictionary()).getGraphs().getSortedEntries());
	}
    }
}
