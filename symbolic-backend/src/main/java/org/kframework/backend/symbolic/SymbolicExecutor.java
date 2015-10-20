package org.kframework.backend.symbolic;

import org.kframework.backend.rewriter.SymbolicRewriter;
import org.kframework.compile.utils.RuleCompilerSteps;
import org.kframework.definition.Module;
import org.kframework.kil.Rule;
import org.kframework.kil.Term;
import org.kframework.krun.KRunExecutionException;
import org.kframework.krun.api.RewriteRelation;
import org.kframework.krun.api.SearchResults;
import org.kframework.krun.tools.Executor;
import org.kframework.rewriter.Rewriter;
import org.kframework.rewriter.SearchType;

import java.util.function.Function;

/**
 * Created by Andrei on 10/20/15.
 */
public class SymbolicExecutor implements Function<Module, Rewriter> {
    @Override
    public Rewriter apply(Module module) {
        return new SymbolicRewriter();
    }
}
