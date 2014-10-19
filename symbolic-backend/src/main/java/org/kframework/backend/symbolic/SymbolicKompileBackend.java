// Copyright (c) 2014 K Team. All Rights Reserved.
package org.kframework.backend.symbolic;

import com.google.inject.Inject;
import org.kframework.backend.BasicBackend;
import org.kframework.backend.FirstStep;
import org.kframework.backend.LastStep;
import org.kframework.backend.maude.KompileBackend;
import org.kframework.backend.maude.MaudeBackend;
import org.kframework.backend.maude.MaudeBuiltinsFilter;
import org.kframework.backend.symbolic.transformers.*;
import org.kframework.compile.FlattenModules;
import org.kframework.compile.ResolveConfigurationAbstraction;
import org.kframework.compile.checks.CheckConfigurationCells;
import org.kframework.compile.checks.CheckRewrite;
import org.kframework.compile.checks.CheckVariables;
import org.kframework.compile.sharing.DeclareCellLabels;
import org.kframework.compile.tags.AddDefaultComputational;
import org.kframework.compile.tags.AddOptionalTags;
import org.kframework.compile.tags.AddStrictStar;
import org.kframework.compile.transformers.*;
import org.kframework.compile.utils.CheckVisitorStep;
import org.kframework.compile.utils.CompileDataStructures;
import org.kframework.compile.utils.CompilerSteps;
import org.kframework.compile.utils.InitializeConfigurationStructure;
import org.kframework.kil.Definition;
import org.kframework.kil.loader.Context;
import org.kframework.utils.Stopwatch;
import org.kframework.utils.file.FileUtil;
import org.kframework.utils.file.JarInfo;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by andrei on 10/17/14.
 */
public class SymbolicKompileBackend extends BasicBackend {

    public static final String SYMBOLIC = "symbolic-kompile";
    public static final String NOTSYMBOLIC = "not-symbolic-kompile";


    @Inject
    SymbolicKompileBackend(Stopwatch sw, Context context) {
        super(sw, context);
    }

