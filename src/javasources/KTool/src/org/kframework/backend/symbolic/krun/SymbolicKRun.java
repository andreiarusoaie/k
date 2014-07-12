package org.kframework.backend.symbolic.krun;

import com.microsoft.z3.*;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.kframework.backend.java.util.Z3Wrapper;
import org.kframework.backend.maude.krun.MaudeKRun;
import org.kframework.compile.utils.RuleCompilerSteps;
import org.kframework.kil.*;
import org.kframework.kil.loader.Context;
import org.kframework.krun.K;
import org.kframework.krun.KRunExecutionException;
import org.kframework.krun.api.*;
import org.kframework.parser.DefinitionLoader;
import org.kframework.parser.concrete.disambiguate.CollectVariablesVisitor;
import org.kframework.utils.Stopwatch;

import java.util.*;
import java.util.List;
import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Created by andreiarusoaie on 03/05/14.
 */
public class SymbolicKRun extends MaudeKRun {

    private String pathConditionVar = "P:K";
    private String bagVar = "B:Bag";
    private String symbolicPattern = "<generatedTop> <path-condition> " + pathConditionVar + " </path-condition> " + bagVar + " </generatedTop> [anywhere]";
    private Rule defaultSymbolicPattern;
    private RuleCompilerSteps defaultSymbolicPatternInfo;
    private int stateCounter;

    public SymbolicKRun(Context context, Stopwatch sw) {
        super(context, sw);
        initialiseSearchParams();
        stateCounter = 0;
    }

    @Override
    public KRunResult<KRunState> run(Term cfg) throws KRunExecutionException {
        while (true) {
            List<SearchResult> solutions = stepAll(cfg);

            // step: return cfg if no solutions found
            if (solutions.isEmpty())
                return new KRunResult<KRunState>(new KRunState(cfg, context));

            // step: choose ONE solution and continue
            SearchResult solution = solutions.get(0);
            cfg = solution.getState().getRawResult();
        }
    }

    // TODO: finish this
    @Override
    public KRunResult<SearchResults> search(Integer bound, Integer depth, SearchType searchType, Rule pattern, Term cfg, RuleCompilerSteps compilationInfo) throws KRunExecutionException {

        // hack: set default values for depth and bound and Integer.MAX_VALUE if null
        depth = depth == null ? Integer.MAX_VALUE : depth;
        bound = bound == null ? Integer.MAX_VALUE : bound;


        List<SearchResult> finalConfigurations = new LinkedList<SearchResult>();
        List<SearchResult> intermediateConfigurations = new LinkedList<SearchResult>();
        List<SearchResult> tempConfigurations = new LinkedList<SearchResult>();

        // start the search from the initial configuration
        KRunState state = new KRunState(cfg, stateCounter, context);
        Map<String, Term> rawSubstitution = new HashMap<String, Term>();
        intermediateConfigurations.add(new SearchResult(state, rawSubstitution, defaultSymbolicPatternInfo, context));

        long s = System.currentTimeMillis();

        while ((depth > 0 || depth == null) && (finalConfigurations.size() < bound || bound == null) && !intermediateConfigurations.isEmpty()) {
            // one step for all intermediate configurations
            tempConfigurations.clear();
            for (SearchResult c : intermediateConfigurations) {
                List<SearchResult> results = stepAll(c.getState().getRawResult());
                // if c is final then collect it, otherwise prepare for the next search
                if (results.isEmpty()) {
                    finalConfigurations.add(c);
                    System.out.println("Found sol number " + finalConfigurations.size() + " in " + (System.currentTimeMillis() - s) + " millis.");
                    s = System.currentTimeMillis();
                }
                else {
                    tempConfigurations.addAll(results);
                }
            }
            intermediateConfigurations.clear();
            intermediateConfigurations.addAll(tempConfigurations);
            depth--;
        }

        List<SearchResult> resultsF = new LinkedList<SearchResult>();
        resultsF.addAll(finalConfigurations);
        SearchResults res = new SearchResults(resultsF, null, false, context);

        return new KRunResult<SearchResults>(res);
    }

    /**
     * Run the given configuration one step and return the list of results.
     * @param cfg
     * @return
     * @throws KRunExecutionException
     */
    private List<SearchResult> stepAll(Term cfg) throws KRunExecutionException {
        // step: search all solutions with depth 1
        KRunResult<SearchResults> results = super.search(null, 1, SearchType.PLUS, defaultSymbolicPattern, cfg, defaultSymbolicPatternInfo);

        // step: get solutions
        List<SearchResult> solutions = results.getResult().getSolutions();

        // step: filter feasible path conditions
        return filterFeasibleSolutions(solutions);
    }

    private List<SearchResult> stepAll(List<SearchResult> configurations) throws KRunExecutionException {
        List<SearchResult> all = new ArrayList<SearchResult>();
        for (SearchResult configuration : configurations) {
            all.addAll(stepAll(configuration.getState().getRawResult()));
        }
        return all;
    }

    /**
     * Initialise krun parameters, i.e. default pattern for search and pattern info.
     */
    private void initialiseSearchParams() {
        try {
            org.kframework.parser.concrete.KParser.ImportTblRule(new File(K.compiled_def));
//            org.kframework.parser.concrete.KParser.ImportTbl(K.compiled_def + "/def/Concrete.tbl");
            ASTNode pattern = DefinitionLoader.parsePattern(
                    symbolicPattern,
                    "Default symbolic pattern",
                    org.kframework.kil.KSorts.BAG,
                    context);
            CollectVariablesVisitor vars = new CollectVariablesVisitor(context);
            vars.visitNode(pattern);
            defaultSymbolicPatternInfo = new RuleCompilerSteps(K.definition, context);
            pattern = defaultSymbolicPatternInfo.compile(new Rule((Sentence) pattern), null);

            defaultSymbolicPattern = (Rule) pattern;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter the feasible solutions. A solution is feasible if the path condition
     * along it is not unsatisfiable.
     *
     * @param solutions is a list of search results
     * @return the list of feasible solutions
     */
    private List<SearchResult> filterFeasibleSolutions(List<SearchResult> solutions) {

        // collect unfeasible solutions
        List<SearchResult> unfeasibleSolutions = new ArrayList<SearchResult>();
        for (SearchResult solution : solutions) {
            // get path condition from substitution
            Term pathCondition = solution.getSubstitution().get(pathConditionVar);

            // translate to SMTLIB
            KILToSMTLib smtLib = new KILToSMTLib(context);
            smtLib.visitNode(pathCondition);

            // initialize the SMT solver and collect solution if unfeasible
            try {
                com.microsoft.z3.Context context = Z3Wrapper.newContext();
                Solver solver = context.MkSolver();
                solver.Assert(context.ParseSMTLIB2String(smtLib.getSmtlibQuery(), null, null, null, null));
                if (solver.Check() == Status.UNSATISFIABLE) {
                    unfeasibleSolutions.add(solution);
                }
            } catch (Z3Exception e) {
                e.printStackTrace();
            }
        }

        // remove unfeasible solutions
        for (SearchResult unfeasibleSolution : unfeasibleSolutions) {
            solutions.remove(unfeasibleSolution);
        }

        return solutions;
    }
}
