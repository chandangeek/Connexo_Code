/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl.matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeMatcher<T> implements Matcher<T> {

    private List<Matcher<T>> matchers = new ArrayList<>();

    @SafeVarargs
    private CompositeMatcher(Matcher<T>... matchers) {
        Collections.addAll(this.matchers, matchers);
    }

    @Override
    public boolean match(T field) {
        for (Matcher<T> matcher : matchers) {
            if (matcher.match(field)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<T> getAllMatches() {
        List<T> allMatches = new ArrayList<>();
        for (Matcher<T> matcher : matchers) {
            allMatches.addAll(matcher.getAllMatches());
        }
        return allMatches;
    }

    public static final CompositeMatcher createMatcherFor(Matcher... matchers) {
        return new CompositeMatcher(matchers);
    }
}
