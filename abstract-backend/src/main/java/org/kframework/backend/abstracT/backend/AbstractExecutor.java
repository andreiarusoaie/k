package org.kframework.backend.abstracT.backend;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.kframework.backend.abstracT.graph.AbstractGraph;
import org.kframework.backend.abstracT.graph.AbstractGraphEdge;
import org.kframework.backend.abstracT.graph.AbstractGraphNode;
import org.kframework.backend.abstracT.graph.EdgeType;
import org.kframework.backend.abstracT.rewriter.AbstractRewriter;
import org.kframework.backend.abstracT.graph.specification.AbstractGraphNodeSpecification;
import org.kframework.backend.abstracT.graph.specification.AbstractGraphSpecification;
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
import org.kframework.backend.java.symbolic.JavaSymbolicExecutor;
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
import org.kframework.krun.KRunExecutionException;
import org.kframework.krun.api.KRunState;
import org.kframework.krun.api.RewriteRelation;
import org.kframework.krun.api.SearchResults;
import org.kframework.krun.api.SearchType;
import org.kframework.parser.ProgramLoader;
import org.kframework.utils.Stopwatch;

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
public class AbstractExecutor extends JavaSymbolicExecutor {

    private final AbstractOptions abstractOptions;
    private final Provider<ProgramLoader> programLoader;

    @Inject
    AbstractExecutor(org.kframework.kil.loader.Context context, JavaExecutionOptions javaOptions, KILtoBackendJavaKILTransformer kilTransformer, GlobalContext globalContext, Provider<SymbolicRewriter> symbolicRewriter, Provider<PatternMatchRewriter> patternMatchRewriter, KILtoBackendJavaKILTransformer transformer, Definition definition, KRunState.Counter counter, Stopwatch sw, AbstractOptions abstractOptions, Provider<ProgramLoader> programLoader) {
        super(context, javaOptions, kilTransformer, globalContext, symbolicRewriter, patternMatchRewriter, transformer, definition, counter, sw);
        this.abstractOptions = abstractOptions;
        this.programLoader = programLoader;
    }

    @Override
    public SearchResults search(Integer bound, Integer depth, SearchType searchType, Rule pattern, Term cfg, RuleCompilerSteps compilationInfo, boolean computeGraph) throws KRunExecutionException {
        AbstractGraphSpecification abstractGraphSpecification = new AbstractGraphSpecification(abstractOptions.abstractGraph, programLoader, getContext());
        Map.Entry<ConstrainedTerm, ConstrainedTerm> mainGoal = getMainFormula(abstractGraphSpecification);
        List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G = getListOfGoals(abstractGraphSpecification);
        AbstractGraph abstractGraph = getGraph(mainGoal, G, pattern);
        abstractGraph.displayGraph();
        new Scanner(System.in).nextLine();
        return super.search(bound, depth, searchType, pattern, cfg, compilationInfo, computeGraph);
    }

    // this method is 'hacky' since it returns the first formula as the main formula
    // TODO: fix the way we identify the main formula
    private Map.Entry<ConstrainedTerm, ConstrainedTerm> getMainFormula(AbstractGraphSpecification abstractGraphSpecification) {
        AbstractGraphNodeSpecification abstractGraphNodeSpecification = abstractGraphSpecification.getAbstractGraphNodeSpecs().get(0);
        return getConstrainedFormula(abstractGraphNodeSpecification);
    }


    private List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> getListOfGoals(AbstractGraphSpecification abstractGraphSpecification) {
        List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> goals = new ArrayList<>();
        for (AbstractGraphNodeSpecification abstractGraphNodeSpecification : abstractGraphSpecification.getAbstractGraphNodeSpecs()) {
            goals.add(new AbstractMap.SimpleEntry<ConstrainedTerm, ConstrainedTerm>(getConstrainedFormula(abstractGraphNodeSpecification)));
        }
        return goals;
    }

