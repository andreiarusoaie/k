package org.kframework.backend.abstracT.backend;

import com.beust.jcommander.Parameter;
import org.kframework.utils.inject.RequestScoped;

/**
 * Created by Andrei on 14.07.2015.
 */
@RequestScoped
public final class AbstractOptions {
    @Parameter(names="--abstract-graph", description="Build the abstract graph using the specification file given as argument", required=true)
    public String abstractGraph = null;

}
