package org.kframework.backend.symbolic;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import org.kframework.krun.tools.Executor;

/**
 * Created by Andrei on 10/20/15.
 */
public class SymbolicDefinitionModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder<String, Executor> rewriterBinder = MapBinder.newMapBinder(binder(), String.class, Executor.class);
        rewriterBinder.addBinding("symbolic").to(SymbolicExecutor.class);
    }
}
