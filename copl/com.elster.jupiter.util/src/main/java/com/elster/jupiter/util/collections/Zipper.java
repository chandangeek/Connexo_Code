/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import com.elster.jupiter.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Zipper<A, B> {

    private final Matcher<? super A, ? super B> matcher;

    public Zipper(Matcher<? super A, ? super B> matcher) {
        this.matcher = matcher;
    }

    public interface Matcher<X, Y> {
        boolean match(X x, Y y);
    }

    public List<Pair<A, B>> zip(Collection<? extends A> first, Collection<? extends B> second) {
        LeftOvers leftOvers = new LeftOvers(second);

        List<Pair<A, B>> pairs = first.stream()
                .map(a -> Pair.of((A) a, leftOvers.pickFor(a)))
                .collect(Collectors.toCollection(ArrayList::new));
        
        leftOvers.getLeftOvers().stream()
                .map(b -> Pair.of((A) null, b))
                .forEach(pairs::add);
        
        return pairs;
    }

    private final class LeftOvers {
        private final List<? extends B> leftOvers;

        private LeftOvers(Collection<? extends B> leftOvers) {
            this.leftOvers = new ArrayList<>(leftOvers);
        }

        private B pickFor(A a) {
            for (Iterator<? extends B> iterator = leftOvers.iterator(); iterator.hasNext(); ) {
                B b = iterator.next();
                if (matcher.match(a, b)) {
                    iterator.remove();
                    return b;
                }
            }
            return null;
        }
        
        private List<? extends B> getLeftOvers() {
            return leftOvers;
        }
    }
}
