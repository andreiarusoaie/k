package org.kframework.backend.abstracT.backend;

import com.google.inject.Inject;
import com.google.inject.Provider;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.kframework.backend.abstracT.graph.AbstractGraph;
import org.kframework.backend.abstracT.graph.AbstractGraphEdge;
import org.kframework.backend.abstracT.graph.AbstractGraphNode;
import org.kframework.backend.abstracT.graph.EdgeType;
import org.kframework.backend.abstracT.graph.NodeStatus;
import org.kframework.backend.abstracT.logger.Logger;
import org.kframework.backend.abstracT.rewriter.AbstractRewriter;
import org.kframework.backend.abstracT.xml.input.Goals;
import org.kframework.backend.abstracT.xml.input.RLGoal;
import org.kframework.backend.java.kil.ConstrainedTerm;
import org.kframework.backend.java.kil.Definition;
import org.kframework.backend.java.kil.GlobalContext;
import org.kframework.backend.java.kil.TermContext;
import org.kframework.backend.java.kil.Variable;
import org.kframework.backend.java.symbolic.BackendJavaKILtoKILTransformer;
import org.kframework.backend.java.symbolic.ConjunctiveFormula;
import org.kframework.backend.java.symbolic.CopyOnWriteTransformer;
import org.kframework.backend.java.symbolic.Equality;
import org.kframework.backend.java.symbolic.KILtoBackendJavaKILTransformer;
import org.kframework.backend.java.symbolic.PatternMatchRewriter;
import org.kframework.backend.java.symbolic.PersistentUniqueList;
import org.kframework.backend.java.symbolic.Substitution;
import org.kframework.backend.java.symbolic.SymbolicRewriter;
import org.kframework.compile.utils.RuleCompilerSteps;
import org.kframework.kil.ASTNode;
import org.kframework.kil.BoolBuiltin;
import org.kframework.kil.Rule;
import org.kframework.kil.Term;
import org.kframework.kil.loader.Context;
import org.kframework.krun.KRunExecutionException;
import org.kframework.krun.api.KRunState;
import org.kframework.krun.api.RewriteRelation;
import org.kframework.krun.api.SearchResults;
import org.kframework.krun.tools.Executor;
import org.kframework.parser.ProgramLoader;
import org.kframework.rewriter.SearchType;
import org.kframework.utils.Stopwatch;
import org.kframework.utils.file.FileUtil;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by andrei on 14.07.2015.
 */
public class AbstractExecutor implements Executor {

    private final KILtoBackendJavaKILTransformer kilTransformer;
    private final GlobalContext globalContext;
    private final Provider<SymbolicRewriter> symbolicRewriter;
    private final KILtoBackendJavaKILTransformer transformer;
    private final Context context;
    private final Stopwatch sw;
    private final AbstractOptions abstractOptions;
    private final Provider<ProgramLoader> programLoader;

    @Inject
    AbstractExecutor(org.kframework.kil.loader.Context context, KILtoBackendJavaKILTransformer kilTransformer, GlobalContext globalContext, Provider<SymbolicRewriter> symbolicRewriter, KILtoBackendJavaKILTransformer transformer, Definition definition, Stopwatch sw, AbstractOptions abstractOptions, Provider<ProgramLoader> programLoader) {
        this.context = context;
        this.kilTransformer = kilTransformer;
        this.globalContext = globalContext;
        this.symbolicRewriter = symbolicRewriter;
        this.transformer = transformer;
        globalContext.setDefinition(definition);
        this.sw = sw;
        this.abstractOptions = abstractOptions;
        this.programLoader = programLoader;
    }

    @Override
    public SearchResults search(Integer bound, Integer depth, SearchType searchType, Rule pattern, Term cfg, RuleCompilerSteps compilationInfo, boolean computeGraph) throws KRunExecutionException {

        // main tasks
        Goals goals = new Goals(abstractOptions.goals, programLoader, getContext());
        Map.Entry<ConstrainedTerm, ConstrainedTerm> mainGoal = getMainFormula(goals);
        List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G = getListOfGoals(goals);

        // verification
        AbstractGraph abstractGraph = getGraph(mainGoal, G, pattern);
        abstractGraph = annotateAbstractGraph(abstractGraph, pattern);

        // log status
        String status = abstractGraph.isValid() ? "Proof succeeded!" : "Proof failed!";
        Logger.putLine(status);
        System.out.println(status);

        // options
        if (abstractOptions.window) {
            abstractGraph.displayGraph(status + " Check " + abstractOptions.outputLog + " for details.");
            new Scanner(System.in).nextLine();
        }

        if (abstractOptions.exportFile != null) {
            abstractGraph.saveGraphAsImage(abstractOptions.exportFile);
        }

        FileUtil.save(new File(abstractOptions.outputLog), Logger.getStringBuilder().toString());
        return new SearchResults(new ArrayList<>(), new DirectedSparseGraph<>());
    }

