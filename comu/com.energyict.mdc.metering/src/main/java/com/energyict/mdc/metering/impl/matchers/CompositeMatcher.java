package com.energyict.mdc.metering.impl.matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Combines several Matchers.
 * Calling {@link #match(T)} will delegate the match to the listed matchers.
 * If at least one match matches, then True is returned, false otherwise.
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/12/13
 * Time: 13:13
 */
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
