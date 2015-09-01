package org.kframework.backend.abstracT.graph;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.kframework.backend.java.kil.ConstrainedTerm;
import org.kframework.utils.errorsystem.KEMException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    /**
     * Check whether the graph has a node determined by lhs and rhs.
     * @param lhs
     * @param rhs
     * @return {@value true} if there is a node in {@field abstractNodes} which has
     * lhs and rhs equal to {@param lhs} and {@param rhs}, respetively. Otherwise,
     * return {@value false}
     */
    public boolean hasNode(ConstrainedTerm lhs, ConstrainedTerm rhs) {
        for (AbstractGraphNode abstractGraphNode : abstractNodes) {
            if (abstractGraphNode.getLhs().equals(lhs) && abstractGraphNode.equals(rhs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given two {@link ConstrainedTerm} return the corresponding node in the graph
     * @param lhs
     * @param rhs
     * @return an {@link AbstractGraphNode} from {@field abstractNodes} which has lhs and rhs
     * equal to {@param lhs} and {@param rhs}, respectively. If such a node does not exists
     * then return {@value null}.
     */
    public AbstractGraphNode getNode(ConstrainedTerm lhs, ConstrainedTerm rhs) {
        for (AbstractGraphNode abstractGraphNode : abstractNodes) {
            if (abstractGraphNode.getLhs().equals(lhs) && abstractGraphNode.getRhs().equals(rhs)) {
                return abstractGraphNode;
            }
        }
        return null;
    }

    /**
     * Check whether a given node is in the graph.
     * @param node
     * @return true if {@param node} is in {@field abstractNodes}
     */
    public boolean hasNode(AbstractGraphNode node) {
        return abstractNodes.contains(node);
    }

    /**
     * Appends a given node to the existing set of nodes only if
     * the node is not present in the graph.
     * @param node
     */
    public void addNode(AbstractGraphNode node) {
        if (!hasNode(node)) {
            abstractNodes.add(node);
        }
    }

    /**
     * Check whether the graph contains a given edge
     * @param edge
     * @return true if {@param edge} is in {@field abstractEdges}
     */
    private boolean hasEdge(AbstractGraphEdge edge) {
        return abstractEdges.contains(edge);
    }

    /**
     * Append a new edge to the existing graph.
     * @param edge
     */
    public void addEdge(AbstractGraphEdge edge) {
        if (hasNode(edge.getSource()) && hasNode(edge.getTarget()) && !hasEdge(edge)) {
            abstractEdges.add(edge);
        }
    }

    /**
     * Create an empty {@link AbstractGraph} instance.
     * @return an instance of {@link AbstractGraph} with empty lists of nodes and edges
     */
    public static AbstractGraph empty() {
        return new AbstractGraph(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Append a given {@link AbstractGraph} to the current graph
     * @param subgraph
     */
    public void addSubgraph(AbstractGraph subgraph) {
        for (AbstractGraphNode node : subgraph.abstractNodes) {
            addNode(node);
        }
        abstractEdges.addAll(subgraph.abstractEdges);
    }

    /**
     * Returns the list of successors of a given node.
     * @param node
     * @return the list of all successors of {@param node}
     */
    public List<AbstractGraphNode> getSuccesors(AbstractGraphNode node) {
        List<AbstractGraphNode> results = new ArrayList<>();
        for (AbstractGraphEdge edge : abstractEdges) {
            if (edge.getSource().equals(node)) {
                results.add(edge.getTarget());
            }
        }
        return results;
    }

    /**
     * Computer a list of nodes which are the successors of a given node
     * linked by an edge of a given type.
     * @param node
     * @param edgeType
     * @return the list of successors of {@param node} linked by an edge of {@param edgeType}
     */
    public List<AbstractGraphNode> getSuccesorsByEdgeType(AbstractGraphNode node, EdgeType edgeType) {
        List<AbstractGraphNode> results = new ArrayList<>();
        for (AbstractGraphEdge edge : abstractEdges) {
            if (edge.getSource().equals(node) && edge.getEdgeType().equals(edgeType)) {
                results.add(edge.getTarget());
            }
        }
        return results;
    }

    /**
     * Check whether a given has successors
     * @param node
     * @return true if {@param node} has successors
     */
    public boolean hasSuccessors(AbstractGraphNode node) {
        return !getSuccesors(node).isEmpty();
    }

    /**
     * Computes a list of nodes which have no successors.
     * @return a list of nodes representing the 'frontier' of this graph
     */
    public List<AbstractGraphNode> getFrontier() {
        List<AbstractGraphNode> frontier = new ArrayList<>();
        for (AbstractGraphNode node : abstractNodes) {
            if (!hasSuccessors(node)) {
                frontier.add(node);
            }
        }
        return frontier;
    }

    public List<AbstractGraphNode> getAbstractNodes() {
        return abstractNodes;
    }

    public List<AbstractGraphEdge> getAbstractEdges() {
        return abstractEdges;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractGraph) {
            AbstractGraph abstractGraph = (AbstractGraph) obj;
            return abstractNodes.equals(abstractGraph.abstractNodes) && abstractEdges.equals(abstractGraph.abstractEdges);
        }
        return false;
    }

    /**
     * Check whether this graph is a singleton, i.e., it contains
     * exactly one node and no edges
     * @param singletonNode
     * @return true if the graph is a singleton
     */
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

    /**
     * Convert the existing graph into a jung graph for display only.
     * @return a {@link DirectedSparseGraph} version of this graph
     */
    private DirectedSparseGraph getJungGraph() {
        DirectedSparseGraph jungGraph = new DirectedSparseGraph();
        for (AbstractGraphEdge abstractGraphEdge : abstractEdges) {
            jungGraph.addEdge(abstractGraphEdge.toString(), abstractGraphEdge.getSource().toString(), abstractGraphEdge.getTarget().toString());
        }
        return  jungGraph;
    }

    /**
     * Prepares a {@link JFrame} and displays the current graph on it.
     */
    public void displayGraph(String title) {
        VisualizationViewer visualizationViewer = new VisualizationViewer(new FRLayout(getJungGraph()), new Dimension(1024, 720));
        visualizationViewer.setVertexToolTipTransformer(new ToStringLabeller<String>());
        JFrame frame = new JFrame();
        frame.getContentPane().add(visualizationViewer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle(title);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Checks whether a node is terminal, i.e., it has no successors
     * @param node
     * @return {@value true} if the node is terminal, or {@value false}, otherwise.
     */
    public boolean isTerminalNode(AbstractGraphNode node) {
        if (abstractNodes.contains(node) && getSuccesors(node).isEmpty()) {
            return true;
        }
        return false;
    }


    /**
     * Computer a list of nodes which are the predecessors of a given node
     * linked by an edge of a given type.
     * @param abstractGraphNode
     * @param edgeType
     * @return the list of predecessors of {@param node} linked by an edge of {@param edgeType}
     */
    public List<AbstractGraphNode> getPredecessorsByEdgeType(AbstractGraphNode abstractGraphNode, EdgeType edgeType) {
        List<AbstractGraphNode> predecessors = new ArrayList<>();
        for (AbstractGraphEdge edge : abstractEdges) {
            if(edge.getTarget().equals(abstractGraphNode) && edgeType == edge.getEdgeType()) {
                predecessors.add(edge.getSource());
            }
        }
        return predecessors;
    }

    public boolean isValid() {
        for (AbstractGraphNode node : abstractNodes) {
            if (node.getStatus() != NodeStatus.VALID) {
                return false;
            }
        }
        return true;
    }

    public void saveGraphAsImage(String exportFile) {
        VisualizationViewer visualizationViewer = new VisualizationViewer(new FRLayout(getJungGraph()), new Dimension(1024, 720));
        visualizationViewer.setVertexToolTipTransformer(new ToStringLabeller<String>());
        JFrame frame = new JFrame();
        frame.getContentPane().add(visualizationViewer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        BufferedImage bufferedImage = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        frame.paint(graphics2D);
        try {
            ImageIO.write(bufferedImage, "jpg", new File(exportFile));
        } catch (IOException e) {
            throw KEMException.criticalError("Cannot save graph as image: " + e.getLocalizedMessage());
        }

    }
}
