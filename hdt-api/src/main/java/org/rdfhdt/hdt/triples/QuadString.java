/**
 *
 */
package org.rdfhdt.hdt.triples;

import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.util.UnicodeEscape;

/**
 * @author José M. Giménez-García
 *
 */
public class QuadString extends TripleString {

    private CharSequence graph;

    public QuadString(final CharSequence subject, final CharSequence predicate, final CharSequence object, final CharSequence graph) {
	super(subject, predicate, object);
	this.graph = graph;
    }

    public QuadString(final QuadString other) {
	super(other);
	this.graph = other.graph;
    }

    public CharSequence getGraph() {
	return this.graph;
    }

    public void setGraph(final CharSequence graph) {
	this.graph = graph;
    }

    public void setAll(final CharSequence subject, final CharSequence predicate, final CharSequence object, final CharSequence graph) {
	super.setAll(subject, predicate, object);
	this.graph = graph;
    }

    public boolean equals(final QuadString other) {
	return !(super.equals(other) || !this.graph.equals(other.graph));
    }

    public boolean match(final QuadString pattern) {
	return super.match(pattern) && pattern.getGraph().equals(this.graph);
    }

    @Override
    public void clear() {
	super.clear();
	this.graph = "";
    }

    @Override
    public boolean isEmpty() {
	return super.isEmpty() && this.graph.length() == 0;
    }

    @Override
    public boolean hasEmpty() {
	return super.hasEmpty() || this.graph.length() == 0;
    }

    @Override
    public void read(String line) throws ParserException {
	int split, posa, posb;
	this.clear();

	line = line.replaceAll("\\t", " ");

	// SET SUBJECT
	posa = 0;
	posb = split = line.indexOf(' ', posa);

	if (posb == -1)
	    return; // Not found, error.
	if (line.charAt(posa) == '<')
	    posa++; // Remove <
	if (line.charAt(posb - 1) == '>')
	    posb--; // Remove >

	this.setSubject(line.substring(posa, posb));

	// SET PREDICATE
	posa = split + 1;
	posb = split = line.indexOf(' ', posa);

	if (posb == -1)
	    return;
	if (line.charAt(posa) == '<')
	    posa++;
	if (posb > posa && line.charAt(posb - 1) == '>')
	    posb--;

	this.setPredicate(line.substring(posa, posb));

	// SET OBJECT
	posa = split + 1;
	posb = line.length();

	if (line.charAt(posb - 1) == '.')
	    posb--; // Remove trailing <space> <dot> from NTRIPLES.
	if (line.charAt(posb - 1) == ' ')
	    posb--;

	if (line.charAt(posa) == '<') {
	    posa++;

	    // Remove trailing > only if < appears, so "some"^^<http://datatype> is kept as-is.
	    if (posb > posa && line.charAt(posb - 1) == '>')
		posb--;
	}

	this.setObject(UnicodeEscape.unescapeString(line.substring(posa, posb)));
    }

}
