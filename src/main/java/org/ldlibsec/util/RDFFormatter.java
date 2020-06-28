package org.ldlibsec.util;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class RDFFormatter {

    public static String getTripleBody(Triple t) {
        StringBuilder sb = new StringBuilder();
        sb.append(format(t.getSubject()))
                .append(" ").append(format(t.getPredicate()))
                .append(" ").append(format(t.getObject())).append(" . ");
        return sb.toString();
    }

    public static CharSequence format(Node subject) {
        if(subject.isURI()){
            return "<"+subject.getURI()+">";
        }
        return subject.toString(true);
    }
}
