/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/tools/org/rdfhdt/hdt/tools/HdtSearch.java $
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contacting the authors:
 *   Mario Arias:               mario.arias@deri.org
 *   Javier D. Fernandez:       jfergar@infor.uva.es
 *   Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 *   Alejandro Andres:          fuzzy.alej@gmail.com
 */
package org.rdfhdt.hdt.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.hdt.HDTPrivate;
import org.rdfhdt.hdt.hdt.HDTVersion;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.QuadString;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.util.StopWatch;
import org.rdfhdt.hdt.util.UnicodeEscape;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;


/**
 * @author mario.arias
 *
 */
public class HdtSearch implements ProgressListener {
	@Parameter(description = "<HDT File>")
	public List<String> parameters = Lists.newArrayList();

	@Parameter(names = "-version", description = "Prints the HDT version number")
	public static boolean showVersion;

	public String hdtInput;

	@Parameter(names = "-memory", description = "Load the whole file into main memory. Ensures fastest querying.")
	public boolean loadInMemory;

	protected static void iterate(final HDTPrivate hdt, CharSequence subject, CharSequence predicate, CharSequence object, CharSequence graph) throws NotFoundException {
		final StopWatch iterateTime = new StopWatch();
		int count = 0;

		subject = subject.length()==1 && subject.charAt(0)=='?' ? "" : subject;
		predicate = predicate.length()==1 && predicate.charAt(0)=='?' ? "" : predicate;
		object = object.length()==1 && object.charAt(0)=='?' ? "" : object;
		graph = graph.length() == 1 && graph.charAt(0) == '?' ? "" : graph;

		// Iterate over triples as Strings
		final IteratorTripleString it = hdt.search(subject, predicate, object, graph);
		count = 0;
		while(it.hasNext()) {
			final TripleString triple = it.next();
			System.out.println(triple);
			count++;
		}

		//		Iterate over triples only as IDs
		//		TripleID patternID = DictionaryUtil.tripleStringtoTripleID(hdt.getDictionary(), new TripleString(subject, predicate, object));
		//		IteratorTripleID it2 = hdt.getTriples().search(patternID);
		//		while(it2.hasNext()) {
		//			TripleID triple = it2.next();
		//			System.out.println(triple);
		//			count++;
		//		}
		System.out.println("Iterated "+ count + " triples in "+iterateTime.stopAndShow());
	}

	private void help() {
		System.out.println("HELP:");
		System.out.println("Please write Triple Search Pattern, using '?' for wildcards. e.g ");
		System.out.println("   http://www.somewhere.com/mysubject ? ?");
		System.out.println("Use 'exit' or 'quit' to terminate interactive shell.");
	}

	/**
	 * Read from a line, where each component is separated by space.
	 * @param line
	 */
	private static void parseTriplePattern(final TripleString dest, final String line) throws ParserException {
		int split, posa, posb;
		dest.clear();

		// SET SUBJECT
		posa = 0;
		posb = split = line.indexOf(' ', posa);

		if(posb==-1)
		{
			throw new ParserException("Make sure that you included three terms."); // Not found, error.
		}

		dest.setSubject(line.substring(posa, posb));

		// SET PREDICATE
		posa = split+1;
		posb = split = line.indexOf(' ', posa);

		if(posb==-1) {
			throw new ParserException("Make sure that you included three terms.");
		}

		dest.setPredicate(line.substring(posa, posb));

		// SET OBJECT
		posa = split+1;
		// posb = line.length();
		posb = split = line.indexOf(' ', posa);
		if (split < 0) {
			posb = split = line.length();
		}

		if(line.charAt(posb-1)=='.')
		{
			posb--;	// Remove trailing <space> <dot> from NTRIPLES.
		}
		if(line.charAt(posb-1)==' ') {
			posb--;
		}

		dest.setObject(UnicodeEscape.unescapeString(line.substring(posa, posb)));

		// SET GRAPH
		posa = split + 1;
		if (posa < line.length()) {
			posb = line.length();

			if (line.charAt(posb - 1) == '.') {
				posb--; // Remove trailing <space> <dot> from NTRIPLES.
			}
			if (line.charAt(posb - 1) == ' ') {
				posb--;
			}

			if (line.charAt(posb - 1) == '.') {
				posb--; // Remove trailing <space> <dot> from NTRIPLES.
			}
			if (line.charAt(posb - 1) == ' ') {
				posb--;
			}

			((QuadString) dest).setGraph(UnicodeEscape.unescapeString(line.substring(posa, posb)));
		} else {
			((QuadString) dest).setGraph(UnicodeEscape.unescapeString("?"));
		}
	}

	public void execute() throws IOException {
		HDTPrivate hdt;
		if(this.loadInMemory) {
			hdt = HDTManager.loadIndexedHDT(this.hdtInput, this);
		} else {
			hdt= HDTManager.mapIndexedHDT(this.hdtInput, this);
		}

		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			final TripleString triplePattern = new QuadString();

			while(true) {
				System.out.print(">> ");
				System.out.flush();
				final String line=in.readLine();
				if(line==null || line.equals("exit") || line.equals("quit")) {
					break;
				}
				if(line.equals("help")) {
					help();
					continue;
				}

				try {
					parseTriplePattern(triplePattern, line);
					System.out.println(
							"Query: |" + triplePattern.getSubject() + "| |" + triplePattern.getPredicate() + "| |" + triplePattern.getObject() + "| |" + ((QuadString) triplePattern).getGraph() + "|");

					iterate(hdt, triplePattern.getSubject(), triplePattern.getPredicate(), triplePattern.getObject(), ((QuadString) triplePattern).getGraph());
				} catch (final ParserException e) {
					System.err.println("Could not parse triple pattern: "+e.getMessage());
					help();
				} catch (final NotFoundException e) {
					System.err.println("No results found.");
				}

			}
		} finally {
			if(hdt!=null) {
				hdt.close();
			}
			in.close();
		}
	}

	/* (non-Javadoc)
	 * @see hdt.ProgressListener#notifyProgress(float, java.lang.String)
	 */
	@Override
	public void notifyProgress(final float level, final String message) {
		//System.out.println(message + "\t"+ Float.toString(level));
	}

	public static void main(final String[] args) throws Throwable {
		final HdtSearch hdtSearch = new HdtSearch();
		final JCommander com = new JCommander(hdtSearch, args);
		com.setProgramName("hdtSearch");

		if (showVersion) {
			System.out.println(HDTVersion.get_version_string("."));
			System.exit(0);
		}

		if(hdtSearch.parameters.size()!=1) {
			com.usage();
			System.exit(1);
		}

		hdtSearch.hdtInput = hdtSearch.parameters.get(0);

		hdtSearch.execute();
	}


}
