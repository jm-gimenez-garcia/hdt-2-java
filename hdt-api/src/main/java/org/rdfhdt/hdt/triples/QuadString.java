/**
 *
 */
package org.rdfhdt.hdt.triples;

import java.io.IOException;

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

	public QuadString(final TripleString other) {
		super(other);
		if (other instanceof QuadString) {
			this.graph = other instanceof QuadString ? ((QuadString) other).graph : null;
		}
	}

	public QuadString() {
		this(null,null,null,null);
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

	@Override
	public boolean equals(final TripleString other) {
		return super.equals(other) && (other instanceof QuadString ? this.graph.equals(((QuadString) other).getGraph()) : true);
	}

	@Override
	public boolean match(final TripleString pattern) {
		boolean result = true;

		if (pattern instanceof QuadString) {
			final CharSequence graphPattern = ((QuadString) pattern).getGraph();
			result = (graphPattern == "" || getGraph() == graphPattern);
		}

		return result && super.match(pattern);
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
		clear();

		line = line.replaceAll("\\t", " ");

		// SET SUBJECT
		posa = 0;
		posb = split = line.indexOf(' ', posa);

		if (posb == -1)
		{
			return; // Not found, error.
		}
		if (line.charAt(posa) == '<')
		{
			posa++; // Remove <
		}
		if (line.charAt(posb - 1) == '>')
		{
			posb--; // Remove >
		}

		setSubject(line.substring(posa, posb));

		// SET PREDICATE
		posa = split + 1;
		posb = split = line.indexOf(' ', posa);

		if (posb == -1) {
			return;
		}
		if (line.charAt(posa) == '<') {
			posa++;
		}
		if (posb > posa && line.charAt(posb - 1) == '>') {
			posb--;
		}

		setPredicate(line.substring(posa, posb));

		// SET OBJECT
		posa = split + 1;
		posb = line.length();

		if (line.charAt(posb - 1) == '.')
		{
			posb--; // Remove trailing <space> <dot> from NTRIPLES.
		}
		if (line.charAt(posb - 1) == ' ') {
			posb--;
		}

		if (line.charAt(posa) == '<') {
			posa++;

			// Remove trailing > only if < appears, so "some"^^<http://datatype> is kept as-is.
			if (posb > posa && line.charAt(posb - 1) == '>') {
				posb--;
			}
		}

		setObject(UnicodeEscape.unescapeString(line.substring(posa, posb)));
	}

	@Override
	public String toString() {
		return super.toString() + " " + this.graph;
	}

	@Override
	public final void dumpNtriple(final Appendable out) throws IOException {

		final char s0 = this.subject.charAt(0);
		if (s0 == '_' || s0 == '<') {
			out.append(this.subject);
		} else {
			out.append('<').append(this.subject).append('>');
		}

		final char p0 = this.predicate.charAt(0);
		if (p0 == '<') {
			out.append(' ').append(this.predicate).append(' ');
		} else {
			out.append(" <").append(this.predicate).append("> ");
		}

		final char o0 = this.object.charAt(0);
		if (o0 == '"') {
			UnicodeEscape.escapeString(this.object.toString(), out);
			out.append(" .\n");
		} else if (o0 == '_' || o0 == '<') {
			out.append(this.object);
		} else {
			out.append('<').append(this.object).append('>');
		}

		if (this.graph != null) {
			final char g0 = this.graph.charAt(0);
			if (g0 == '_' || g0 == '<') {
				out.append(this.graph);
			} else {
				out.append(" <").append(this.graph).append('>');
			}
		}

		out.append(" .\n");

	}

}
