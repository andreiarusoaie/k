package org.kframework.backend.symbolic;

import com.google.inject.Inject;
import com.google.inject.Provider;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.kframework.backend.java.kil.ConstrainedTerm;
import org.kframework.backend.java.kil.Definition;
import org.kframework.backend.java.kil.GlobalContext;
import org.kframework.backend.java.kil.TermContext;
import org.kframework.backend.java.kil.Variable;
import org.kframework.backend.java.symbolic.BackendJavaKILtoKILTransformer;
import org.kframework.backend.java.symbolic.ConjunctiveFormula;
import org.kframework.backend.java.symbolic.CopyOnWriteTransformer;
import org.kframework.backend.java.symbolic.Equality;
import org.kframework.backend.java.symbolic.JavaExecutionOptions;
import org.kframework.backend.java.symbolic.KILtoBackendJavaKILTransformer;
import org.kframework.backend.java.symbolic.PatternMatchRewriter;
import org.kframework.backend.java.symbolic.PersistentUniqueList;
import org.kframework.backend.java.symbolic.Substitution;
import org.kframework.backend.java.util.JavaKRunState;
import org.kframework.backend.logger.Logger;
import org.kframework.backend.rewriter.SymbolicRewriter;
import org.kframework.backend.xml.input.Goals;
import org.kframework.backend.xml.input.RLGoal;
import org.kframework.compile.utils.RuleCompilerSteps;
import org.kframework.kil.ASTNode;
import org.kframework.kil.BoolBuiltin;
import org.kframework.kil.Rule;
import org.kframework.kil.Term;
import org.kframework.kil.loader.Context;
import org.kframework.krun.KRunExecutionException;
import org.kframework.krun.SubstitutionFilter;
import org.kframework.krun.api.KRunState;
import org.kframework.krun.api.RewriteRelation;
import org.kframework.krun.api.SearchResult;
import org.kframework.krun.api.SearchResults;
import org.kframework.krun.tools.Executor;
import org.kframework.parser.ProgramLoader;
import org.kframework.rewriter.SearchType;
import org.kframework.utils.Stopwatch;
import org.kframework.utils.errorsystem.KEMException;
import org.kframework.utils.file.FileUtil;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrei on 10/20/15.
 */
public class SymbolicExecutor implements Executor {

    private final KILtoBackendJavaKILTransformer kilTransformer;
    private final GlobalContext globalContext;
    private final Provider<SymbolicRewriter> symbolicRewriter;
    private final KILtoBackendJavaKILTransformer transformer;
    private final Context context;
    private final Stopwatch sw;
    private final SymbolicOptions symbolicOptions;
    private final JavaExecutionOptions javaOptions;
    private final Provider<ProgramLoader> programLoader;
    private final Provider<PatternMatchRewriter> patternMatchRewriter;
    private final KRunState.Counter counter;

    @Inject
    SymbolicExecutor(org.kframework.kil.loader.Context context, KILtoBackendJavaKILTransformer kilTransformer, GlobalContext globalContext, Provider<SymbolicRewriter> symbolicRewriter, Provider<PatternMatchRewriter> patternMatchRewriter, KILtoBackendJavaKILTransformer transformer, Definition definition, Stopwatch sw, SymbolicOptions symbolicOptions, JavaExecutionOptions javaOptions, Provider<ProgramLoader> programLoader, KRunState.Counter counter) {
        this.context = context;
        this.kilTransformer = kilTransformer;
        this.globalContext = globalContext;
        this.symbolicRewriter = symbolicRewriter;
        this.transformer = transformer;
        globalContext.setDefinition(definition);
        this.sw = sw;
        this.symbolicOptions = symbolicOptions;
        this.javaOptions = javaOptions;
        this.programLoader = programLoader;
        this.patternMatchRewriter = patternMatchRewriter;
        this.counter = counter;
    }

    @Override
    public RewriteRelation run(Term cfg, boolean computeGraph) throws KRunExecutionException {
        org.kframework.backend.java.kil.Term term = getJavaKilTerm(cfg);
        TermContext termContext = getTermContext(term);
        ConstrainedTerm constrainedTerm = new ConstrainedTerm(term, ConjunctiveFormula.of(termContext));
        return conventionalRewriteRun(constrainedTerm, -1, computeGraph);
    }

