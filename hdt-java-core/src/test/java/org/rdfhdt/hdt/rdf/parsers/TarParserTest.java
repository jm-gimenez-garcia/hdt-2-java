package org.rdfhdt.hdt.rdf.parsers;

import org.junit.Test;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.rdf.RDFParserCallback.RDFCallback;
import org.rdfhdt.hdt.triples.QuadString;
import org.rdfhdt.hdt.triples.TripleString;

public class TarParserTest implements RDFCallback {

    @Test
    public void test() throws ParserException {
	final RDFParserTar parser = new RDFParserTar();
	//		parser.doParse("/Users/mck/rdf/dataset/tgztest.tar.gz", "http://www.rdfhdt.org", RDFNotation.NTRIPLES, this);
    }

    @Override
    public void processTriple(final TripleString triple, final long pos) {

    }

    @Override
    public void processQuad(final QuadString quad, final long pos) {

    }

}
