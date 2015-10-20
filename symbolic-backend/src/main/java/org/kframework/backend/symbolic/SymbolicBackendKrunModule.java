package org.kframework.backend.symbolic;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.kframework.krun.tools.Executor;
import org.kframework.utils.inject.Options;

/**
 * Created by andrei on 10/20/15.
 */
public class SymbolicBackendKrunModule extends AbstractModule {
    SymbolicOptions options = new SymbolicOptions();

    @Override
    protected void configure() {
        // bind appropriate class to instance
        bind(SymbolicOptions.class).toInstance(options);

        // bind options to be parsed and accessible to the user
        Multibinder<Object> optionsBinder = Multibinder.newSetBinder(binder(), Object.class, Options.class);
        optionsBinder.addBinding().toInstance(options);

        // add options as experimental
        Multibinder<Class<?>> experimentalOptionsBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<Class<?>>() {}, Options.class);
        experimentalOptionsBinder.addBinding().toInstance(SymbolicOptions.class);
    }
}