    @Override
    public SearchResults search(Integer bound, Integer depth, SearchType searchType, Rule pattern, Term cfg, RuleCompilerSteps compilationInfo, boolean computeGraph) throws KRunExecutionException {

        if (symbolicOptions.goals != null) {

            // main tasks
            Goals goals = new Goals(symbolicOptions.goals, programLoader, getContext());
            List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G = new ArrayList<>();
            Map<Map.Entry<ConstrainedTerm, ConstrainedTerm>, org.kframework.backend.java.kil.Rule> correspondingRules = new HashMap<>();
            for (RLGoal rlGoal : goals.getRlGoals()) {
                // build the new goal and put it in G
                Map.Entry<ConstrainedTerm, ConstrainedTerm> g = new AbstractMap.SimpleEntry<>(getConstrainedFormula(rlGoal, false));
                G.add(g);

                // build the KIL rule and then transform it into a java KIL rule
                Rule kilRule = getFormulaAsRule(rlGoal);
                org.kframework.backend.java.kil.Rule javaKilRule = getKilTransformer().transformAndEval(kilRule);
                correspondingRules.put(g, javaKilRule.getFreshRule(getTermContext(javaKilRule.leftHandSide())));
            }

            boolean proved = false;
            List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> initialDerivatives = derivatives(G);
            if (initialDerivatives == null || initialDerivatives.isEmpty()) {
                Logger.failed("The initial goals are not derivable.");
                System.out.println("The initial goals are not derivable.");
            }
            else  {
                proved = prove(initialDerivatives, G, symbolicOptions.maxSteps, correspondingRules);
            }
            // log status
            String status = proved ? "Proof succeeded!" : "Proof failed!";
            System.out.println(status);

            // save log
            FileUtil.save(new File(symbolicOptions.outputLog), Logger.getStringBuilder().toString());

            return new SearchResults(new ArrayList<>(), new DirectedSparseGraph<>());
        }
        else {
            // simple search using the java backend
            bound = bound == null ? -1 : bound;
            depth = depth == null ? -1 : depth;

            // The pattern needs to be a rewrite in order for the transformer to be
            // able to handle it, so we need to give it a right-hand-side.
            org.kframework.kil.Cell c = new org.kframework.kil.Cell();
            c.setLabel("generatedTop");
            c.setContents(new org.kframework.kil.Bag());
            pattern.setBody(new org.kframework.kil.Rewrite(pattern.getBody(), c, context));
            org.kframework.backend.java.kil.Rule patternRule = transformer.transformAndEval(pattern);

            List<SearchResult> searchResults = new ArrayList<>();
            List<Substitution<Variable, org.kframework.backend.java.kil.Term>> hits;
            org.kframework.backend.java.kil.Term initialTerm = kilTransformer.transformAndEval(cfg);
            TermContext termContext = TermContext.of(globalContext);
            if (!javaOptions.symbolicExecution) {
                if (computeGraph) {
                    throw KEMException.criticalError("Compute Graph with Pattern Matching Not Implemented Yet");
                }
                hits = getPatternMatchRewriter().search(initialTerm,
                        patternRule, bound, depth, searchType, termContext);
            } else {
                SymbolicRewriter rewriter = getSymbolicRewriter();
                hits = rewriter.search(initialTerm,
                        patternRule, bound, depth, searchType, termContext);
            }

            for (Map<Variable, org.kframework.backend.java.kil.Term> map : hits) {
                // Construct substitution map from the search results
                Map<String, org.kframework.kil.Term> substitutionMap =
                        new HashMap<>();
                for (Variable var : map.keySet()) {
                    org.kframework.kil.Term kilTerm =
                            (org.kframework.kil.Term) map.get(var).accept(
                                    new BackendJavaKILtoKILTransformer(context));
                    substitutionMap.put(var.name(), kilTerm);
                }

                // Apply the substitution to the pattern
                org.kframework.kil.Term rawResult =
                        (org.kframework.kil.Term) new SubstitutionFilter(substitutionMap, context)
                                .visitNode(pattern.getBody());

                searchResults.add(new SearchResult(
                        new JavaKRunState(rawResult, counter),
                        substitutionMap,
                        compilationInfo));
            }

            return new SearchResults(searchResults, null);
        }
    }

