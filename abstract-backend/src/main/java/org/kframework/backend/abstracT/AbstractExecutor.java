package org.kframework.backend.abstracT;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.kframework.backend.java.kil.Definition;
import org.kframework.backend.java.kil.GlobalContext;
import org.kframework.backend.java.symbolic.JavaExecutionOptions;
import org.kframework.backend.java.symbolic.JavaSymbolicExecutor;
import org.kframework.backend.java.symbolic.KILtoBackendJavaKILTransformer;
import org.kframework.backend.java.symbolic.PatternMatchRewriter;
import org.kframework.backend.java.symbolic.SymbolicRewriter;
import org.kframework.krun.api.KRunState;
import org.kframework.utils.Stopwatch;

/**
 * Created by andrei on 14.07.2015.
 */
public class AbstractExecutor extends JavaSymbolicExecutor {

    @Inject
    AbstractExecutor(org.kframework.kil.loader.Context context, JavaExecutionOptions javaOptions, KILtoBackendJavaKILTransformer kilTransformer, GlobalContext globalContext, Provider<SymbolicRewriter> symbolicRewriter, Provider<PatternMatchRewriter> patternMatchRewriter, KILtoBackendJavaKILTransformer transformer, Definition definition, KRunState.Counter counter, Stopwatch sw) {
        super(context, javaOptions, kilTransformer, globalContext, symbolicRewriter, patternMatchRewriter, transformer, definition, counter, sw);
    }


}
