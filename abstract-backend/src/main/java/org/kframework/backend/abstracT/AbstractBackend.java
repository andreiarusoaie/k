package org.kframework.backend.abstracT;


import org.kframework.kil.Definition;
import org.kframework.kil.loader.Context;
import org.kframework.kompile.KompileOptions;
import org.kframework.utils.Stopwatch;

/**
 * Created by Andrei on 7/12/2015.
 */
public class AbstractBackend extends org.kframework.backend.BasicBackend {

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
        return false;
    }
}
