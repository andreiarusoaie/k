package org.kframework.backend.xml.spec;

import org.kframework.backend.xml.input.RLGoal;
import org.kframework.kil.Term;

import java.util.List;

/**
 * Created by andrei on 8/17/15.
 */
public class RLGoalSpec extends RLGoal {

    private List<Action> actions;

    public RLGoalSpec(Term lhs, Term rhs, Term lhsConstraint, Term rhsConstraint, int id) {
        super(lhs, rhs, lhsConstraint, rhsConstraint, id);
    }


}