    private Map.Entry<ConstrainedTerm, ConstrainedTerm> getMainFormula(Goals goals) {
        return getConstrainedFormula(goals.getMainGoal());
    }


    private List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> getListOfGoals(Goals abstractGraphSpecification) {
        List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> goals = new ArrayList<>();
        for (RLGoal RLGoal : abstractGraphSpecification.getRlGoals()) {
            goals.add(new AbstractMap.SimpleEntry<>(getConstrainedFormula(RLGoal)));
        }
        return goals;
    }

    private Map.Entry<ConstrainedTerm, ConstrainedTerm> getConstrainedFormula(RLGoal RLGoal){
        TermContext termContext = TermContext.of(getGlobalContext());

        org.kframework.backend.java.kil.Term lhsTerm = getKilTransformer().transformAndEval(RLGoal.getLhs());
        org.kframework.backend.java.kil.Term lhsConstraint = getKilTransformer().transformAndEval(RLGoal.getLhsConstraint());
        ConstrainedTerm lhs = getConstrainedTerm(lhsTerm, lhsConstraint, termContext);

        org.kframework.backend.java.kil.Term rhsTerm = getKilTransformer().transformAndEval(RLGoal.getRhs());
        org.kframework.backend.java.kil.Term rhsConstraint = getKilTransformer().transformAndEval(RLGoal.getRhsConstraint());
        ConstrainedTerm rhs = getConstrainedTerm(rhsTerm, rhsConstraint, termContext);

        return new AbstractMap.SimpleEntry<ConstrainedTerm, ConstrainedTerm>(lhs, rhs);
    }

    /**
     * Given a set of goals G and a "main" formula in G the method
     * builds the abstract graph for this formula
     * @param main  the "main" RL formula
     * @param G the set of goals (including the main formula)
     * @param pattern the rule pattern required by the rewriter
     * @return the abstract graph for main RL formula
     * @throws KRunExecutionException
     */
    private AbstractGraph getGraph(
            Map.Entry<ConstrainedTerm, ConstrainedTerm> main,
            List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G,
            Rule pattern) throws KRunExecutionException {

        // compute the first set of derivatives and then process each of them
        List<ConstrainedTerm> derivatives = AbstractRewriter.oneSearchStep(main.getKey(), getSymbolicRewriter());

        AbstractGraph graph = AbstractGraph.empty();
        AbstractGraphNode root = new AbstractGraphNode(main.getKey(), main.getValue());
        graph.addNode(root);
        for (ConstrainedTerm derivative : derivatives) {
            AbstractGraphNode child = new AbstractGraphNode(derivative, main.getValue());
            graph.addNode(child);
            graph.addEdge(new AbstractGraphEdge(root, child, EdgeType.SYMBOLIC_STEP));
            graph = construct(new AbstractMap.SimpleEntry<>(derivative, main.getValue()), G, pattern, graph, abstractOptions.maxConstructDepth);
        }
        return graph;
    }