    @Override
    public Definition firstStep(Definition javaDef) {
        Properties specialMaudeHooks = new Properties();
        Properties maudeHooks = new Properties();
        try {
            FileUtil.loadProperties(maudeHooks, KompileBackend.class, "MaudeHooksMap.properties");
            FileUtil.loadProperties(specialMaudeHooks, KompileBackend.class, "SpecialMaudeHooks.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
        MaudeBuiltinsFilter builtinsFilter = new MaudeBuiltinsFilter(maudeHooks, specialMaudeHooks, context);
        builtinsFilter.visitNode(javaDef);
        final String mainModule = javaDef.getMainModule();
        StringBuilder builtins = new StringBuilder()
                .append("mod ").append(mainModule).append("-BUILTINS is\n").append(" including ")
                .append(mainModule).append("-BASE .\n")
                .append(builtinsFilter.getResult()).append("endm\n");
        FileUtil.save(context.kompiled.getAbsolutePath() + "/builtins.maude", builtins);
        sw.printIntermediate("Generating equations for hooks");
        javaDef = (Definition) new DeleteFunctionRules(maudeHooks.stringPropertyNames(), context)
                .visitNode(javaDef);
        return super.firstStep(javaDef);
    }

    @Override
    public void run(Definition javaDef) {
        MaudeBackend maude = new MaudeBackend(sw, context);
        maude.run(javaDef);

        String load = "load \"" + JarInfo.getKBase(true) + JarInfo.MAUDE_LIB_DIR + "/k-prelude\"\n";

        // load libraries if any
        String maudeLib = "".equals(options.experimental.lib) ? "" : "load " + JarInfo.windowfyPath(new File(options.experimental.lib).getAbsolutePath()) + "\n";
        load += maudeLib;

        final String mainModule = javaDef.getMainModule();
        //String defFile = javaDef.getMainFile().replaceFirst("\\.[a-zA-Z]+$", "");

        StringBuilder main = new StringBuilder().append(load).append("load \"base.maude\"\n")
                .append("load \"builtins.maude\"\n").append("mod ").append(mainModule).append(" is \n")
                .append("  including ").append(mainModule).append("-BASE .\n")
                .append("  including ").append(mainModule).append("-BUILTINS .\n")
                .append("eq mainModule = '").append(mainModule).append(" .\nendm\n");
        FileUtil.save(context.kompiled.getAbsolutePath() + "/" + "main.maude", main);
    }

    @Override
    public String getDefaultStep() {
        return "LastStep";
    }

    @Override
    public boolean documentation() {
        return false;
    }

    @Override
    public boolean generatesDefinition() {
        return true;
    }

    @Override
    public CompilerSteps<Definition> getCompilationSteps() {
        CompilerSteps<Definition> steps = new CompilerSteps<Definition>(context);
        steps.add(new FirstStep(this, context));
        steps.add(new CheckVisitorStep<Definition>(new CheckConfigurationCells(context), context));
        steps.add(new RemoveBrackets(context));
        steps.add(new SetVariablesInferredSort(context));
        steps.add(new AddEmptyLists(context));
        steps.add(new RemoveSyntacticCasts(context));
//        steps.add(new EnforceInferredSorts(context));
        steps.add(new CheckVisitorStep<Definition>(new CheckVariables(context), context));
        steps.add(new CheckVisitorStep<Definition>(new CheckRewrite(context), context));
        steps.add(new FlattenModules(context));
        steps.add(new StrictnessToContexts(context));
        steps.add(new FreezeUserFreezers(context));
        steps.add(new ContextsToHeating(context));
        steps.add(new AddSupercoolDefinition(context));
        steps.add(new AddHeatingConditions(context));
        steps.add(new AddSuperheatRules(context));
        steps.add(new ResolveSymbolicInputStream(context)); //symbolic step
        steps.add(new DesugarStreams(context));
        steps.add(new ResolveFunctions(context));
        steps.add(new TagUserRules(context)); //symbolic step
        steps.add(new AddKCell(context));
        steps.add(new AddStreamCells(context));
        steps.add(new AddSymbolicK(context));
        steps.add(new AddSemanticEquality(context));
        steps.add(new ResolveFreshVarMOS(context));
        steps.add(new AddTopCellConfig(context));
        steps.add(new AddTopCellRules(context)); // required by symbolic
        steps.add(new AddConditionToConfig(context)); // symbolic step
        steps.add(new ResolveBinder(context));
        steps.add(new ResolveAnonymousVariables(context));
        steps.add(new AddK2SMTLib(context));
        steps.add(new AddPredicates(context));
        steps.add(new ResolveSyntaxPredicates(context));
        steps.add(new ResolveBuiltins(context));
        steps.add(new ResolveListOfK(context));
        steps.add(new FlattenSyntax(context));
        steps.add(new ResolveBlockingInput(context));
        steps.add(new InitializeConfigurationStructure(context));
        steps.add(new AddKStringConversion(context));
        steps.add(new AddKLabelConstant(context));
        steps.add(new ResolveHybrid(context));
        steps.add(new ResolveConfigurationAbstraction(context));
        steps.add(new ResolveOpenCells(context));
        steps.add(new ResolveRewrite(context));
        steps.add(new CompileDataStructures(context));
        steps.add(new Cell2DataStructure(context));
        steps.add(new ReplaceConstants(context)); // symbolic step
        steps.add(new AddPathCondition(context)); // symbolic step
        steps.add(new ResolveSupercool(context));
        steps.add(new AddStrictStar(context));
        steps.add(new AddDefaultComputational(context));
        steps.add(new AddOptionalTags(context));
        steps.add(new DeclareCellLabels(context));
        steps.add(new LastStep(this, context));
        return steps;
    }
}
