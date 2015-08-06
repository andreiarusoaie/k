package org.kframework.backend.abstracT.xml.input;

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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by andrei on 16/07/15.
 */
public class Goals {
    private List<RLGoal> rlGoals;
    private RLGoal mainGoal;

    public Goals(String graphXMLFilename, Provider<ProgramLoader> programLoader, Context context) {
        // init rlGoals
        rlGoals = new LinkedList<>();
        mainGoal = null;
        load(graphXMLFilename, programLoader, context);

        if (mainGoal == null) {
            throw KEMException.internalError("Main RL formula (with id = 0) not found.");
        }
    }

    private void load(String graphXMLFilename, Provider<ProgramLoader> programLoader, Context context) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File xmlFile = FileUtils.getFile(graphXMLFilename);
            Document document = builder.parse(xmlFile);
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    Element childNode = (Element) node;
                    switch (childNode.getNodeName()) {
                    case XMLNodeNames.RLFORMULA:
                        rlGoals.add(load(childNode, programLoader, context));
                        break;
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw  KEMException.criticalError(e.getLocalizedMessage());
        } catch (SAXException e) {
            throw KEMException.criticalError(e.getLocalizedMessage());
        } catch (FileNotFoundException e) {
            throw KEMException.criticalError(e.getLocalizedMessage());
        } catch (IOException e) {
            throw KEMException.criticalError(e.getLocalizedMessage());
        }
    }

    private RLGoal load(Element rlNode, Provider<ProgramLoader> programLoader, Context context) {
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
                        throw KEMException.criticalError("Node " + rlNode + " id " + rlNode.getAttribute("id") + " contains node " + XMLNodeNames.LHS + " more than once");
                    }
                    break;
                case XMLNodeNames.RHS:
                    if (rhs == null) {
                        rhs = programLoader.get().processPgm(new StringReader(childNode.getTextContent()), null, Sort.BAG_ITEM, context, ParserType.RULES);
                        rhs = MetaK.wrap(rhs, MetaK.Constants.generatedTopCellLabel, Cell.Ellipses.NONE);
                    } else {
                        throw KEMException.criticalError("Node " + rlNode + " id " + rlNode.getAttribute("id") + " contains node " + XMLNodeNames.RHS + " more than once");
                    }
                    break;
                case XMLNodeNames.LHS_CONSTRAINT :
                    if (lhs != null) {
                        lhsConstraint = programLoader.get().processPgm(new StringReader(childNode.getTextContent()), null, Sort.BAG_ITEM, context, ParserType.RULES);
                    } else {
                        throw KEMException.criticalError("Cannot load lhs constaint for " + rlNode + " id " + rlNode.getAttribute("id"));
                    }
                    break;
                case XMLNodeNames.RHS_CONSTRAINT :
                    if (rhs != null) {
                        rhsConstraint = programLoader.get().processPgm(new StringReader(childNode.getTextContent()), null, Sort.BAG_ITEM, context, ParserType.RULES);
                    } else {
                        throw KEMException.criticalError("Cannot load rhs constaint for " + rlNode + " id " + rlNode.getAttribute("id"));
                    }
                    break;
                }
            }
        }

        if (lhs != null && rhs != null) {
            RLGoal rlGoal = new RLGoal(lhs, rhs, lhsConstraint, rhsConstraint);
            if (getId(rlNode) == 0) { mainGoal = rlGoal; }
            return rlGoal;
        } else {
            throw KEMException.criticalError("Please provide both lhs and rhs for node " + rlNode + " id " + rlNode.getAttribute("id"));
        }
    }

    private Integer getId(Element rlNode) {
        NamedNodeMap attributes = rlNode.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.item(i).getNodeName().equals(XMLNodeNames.RULE_ID)) {
                try {
                    return Integer.parseInt(attributes.item(i).getNodeValue());
                } catch (NumberFormatException e) {
                    throw KEMException.criticalError(e.getLocalizedMessage());
                }
            }
        }

        return -1;
    }

    public List<RLGoal> getRlGoals() {
        return rlGoals;
    }

    public RLGoal getMainGoal() {
        return mainGoal;
    }
}