    private AbstractGraph construct(
            Map.Entry<ConstrainedTerm, ConstrainedTerm> formula,
            List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G,
            Rule pattern,
            AbstractGraph graph,
            int timeout
            ) throws KRunExecutionException {

        // finish if timeout <= 0
        if (timeout <= 0) {
            Logger.failed("Construct depth max size reached. Quitting.");
            return  graph;
        };


        if (formula.getKey().implies(formula.getValue())) {
            Logger.putLines("Implication succeeded:", formula.getKey().toString(), "->", formula.getValue().toString());
            return graph;
        }
        else {
            Map.Entry<ConstrainedTerm, ConstrainedTerm> circ = searchCircularity(formula.getKey(), G);
            if (circ != null) {
                AbstractGraphNode sourceNode = graph.getNode(formula.getKey(), formula.getValue());
                AbstractGraphNode circNode = new AbstractGraphNode(circ.getKey(), formula.getValue());
                Logger.putLine("Found circularity: " + circNode.toString());

                if (graph.hasNode(circNode)) {
                    List<AbstractGraphNode> circDerivatives = graph.getSuccesorsByEdgeType(circNode, EdgeType.SYMBOLIC_STEP);
                    for (AbstractGraphNode circDerivative : circDerivatives) {
                        // add an edge from current node to circularity's children
                        graph.addEdge(new AbstractGraphEdge(sourceNode, circDerivative, EdgeType.CIRCULARITY));
//                        // TODO: instancenode
//                        AbstractGraphNode instNode = new AbstractGraphNode(circ.getKey(), formula.getValue());
//                        graph.addEdge(new AbstractGraphEdge(circDerivative, instNode, EdgeType.CIRCINSTANCE));
                    }
                }
                else {
                    // add circ in the graph in order to detect if it was processed at previous step
                    graph.addNode(circNode);

                    List<ConstrainedTerm> circDerivatives = AbstractRewriter.oneSearchStep(circ.getKey(), getSymbolicRewriter());
                    for (ConstrainedTerm circDerivative : circDerivatives) {
                        // build a new node and add it to graph
                        AbstractGraphNode targetNode = new AbstractGraphNode(circDerivative, formula.getValue());
                        graph.addNode(targetNode);

                        // add symb step from circ to circ der (internal stuff)
                        graph.addEdge(new AbstractGraphEdge(circNode, targetNode, EdgeType.SYMBOLIC_STEP));

                        // add symb step from formula to circ der
                        graph.addEdge(new AbstractGraphEdge(sourceNode, targetNode, EdgeType.CIRCULARITY));

                        // call construct over circ derivatives
                        graph = construct(new AbstractMap.SimpleEntry<>(circDerivative, formula.getValue()), G, pattern, graph, timeout - 1);
                    }
                }
            }
            else {
                AbstractGraphNode toExpand = new AbstractGraphNode(formula.getKey(), formula.getValue());
                AbstractGraph graphToExpand = AbstractGraph.empty();
                graphToExpand.addNode(toExpand);
                Logger.putLine("Expanding " + toExpand);
                AbstractGraph symbolicGraph = expand(graphToExpand, G, pattern, abstractOptions.maxExpandDepth);
                graph.addSubgraph(symbolicGraph);

                if (!symbolicGraph.isSingletonGraph(toExpand)) {
                    List<AbstractGraphNode> frontier = symbolicGraph.getFrontier();
                    for (AbstractGraphNode toProcess : frontier) {
                        graph = construct(new AbstractMap.SimpleEntry<ConstrainedTerm, ConstrainedTerm>(toProcess.getLhs(), formula.getValue()), G, pattern, graph, timeout - 1);
                    }
                }
                else {
                    if (!formula.getKey().implies(formula.getValue())) {
                        Logger.failed("Failed to prove implication, find circularity or derive goal: " + formula);
                    }
                }
            }
        }

        return graph;
    }

