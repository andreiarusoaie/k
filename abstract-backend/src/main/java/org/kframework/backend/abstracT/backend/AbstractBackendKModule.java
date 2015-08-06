package org.kframework.backend.abstracT.backend;

import com.google.common.collect.ImmutableList;
import org.kframework.main.KModule;

import java.util.List;

import com.google.inject.Module;

/**
 * Created by Andrei on 7/12/2015.
 */
public class AbstractBackendKModule implements KModule {
    @Override
    public List<Module> getKDocModules() {
        return ImmutableList.of();
    }

    @Override
    public List<Module> getKompileModules() {
        return ImmutableList.<Module>of(new AbstractBackendKompileModule());
    }

    @Override
    public List<Module> getKastModules() {
        return ImmutableList.of();
    }

    @Override
    public List<Module> getDefinitionSpecificKRunModules() {
        return ImmutableList.of(new AbstractDefinitionModule());
    }

    @Override
    public List<Module> getKTestModules() {
        return ImmutableList.of();
    }

    @Override
    public List<Module> getKRunModules(List<Module> definitionSpecificModules) {
        return ImmutableList.of(new AbstractKRunModule());
    }
}
