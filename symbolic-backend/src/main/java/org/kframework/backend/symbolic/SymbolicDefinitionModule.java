package org.kframework.backend.symbolic;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import org.kframework.backend.rewriter.SymbolicRewriter;
import org.kframework.definition.Module;
import org.kframework.krun.tools.Executor;
import org.kframework.rewriter.Rewriter;

import java.util.function.Function;

/**
 * Created by andrei on 10/20/15.
 */
public class SymbolicDefinitionModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder<String, Function<Module, Rewriter>> rewriterBinder = MapBinder.newMapBinder(
                binder(), TypeLiteral.get(String.class), new TypeLiteral<Function<org.kframework.definition.Module, Rewriter>>() {
                });
        rewriterBinder.addBinding("symbolic").to(SymbolicExecutor.class);
    }
}
