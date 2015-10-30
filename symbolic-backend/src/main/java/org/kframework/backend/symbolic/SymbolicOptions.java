package org.kframework.backend.symbolic;

import com.beust.jcommander.Parameter;

/**
 * Created by Andrei on 10/19/15.
 */
public class SymbolicOptions {
    @Parameter(names="--goals", description="Proves the goals from the specification (XML) file given as argument", required=false)
    public String goals = null;

    @Parameter(names="--max-depth", description = "Maximum depth for symbolic execution (the default is 100 steps)", required = false)
    public Integer maxDepth = 100;

}
