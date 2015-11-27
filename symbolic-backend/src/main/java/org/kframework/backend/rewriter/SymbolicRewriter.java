package org.kframework.backend.rewriter;

import com.google.inject.Inject;
import org.kframework.backend.java.kil.ConstrainedTerm;
import org.kframework.backend.java.kil.Definition;
import org.kframework.backend.java.kil.Term;
import org.kframework.backend.java.symbolic.ConjunctiveFormula;
import org.kframework.backend.java.symbolic.JavaExecutionOptions;
import org.kframework.kompile.KompileOptions;
import org.kframework.krun.KRunExecutionException;
import org.kframework.krun.api.KRunState;

import java.util.List;

/**
 * Created by Andrei on 10/20/15.
 */
public class SymbolicRewriter extends org.kframework.backend.java.symbolic.SymbolicRewriter {

    @Inject
    public SymbolicRewriter(Definition definition, KompileOptions kompileOptions, JavaExecutionOptions javaOptions, KRunState.Counter counter) {
        super(definition, kompileOptions, javaOptions, counter);
    }

    /**
     * Computes a list of {@link ConstrainedTerm} after one symbolic execution step
     * @param constrainedTerm is the initial term to be executed
     * @return a list of {@link ConstrainedTerm}
     * @throws KRunExecutionException
     */
    public List<ConstrainedTerm> oneSearchStep(ConstrainedTerm constrainedTerm) throws KRunExecutionException {
        ConjunctiveFormula cF = constrainedTerm.constraint().simplify();
        Term term = constrainedTerm.term().substituteAndEvaluate(cF.substitution(), constrainedTerm.termContext());
        constrainedTerm = new ConstrainedTerm(term, cF);
        return computeRewriteStep(constrainedTerm , 0, false);
    }

    public List<ConstrainedTerm> oneStepWithRule(ConstrainedTerm constrainedTerm, org.kframework.backend.java.kil.Rule rule) {
        return computeRewriteStepByRule(constrainedTerm, rule);
    }
}
