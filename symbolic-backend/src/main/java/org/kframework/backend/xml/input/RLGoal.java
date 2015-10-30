package org.kframework.backend.xml.input;

import org.kframework.kil.Term;

/**
 * Created by andrei on 16/07/15.
 */
public class RLGoal {
    private int id;
    private Term lhs;
    private Term rhs;
    private Term lhsConstraint;
    private Term rhsConstraint;

    public RLGoal(Term lhs, Term rhs, Term lhsConstraint, Term rhsConstraint, int id) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.lhsConstraint = lhsConstraint;
        this.rhsConstraint = rhsConstraint;
        this.id = id;
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

    public Term getRhsConstraint() { return rhsConstraint; }
}
