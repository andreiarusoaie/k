package org.kframework.backend.abstracT;

import org.kframework.main.KModule;

import java.util.List;

import com.google.inject.Module;

/**
 * Created by Andrei on 7/12/2015.
 */
public class AbstractBackendModule implements KModule {
    @Override
    public List<Module> getKDocModules() {
        return null;
    }

    @Override
    public List<Module> getKompileModules() {
        return null;
    }

    @Override
    public List<Module> getKastModules() {
        return null;
    }

    @Override
    public List<Module> getDefinitionSpecificKRunModules() {
        return null;
    }

    @Override
    public List<Module> getKTestModules() {
        return null;
    }

    @Override
    public List<Module> getKRunModules(List<Module> definitionSpecificModules) {
        return null;
    }
}
