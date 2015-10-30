package org.kframework.backend.symbolic;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import org.kframework.krun.api.io.FileSystem;
import org.kframework.krun.ioserver.filesystem.portable.PortableFileSystem;

/**
 * Created by Andrei on 10/19/15.
 */
public class SymbolicBackendKompileModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FileSystem.class).to(PortableFileSystem.class);

        MapBinder<String, org.kframework.backend.Backend> mapBinder = MapBinder.newMapBinder(
                binder(), String.class, org.kframework.backend.Backend.class);
        mapBinder.addBinding("symbolic").to(SymbolicBackend.class);
    }
}
