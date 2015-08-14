package org.kframework.backend.abstracT.graph;

import org.apache.commons.collections15.OrderedMap;
import org.kframework.backend.java.kil.ConstrainedTerm;

/**
 * Created by andrei on 16/07/15.
 */
public class AbstractGraphNode {
    private ConstrainedTerm lhs, rhs;
    private NodeStatus status;

    public AbstractGraphNode(ConstrainedTerm lhs, ConstrainedTerm rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        status = NodeStatus.VALID;
    }

    public ConstrainedTerm getLhs() {
        return lhs;
    }

    public ConstrainedTerm getRhs() {
        return rhs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractGraphNode) {
            AbstractGraphNode instance = (AbstractGraphNode) obj;
            return instance.getLhs().equals(lhs) && instance.getRhs().equals(rhs);
        }
        return false;
    }

    /**
     * Deep copy modulo ConstrainedTerms (which are shallow copied)
     * @return a copy of this node
     */
    public AbstractGraphNode copy() {
        return new AbstractGraphNode((ConstrainedTerm) lhs.shallowCopy(), (ConstrainedTerm) rhs.shallowCopy());
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return lhs + "\n=>\n" + rhs;
    }

    public NodeStatus getStatus() {
        return status;
    }
}
