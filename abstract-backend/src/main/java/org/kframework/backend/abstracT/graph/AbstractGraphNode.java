package org.kframework.backend.abstracT.graph;

import org.apache.commons.collections15.OrderedMap;
import org.kframework.backend.java.kil.ConstrainedTerm;

/**
 * Created by andrei on 16/07/15.
 */
public class AbstractGraphNode {
    private ConstrainedTerm lhs, rhs;

    public AbstractGraphNode(ConstrainedTerm lhs, ConstrainedTerm rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
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

    @Override
    public String toString() {
        return lhs + "\n=>\n" + rhs;
    }
}
