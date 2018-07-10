package org.rdfhdt.hdtjena.transform;

/**
 * Created by ami on 24/05/2018.
 */
public class GraphQuad {
    private String subject;
    private String predicate;
    private String object;
    private String graph;

    public GraphQuad() {
    }

    public GraphQuad(String subject, String predicate, String object, String graph) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.graph = graph;
    }

    public GraphQuad(String subject, String predicate, String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public String getQuadString()
    {
        return "graph "+this.graph+" {"+this.subject+" "+this.predicate+" "+this.object+" .}";
    }
    public String getTripleString()
    {
        return " "+this.subject+" "+this.predicate+" "+this.object+" .";
    }
}
