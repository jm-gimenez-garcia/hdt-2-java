package org.rdfhdt.hdtjena.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ami on 04/06/2018.
 */
public class GraphConstruct {
    private String graph;
    private List<String> subjects;
    private List<String> predicates;
    private List<String> objects;
    private List<String> keepC;
    private List<String> keepV;

    public String getGraph() {
        return graph;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public List<String> getPredicates() {
        return predicates;
    }

    public List<String> getObjects() {
        return objects;
    }

    public GraphConstruct() {
        keepC =new ArrayList<>();
        keepV=new ArrayList<>();
        subjects=new ArrayList<>();
        predicates=new ArrayList<>();
        objects=new ArrayList<>();

    }
    public void insertKeepC(String k)
    {
        keepC.add(k);
    }
    public void insertKeepV(String k)
    {
        keepV.add(k);
    }
    public void insert(String sub,String pred, String obj)
    {
        subjects.add(sub);
        predicates.add(pred);
        objects.add(obj);
    }

    public void insertSubject(String sub)
    {
        subjects.add(sub);
    }
    public void insertPredicate(String pred)
    {
        predicates.add(pred);
    }
    public void insertObj(String obj)
    {
        objects.add(obj);
    }

    public void setGraph(String graph)
    {
        this.graph=graph;
    }

    public List<String> getKeepC() {
        return keepC;
    }
    public List<String> getKeepV() {
        return keepV;
    }
}
