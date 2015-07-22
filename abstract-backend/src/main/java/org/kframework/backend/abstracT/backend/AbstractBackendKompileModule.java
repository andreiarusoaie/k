package org.kframework.backend.abstracT.backend;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import org.kframework.backend.Backend;
import org.kframework.krun.api.io.FileSystem;
import org.kframework.krun.ioserver.filesystem.portable.PortableFileSystem;

/**
 * Created by Andrei on 7/12/2015.
 */
public class AbstractBackendKompileModule extends AbstractModule{



    @Override
    protected void configure() {
        bind(FileSystem.class).to(PortableFileSystem.class);

        MapBinder<String, Backend> mapBinder = MapBinder.newMapBinder(
                binder(), String.class, Backend.class);
        mapBinder.addBinding("abstract").to(AbstractBackend.class);
    }

}