    private AbstractGraph expand(
            AbstractGraph graph,
            List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G,
            Rule pattern,
            int maxDepth) throws KRunExecutionException {


        while (maxDepth > 0) {
            List<AbstractGraphNode> frontier = graph.getFrontier();
            AbstractGraphNode toProcess = null;
            List<ConstrainedTerm> derivatives = null;
            for (AbstractGraphNode abstractGraphNode : frontier) {
                List<ConstrainedTerm> cDerivatives = AbstractRewriter.oneSearchStep(abstractGraphNode.getLhs(), getSymbolicRewriter());
                if (!abstractGraphNode.getLhs().implies(abstractGraphNode.getRhs()) &&
                        searchCircularity(abstractGraphNode.getLhs(), G) == null &&
                        !cDerivatives.isEmpty()) {
                    toProcess = abstractGraphNode;
                    derivatives = cDerivatives;
                    break;
                }
            }
            if (toProcess == null) {
                if (maxDepth - 1 > 0) {
                    Logger.putLine("Expand finished naturally.");
                }
                else {
                    Logger.failed("Expand depth max size reached. Quit.");
                }
                break;
            } else {
                for (ConstrainedTerm d : derivatives) {
                    AbstractGraphNode successor = new AbstractGraphNode(d, toProcess.getRhs());
                    graph.addNode(successor);
                    graph.addEdge(new AbstractGraphEdge(toProcess, successor, EdgeType.SYMBOLIC_STEP));
                }
            }
            maxDepth--;
        }

        return graph;
    }



//    private AbstractGraph getGraphFromSpec(Map.Entry<Map.Entry<ConstrainedTerm, ConstrainedTerm>, List<Action>> main,
//                                           List<Map.Entry<Map.Entry<ConstrainedTerm, ConstrainedTerm>, List<Action>>> G,
//                                           Rule pattern) throws KRunExecutionException {
//
//        // compute the first set of derivatives and then process each of them
//        List<ConstrainedTerm> derivatives = AbstractRewriter.oneSearchStep(main.getKey().getKey(), pattern, getGlobalContext(), getSymbolicRewriter(), getContext(), getTransformer());
//
//        AbstractGraph graph = AbstractGraph.empty();
//        AbstractGraphNode root = new AbstractGraphNode(main.getKey().getKey(), main.getKey().getValue());
//        graph.addNode(root);
//        for (ConstrainedTerm derivative : derivatives) {
//            AbstractGraphNode child = new AbstractGraphNode(derivative, main.getKey().getValue());
//            graph.addNode(child);
//            graph.addEdge(new AbstractGraphEdge(root, child, EdgeType.SYMBOLIC_STEP));
//            graph = construct(new AbstractMap.SimpleEntry<>(derivative, main.getKey().getValue()), G, pattern, graph, abstractOptions.maxConstructDepth);
//        }
//        return graph;
//
//
//        return null;
//    }


//    private AbstractGraph expandUntilCircOrNotDerivable(AbstractGraph graph, ConstrainedTerm circ, Rule pattern, int maxDepth) throws KRunExecutionException {
//
//        while (maxDepth > 0) {
//            List<AbstractGraphNode> frontier = graph.getFrontier();
//            AbstractGraphNode toProcess = null;
//            List<ConstrainedTerm> derivatives = null;
//            for (AbstractGraphNode abstractGraphNode : frontier) {
//                List<ConstrainedTerm> cDerivatives = AbstractRewriter.oneSearchStep(abstractGraphNode.getLhs(), pattern, getGlobalContext(), getSymbolicRewriter(), getContext(), getTransformer());
//                if (!abstractGraphNode.getLhs().implies(abstractGraphNode.getRhs()) &&
//                        searchCircularity(abstractGraphNode.getLhs(), G) == null &&
//                        !cDerivatives.isEmpty()) {
//                    toProcess = abstractGraphNode;
//                    derivatives = cDerivatives;
//                    break;
//                }
//            }
//            if (toProcess == null) {
//                if (maxDepth - 1 > 0) {
//                    Logger.putLine("Expand finished naturally.");
//                }
//                else {
//                    Logger.failed("Expand depth max size reached. Quit.");
//                }
//                break;
//            } else {
//                for (ConstrainedTerm d : derivatives) {
//                    AbstractGraphNode successor = new AbstractGraphNode(d, toProcess.getRhs());
//                    graph.addNode(successor);
//                    graph.addEdge(new AbstractGraphEdge(toProcess, successor, EdgeType.SYMBOLIC_STEP));
//                }
//            }
//            maxDepth--;
//
//        }
//
//        return null;
//    }

    

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

    private Term toKIL(org.kframework.backend.java.kil.Term javaTerm) {
        return (Term) javaTerm.accept(new BackendJavaKILtoKILTransformer(getContext()));
    }

    private org.kframework.backend.java.kil.Term toJavaKIL(Term term) {
        org.kframework.backend.java.kil.Term javaTerm = getKilTransformer().transformAndEval(term);
        TermContext termContext = TermContext.of(getGlobalContext());
        termContext.setTopTerm(javaTerm);
        return javaTerm;
    }

    private ConstrainedTerm getConstrainedTerm(org.kframework.backend.java.kil.Term initialTerm, org.kframework.backend.java.kil.Term initialConstraint, TermContext termContext) {
        // prepare the left hand side
        Equality equality = new Equality(initialConstraint, getKilTransformer().transformAndEval(BoolBuiltin.TRUE), termContext);
        Collection<Equality> equalityCollection = new LinkedList<>();
        equalityCollection.add(equality);
        PersistentUniqueList<Equality> persistentUniqueList = PersistentUniqueList.from(equalityCollection);
        return new ConstrainedTerm(initialTerm, ConjunctiveFormula.of(Substitution.empty(), persistentUniqueList, PersistentUniqueList.empty(), termContext));
    }

