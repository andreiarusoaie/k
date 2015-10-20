package org.kframework.backend.symbolic;

import com.google.inject.Inject;
import org.kframework.definition.Definition;
import org.kframework.kompile.CompiledDefinition;
import org.kframework.kompile.Kompile;
import org.kframework.kompile.KompileOptions;
import org.kframework.kore.compile.Backend;
import org.kframework.main.GlobalOptions;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.file.FileUtil;

import java.util.function.Function;

/**
 * Created by Andrei on 10/19/15.
 */
public class SymbolicBackend implements Backend{
    private final KExceptionManager kem;
    private final FileUtil files;
    private final GlobalOptions globalOptions;
    private final KompileOptions kompileOptions;
    private final SymbolicOptions options;

    @Inject
    public SymbolicBackend(KExceptionManager kem, FileUtil files, GlobalOptions globalOptions, KompileOptions kompileOptions, SymbolicOptions options) {
        this.kem = kem;
        this.files = files;
        this.globalOptions = globalOptions;
        this.kompileOptions = kompileOptions;
        this.options = options;
    }

    @Override
    public void accept(CompiledDefinition def) { }

    @Override
    public Function<Definition, Definition> steps(Kompile kompile) {
        return kompile.defaultSteps();
    }
}
