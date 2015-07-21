package org.kframework.backend.abstracT.graph;


/**
 * Created by andrei on 16/07/15.
 */
public class AbstractGraphEdge {
    private AbstractGraphNode source;
    private AbstractGraphNode target;
    private EdgeType edgeType;

    public AbstractGraphEdge(AbstractGraphNode source, AbstractGraphNode target, EdgeType edgeType) {
        this.source = source;
        this.target = target;
        this.edgeType = edgeType;
    }

    public AbstractGraphNode getSource() {
        return source;
    }

    public AbstractGraphNode getTarget() {
        return target;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractGraphEdge) {
            AbstractGraphEdge instance = (AbstractGraphEdge) obj;
            return instance.getSource().equals(source) && instance.getTarget().equals(target) && instance.getEdgeType().equals(edgeType);
        }
        return false;
    }
}
