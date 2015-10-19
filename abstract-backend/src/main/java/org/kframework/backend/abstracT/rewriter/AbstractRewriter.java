package org.kframework.backend.abstracT.rewriter;

import org.kframework.backend.java.kil.ConstrainedTerm;
import org.kframework.backend.java.kil.GlobalContext;
import org.kframework.backend.java.kil.TermContext;
import org.kframework.backend.java.symbolic.KILtoBackendJavaKILTransformer;
import org.kframework.backend.java.symbolic.SymbolicRewriter;
import org.kframework.kil.Rule;
import org.kframework.krun.KRunExecutionException;
import org.kframework.krun.api.SearchType;

import java.util.ArrayList;
import java.util.Collections;
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
     * @param pattern represents the rewriter's search pattern
     * @param globalContext the global context
     * @param rewriter is a {@link SymbolicRewriter} instance
     * @param context the local context
     * @param transformer the {@link KILtoBackendJavaKILTransformer} required by the rewriter
     * @return a list of {@link ConstrainedTerm}
     * @throws KRunExecutionException
     */
    public static List<ConstrainedTerm> oneSearchStep(
            ConstrainedTerm constrainedTerm,
            Rule pattern,
            GlobalContext globalContext,
            SymbolicRewriter rewriter,
            org.kframework.kil.loader.Context context,
            KILtoBackendJavaKILTransformer transformer
            ) throws KRunExecutionException {

        // empty claims
        List<org.kframework.backend.java.kil.Rule> claims = Collections.emptyList();

        // custom parameters for search; -1 for bound means that there is no bound limit
        Integer bound = -1;
        Integer depth = 1;
        // do not ask for the execution graph
        boolean computeGraph = false;

        // pass pattern as a rule for the engine
        org.kframework.backend.java.kil.Rule patternRule = preparePatternRule(pattern, context, transformer);

        // prepare term context
        TermContext termContext = TermContext.of(globalContext);

        // target term unavailable for now in SymbolicRewriter
        org.kframework.backend.java.kil.Term targetTerm = null;

        // call search - one step
        rewriter.search(constrainedTerm, targetTerm, claims, patternRule, bound, depth, SearchType.PLUS, termContext, computeGraph);

        return rewriter.getResults();
    }

    // The pattern needs to be a rewrite in order for the transformer to be
    // able to handle it, so we need to give it a right-hand-side.
    private static org.kframework.backend.java.kil.Rule preparePatternRule(Rule pattern, org.kframework.kil.loader.Context context, KILtoBackendJavaKILTransformer transformer) {
        org.kframework.kil.Cell c = new org.kframework.kil.Cell();
        c.setLabel("generatedTop");
        c.setContents(new org.kframework.kil.Bag());
        pattern.setBody(new org.kframework.kil.Rewrite(pattern.getBody(), c, context));
        return transformer.transformAndEval(pattern);
    }

    /**
     * Apply a given rule to a ConstrainedTerm
     * @param rewriter the symbolic rewriter
     * @param term the constrained term
     * @param rule the rule to be applied
     * @return the term after rule application
     */
    public static ConstrainedTerm applyRule(SymbolicRewriter rewriter, ConstrainedTerm term, org.kframework.backend.java.kil.Rule rule) {
        List<org.kframework.backend.java.kil.Rule> rules = new ArrayList<>();
        rules.add(rule);
        return rewriter.applyRule(term, rules);
    }
}