    /**
     * Traverses the abstract graph and checks the following:
     * 1. for every terminal node: lhs implies rhs
     * 2. for every intermediate node: all EdgeType.SYMBOLIC children = all symbolic successors, or
     *    all EdgeType.CIRC = symbolic derivatives of Circ and lhs implies (closure)Circ.lhs
     *
     * @return a new {@link AbstractGraph} which contains status information for every node
     */
    private AbstractGraph annotateAbstractGraph(AbstractGraph abstractGraph, Rule pattern) throws KRunExecutionException {

        AbstractGraph graph = abstractGraph;
        for (AbstractGraphNode node : graph.getAbstractNodes()) {
            if (graph.isTerminalNode(node) && !node.getLhs().implies(node.getRhs())) {
                node.setStatus(NodeStatus.INVALID_IMPLICATION);
            }
            else {
                List<ConstrainedTerm> symbolicChildren = gatherLhs(graph.getSuccesorsByEdgeType(node, EdgeType.SYMBOLIC_STEP));
                List<AbstractGraphNode> circChildren = graph.getSuccesorsByEdgeType(node, EdgeType.CIRCULARITY);
                if (!symbolicChildren.isEmpty()) {
                    if (!circChildren.isEmpty()) {
                        node.setStatus(NodeStatus.INVALID_SYMBOLIC);
                    }
                    else {
                        List<ConstrainedTerm> symbolicSteps = AbstractRewriter.oneSearchStep(node.getLhs(), getSymbolicRewriter());
                        if (!listAsSetEquality(symbolicChildren, symbolicSteps)) {
                            node.setStatus(NodeStatus.INVALID_SYMBOLIC);
                        }
                    }
                }
                else {
                    if (!circChildren.isEmpty()) {
                        List<AbstractGraphNode> firstChildPred = graph.getPredecessorsByEdgeType(circChildren.get(0), EdgeType.SYMBOLIC_STEP);
                        if (firstChildPred.size() == 1 && !node.getLhs().implies(replaceVariablesWithFresh(firstChildPred.get(0).getLhs()))) {
                            node.setStatus(NodeStatus.INVALID_CIRC);
                        }
                    }
                }
            }
        }

        return graph;
    }

    private boolean listAsSetEquality(List<ConstrainedTerm> list1, List<ConstrainedTerm> list2) {
        for(Object obj : list1) {
            if (!list2.contains(obj)) {
                return false;
            }
        }
        for(Object obj : list2) {
            if (!list1.contains(obj)) {
                return false;
            }
        }
        return true;
    }

    private List<ConstrainedTerm> gatherLhs(List<AbstractGraphNode> symbolicChildren) {
        List<ConstrainedTerm> result = new ArrayList<>();
        for (AbstractGraphNode node : symbolicChildren) {
            result.add(node.getLhs());
        }
        return result;
    }

    @Override
    public RewriteRelation run(org.kframework.kil.Term cfg, boolean computeGraph) throws KRunExecutionException {
        return javaRewriteEngineRun(cfg, -1, computeGraph);
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
        KRunState finalState =  rewriter.rewrite(constrainedTerm, bound);

        return new RewriteRelation(finalState, null);
    }

    /**
     * Rewrite Enginre run that creates a new KRun State.
     * @param cfg The term configuration to begin with.
     * @param bound The number of steps
     * @param computeGraph Option to compute Execution Graph,
     * @return The execution relation.
     */
    protected RewriteRelation javaRewriteEngineRun(org.kframework.kil.Term cfg, int bound, boolean computeGraph) {
        org.kframework.backend.java.kil.Term term = getJavaKilTerm(cfg);
        TermContext termContext = getTermContext(term);
        ConstrainedTerm constrainedTerm = new ConstrainedTerm(term, ConjunctiveFormula.of(termContext));
        return conventionalRewriteRun(constrainedTerm, bound, computeGraph);
    }

    @Override
    public RewriteRelation step(org.kframework.kil.Term cfg, int steps, boolean computeGraph)
            throws KRunExecutionException {
        return javaRewriteEngineRun(cfg, steps, computeGraph);
    }

    public SymbolicRewriter getSymbolicRewriter() {
        return symbolicRewriter.get();
    }


    public GlobalContext getGlobalContext() {
        return globalContext;
    }

    public Context getContext() {
        return context;
    }

    public KILtoBackendJavaKILTransformer getKilTransformer() {
        return kilTransformer;
    }

    public KILtoBackendJavaKILTransformer getTransformer() {
        return transformer;
    }

}
