// Copyright (c) 2012-2014 K Team. All Rights Reserved.
package org.kframework.kast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kframework.backend.maude.MaudeFilter;
import org.kframework.backend.unparser.IndentationOptions;
import org.kframework.backend.unparser.KastFilter;
import org.kframework.kil.ASTNode;
import org.kframework.kil.Sort;
import org.kframework.kil.Source;
import org.kframework.kil.loader.Context;
import org.kframework.main.FrontEnd;
import org.kframework.parser.ProgramLoader;
import org.kframework.utils.Stopwatch;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.file.Environment;
import org.kframework.utils.file.JarInfo;
import org.kframework.utils.inject.JCommanderModule;
import org.kframework.utils.inject.JCommanderModule.ExperimentalUsage;
import org.kframework.utils.inject.JCommanderModule.Usage;
import org.kframework.utils.inject.CommonModule;
import org.kframework.utils.inject.Main;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;

public class KastFrontEnd extends FrontEnd {

    public static List<Module> getModules(String[] args) {
        KastOptions options = new KastOptions();

        List<Module> modules = new ArrayList<>();
        modules.add(new KastModule(options));
        modules.add(new JCommanderModule(args));
        modules.add(new CommonModule());
        return modules;
    }

    private final KastOptions options;
    private final Provider<Context> contextProvider;
    private final Stopwatch sw;
    private final Map<String, String> env;
    private final ProgramLoader loader;

    @Inject
    KastFrontEnd(
            KastOptions options,
            @Main Provider<Context> contextProvider,
            @Usage String usage,
            @ExperimentalUsage String experimentalUsage,
            Stopwatch sw,
            KExceptionManager kem,
            JarInfo jarInfo,
            @Environment Map<String, String> env,
            ProgramLoader loader) {
        super(kem, options.global, usage, experimentalUsage, jarInfo);
        this.options = options;
        this.contextProvider = contextProvider;
        this.sw = sw;
        this.env = env;
        this.loader = loader;
    }

    /**
     *
     * @return true if the application terminated normally; false otherwise
     */
    @Override
    public boolean run() {
        String stringToParse = options.stringToParse();
        Source source = options.source();

        Context context = contextProvider.get();
        Sort sort = sort(options.sort, context);

        ASTNode out = loader.processPgm(stringToParse, source, sort, context, options.parser);
        StringBuilder kast;
        if (options.experimental.pretty) {
            IndentationOptions indentationOptions = new IndentationOptions(options.experimental.maxWidth(),
                    options.experimental.auxTabSize, options.experimental.tabSize);
            KastFilter kastFilter = new KastFilter(indentationOptions, options.experimental.nextLine, context);
            kastFilter.visitNode(out);
            kast = kastFilter.getResult();
        } else {
            MaudeFilter maudeFilter = new MaudeFilter(context);
            maudeFilter.visitNode(out);
            kast = maudeFilter.getResult();
            kast.append("\n");
        }

        System.out.print(kast.toString());

        sw.printIntermediate("Maudify Program");
        sw.printTotal("Total");
        return true;
    }


    private Sort sort(Sort sort, Context context) {
        if (sort == null) {
            if (env.get("KRUN_SORT") != null) {
                sort = Sort.of(env.get("KRUN_SORT"));
            } else {
                sort = context.startSymbolPgm;
            }
        }
        return sort;
    }
}