    private boolean prove(List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G, List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G0, int depth, Map<Map.Entry<ConstrainedTerm, ConstrainedTerm>, org.kframework.backend.java.kil.Rule> correspondingRules) throws KRunExecutionException {

        Logger.putLine("Steps left: " + depth + "  curent goals: ");
        for (Map.Entry<ConstrainedTerm, ConstrainedTerm> g : G) {
            Logger.putSimpleLine("Goal " + G.indexOf(g) + ":\n"  + g.getKey() + "\n=>\n" + g.getValue() + "\n");
        }
        Logger.putSimpleLine("\n");
        if (depth == 0) {
            Logger.failed("Max depth reached!");
            return false;
        }
        else {
            -- depth;
            if (G.isEmpty()) {
                Logger.putLine("Proof succeeded!");
                return true;
            }
            else {
                Map.Entry<ConstrainedTerm, ConstrainedTerm> currentGoal = G.remove(0);
                Logger.putLine("Current goal: " + currentGoal.getKey() + "\n=>\n" + currentGoal.getValue());
                if (currentGoal.getKey().implies(currentGoal.getValue())) {
                    Logger.putLine("Implication succeeded: " + currentGoal.getKey() + "\nimplies\n" + currentGoal.getValue());
                    // continue
                    return prove(G, G0, depth, correspondingRules);
                }
                else {
                    Logger.putLine("Implication failed: " + currentGoal.getKey() + "\ndoes not imply\n" + currentGoal.getValue() + ";\ncontinue...");
                    Map.Entry<ConstrainedTerm, ConstrainedTerm> circ = searchCircularity(currentGoal.getKey(), G0);
                    if (circ != null) {
                        Logger.putLine("Circularity found: " + circ.getKey() + "\n=>\n" + circ.getValue());
                        List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> derWithCirc = derivativeWithRule(currentGoal, correspondingRules.get(circ));
                        if (derWithCirc != null && !derWithCirc.isEmpty()) {
                            Logger.putLine("Added goal: derivative with circularity.");
                            G.addAll(derWithCirc);
                        } else{
                            Logger.putLine("The rewrite engine failed to compute the derivative of\n" + currentGoal.getKey() + "\n\nwith circularity\n\n" + circ + "\n.Failing...\n");
                            return false;
                        }
                        return prove(G, G0, depth, correspondingRules);
                    }
                    else {
                        Logger.putLine("Circularity not found for current goal; continue...");
                        List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> ders = derivative(currentGoal);
                        if (ders != null && !ders.isEmpty()) {
                            Logger.putLine("Compute derivates...");
                            G.addAll(derivative(currentGoal));
                            Logger.putLine("Derivatives added; continue...");
                            return prove(G, G0, depth, correspondingRules);
                        }
                        else {
                            Logger.failed("The left hand side of the current goal is not derivable: " + currentGoal.getKey());
                            return  false;
                        }
                    }
                }
            }
        }
    }

    private List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> derivativeWithRule(Map.Entry<ConstrainedTerm, ConstrainedTerm> currentGoal, org.kframework.backend.java.kil.Rule circ) {
        List<ConstrainedTerm> constraintTerms = getSymbolicRewriter().oneStepWithRule(currentGoal.getKey(), circ);
        List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> results = new LinkedList<>();
        for (ConstrainedTerm term : constraintTerms) {
            results.add(new AbstractMap.SimpleEntry<ConstrainedTerm, ConstrainedTerm>(term, currentGoal.getValue()));
        }
        return  results;
    }


    private Map.Entry<ConstrainedTerm,ConstrainedTerm> searchCircularity(ConstrainedTerm phi, List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G) {
        for (Map.Entry<ConstrainedTerm, ConstrainedTerm> c : G) {
            if (phi.implies(replaceVariablesWithFresh(c.getKey()))) {
                return c;
            }
        }
        return null;
    }

    private ConstrainedTerm replaceVariablesWithFresh(ConstrainedTerm term) {
        return (ConstrainedTerm) term.accept(new CopyOnWriteTransformer(term.termContext()) {
            @Override
            public ASTNode transform(Variable variable) {
                return new Variable(variable.name().concat("gen"), variable.sort());
            }
        });
    }


    private List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> derivatives(List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G) throws KRunExecutionException {

        List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> derivatives = new LinkedList<>();
        for (Map.Entry<ConstrainedTerm, ConstrainedTerm> g : G ) {
            derivatives.addAll(derivative(g));
        }

        return derivatives;
    }

