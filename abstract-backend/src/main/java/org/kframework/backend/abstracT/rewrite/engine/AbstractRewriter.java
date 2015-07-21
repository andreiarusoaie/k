package org.kframework.backend.abstracT.rewrite.engine;

import org.kframework.backend.java.kil.ConstrainedTerm;
import org.kframework.backend.java.kil.GlobalContext;
import org.kframework.backend.java.kil.TermContext;
import org.kframework.backend.java.symbolic.KILtoBackendJavaKILTransformer;
import org.kframework.backend.java.symbolic.SymbolicRewriter;
import org.kframework.kil.Rule;
import org.kframework.kil.Term;
import org.kframework.krun.KRunExecutionException;
import org.kframework.krun.api.SearchType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by andrei on 18/07/15.
 */
public class AbstractRewriter {

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

        // custom parameters for search
        Integer bound = -1;
        Integer depth = 1;
        boolean computeGraph = false;

        // pattern rule
        org.kframework.backend.java.kil.Rule patternRule = preparePatternRule(pattern, context, transformer);

        // prepare context for rewrite engine
        TermContext termContext = TermContext.of(globalContext);

        // target term unavailable for now in SymbolicRewriter
        org.kframework.backend.java.kil.Term targetTerm = null;

        // call search - one step
        rewriter.search(constrainedTerm, targetTerm, claims, patternRule, bound, depth, SearchType.PLUS, termContext, computeGraph);

        return rewriter.getResults();
//            for (Map<Variable, org.kframework.backend.java.kil.Term> map : hits) {
//                // Construct substitution map from the search results
//                Map<String, org.kframework.kil.Term> substitutionMap =
//                        new HashMap<String, Term>();
//                for (Variable var : map.keySet()) {
//                    org.kframework.kil.Term kilTerm =
//                            (org.kframework.kil.Term) map.get(var).accept(
//                                    new BackendJavaKILtoKILTransformer(getContext()));
//                    substitutionMap.put(var.name(), kilTerm);
//                }
//
//                // Apply the substitution to the pattern
//                org.kframework.kil.Term rawResult =
//                        (org.kframework.kil.Term) new SubstitutionFilter(substitutionMap, getContext())
//                                .visitNode(pattern.getBody());
//
//                searchResults.add(new SearchResult(
//                        new JavaKRunState(rawResult, getCounter()),
//                        substitutionMap,
//                        compilationInfo));
//            }
//
//        }
    }

    private static org.kframework.backend.java.kil.Rule preparePatternRule(Rule pattern, org.kframework.kil.loader.Context context, KILtoBackendJavaKILTransformer transformer) {
        // The pattern needs to be a rewrite in order for the transformer to be
        // able to handle it, so we need to give it a right-hand-side.
        org.kframework.kil.Cell c = new org.kframework.kil.Cell();
        c.setLabel("generatedTop");
        c.setContents(new org.kframework.kil.Bag());
        pattern.setBody(new org.kframework.kil.Rewrite(pattern.getBody(), c, context));
        return transformer.transformAndEval(pattern);
    }

}
