/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/tools/org/rdfhdt/hdt/tools/RDF2HDT.java $
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
package org.rdfhdt.hdt.tools;

import java.io.IOException;
import java.util.List;

import org.rdfhdt.hdt.dictionary.CompositeDictionary;
import org.rdfhdt.hdt.dictionary.GraphsDictionary;
import org.rdfhdt.hdt.dictionary.TriplesDictionary;
import org.rdfhdt.hdt.dictionary.impl.ReificationDictionary;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.hdt.HDTPrivate;
import org.rdfhdt.hdt.hdt.HDTVersion;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.util.StopWatch;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

/**
 * @author mario.arias, José M. Giménez-García
 *
 */
public class RDF2HDT implements ProgressListener {

    public String	  rdfInput;
    public String	  hdtOutput;

    @Parameter(description = "<input RDF> <output HDT>")
    public List<String>	  parameters = Lists.newArrayList();

    @Parameter(names = "-options", description = "HDT Conversion options (override those of config file)")
    public String	  options;

    @Parameter(names = "-config", description = "Conversion config file")
    public String	  configFile;

    @Parameter(names = "-rdftype", description = "Type of RDF Input (ntriples, nquad, n3, turtle, rdfxml)")
    public String	  rdfType;

    @Parameter(names = "-version", description = "Prints the HDT version number")
    public static boolean showVersion;

    @Parameter(names = "-base", description = "Base URI for the dataset")
    public String	  baseURI;

    @Parameter(names = "-index", description = "Generate also external indices to solve all queries")
    public boolean	  generateIndex;

    @Parameter(names = "-quiet", description = "Do not show progress of the conversion")
    public boolean	  quiet;

    @Parameter(names = "-reif", description = "Create HDT for reified statements")
    public boolean	  reif;

    public void execute() throws ParserException, IOException {
	HDTSpecification spec;
	if (this.configFile != null) {
	    spec = new HDTSpecification(this.configFile);
	} else {
	    spec = new HDTSpecification();
	}
	if (this.options != null) {
	    spec.setOptions(this.options);
	}
	if (this.baseURI == null) {
	    this.baseURI = "file://" + this.rdfInput;
	}

	RDFNotation notation = null;
	if (this.rdfType != null) {
	    try {
		notation = RDFNotation.parse(this.rdfType);
	    } catch (final IllegalArgumentException e) {
		System.out.println("Notation " + this.rdfType + " not recognised.");
	    }
	}

	if (notation == null) {
	    try {
		notation = RDFNotation.guess(this.rdfInput);
	    } catch (final IllegalArgumentException e) {
		notation = this.reif ? RDFNotation.NQUADS : RDFNotation.NTRIPLES;
		System.out.println("Could not guess notation for " + this.rdfInput + " Trying " + notation);
	    }
	}

	HDTPrivate hdt = HDTManager.generateHDT(this.rdfInput, this.baseURI, notation, spec, this.reif, this);

	// Show Basic stats
	if (!this.quiet) {
	    System.out.println("Final HDT created.");
	    System.out.println("The HDT is a " + hdt.getClass().getName());
	    System.out.println("The dictionary is a " + hdt.getDictionary().getClass().getName());
	    System.out.println("The triples is a " + hdt.getTriples().getClass().getName());
	    System.out.println("Total Triples: " + hdt.getTriples().getNumberOfElements());
	    if (hdt.getDictionary() instanceof CompositeDictionary) {
		final ReificationDictionary rd = (ReificationDictionary) hdt.getDictionary();
		final TriplesDictionary td = rd.getTriplesDictionary();
		final GraphsDictionary gd = rd.getGraphsDictionary();
		System.out.println("Different subjects: " + rd.getNsubjects());
		System.out.println("Different predicates: " + rd.getNpredicates());
		System.out.println("Different objects: " + rd.getNobjects());
		System.out.println("Different graphs:" + rd.getNgraphs());
		System.out.println("Different subjects in triples dictionary: " + td.getNsubjects());
		System.out.println("Different predicates in triples dictionary: " + td.getNpredicates());
		System.out.println("Different objects in triples dictionary: " + td.getNobjects());
		System.out.println("Common Subject/Object in triples dictionary:" + td.getNshared());
		System.out.println("Different subjects in graphs dictionary: " + gd.getNsubjects());
		System.out.println("Different objects in graphs dictionary: " + gd.getNobjects());
		System.out.println("Common Subject/Object in graphs dictionary:" + gd.getNshared());
		System.out.println("Unused Graphs in graphs dictionary:" + gd.getNgraphs());
	    } else if (hdt.getDictionary() instanceof TriplesDictionary) {
		final TriplesDictionary td = (TriplesDictionary) hdt.getDictionary();
		System.out.println("Different subjects: " + td.getNsubjects());
		System.out.println("Different predicates: " + td.getNpredicates());
		System.out.println("Different objects: " + td.getNobjects());
		System.out.println("Common Subject/Object:" + td.getNshared());
	    } else if (hdt.getDictionary() instanceof GraphsDictionary) {
		final GraphsDictionary gd = (GraphsDictionary) hdt.getDictionary();
		System.out.println("Different subjects: " + gd.getNsubjects());
		System.out.println("Different objects: " + gd.getNobjects());
		System.out.println("Common Subject/Object:" + gd.getNshared());
		System.out.println("Unused Graphs:" + gd.getNgraphs());
	    }
	}

	try {

	    // Dump to HDT file
	    final StopWatch sw = new StopWatch();
	    hdt.saveToHDT(this.hdtOutput, this);
	    System.out.println("HDT saved to file in: " + sw.stopAndShow());

	    // Generate index and dump it to .hdt.index file
	    sw.reset();
	    if (this.generateIndex) {
		hdt = HDTManager.indexedHDT(hdt, this);
		System.out.println("Index generated and saved in: " + sw.stopAndShow());
	    }

	    // Debug all inserted triples
	    try {
		final IteratorTripleString iterator = this.reif ? hdt.search("", "", "", "") : hdt.search("", "", "");
		while (iterator.hasNext()) {
		    System.out.print(iterator.next().asNtriple());
		}
	    } catch (final NotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} finally {
	    if (hdt != null) hdt.close();
	}


    }

    /*
     * (non-Javadoc)
     * @see hdt.ProgressListener#notifyProgress(float, java.lang.String)
     */
    @Override
    public void notifyProgress(final float level, final String message) {
	if (!this.quiet) {
	    System.out.print("\r" + message + "\t" + Float.toString(level) + "                            \r");
	}
    }

    public static void main(final String[] args) throws Throwable {
	final RDF2HDT rdf2hdt = new RDF2HDT();
	final JCommander com = new JCommander(rdf2hdt, args);
	com.setProgramName("rdf2hdt");

	if (rdf2hdt.parameters.size() == 1) {
	    System.err.println("No input file specified, reading from standard input.");
	    rdf2hdt.rdfInput = "-";
	    rdf2hdt.hdtOutput = rdf2hdt.parameters.get(0);
	} else if (rdf2hdt.parameters.size() == 2) {
	    rdf2hdt.rdfInput = rdf2hdt.parameters.get(0);
	    rdf2hdt.hdtOutput = rdf2hdt.parameters.get(1);

	} else if (showVersion) {
	    System.out.println(HDTVersion.get_version_string("."));
	    System.exit(0);
	} else {
	    com.usage();
	    System.exit(1);
	}

	System.out.println("Converting " + rdf2hdt.rdfInput + " to " + rdf2hdt.hdtOutput + " as " + rdf2hdt.rdfType);

	rdf2hdt.execute();
    }
}
