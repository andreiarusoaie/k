package org.kframework.backend.abstracT.backend;

import com.beust.jcommander.Parameter;
import org.kframework.utils.inject.RequestScoped;

/**
 * Created by Andrei on 14.07.2015.
 */
@RequestScoped
public final class AbstractOptions {
    @Parameter(names="--goals", description="Build the abstract graph using the specification file given as argument", required=false)
    public String goals = null;

    @Parameter(names="--output-log", description="Save graph building logs into file", required=false)
    public String outputLog = "proof-log.txt";

    @Parameter(names="--window-display", description = "Display a graphical representation of the abstract graph in a new window", required = false)
    public Boolean window = false;

    @Parameter(names="--expand-depth", description = "Maximum depth for expand", required = false)
    public Integer maxExpandDepth = 10;

    @Parameter(names="--construct-depth", description = "Maximum depth for construct", required = false)
    public Integer maxConstructDepth = 10;

    @Parameter(names="--export-image", description="Save graph building logs into file", required=false)
    public String exportFile = null;
}