    private List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> derivative(Map.Entry<ConstrainedTerm, ConstrainedTerm> g) throws KRunExecutionException {
        List<ConstrainedTerm> leftHandSides = symbolicRewriter.get().oneSearchStep(g.getKey());
        List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> rlDerivatives = new LinkedList<>();
        for (ConstrainedTerm lhs : leftHandSides) {
            rlDerivatives.add(new AbstractMap.SimpleEntry<ConstrainedTerm, ConstrainedTerm>(lhs, g.getValue()));
        }
        return rlDerivatives;
    }

    @Override
    public RewriteRelation step(Term cfg, int steps, boolean computeGraph) throws KRunExecutionException {
        throw new UnsupportedOperationException("step is not supported in the symbolic backend");
    }

    /**
     * private method to convert a generic kil term to java kil.1
     *
     * @return JavaKil Term.
     */
    private org.kframework.backend.java.kil.Term getJavaKilTerm(org.kframework.kil.Term cfg) {
        org.kframework.backend.java.kil.Term term = kilTransformer.transformAndEval(cfg);
        sw.printIntermediate("Convert initial configuration to internal representation");
        TermContext termContext = TermContext.of(globalContext);
        termContext.setTopTerm(term);
        return term;
    }

    /**
     * Given a term, return the TermContext constructed from the globalContext
     *
     * @param term
     * @return
     */
    private TermContext getTermContext(org.kframework.backend.java.kil.Term term) {
        TermContext termContext = TermContext.of(globalContext);
        termContext.setTopTerm(term);
        return termContext;
    }

    private RewriteRelation conventionalRewriteRun(ConstrainedTerm constrainedTerm, int bound, boolean computeGraph) {
        SymbolicRewriter rewriter = symbolicRewriter.get();
        KRunState finalState = rewriter.rewrite(constrainedTerm, bound);

        return new RewriteRelation(finalState, null);
    }


    public Context getContext() {
        return context;
    }


    private Rule getFormulaAsRule(RLGoal rlGoal) {
        return  new Rule(rlGoal.getLhs(), rlGoal.getRhs(), rlGoal.getLhsConstraint(), rlGoal.getRhsConstraint(), getContext());
    }

    private Map.Entry<ConstrainedTerm, ConstrainedTerm> getConstrainedFormula(RLGoal RLGoal, boolean renamingEnabled) {
        TermContext termContext = TermContext.of(getGlobalContext());

        org.kframework.backend.java.kil.Term lhsTerm = getKilTransformer().transformAndEval(RLGoal.getLhs());
        org.kframework.backend.java.kil.Term lhsConstraint = getKilTransformer().transformAndEval(RLGoal.getLhsConstraint());
        ConstrainedTerm lhs = getConstrainedTerm(lhsTerm, lhsConstraint, termContext);
        lhs = renamingEnabled ? replaceVariablesWithFresh(lhs) : lhs;

        org.kframework.backend.java.kil.Term rhsTerm = getKilTransformer().transformAndEval(RLGoal.getRhs());
        org.kframework.backend.java.kil.Term rhsConstraint = getKilTransformer().transformAndEval(RLGoal.getRhsConstraint());
        ConstrainedTerm rhs = getConstrainedTerm(rhsTerm, rhsConstraint, termContext);
        rhs = renamingEnabled ? replaceVariablesWithFresh(rhs) : rhs;

        return new AbstractMap.SimpleEntry<>(lhs, rhs);
    }

    private ConstrainedTerm getConstrainedTerm(org.kframework.backend.java.kil.Term initialTerm, org.kframework.backend.java.kil.Term initialConstraint, TermContext termContext) {
        // prepare the left hand side
        Equality equality = new Equality(initialConstraint, getKilTransformer().transformAndEval(BoolBuiltin.TRUE), termContext);
        Collection<Equality> equalityCollection = new LinkedList<>();
        equalityCollection.add(equality);
        PersistentUniqueList<Equality> persistentUniqueList = PersistentUniqueList.from(equalityCollection);
        return new ConstrainedTerm(initialTerm, ConjunctiveFormula.of(Substitution.empty(), persistentUniqueList, PersistentUniqueList.empty(), termContext));
    }

    public GlobalContext getGlobalContext() {
        return globalContext;
    }

    public KILtoBackendJavaKILTransformer getKilTransformer() {
        return kilTransformer;
    }

    public SymbolicRewriter getSymbolicRewriter() {
        return symbolicRewriter.get();
    }

    private PatternMatchRewriter getPatternMatchRewriter() {
        return patternMatchRewriter.get();
    }

}
