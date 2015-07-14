package org.kframework.backend.abstracT;

import com.beust.jcommander.Parameter;
import org.kframework.utils.inject.RequestScoped;

/**
 * Created by andrei on 14.07.2015.
 */
@RequestScoped
public final class AbstractOptions {
    @Parameter(names="--verify", description="Perform verification of the RL formulas from the file given as argument")
    public String RLFileName = null;

}
