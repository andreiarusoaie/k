package org.kframework.backend.abstracT;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.kframework.krun.api.KRunState;

/**
 * Created by andrei on 14.07.2015.
 */
public class AbstractGraph {

    private DirectedGraph<KRunState, DefaultEdge> directedGraph;

    public AbstractGraph() {
        directedGraph = (DirectedGraph<KRunState, DefaultEdge>) new DefaultDirectedGraph<KRunState, DefaultEdge>(DefaultEdge.class);
    }

    public void addEdge(KRunState source, KRunState target) {
        directedGraph.addVertex(source);
        directedGraph.addVertex(target);
        directedGraph.addEdge(source, target);
    }

}
