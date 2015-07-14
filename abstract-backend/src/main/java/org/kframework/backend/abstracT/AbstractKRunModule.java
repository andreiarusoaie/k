package org.kframework.backend.abstracT;

import com.beust.jcommander.JCommander;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.kframework.Rewriter;
import org.kframework.backend.java.kil.Definition;
import org.kframework.backend.java.kil.GlobalContext;
import org.kframework.backend.java.ksimulation.Simulator;
import org.kframework.backend.java.symbolic.FreshRules;
import org.kframework.backend.java.symbolic.InitializeRewriter;
import org.kframework.backend.java.symbolic.JavaSymbolicCommonModule;
import org.kframework.backend.java.symbolic.JavaSymbolicExecutor;
import org.kframework.backend.java.symbolic.Stage;
import org.kframework.backend.java.symbolic.SymbolicRewriter;
import org.kframework.definition.Module;
import org.kframework.kil.loader.Context;
import org.kframework.krun.KRunOptions;
import org.kframework.krun.api.KRunResult;
import org.kframework.krun.tools.Executor;
import org.kframework.main.AnnotatedByDefinitionModule;
import org.kframework.transformation.ToolActivation;
import org.kframework.transformation.Transformation;
import org.kframework.utils.BinaryLoader;
import org.kframework.utils.Stopwatch;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.file.FileUtil;
import org.kframework.utils.inject.Annotations;
import org.kframework.utils.inject.DefinitionScoped;
import org.kframework.utils.inject.Main;
import org.kframework.utils.inject.Options;
import org.kframework.utils.inject.RequestScoped;
import org.kframework.utils.inject.Spec;

import java.util.List;
import java.util.function.Function;

/**
 * Created by andrei on 14.07.2015.
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
