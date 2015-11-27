package org.kframework.backend.symbolic;

import com.beust.jcommander.Parameter;

/**
 * Created by Andrei on 10/19/15.
 */
public class SymbolicOptions {
    @Parameter(names="--goals", description="Proves the goals from the specification (XML) file given as argument", required=false)
    public String goals = null;

    @Parameter(names="--max-steps", description = "The maximum number of prover steps (the default is 100)", required = false)
    public Integer maxSteps = 100;

    @Parameter(names="--output-log", description="Save graph building logs into file", required=false)
    public String outputLog = "proof-log.txt";

}
