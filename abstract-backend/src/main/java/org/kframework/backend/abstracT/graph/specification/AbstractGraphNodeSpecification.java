package org.kframework.backend.abstracT.graph.specification;

import org.apache.commons.collections15.OrderedMap;
import org.apache.commons.math3.util.Pair;
import org.kframework.kil.Term;

import java.util.SortedMap;

/**
 * Created by andrei on 16/07/15.
 */
public class AbstractGraphNodeSpecification {
    private Term lhs;
    private Term rhs;
    private Term lhsConstraint;
    private Term rhsConstraint;
    private SortedMap<Integer, Pair<Integer, Integer>> steps;

    public AbstractGraphNodeSpecification(Term lhs, Term rhs, Term lhsConstraint, Term rhsConstraint, SortedMap<Integer, Pair<Integer, Integer>> steps) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.lhsConstraint = lhsConstraint;
        this.rhsConstraint = rhsConstraint;
        this.steps = steps;
    }

    public Term getLhs() {
        return lhs;
    }

    public Term getRhs() {
        return rhs;
    }

    public Term getLhsConstraint() {
        return lhsConstraint;
    }

    public Term getRhsConstraint() {
        return rhsConstraint;
    }

    public SortedMap<Integer, Pair<Integer, Integer>> getSteps() {
        return steps;
    }
}
