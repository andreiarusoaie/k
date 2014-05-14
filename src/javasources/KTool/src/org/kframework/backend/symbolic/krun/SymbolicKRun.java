package org.kframework.backend.symbolic.krun;

import com.microsoft.z3.*;
import com.microsoft.z3.Sort;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.kframework.backend.java.util.Z3Wrapper;
import org.kframework.backend.maude.krun.MaudeKRun;
import org.kframework.backend.symbolic.SymbolicBackend;
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
import java.util.Map;

/**
 * Created by andreiarusoaie on 03/05/14.
 */
public class SymbolicKRun extends MaudeKRun {

    private String pathConditionVar = "P:K";
    private String symbolicPattern = "<generatedTop> <path-condition> " + pathConditionVar + " </path-condition> B:Bag </generatedTop> [anywhere]";
    private Rule defaultSymbolicPattern;
    private RuleCompilerSteps defaultSymbolicPatternInfo;


    public SymbolicKRun(Context context, Stopwatch sw) {
        super(context, sw);
        initialiseSearchParams();
    }

    @Override
    public KRunResult<KRunState> run(Term cfg) throws KRunExecutionException {

        while (true) {
            // (missing) step: filter (feasible) path conditions
            List<SearchResult> solutions = stepAll(cfg);

            // step: exit when the solutions list is empty
            if (solutions.isEmpty())
                return new KRunResult<>(new KRunState(cfg, context));

            // step: choose some solution and continue
            SearchResult solution = solutions.get(0);
            cfg = solution.getState().getRawResult();
        }
    }

    @Override
    public KRunResult<SearchResults> search(Integer bound, Integer depth, SearchType searchType, Rule pattern, Term cfg, RuleCompilerSteps compilationInfo) throws KRunExecutionException {

        int stateCounter = 0;
        DirectedGraph<KRunState, Transition> graph = new DirectedSparseGraph<>();





    }

    private List<SearchResult> stepAll(Term cfg) throws KRunExecutionException {
        // step: search all solutions with depth 1
        KRunResult<SearchResults> results = super.search(null, 1, SearchType.PLUS, defaultSymbolicPattern, cfg, defaultSymbolicPatternInfo);

        // step: get solutions
        List<SearchResult> solutions = results.getResult().getSolutions();

        // step: filter feasible path conditions
        return filterFeasibleConditions(solutions);
    }

    private List<SearchResult> stepAll(List<SearchResult> configurations) throws KRunExecutionException {
        List<SearchResult> all = new ArrayList<>();
        for (SearchResult configuration : configurations) {
            all.addAll(stepAll(configuration.getState().getRawResult()));
        }
        return all;
    }

    private void initialiseSearchParams() {
        try {
            org.kframework.parser.concrete.KParser.ImportTbl(K.compiled_def + "/def/Concrete.tbl");
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
    private List<SearchResult> filterFeasibleConditions(List<SearchResult> solutions) {

        // collect unfeasible solutions
        List<SearchResult> unfeasibleSolutions = new ArrayList<>();
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
