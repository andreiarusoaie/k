package org.kframework.backend.abstracT.graph;

import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import org.kframework.backend.java.kil.ConstrainedTerm;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrei on 14.07.2015.
 */
public class AbstractGraph {

    List<AbstractGraphNode> abstractGraphNodes;
    List<AbstractGraphEdge> abstractGraphEdges;

    public AbstractGraph(List<AbstractGraphNode> abstractGraphNodes, List<AbstractGraphEdge> abstractGraphEdges) {
        this.abstractGraphNodes = abstractGraphNodes;
        this.abstractGraphEdges = abstractGraphEdges;
    }

    public boolean hasNode(ConstrainedTerm lhs, ConstrainedTerm rhs) {
        for (AbstractGraphNode abstractGraphNode : abstractGraphNodes) {
            if (abstractGraphNode.getLhs().equals(lhs) && abstractGraphNode.equals(rhs)) {
                return true;
            }
        }
        return false;
    }

    public AbstractGraphNode getNode(ConstrainedTerm lhs, ConstrainedTerm rhs) {
        for (AbstractGraphNode abstractGraphNode : abstractGraphNodes) {
            if (abstractGraphNode.getLhs().equals(lhs) && abstractGraphNode.getRhs().equals(rhs)) {
                return abstractGraphNode;
            }
        }
        return null;
    }

    public boolean hasNode(AbstractGraphNode node) {
        return abstractGraphNodes.contains(node);
    }

    public void addNode(AbstractGraphNode node) {
        if (!hasNode(node)) {
            abstractGraphNodes.add(node);
        }
    }

    private boolean hasEdge(AbstractGraphEdge edge) {
        return abstractGraphEdges.contains(edge);
    }

    public void addEdge(AbstractGraphEdge edge) {
        if (hasNode(edge.getSource()) && hasNode(edge.getTarget()) && !hasEdge(edge)) {
            abstractGraphEdges.add(edge);
        }
    }

//    public void addEdge(AbstractGraphNode source, AbstractGraphNode target, EdgeType edgeType) {
//        AbstractGraphEdge edge = new AbstractGraphEdge(source, target, edgeType);
//        if (hasNode(source) && hasNode(target) && !hasEdge(edge)) {
//            abstractGraphEdges.add(edge);
//        }
//    }


    public static AbstractGraph empty() {
        return new AbstractGraph(new ArrayList<>(), new ArrayList<>());
    }

    public void addSubgraph(AbstractGraph subgraph) {
        for (AbstractGraphNode node : subgraph.abstractGraphNodes) {
            addNode(node);
        }
        abstractGraphEdges.addAll(subgraph.abstractGraphEdges);
    }

    public List<AbstractGraphNode> getSuccesors(AbstractGraphNode node) {
        List<AbstractGraphNode> results = new ArrayList<>();
        for (AbstractGraphEdge edge : abstractGraphEdges) {
            if (edge.getSource().equals(node)) {
                results.add(edge.getTarget());
            }
        }
        return results;
    }

    public List<AbstractGraphNode> getSuccesorsByEdgeType(AbstractGraphNode node, EdgeType edgeType) {
        List<AbstractGraphNode> results = new ArrayList<>();
        for (AbstractGraphEdge edge : abstractGraphEdges) {
            if (edge.getSource().equals(node) && edge.getEdgeType().equals(edgeType)) {
                results.add(edge.getTarget());
            }
        }
        return results;
    }

    public boolean hasSuccessors(AbstractGraphNode node) {
        return !getSuccesors(node).isEmpty();
    }

    public List<AbstractGraphNode> getFrontier() {
        List<AbstractGraphNode> frontier = new ArrayList<>();
        for (AbstractGraphNode node : abstractGraphNodes) {
            if (!hasSuccessors(node)) {
                frontier.add(node);
            }
        }
        return frontier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractGraph) {
            AbstractGraph abstractGraph = (AbstractGraph) obj;
            return abstractGraphNodes.equals(abstractGraph.abstractGraphNodes) && abstractGraphEdges.equals(abstractGraph.abstractGraphEdges);
        }
        return false;
    }

    public boolean isSingletonGraph(AbstractGraphNode singletonNode) {
        return (abstractGraphEdges.size() == 0 && abstractGraphNodes.size() == 1 && abstractGraphNodes.get(0).equals(singletonNode));
    }

    private DirectedSparseGraph getJungGraph() {
        DirectedSparseGraph jungGraph = new DirectedSparseGraph();
//        for (AbstractGraphNode abstractGraphNode : abstractGraphNodes) {
//            jungGraph.addVertex(abstractGraphNode);
//        }
        for (AbstractGraphEdge abstractGraphEdge : abstractGraphEdges) {
            jungGraph.addEdge(abstractGraphEdge.toString(), abstractGraphEdge.getSource().toString(), abstractGraphEdge.getTarget().toString());
        }
        return  jungGraph;
    }

    public void displayGraph() {
        VisualizationImageServer visualizationImageServer = new VisualizationImageServer(new FRLayout2(getJungGraph()), new Dimension(1024, 720));
        JFrame frame = new JFrame();
        frame.getContentPane().add(visualizationImageServer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
