package org.kframework.backend.abstracT.graph.specification;

import com.google.inject.Provider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;
import org.kframework.compile.utils.MetaK;
import org.kframework.kil.Cell;
import org.kframework.kil.Sort;
import org.kframework.kil.Term;
import org.kframework.kil.loader.Context;
import org.kframework.parser.ParserType;
import org.kframework.parser.ProgramLoader;
import org.kframework.utils.errorsystem.KEMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by andrei on 16/07/15.
 */
public class AbstractGraphSpecification {
    private List<AbstractGraphNodeSpecification> abstractGraphNodeSpecs;


    public AbstractGraphSpecification(String graphXMLFilename, Provider<ProgramLoader> programLoader, Context context) {
        // init abstractGraphNodeSpecs
        abstractGraphNodeSpecs = new LinkedList<>();
        load(graphXMLFilename, programLoader, context);
    }

    private void load(String graphXMLFilename, Provider<ProgramLoader> programLoader, Context context) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(FileUtils.getFile(graphXMLFilename));

            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    Element childNode = (Element) node;
                    switch (childNode.getNodeName()) {
                    case XMLNodeNames.RLFORMULA:
                        abstractGraphNodeSpecs.add(load(childNode, programLoader, context));
                        break;
                    }
                }
            }
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AbstractGraphNodeSpecification load(Element rlNode, Provider<ProgramLoader> programLoader, Context context) {
        NodeList chNodes = rlNode.getChildNodes();

        Term lhs = null;
        Term lhsConstraint = null;
        Term rhs = null;
        Term rhsConstraint = null;
        SortedMap<Integer, Pair<Integer, Integer>> steps = new TreeMap<>();

        for (int i = 0; i < chNodes.getLength(); i++) {
            Node node = chNodes.item(i);

            if (node instanceof Element) {
                Element childNode = (Element) node;
                switch (childNode.getNodeName()) {
                case XMLNodeNames.LHS:
                    if (lhs == null) {
                        lhs = programLoader.get().processPgm(new StringReader(childNode.getTextContent()), null, Sort.BAG_ITEM, context, ParserType.RULES);
                        lhs = MetaK.wrap(lhs, MetaK.Constants.generatedTopCellLabel, Cell.Ellipses.NONE);
                    } else {

                    }
                    break;
                case XMLNodeNames.RHS:
                    if (rhs == null) {
                        rhs = programLoader.get().processPgm(new StringReader(childNode.getTextContent()), null, Sort.BAG_ITEM, context, ParserType.RULES);
                        rhs = MetaK.wrap(rhs, MetaK.Constants.generatedTopCellLabel, Cell.Ellipses.NONE);
                    } else {
                        KEMException.criticalError("Node " + rlNode + " id " + rlNode.getAttribute("id") + " contains node " + XMLNodeNames.RHS + " more than once");
                    }
                    break;
                case XMLNodeNames.LHS_CONSTRAINT :
                    if (lhs != null) {
                        lhsConstraint = programLoader.get().processPgm(new StringReader(childNode.getTextContent()), null, Sort.BAG_ITEM, context, ParserType.RULES);
                    } else {
                        KEMException.criticalError("Cannot load lhs constaint for " + rlNode + " id " + rlNode.getAttribute("id"));
                    }
                    break;
                case XMLNodeNames.RHS_CONSTRAINT :
                    if (rhs != null) {
                        rhsConstraint = programLoader.get().processPgm(new StringReader(childNode.getTextContent()), null, Sort.BAG_ITEM, context, ParserType.RULES);
                    } else {
                        KEMException.criticalError("Cannot load rhs constaint for " + rlNode + " id " + rlNode.getAttribute("id"));
                    }
                    break;
                case XMLNodeNames.STEP:
                    Integer stepNumber = Integer.valueOf(childNode.getAttribute(XMLNodeNames.STEP_NO));
                    Integer ruleId = Integer.valueOf(childNode.getAttribute(XMLNodeNames.RULE_ID));
                    Integer depth = Integer.valueOf(childNode.getAttribute(XMLNodeNames.DEPTH));
                    if (steps.containsKey(stepNumber)) {
                        KEMException.criticalError("Node " + rlNode + " id " + rlNode.getAttribute("id") + " contains duplicate <step> idendifiers.");
                    } else {
                        steps.put(stepNumber, new Pair(ruleId, depth));
                    }
                    break;
                }
            }
        }
        if (lhs != null && rhs != null) {
            return new AbstractGraphNodeSpecification(lhs, rhs, lhsConstraint, rhsConstraint, steps);
        } else {
            KEMException.criticalError("Please provide both lhs and rhs for node " + rlNode + " id " + rlNode.getAttribute("id"));
            return null;
        }
    }

    public List<AbstractGraphNodeSpecification> getAbstractGraphNodeSpecs() {
        return abstractGraphNodeSpecs;
    }
}
