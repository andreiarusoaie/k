package org.kframework.backend.abstracT;


import com.google.inject.Inject;
import org.kframework.backend.Backends;
import org.kframework.kil.Definition;
import org.kframework.kil.loader.Context;
import org.kframework.kompile.KompileOptions;
import org.kframework.utils.Stopwatch;
import org.kframework.backend.BasicBackend;

/**
 * Created by Andrei on 7/12/2015.
 */
public class AbstractBackend extends BasicBackend {

    @Inject
    public AbstractBackend(Stopwatch sw, Context context, KompileOptions options) {
        super(sw, context, options);
    }

    @Override
    public void run(Definition definition) {

    }

    @Override
    public String getDefaultStep() {
        return null;
    }

    @Override
    public boolean generatesDefinition() {
        return true;
    }

    @Override
    public String autoincludedFile() {
        return Backends.AUTOINCLUDE_JAVA;
    }

}
