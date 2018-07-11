package org.rdfhdt.hdtjena.transform;

import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ami on 23/05/2018.
 */
public class Qexec {

    private Dataset dataset;
    private RDFDataMgr rdfDataMgr;
    private Query query;

    public void setDataSet(String dataSetPath, Lang l) {
        this.rdfDataMgr = new RDFDataMgr();// this will create an rdf data manager to read n-quad
        this.dataset = rdfDataMgr.loadDataset(dataSetPath, l);
    }

    public void printDataSet() {
        this.rdfDataMgr.write(System.out, this.dataset, Lang.NQUADS);// this line will print the whole dataset with n-quads
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = QueryFactory.create(query);
    }

    public void execQuery(String query) {
        this.query = QueryFactory.create(query);
        QueryExecution qe = QueryExecutionFactory.create(this.query, this.dataset);
        ResultSet resultSet = qe.execSelect();
        ResultSetFormatter.out(System.out, resultSet);
    }

    public void execQuery(String query, Dataset ds) {
        this.query = QueryFactory.create(query);
        QueryExecution qe = QueryExecutionFactory.create(this.query, ds);
        ResultSet resultSet = qe.execSelect();
        ResultSetFormatter.out(System.out, resultSet);
    }

    public String getSTRQueryPattern() {
        return this.query.getQueryPattern().toString();
    }

    public String getGraphURI() {
        return this.query.getNamedGraphURIs().toString();
    }

    public List<String> getResultsVar() {
        return this.query.getResultVars();
    }

    public List<Node> getGraphVar() {
        List<Node> l = new ArrayList<>();
        ElementWalker.walk(this.query.getQueryPattern(),
                new ElementVisitorBase() {
                    public void visit(ElementNamedGraph el) {
                        l.add(el.getGraphNameNode());
                    }
                });
        return l;
    }

    public List<TriplePath> getQueryElements() {
        List<TriplePath> l = new ArrayList<>();
        ElementWalker.walk(this.query.getQueryPattern(),
                new ElementVisitorBase() {
                    public void visit(ElementPathBlock el) {
                        Iterator<TriplePath> triples = el.patternElts();
                        while (triples.hasNext()) {
                            TriplePath p = triples.next();
                            l.add(p);
                        }
                    }
                });
        return l;
    }

    public Map<String, Integer> mapCounter(List<String> list) {
        Map<String, Integer> duplicates = new HashMap<String, Integer>();

        for (String str : list) {
            if (duplicates.containsKey(str)) {
                duplicates.put(str, duplicates.get(str) + 1);
            } else {
                duplicates.put(str, 1);
            }
        }

        return duplicates;
    }

    public void printMap(Map<String, Integer> map) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    public List<String> getMaxinMap(Map<String, Integer> mp) {
        List<String> l = new ArrayList<>();
        int mxval = (Collections.max(mp.values()));
        for (Map.Entry<String, Integer> entry : mp.entrySet()) {  // Itrate through hashmap
            if (entry.getValue() == mxval) {
                l.add(entry.getKey());
            }
        }
        return l;
    }

