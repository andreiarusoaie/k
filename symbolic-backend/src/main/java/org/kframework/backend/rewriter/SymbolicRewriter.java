package org.kframework.backend.rewriter;

import org.kframework.RewriterResult;
import org.kframework.definition.Rule;
import org.kframework.kore.K;
import org.kframework.kore.KVariable;
import org.kframework.rewriter.Rewriter;
import org.kframework.rewriter.SearchType;
import scala.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Andrei on 10/20/15.
 */
public class SymbolicRewriter implements Rewriter {

    @Override
    public RewriterResult execute(K k, Optional<Integer> depth) {
        throw new UnsupportedOperationException("run not supported");
    }

    @Override
    public List<? extends Map<? extends KVariable, ? extends K>> match(K k, Rule rule) {
        throw new UnsupportedOperationException("match not supported");
    }

    @Override
    public List<? extends Map<? extends KVariable, ? extends K>> search(K initialConfiguration, Optional<Integer> depth, Optional<Integer> bound, Rule pattern, SearchType searchType) {
        throw new UnsupportedOperationException("search not supported");
    }

    @Override
    public Tuple2<RewriterResult, List<? extends Map<? extends KVariable, ? extends K>>> executeAndMatch(K k, Optional<Integer> depth, Rule rule) {
        throw new UnsupportedOperationException("exec and match not supported");
    }

    @Override
    public List<K> prove(List<Rule> rules) {
        throw new UnsupportedOperationException("prove not supported");
    }
}
