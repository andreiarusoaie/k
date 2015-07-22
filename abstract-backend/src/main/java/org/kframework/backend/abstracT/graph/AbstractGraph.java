package org.kframework.backend.abstracT.graph;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.kframework.backend.java.kil.ConstrainedTerm;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrei on 14.07.2015.
 */
public class AbstractGraph {

    List<AbstractGraphNode> abstractNodes;
    List<AbstractGraphEdge> abstractEdges;

    public AbstractGraph(List<AbstractGraphNode> abstractNodes, List<AbstractGraphEdge> abstractEdges) {
        this.abstractNodes = abstractNodes;
        this.abstractEdges = abstractEdges;
    }

    public boolean hasNode(ConstrainedTerm lhs, ConstrainedTerm rhs) {
        for (AbstractGraphNode abstractGraphNode : abstractNodes) {
            if (abstractGraphNode.getLhs().equals(lhs) && abstractGraphNode.equals(rhs)) {
                return true;
            }
        }
        return false;
    }

    public AbstractGraphNode getNode(ConstrainedTerm lhs, ConstrainedTerm rhs) {
        for (AbstractGraphNode abstractGraphNode : abstractNodes) {
            if (abstractGraphNode.getLhs().equals(lhs) && abstractGraphNode.getRhs().equals(rhs)) {
                return abstractGraphNode;
            }
        }
        return null;
    }

    public boolean hasNode(AbstractGraphNode node) {
        return abstractNodes.contains(node);
    }

    public void addNode(AbstractGraphNode node) {
        if (!hasNode(node)) {
            abstractNodes.add(node);
        }
    }

    private boolean hasEdge(AbstractGraphEdge edge) {
        return abstractEdges.contains(edge);
    }

    public void addEdge(AbstractGraphEdge edge) {
        if (hasNode(edge.getSource()) && hasNode(edge.getTarget()) && !hasEdge(edge)) {
            abstractEdges.add(edge);
        }
    }

    public static AbstractGraph empty() {
        return new AbstractGraph(new ArrayList<>(), new ArrayList<>());
    }

    public void addSubgraph(AbstractGraph subgraph) {
        for (AbstractGraphNode node : subgraph.abstractNodes) {
            addNode(node);
        }
        abstractEdges.addAll(subgraph.abstractEdges);
    }

    public List<AbstractGraphNode> getSuccesors(AbstractGraphNode node) {
        List<AbstractGraphNode> results = new ArrayList<>();
        for (AbstractGraphEdge edge : abstractEdges) {
            if (edge.getSource().equals(node)) {
                results.add(edge.getTarget());
            }
        }
        return results;
    }

    public List<AbstractGraphNode> getSuccesorsByEdgeType(AbstractGraphNode node, EdgeType edgeType) {
        List<AbstractGraphNode> results = new ArrayList<>();
        for (AbstractGraphEdge edge : abstractEdges) {
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
        for (AbstractGraphNode node : abstractNodes) {
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
            return abstractNodes.equals(abstractGraph.abstractNodes) && abstractEdges.equals(abstractGraph.abstractEdges);
        }
        return false;
    }

    public boolean isSingletonGraph(AbstractGraphNode singletonNode) {
        return (abstractEdges.size() == 0 && abstractNodes.size() == 1 && abstractNodes.get(0).equals(singletonNode));
    }

    /**
     * Computes a deep copy of the graph modulo node's contents
     * @return a copy this graph
     */
    public AbstractGraph copy() {
        List<AbstractGraphNode> newAbstractNodes = new ArrayList<AbstractGraphNode>(abstractNodes.size());
        for (AbstractGraphNode node : abstractNodes) {
            newAbstractNodes.add(node.copy());
        }
        List<AbstractGraphEdge> newAbstractEdges = new ArrayList<AbstractGraphEdge>(abstractEdges.size());
        for (AbstractGraphEdge edge : abstractEdges) {
            newAbstractEdges.add(edge.copy());
        }
        return new AbstractGraph(newAbstractNodes, newAbstractEdges);
    }

    private DirectedSparseGraph getJungGraph() {
        DirectedSparseGraph jungGraph = new DirectedSparseGraph();
        for (AbstractGraphEdge abstractGraphEdge : abstractEdges) {
            jungGraph.addEdge(abstractGraphEdge.toString(), abstractGraphEdge.getSource().toString(), abstractGraphEdge.getTarget().toString());
        }
        return  jungGraph;
    }

    public void displayGraph() {
        VisualizationViewer visualizationViewer = new VisualizationViewer(new FRLayout(getJungGraph()), new Dimension(1024, 720));
        visualizationViewer.setVertexToolTipTransformer(new ToStringLabeller<String>());
        JFrame frame = new JFrame();
        frame.getContentPane().add(visualizationViewer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
