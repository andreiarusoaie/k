package org.kframework.backend.symbolic.krun;

import org.kframework.compile.transformers.AddSymbolicK;
import org.kframework.kil.*;
import org.kframework.kil.loader.Context;
import org.kframework.kil.visitors.NonCachingVisitor;
import org.kframework.utils.errorsystem.KException;
import org.kframework.utils.general.GlobalSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andreiarusoaie on 09/05/14.
 */
public class KILToSMTLib extends NonCachingVisitor {

    private StringBuilder smtlibFormula;
    private Map<String, String> smtlibVariables;

    public KILToSMTLib(Context context) {
        super(context);
        smtlibFormula = new StringBuilder();
        smtlibVariables = new HashMap<>();
    }


    @Override
    public Void visit(KApp node, Void aVoid) throws RuntimeException {
        visitNode(node.getLabel(), aVoid);

        if (isSymbolic(node)) {
            GenericToken symbolicToken = getSymbolicToken(node);
            smtlibFormula.append(getSmtlibVarName(symbolicToken.value()));
            smtlibVariables.put(symbolicToken.value(), getSymbolicTokenSort(node));
        }

        visitNode(node.getChild(), aVoid);
        return null;
    }

    @Override
    public Void visit(TermCons node, Void aVoid) throws RuntimeException {
        String smtlibOperation = node.getProduction().getAttribute("smtlib");
        if (smtlibOperation != null) {
            smtlibFormula.append("(" + smtlibOperation + " ");
            for (Term child : node.getContents()) {
                visitNode(child, aVoid);
                smtlibFormula.append(" ");
            }
            if (!node.getContents().isEmpty()) {
                smtlibFormula.deleteCharAt(smtlibFormula.length() - 1);
            }
            smtlibFormula.append(")");
        } else {
            GlobalSettings.kem.register(new KException(KException.ExceptionType.ERROR, KException.KExceptionGroup.CRITICAL, "Could not translate to SMTLIB the term " + node + " because it's corresponding production does not have an smtlib attribute."));
        }
        return null;
    }

    @Override
    public Void visit(BoolBuiltin node, Void aVoid) throws RuntimeException {
        smtlibFormula.append(node.booleanValue());
        return null;
    }

    @Override
    public Void visit(IntBuiltin node, Void aVoid) throws RuntimeException {
        smtlibFormula.append(node.bigIntegerValue());
        return null;
    }


    public String getSmtlibQuery() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> smtlibVariable : smtlibVariables.entrySet()) {
            result.append("(set-logic AUFNIRA)\n(declare-fun ");
            result.append(getSmtlibVarName(smtlibVariable.getKey()));
            result.append(" () ");
            result.append(smtlibVariable.getValue());
            result.append(")\n");
        }
        result.append("(assert ");
        result.append(smtlibFormula);
        result.append(")");
        return result.toString();
    }

    private String getSmtlibVarName(String symbolicVar) {
        return "__var__" + symbolicVar;
    }

    private boolean isSymbolic(KApp kapp) {
        if (kapp.getLabel() instanceof KLabelConstant) {
            return ((KLabelConstant) kapp.getLabel()).getLabel().startsWith(AddSymbolicK.getSymbolicConstructorPrefix());
        }
        return false;
    }

    private GenericToken getSymbolicToken(KApp kapp) {
        java.util.List<Term> kappChildren = ((KList) kapp.getChild()).getContents();
        return (GenericToken) ((KApp) kappChildren.get(0)).getLabel();
    }

    private String getSymbolicTokenSort(KApp kapp) {
        if (kapp.getLabel() instanceof KLabelConstant) {
            return ((KLabelConstant) kapp.getLabel()).getLabel().substring(AddSymbolicK.getSymbolicConstructorPrefix().length());
        }
        GlobalSettings.kem.register(new KException(KException.ExceptionType.ERROR, KException.KExceptionGroup.CRITICAL, "Could not infer sort for " + kapp));
        return "";
    }

    public Map<String, String> getSmtlibVariables() {
        return smtlibVariables;
    }

}
