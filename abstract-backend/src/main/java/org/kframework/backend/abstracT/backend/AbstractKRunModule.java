package org.kframework.backend.abstracT.backend;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.kframework.utils.inject.Options;

/**
 * Created by Andrei on 14.07.2015.
 */
public class AbstractKRunModule extends AbstractModule {
    AbstractOptions options = new AbstractOptions();

    @Override
    protected void configure() {

        // bind appropriate class to instance
        bind(AbstractOptions.class).toInstance(options);

        // bind options to be parsed and accessible to the user
        Multibinder<Object> optionsBinder = Multibinder.newSetBinder(binder(), Object.class, Options.class);
        optionsBinder.addBinding().toInstance(options);

        // add options as experimental
        Multibinder<Class<?>> experimentalOptionsBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<Class<?>>() {}, Options.class);
        experimentalOptionsBinder.addBinding().toInstance(AbstractOptions.class);
    }
}