    public String uriCorrection(String newQuery) {
        Pattern urlPattern = Pattern.compile(
                "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                        + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                        + "[\\p{Alnum},%_=?&#\\-+()\\[\\]\\*$~@!:/;']*)");
        Matcher matcher = urlPattern.matcher(newQuery);
        StringBuffer buf = new StringBuffer(newQuery);
        List<Integer>stEnd=new ArrayList<>();
        List<Integer>stStart=new ArrayList<>();

        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            stStart.add(matchStart);
            stEnd.add(matchEnd);
        }
        for(int i=0;i<stStart.size();i++)
        {if(i==0)
            buf.insert(stStart.get(i)+i, "<");
        else
            buf.insert(stStart.get(i)+i+i, "<");
            buf.insert(stEnd.get(i)+i+i+1, ">");
        }
        newQuery=buf.toString();
        return newQuery;
    }

    public String reification() {
        //String listString = String.join(", ", triples);
        List<String> subjects = new ArrayList<>();
        List<String> predicates = new ArrayList<>();
        List<String> objects = new ArrayList<>();
        List<String> ngraphs = new ArrayList<>();
        List<String> nsubjects = new ArrayList<>();
        List<String> npredicates = new ArrayList<>();
        List<String> nobjects = new ArrayList<>();
        List<GraphQuad> graphQuads = new ArrayList<>();
        List<String> glue = new ArrayList<>();

        for (int i = 0; i < getQueryElements().size(); i++) {
            subjects.add(getQueryElements().get(i).getSubject().toString());
            predicates.add(getQueryElements().get(i).getPredicate().toString());
            objects.add(getQueryElements().get(i).getObject().toString());
        }

        for (int i = 0; i < predicates.size(); i++) {
            if (objects.get(i).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#statement")) {
                ngraphs.add(subjects.get(i));
            }
            if (predicates.get(i).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject")) {
                nsubjects.add(objects.get(i));
            }
            if (predicates.get(i).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate")) {
                npredicates.add(objects.get(i));
            }
            if (predicates.get(i).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#object")) {
                nobjects.add(objects.get(i));
            }
        }

        for (int i = 0; i < nsubjects.size(); i++) {
            GraphQuad graphQuad = new GraphQuad(nsubjects.get(i), npredicates.get(i), nobjects.get(i), ngraphs.get(i));
            graphQuads.add(graphQuad);
        }
        for (int i = 0; i < graphQuads.size(); i++) {
            glue.add(graphQuads.get(i).getQuadString());
        }
        String listString = String.join(" ", glue);

        for (int i = 0; i < graphQuads.size(); i++) {
            int m = predicates.indexOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            predicates.remove(m);
            subjects.remove(m);
            objects.remove(m);
            int mm = predicates.indexOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");
            predicates.remove(mm);
            subjects.remove(mm);
            objects.remove(mm);
            int mmm = predicates.indexOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");
            predicates.remove(mmm);
            subjects.remove(mmm);
            objects.remove(mmm);
            int mmmm = predicates.indexOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");
            predicates.remove(mmmm);
            subjects.remove(mmmm);
            objects.remove(mmmm);
        }

        List<GraphQuad> remaining = new ArrayList<>();
        for (int i = 0; i < predicates.size(); i++) {
            GraphQuad g = new GraphQuad(subjects.get(i), predicates.get(i), objects.get(i));
            remaining.add(g);
        }
        List<String> glue2 = new ArrayList<>();
        for (int i = 0; i < remaining.size(); i++) {
            glue2.add(remaining.get(i).getTripleString());
        }
        String listString2 = String.join(" ", glue2);
        String selectQueryVars = String.join(" ?", getResultsVar());
        selectQueryVars = "?" + selectQueryVars;
        String q=uriCorrection("select " + selectQueryVars + " where{" + listString + listString2 + "}");
        System.out.println(q);
        return q;
    }

    public String namedGraph() {
        if (this.query.toString().toLowerCase().contains("graph") || query.toString().toLowerCase().contains("from")) {
            String selectQueryVars = String.join(" ?", getResultsVar());
            selectQueryVars = "?" + selectQueryVars;
            System.out.print("select " + selectQueryVars + " where " + this.query.getQueryPattern().toString() + "");
            return "select " + selectQueryVars + " where " + this.query.getQueryPattern().toString() + "";
        }
        return "this is not a named graph";
    }

    public String singleton() {
        List<String> subjects = new ArrayList<>();
        List<String> predicates = new ArrayList<>();
        List<String> objects = new ArrayList<>();
        List<String> ngraphs = new ArrayList<>();
        List<String> nsubjects = new ArrayList<>();
        List<String> npredicates = new ArrayList<>();
        List<String> nobjects = new ArrayList<>();

        for (int i = 0; i < getQueryElements().size(); i++) {
            subjects.add(getQueryElements().get(i).getSubject().toString());
            predicates.add(getQueryElements().get(i).getPredicate().toString());
            objects.add(getQueryElements().get(i).getObject().toString());
        }
        for (int i = 0; i < predicates.size(); i++) {
            if (predicates.get(i).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#singletonPropertyOf")) {
                ngraphs.add(subjects.get(i));
                npredicates.add(objects.get(i)); // if statement is true 100%
                subjects.remove(i);
                predicates.remove(i);
                objects.remove(i);
            }
        }
        String qr = "";
        for (int i = 0; i < predicates.size(); i++) {
            nsubjects.add(subjects.get(i));
            nobjects.add(objects.get(i));
        }
        for (int j = 0; j < ngraphs.size(); j++) {
            qr = qr + " graph " + ngraphs.get(j) + " { ";
            for (int i = 0; i < nsubjects.size(); i++) {
                if (ngraphs.get(j).equals(predicates.get(i))) {
                    qr = qr + nsubjects.get(i) + " ";
                    // predicates.set(i,npredicates.get(j));
                    qr = qr + npredicates.get(j) + " ";
                    qr = qr + nobjects.get(i) + ". ";
                }
            }
            qr = qr + "}";

        }
        for (int i = 0; i < nsubjects.size(); i++) {
            if (!ngraphs.contains(predicates.get(i))) {
                qr = qr + nsubjects.get(i) + " ";
                // predicates.set(i,npredicates.get(j));
                qr = qr + predicates.get(i) + " ";
                qr = qr + nobjects.get(i) + ". ";
            }
        }

        String selectQueryVars = String.join(" ?", getResultsVar());
        selectQueryVars = "?" + selectQueryVars;
        String q=uriCorrection("select " + selectQueryVars + " where{" + qr + "}");
        System.out.println(q);
        return q;
    }

    public String ndfluents1() {   List<String> subjects = new ArrayList<>();
        List<String> predicates = new ArrayList<>();
        List<String> objects = new ArrayList<>();
        List<String> ngraphs = new ArrayList<>();
        List<String> nsubjects = new ArrayList<>();
        List<String> npredicates = new ArrayList<>();
        List<String> nobjects = new ArrayList<>();
        List<String> subAccordingObj=new ArrayList<>();
        for (int i = 0; i < getQueryElements().size(); i++) {
            subjects.add(getQueryElements().get(i).getSubject().toString());
            predicates.add(getQueryElements().get(i).getPredicate().toString());
            objects.add(getQueryElements().get(i).getObject().toString());
        }
        for (int i=0;i<subjects.size();i++)
        {
            if(predicates.get(i).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
            {
                ngraphs.add(subjects.get(i));
                subjects.remove(i);
                predicates.remove(i);
                objects.remove(i);
            }
            else if(predicates.get(i).equals("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualExtent"))
            {
                if(!ngraphs.contains(objects.get(i)))
                {
                    ngraphs.add(objects.get(i));
                }
            }
            // this par i get the graphs from the query
        }
        for(int i = 0 ;i<subjects.size();i++)
        {
            if(!predicates.get(i).equals("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualExtent")&&!predicates.get(i).equals("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPart"))
            {
                nsubjects.add(subjects.get(i));
                npredicates.add(predicates.get(i));
                nobjects.add(objects.get(i));
            }
        }
            for (int j = 0; j <subjects.size() ; j++) {
                if(predicates.get(j).equals("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPart"))
                {
                    subAccordingObj.add(subjects.get(j));
                }
            }
        for (int i = 0; i < nsubjects.size(); i++) {
            for (int j = 0; j <subjects.size() ; j++) {
                if(nsubjects.get(i).equals(subjects.get(j)) && predicates.get(j).equals("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPart"))
                {
                 nsubjects.set(i,objects.get(j));
                }

            }

        }
        for (int i = 0; i < nobjects.size(); i++) {
            for (int j = 0; j <objects.size() ; j++) {
                if(nobjects.get(i).equals(subjects.get(j)) && predicates.get(j).equals("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPart"))
                {
                    nobjects.set(i,objects.get(j));
                }
            }
        }
        List<String> vkeep=new ArrayList<>();
        List<String> ckeep=new ArrayList<>();
         for(int j=0; j<subjects.size();j++)
         {
          if(predicates.get(j).equals("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPart"))
          {
              ckeep.add(objects.get(j));
              vkeep.add(subjects.get(j));
              subjects.set(j,objects.get(j));
          }
         }
         for(int i=0;i<ckeep.size();i++)
         {
             for(int j =0;j<subjects.size();j++)
             {
                 if(vkeep.get(i).equals(subjects.get(j)))
                 {
                     subjects.set(j,ckeep.get(i));
                 }
             }
         }

        for (int i = 0; i < nobjects.size(); i++) {
            System.out.println(nobjects.get(i));
        }
        String qr="";

        for(int i = 0 ; i<ngraphs.size(); i++)
        {
            qr=qr+" graph "+ngraphs.get(i)+" {";
            for(int z=0;z<subjects.size();z++)
            {
                if(ngraphs.get(i).equals(objects.get(z)))
                    for(int j=0;j<nsubjects.size(); j++)
                    {
                        if(subjects.get(z).equals(nsubjects.get(j))&& ngraphs.get(i).equals(objects.get(z)))
                        {
                            qr = qr + " " + nsubjects.get(j) + " " + npredicates.get(j) + " " + nobjects.get(j) + ".";
                        }
                    }
            }
            qr=qr+"}";
        }
        // return the remaining part of the query

        for(int i=0;i<nsubjects.size();i++)
        {
            if(!qr.contains(nsubjects.get(i) + " " + npredicates.get(i) + " " + nobjects.get(i)))
            {
                qr = qr + " " + nsubjects.get(i) + " " + npredicates.get(i) + " " + nobjects.get(i) + ".";
            }
        }
        String selectQueryVars = String.join(" ?", getResultsVar());
        selectQueryVars = "?" + selectQueryVars;
        String q="";
        if(this.query.toString().contains("*"))
            q="select * where{" + qr + "}";
        else
            q="select " + selectQueryVars + " where{" + qr + "}";
        q=uriCorrection(q);
        System.out.print(q);
        return q;
    }

    public String ndfluents(Map<String,String> map) {
        List<String> subjects = new ArrayList<>();
        List<String> predicates = new ArrayList<>();
        List<String> objects = new ArrayList<>();
        List<String> ngraphs = new ArrayList<>();
        List<String> nsubjects = new ArrayList<>();
        List<String> npredicates = new ArrayList<>();
        List<String> nobjects = new ArrayList<>();

        for (int i = 0; i < getQueryElements().size(); i++) {
            subjects.add(getQueryElements().get(i).getSubject().toString());
            predicates.add(getQueryElements().get(i).getPredicate().toString());
            objects.add(getQueryElements().get(i).getObject().toString());
        }
        for (int i=0;i<subjects.size();i++)
        {

            if(predicates.get(i).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")&& objects.get(i).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#Context")))
            {
                ngraphs.add(subjects.get(i));
                nsubjects.add(subjects.get(i));
                npredicates.add(predicates.get(i));
                nobjects.add(objects.get(i));
                subjects.remove(i);
                predicates.remove(i);
                objects.remove(i);
            }
            else if(predicates.get(i).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualExtent")))
            {
                if(!ngraphs.contains(objects.get(i)))
                {
                    ngraphs.add(objects.get(i));
                    //nsubjects.add(objects.get(i));
                    //npredicates.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
                    //nobjects.add(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#Context"));
                }
            }
            // this par i get the graphs from the query
        }
        for(int i = 0 ;i<subjects.size();i++)
        {
            if(!predicates.get(i).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualExtent"))&&!predicates.get(i).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPartOf")))
            {
                nsubjects.add(subjects.get(i));
                npredicates.add(predicates.get(i));
                nobjects.add(objects.get(i));
            }
        }
        for (int i = 0; i < nsubjects.size(); i++) {
            for (int j = 0; j <subjects.size() ; j++) {
                if(nsubjects.get(i).equals(subjects.get(j)) && predicates.get(j).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPartOf")))
                {
                    nsubjects.set(i,objects.get(j));
                }

            }

        }
        for (int i = 0; i < nobjects.size(); i++) {
            for (int j = 0; j <objects.size() ; j++) {
                if(nobjects.get(i).equals(subjects.get(j)) && predicates.get(j).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPartOf")))
                {
                    nobjects.set(i,objects.get(j));
                }
            }
        }
        String qr="";
        List<GraphConstruct> lgrc=new ArrayList<>();
        for (int i = 0; i <ngraphs.size() ; i++) {
            GraphConstruct gc=new GraphConstruct();
            gc.setGraph(ngraphs.get(i));
            for (int j = 0; j <subjects.size() ; j++)
            {
                if(predicates.get(j).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualExtent")) && objects.get(j).equals(ngraphs.get(i)))
                {
                    gc.insertKeepC(subjects.get(j));
                }
            }
            for (int j = 0; j <subjects.size() ; j++)
            {
                for (int k = 0; k < gc.getKeepC().size() ; k++) {
                    if(subjects.get(j).equals(gc.getKeepC().get(k)) && predicates.get(j).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPartOf")))
                    {
                        gc.insertKeepV(objects.get(j));
                    }
                }
            }
            for (int j = 0; j < subjects.size() ; j++) {
               if(!predicates.get(j).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualExtent")) && !predicates.get(j).equals(map.get("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPartOf")))
               {

                   for (int k = 0; k < gc.getKeepC().size(); k++) {
                       if(objects.get(j).equals(gc.getKeepC().get(k)) && !gc.getKeepC().contains(subjects.get(j)) ){
                           gc.insertSubject(subjects.get(j));
                           gc.insertPredicate(predicates.get(j));
                           gc.insertObj(gc.getKeepV().get(k));
                       }else
                       if(subjects.get(j).equals(gc.getKeepC().get(k)) && !gc.getKeepC().contains(objects.get(j)) ){
                           gc.insertSubject(gc.getKeepV().get(k));
                           gc.insertPredicate(predicates.get(j));
                           gc.insertObj(objects.get(j));
                       }
                       else
                       {
                           if(subjects.get(j).equals(gc.getKeepC().get(k)) ){
                               gc.insertSubject(gc.getKeepV().get(k));
                               gc.insertPredicate(predicates.get(j));
                           }
                           else if(objects.get(j).equals(gc.getKeepC().get(k))) {
                               gc.insertObj(gc.getKeepV().get(k));
                           }
                       }
                   }
            }
            }
            lgrc.add(gc);
        }
        qr="";
        for (int i = 0; i <lgrc.size() ; i++) {
            qr = qr+" graph "+lgrc.get(i).getGraph()+"{";
            for (int j = 0; j <lgrc.get(i).getSubjects().size() ; j++) {
                qr = qr + " " + lgrc.get(i).getSubjects().get(j) + " " + lgrc.get(i).getPredicates().get(j) + " " + lgrc.get(i).getObjects().get(j) + ".";
            }
            qr=qr+"} ";
        }
        // return the remaining part of the query
        for(int i=0;i<nsubjects.size();i++)
        {
            if(!qr.contains(nsubjects.get(i) + " " + npredicates.get(i) + " " + nobjects.get(i)))
            {
                qr = qr + " " + nsubjects.get(i) + " " + npredicates.get(i) + " " + nobjects.get(i) + ".";
            }
        }
        String selectQueryVars = String.join(" ?", getResultsVar());
        selectQueryVars = "?" + selectQueryVars;
        String q="";
        if(this.query.toString().contains("*"))
            q="select * where{" + qr + "}";
        else
            q="select " + selectQueryVars + " where{" + qr + "}";
        q=uriCorrection(q);
        System.out.print(q);
        return q;
    }

    public Map<String,String> metaDataOfNdfluent(String meta) {
        Map<String, String> map = new HashMap<>();
        if(!meta.equals("")) {
        List<String> subjects = new ArrayList<>();
        List<String> predicates = new ArrayList<>();
        List<String> objects = new ArrayList<>();
        Query q = QueryFactory.create(meta);
        List<TriplePath> l = new ArrayList<>();
        ElementWalker.walk(q.getQueryPattern(),
                new ElementVisitorBase() {
                    public void visit(ElementPathBlock el) {
                        Iterator<TriplePath> triples = el.patternElts();
                        while (triples.hasNext()) {
                            TriplePath p = triples.next();
                            l.add(p);
                        }
                    }
                });
        for (int i = 0; i < l.size(); i++) {
            subjects.add(l.get(i).getSubject().toString());
            predicates.add(l.get(i).getPredicate().toString());
            objects.add(l.get(i).getObject().toString());
        }
        for (int i = 0; i < subjects.size(); i++) {
            map.put(objects.get(i), subjects.get(i));
        }
            //for (Map.Entry<String, String> pair : map.entrySet()) {
              //  System.out.println(pair);
            //}
    }else
        {
            map.put("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPart","http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPart");
            map.put("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPartOf","http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualPartOf");
            map.put("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualExtent","http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#contextualExtent");
            map.put("http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#Context","http://www.emse.fr/~zimmermann/Ontologies/ndfluents.ttl#Context");
        }

        return map;
    }

    public String nary() {
        List<String> subjects = new ArrayList<>();
        List<String> predicates = new ArrayList<>();
        List<String> objects = new ArrayList<>();
        List<String> ngraphs = new ArrayList<>();
        List<String> nsubjects = new ArrayList<>();
        List<String> npredicates = new ArrayList<>();
        List<String> nobjects = new ArrayList<>();

        for (int i = 0; i < getQueryElements().size(); i++) {
            subjects.add(getQueryElements().get(i).getSubject().toString());
            predicates.add(getQueryElements().get(i).getPredicate().toString());
            objects.add(getQueryElements().get(i).getObject().toString());
        }

        for(int i = 0; i<subjects.size();i++)
        {
            for (int j = 0; j <subjects.size() ; j++) {


            if(predicates.get(i).equals("nary:statementPropertyOf"))
            {
                if(!npredicates.contains(objects.get(i)))
                    npredicates.add(objects.get(i));

                predicates.set(predicates.indexOf(subjects.get(i)),objects.get(i));
                subjects.remove(i);
                predicates.remove(i);
                objects.remove(i);
            }
            if(predicates.get(i).equals("nary:valuePropertyOf"))
            {
                if(!npredicates.contains(objects.get(i)))
                    npredicates.add(objects.get(i));

                predicates.set(predicates.indexOf(subjects.get(i)),objects.get(i));
                subjects.remove(i);
                predicates.remove(i);
                objects.remove(i);
            }
        }
        }

        for (int i = 0; i < predicates.size(); i++) {
            if(predicates.get(i).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")&& objects.get(i).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#statement"))
            {
                ngraphs.add(subjects.get(i));
                subjects.remove(i);
                predicates.remove(i);
                objects.remove(i);
            }
        }
        for (int i = 0; i <predicates.size() ; i++) {
            if(subjects.contains(objects.get(i)))
            {
                if(!ngraphs.contains(objects.get(i)))
                {
                    ngraphs.add(objects.get(i));
                }
            }
        }

        for (int i = 0; i <ngraphs.size() ; i++) {
            for (int j = 0; j < subjects.size(); j++) {
                if(ngraphs.get(i).equals(subjects.get(j)))
                {
                    if(npredicates.contains(predicates.get(j))) {
                        nobjects.add(objects.get(j));
                    }
                }
                if(ngraphs.get(i).equals(objects.get(j)))
                {
                    if(npredicates.contains(predicates.get(j))){
                        nsubjects.add(subjects.get(j));}
                }
            }
        }
        List<GraphConstruct> lgrc=new ArrayList<>();

        for (int i = 0; i <ngraphs.size() ; i++) {
            GraphConstruct gc=new GraphConstruct();
            gc.setGraph(ngraphs.get(i));
            for (int j = 0; j < subjects.size() ; j++) {
                if(subjects.get(j).equals(ngraphs.get(i)))
                {
                    gc.insertObj(objects.get(j));
                    gc.insertPredicate(predicates.get(j));
                }
                if(objects.get(j).equals(ngraphs.get(i)))
                {
                    gc.insertSubject(subjects.get(j));
                    gc.insertPredicate(predicates.get(j));
                }
            }
            lgrc.add(gc);
        }
        String qr="";
        for (int i = 0; i <lgrc.size() ; i++) {
            qr = qr + " graph " + lgrc.get(i).getGraph() + "{";
            for (int j = 0; j < lgrc.get(i).getSubjects().size(); j++) {
                qr = qr + " " + lgrc.get(i).getSubjects().get(j) + " " + lgrc.get(i).getPredicates().get(j) + " " + lgrc.get(i).getObjects().get(j) + ".";
            }
            qr = qr + "} ";
        }
        for (int i = 0; i <ngraphs.size() ; i++) {
            for (int j = 0; j < subjects.size() ; j++) {
                if(ngraphs.get(i).equals(objects.get(j)) && npredicates.contains(predicates.get(j)) && nsubjects.contains(subjects.get(j)) )
                {
                    subjects.remove(j);
                    objects.remove(j);
                    predicates.remove(j);
                }
            }
        }
        for (int i = 0; i <ngraphs.size() ; i++) {
            for (int j = 0; j < objects.size() ; j++) {
                if (ngraphs.get(i).equals(subjects.get(j)) && npredicates.contains(predicates.get(j)) && nobjects.contains(objects.get(j))) {
                    subjects.remove(j);
                    objects.remove(j);
                    predicates.remove(j);
                }
            }}
        for(int i=0;i<subjects.size();i++)
        {
            if(!qr.contains(subjects.get(i) + " " + predicates.get(i) + " " + objects.get(i)))
            {
                qr = qr + " " + subjects.get(i) + " " + predicates.get(i) + " " + objects.get(i) + ".";
            }
        }
        String selectQueryVars = String.join(" ?", getResultsVar());
        selectQueryVars = "?" + selectQueryVars;
        String q="";
        if(this.query.toString().contains("*"))
            q="select * where{" + qr + "}";
        else
            q="select " + selectQueryVars + " where{" + qr + "}";
        q=uriCorrection(q);
        System.out.println(q);

        return q ;
    }

    public String conversion(String type,String meta ) {
        if (type.equals("reification")) {
            return reification();
        } else if (type.equals("ngraph")) {
            return namedGraph();
        } else if (type.equals("singleton")) {
            return singleton();
        } else if(type.equals("ndfluents")) {
            String mmeta="";
            if(meta!="")
                mmeta="select * where{"+meta+"}";
            return ndfluents(metaDataOfNdfluent(mmeta));
        } else if(type.equals("nary")){
            String s= getQuery().toString().replace("{","{"+meta);
            setQuery(s);
            return nary();
        }
        return "default";
    }

}
