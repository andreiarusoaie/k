package org.kframework.backend.abstracT;

import com.google.inject.Inject;
import com.google.inject.Provider;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import org.kframework.backend.java.kil.Definition;
import org.kframework.backend.java.kil.GlobalContext;
import org.kframework.backend.java.symbolic.JavaExecutionOptions;
import org.kframework.backend.java.symbolic.JavaSymbolicExecutor;
import org.kframework.backend.java.symbolic.KILtoBackendJavaKILTransformer;
import org.kframework.backend.java.symbolic.PatternMatchRewriter;
import org.kframework.backend.java.symbolic.SymbolicRewriter;
import org.kframework.krun.KRunExecutionException;
import org.kframework.krun.api.KRunGraph;
import org.kframework.krun.api.KRunState;
import org.kframework.krun.api.RewriteRelation;
import org.kframework.utils.Stopwatch;

import javax.swing.*;
import java.awt.*;

/**
 * Created by andrei on 14.07.2015.
 */
public class AbstractExecutor extends JavaSymbolicExecutor {

    private final AbstractOptions abstractOptions;

    @Inject
    AbstractExecutor(org.kframework.kil.loader.Context context, JavaExecutionOptions javaOptions, KILtoBackendJavaKILTransformer kilTransformer, GlobalContext globalContext, Provider<SymbolicRewriter> symbolicRewriter, Provider<PatternMatchRewriter> patternMatchRewriter, KILtoBackendJavaKILTransformer transformer, Definition definition, KRunState.Counter counter, Stopwatch sw, AbstractOptions abstractOptions) {
        super(context, javaOptions, kilTransformer, globalContext, symbolicRewriter, patternMatchRewriter, transformer, definition, counter, sw);
        this.abstractOptions = abstractOptions;
    }

    @Override
    public RewriteRelation run(org.kframework.kil.Term cfg, boolean computeGraph) throws KRunExecutionException {
        RewriteRelation rewriteRelation = javaRewriteEngineRun(cfg, -1, true);
        KRunGraph graph = rewriteRelation.getExecutionGraph().get();

        Layout<KRunState, String> layout = new CircleLayout(graph);
        layout.setSize(new Dimension(1000,1000)); // sets the initial size of the space
        // The BasicVisualizationServer<V,E> is parameterized by the edge types
        BasicVisualizationServer<KRunState,String> vv =
                new BasicVisualizationServer<KRunState,String>(layout);
        vv.setPreferredSize(new Dimension(1050,1050)); //Sets the viewing area size

        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);

        return rewriteRelation;
    }

}
