/**
 *
 */
package org.rdfhdt.hdt.iterator;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.rdfhdt.hdt.util.string.CompactString;
import org.rdfhdt.hdt.util.string.ComparableCharSequence;

/**
 * @author José M. Giménez-García
 *
 */
public class Iterators {

    @Test
    public void testCachedItarator() {

	final List<ComparableCharSequence> list = new LinkedList<>();

	list.add(new CompactString("un"));
	list.add(new CompactString("dos"));
	list.add(new CompactString("tres"));
	list.add(new CompactString("probando"));
	list.add(new CompactString("patata"));
	list.add(new CompactString("frita"));

	final CachedIterator<ComparableCharSequence> cachedIterator = new CachedIterator<>(list.iterator());

	while (cachedIterator.hasNext()) {
	    System.out.println("Next: " + cachedIterator.next());
	    System.out.println("Current: " + cachedIterator.getCurrent());
	}
    }

    @Test
    public void testComparableCachedItarator() {

	final List<ComparableCharSequence> list = new LinkedList<>();
	list.add(new CompactString("un"));
	list.add(new CompactString("dos"));
	list.add(new CompactString("tres"));
	list.add(new CompactString("probando"));
	list.add(new CompactString("patata"));
	list.add(new CompactString("frita"));

	final List<ComparableCharSequence> list2 = new LinkedList<>();
	list2.add(new CompactString("one"));
	list2.add(new CompactString("two"));
	list2.add(new CompactString("three"));
	list2.add(new CompactString("testing"));
	list2.add(new CompactString("potato"));
	list2.add(new CompactString("fried"));

	final ComparableCachedIterator<ComparableCharSequence> cachedIterator = new ComparableCachedIterator<>(list.iterator());

	while (cachedIterator.hasNext()) {
	    final ComparableCachedIterator<ComparableCharSequence> cachedIterator2 = new ComparableCachedIterator<>(list2.iterator());
	    while (cachedIterator2.hasNext()) {
		System.out.println(cachedIterator.getNext() + " vs " + cachedIterator2.getNext() + " = " + cachedIterator.compareTo(cachedIterator2));
		System.out.println("Value 2 = " + cachedIterator2.next());
	    }
	    System.out.println("Value 1 = " + cachedIterator.next());
	}
    }

    @Test
    public void testCompositeItarator() {

	final List<ComparableCharSequence> list = new LinkedList<>();
	list.add(new CompactString("un"));
	list.add(new CompactString("dos"));
	list.add(new CompactString("tres"));
	list.add(new CompactString("probando"));
	list.add(new CompactString("patata"));
	list.add(new CompactString("frita"));

	final List<ComparableCharSequence> list2 = new LinkedList<>();
	list2.add(new CompactString("one"));
	list2.add(new CompactString("two"));
	list2.add(new CompactString("three"));
	list2.add(new CompactString("testing"));
	list2.add(new CompactString("potato"));
	list2.add(new CompactString("fried"));

	final CachedIterator<ComparableCharSequence> cachedIterator = new CachedIterator<>(list.iterator());
	final CachedIterator<ComparableCharSequence> cachedIterator2 = new CachedIterator<>(list2.iterator());
	final CompositeIterator<ComparableCharSequence> compositeIterator = new CompositeIterator<>(cachedIterator, cachedIterator2);

	compositeIterator.forEachRemaining(V -> System.out.println(V));
    }

    @Test
    public void testCompositeSortedItarator() {

	final List<ComparableCharSequence> list = new LinkedList<>();
	list.add(new CompactString("un"));
	list.add(new CompactString("dos"));
	list.add(new CompactString("tres"));
	list.add(new CompactString("probando"));
	list.add(new CompactString("patata"));
	list.add(new CompactString("frita"));

	final List<ComparableCharSequence> list2 = new LinkedList<>();
	list2.add(new CompactString("one"));
	list2.add(new CompactString("two"));
	list2.add(new CompactString("three"));
	list2.add(new CompactString("testing"));
	list2.add(new CompactString("potato"));
	list2.add(new CompactString("fried"));

	list.sort(null);
	list2.sort(null);
	final ComparableCachedIterator<ComparableCharSequence> cachedIterator = new ComparableCachedIterator<>(list.iterator());
	final ComparableCachedIterator<ComparableCharSequence> cachedIterator2 = new ComparableCachedIterator<>(list2.iterator());
	final CompositeSortedIterator<ComparableCharSequence> compositeIterator = new CompositeSortedIterator<>(cachedIterator, cachedIterator2);

	compositeIterator.forEachRemaining(V -> System.out.println("Value: " + V));
    }

}
