package org.kframework.backend.symbolic.krun;

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

import java.util.ArrayList;
import java.util.List;

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
    }

    @Override
    public KRunResult<KRunState> run(Term cfg) throws KRunExecutionException {

        initialiseSearchParams();
        while (true) {
            // step: search all solutions with depth 1
            KRunResult<SearchResults> results = this.search(null, 1, SearchType.PLUS, defaultSymbolicPattern, cfg, defaultSymbolicPatternInfo);

            // step: get solutions
            List<SearchResult> solutions = results.getResult().getSolutions();

            // (missing) step: filter (feasible) path conditions
            solutions = filterFeasibleConditions(solutions);

            // step: exit when the solutions list is empty
            if (solutions.isEmpty())
                return new KRunResult<>(new KRunState(cfg, context));

            // step: choose some solution and continue
            SearchResult solution = solutions.get(0);
            cfg = solution.getState().getRawResult();
        }
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
            pattern.accept(vars);
            defaultSymbolicPatternInfo = new RuleCompilerSteps(K.definition, context);
            pattern = defaultSymbolicPatternInfo.compile(new Rule((Sentence) pattern), null);

            defaultSymbolicPattern = (Rule) pattern;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<SearchResult> filterFeasibleConditions(List<SearchResult> solutions) {

        List<SearchResult> unfeasibleSolutions = new ArrayList<>();

        for (SearchResult solution : solutions) {
            Term pathCondition = solution.getSubstitution().get(pathConditionVar);
            if (pathCondition.equals(BoolBuiltin.FALSE)) {
                unfeasibleSolutions.add(solution);
            }
        }

        for (SearchResult unfeasible : unfeasibleSolutions) {
            solutions.remove(unfeasible);
        }

        return solutions;
    }
}
