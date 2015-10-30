package org.kframework.backend.rewriter;

import com.google.inject.Inject;
import org.kframework.backend.java.kil.ConstrainedTerm;
import org.kframework.backend.java.kil.Definition;
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
        List<ConstrainedTerm> terms = null;
        ConstrainedTerm term = constrainedTerm;
        int bound  = 100;
        while (!transition && bound > 0) {
            terms = computeRewriteStep(term, 1, true);
            term = terms.get(0);
            bound--;
        }

        return terms;
    }

    public List<ConstrainedTerm> oneStepWithRule(ConstrainedTerm constrainedTerm, org.kframework.backend.java.kil.Rule rule) {
        return computeRewriteStepByRule(constrainedTerm, rule);
    }
}
