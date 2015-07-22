package org.kframework.backend.abstracT.backend;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import org.kframework.krun.tools.Executor;

/**
 * Created by Andrei on 14.07.2015.
 */
public class AbstractDefinitionModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder<String, Executor> executorBinder = MapBinder.newMapBinder(
                binder(), String.class, Executor.class);
        executorBinder.addBinding("abstract").to(AbstractExecutor.class);
    }
}
