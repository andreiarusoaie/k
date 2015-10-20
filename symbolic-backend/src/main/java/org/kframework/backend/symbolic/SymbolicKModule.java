package org.kframework.backend.symbolic;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import org.kframework.main.KModule;

import java.util.List;

/**
 * Created by Andrei on 10/19/15.
 */
public class SymbolicKModule implements KModule {

    @Override
    public List<Module> getKDocModules() {
        return ImmutableList.of();
    }

    @Override
    public List<Module> getKompileModules() {
        return ImmutableList.<Module>of(new SymbolicBackendKompileModule());
    }

    @Override
    public List<Module> getKastModules() {
        return ImmutableList.of();
    }

    @Override
    public List<Module> getDefinitionSpecificKRunModules() {
        return ImmutableList.of(new SymbolicDefinitionModule());
    }

    @Override
    public List<Module> getKTestModules() {
        return ImmutableList.of();
    }

    @Override
    public List<Module> getKRunModules(List<Module> definitionSpecificModules) {
        return ImmutableList.of();
    }
}