    private Map.Entry<ConstrainedTerm, ConstrainedTerm> getConstrainedFormula(AbstractGraphNodeSpecification abstractGraphNodeSpecification){
        TermContext termContext = TermContext.of(getGlobalContext());

        org.kframework.backend.java.kil.Term lhsTerm = getKilTransformer().transformAndEval(abstractGraphNodeSpecification.getLhs());
        org.kframework.backend.java.kil.Term lhsConstraint = getKilTransformer().transformAndEval(abstractGraphNodeSpecification.getLhsConstraint());
        ConstrainedTerm lhs = getConstrainedTerm(lhsTerm, lhsConstraint, termContext);

        org.kframework.backend.java.kil.Term rhsTerm = getKilTransformer().transformAndEval(abstractGraphNodeSpecification.getRhs());
        org.kframework.backend.java.kil.Term rhsConstraint = getKilTransformer().transformAndEval(abstractGraphNodeSpecification.getRhsConstraint());
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
        List<ConstrainedTerm> derivatives = AbstractRewriter.oneSearchStep(main.getKey(), pattern, getGlobalContext(), getSymbolicRewriter(), getContext(), getTransformer());

        AbstractGraph graph = AbstractGraph.empty();
        AbstractGraphNode root = new AbstractGraphNode(main.getKey(), main.getValue());
        graph.addNode(root);
        for (ConstrainedTerm derivative : derivatives) {
            AbstractGraphNode child = new AbstractGraphNode(derivative, main.getValue());
            graph.addNode(child);
            graph.addEdge(new AbstractGraphEdge(root, child, EdgeType.SYMBOLIC_STEP));
            graph = construct(new AbstractMap.SimpleEntry<>(derivative, main.getValue()), G, pattern, graph);
        }
        return graph;
    }


    private AbstractGraph construct(
            Map.Entry<ConstrainedTerm, ConstrainedTerm> formula,
            List<Map.Entry<ConstrainedTerm, ConstrainedTerm>> G,
            Rule pattern,
            AbstractGraph graph
            ) throws KRunExecutionException {

        if (formula.getKey().implies(formula.getValue())) {
            return graph;
        }
        else {
            Map.Entry<ConstrainedTerm, ConstrainedTerm> circ = searchCircularity(formula.getKey(), G);
            if (circ != null) {
                AbstractGraphNode source = graph.getNode(formula.getKey(), formula.getValue());
                AbstractGraphNode circNode = new AbstractGraphNode(circ.getKey(), formula.getValue());

                if (graph.hasNode(circNode)) {
                    List<AbstractGraphNode> circDerivatives = graph.getSuccesorsByEdgeType(circNode, EdgeType.SYMBOLIC_STEP);
                    for (AbstractGraphNode circDerivative : circDerivatives) {
                        graph.addEdge(new AbstractGraphEdge(source, circDerivative, EdgeType.CIRCULARITY));
                    }
                }
                else {
                    // add circ in the graph in order to detect if it was processed at previous step
                    graph.addNode(circNode);

                    List<ConstrainedTerm> circDerivatives = AbstractRewriter.oneSearchStep(circ.getKey(), pattern, getGlobalContext(), getSymbolicRewriter(), getContext(), getTransformer());
                    for (ConstrainedTerm circDerivative : circDerivatives) {
                        // build a new node and add it to graph
                        AbstractGraphNode targetNode = new AbstractGraphNode(circDerivative, formula.getValue());
                        graph.addNode(targetNode);

                        // add symb step from circ to circ der (internal stuff)
                        graph.addEdge(new AbstractGraphEdge(circNode, targetNode, EdgeType.SYMBOLIC_STEP));

                        // add symb step from formula to circ der
                        graph.addEdge(new AbstractGraphEdge(source, targetNode, EdgeType.CIRCULARITY));

                        // call construct over circ derivatives
                        graph = construct(new AbstractMap.SimpleEntry<>(circDerivative, formula.getValue()), G, pattern, graph);
                    }
                }
            }
            else {
                AbstractGraphNode toExpand = new AbstractGraphNode(formula.getKey(), formula.getValue());
                AbstractGraph graphToExpand = AbstractGraph.empty();
                graphToExpand.addNode(toExpand);
                AbstractGraph symbolicGraph = expand(graphToExpand, G, pattern, 10);
                graph.addSubgraph(symbolicGraph);

                if (!symbolicGraph.isSingletonGraph(toExpand)) {
                    List<AbstractGraphNode> frontier = symbolicGraph.getFrontier();
                    for (AbstractGraphNode toProcess : frontier) {
                        graph = construct(new AbstractMap.SimpleEntry<ConstrainedTerm, ConstrainedTerm>(toProcess.getLhs(), formula.getValue()), G, pattern, graph);
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
                List<ConstrainedTerm> cDerivatives = AbstractRewriter.oneSearchStep(abstractGraphNode.getLhs(), pattern, getGlobalContext(), getSymbolicRewriter(), getContext(), getTransformer());
                if (!abstractGraphNode.getLhs().implies(abstractGraphNode.getRhs()) &&
                        searchCircularity(abstractGraphNode.getLhs(), G) == null &&
                        !cDerivatives.isEmpty()) {
                    toProcess = abstractGraphNode;
                    derivatives = cDerivatives;
                    break;
                }
            }
            if (toProcess == null) {
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

    private boolean checkAbstractGraph() {

        return false;
    }

}
