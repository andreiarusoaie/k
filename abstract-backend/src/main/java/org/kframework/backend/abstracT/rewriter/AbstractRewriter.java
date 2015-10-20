package org.kframework.backend.abstracT.rewriter;

import org.kframework.backend.java.kil.ConstrainedTerm;
import org.kframework.backend.java.symbolic.SymbolicRewriter;
import org.kframework.krun.KRunExecutionException;

import java.util.List;

/**
 * Created by Andrei on 18/07/15.
 * Acts like a wrapper for {@link org.kframework.backend.java.symbolic.SymbolicRewriter},
 * where the rewriter and the execution context(s) are known; the class is meant to
 * contain methods which get a {@link ConstrainedTerm} and return one or more such terms
 * after symbolic execution of the parameter.
 *
 */
public class AbstractRewriter {

    /**
     * Computes a list of {@link ConstrainedTerm} after one symbolic execution step
     * @param constrainedTerm is the initial term to be executed
     * @param rewriter is a {@link SymbolicRewriter} instance
     * @return a list of {@link ConstrainedTerm}
     * @throws KRunExecutionException
     */
    public static List<ConstrainedTerm> oneSearchStep(ConstrainedTerm constrainedTerm, SymbolicRewriter rewriter ) throws KRunExecutionException {
        List<ConstrainedTerm> terms = null;
        ConstrainedTerm term = constrainedTerm;
        while (!rewriter.transition) {
            terms = rewriter.computeRewriteStep(term, 1, true);
            term = terms.get(0);
        }

        return terms;
    }

}
