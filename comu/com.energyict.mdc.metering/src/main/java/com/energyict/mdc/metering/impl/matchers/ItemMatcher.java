/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl.matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemMatcher implements Matcher<Integer> {

    public static ItemMatcher ELECTRICITY_MATCH = new ItemMatcher(1);
    public static ItemMatcher GAS_MATCH = new ItemMatcher(7);

    private final boolean positive;

    private List<Integer> matches = new ArrayList<>();

    private ItemMatcher(Integer... matches) {
        this(true, matches);
    }

    private ItemMatcher(boolean positive, Integer... matches) {
        this.positive = positive;
        Collections.addAll(this.matches, matches);
    }

    /**
     * Creates a matcher that will return true on {@link #match(Integer)} with the given matches.
     *
     * @param matches the matches that will return true
     * @return the newly created matcher.
     */
    public static final ItemMatcher itemMatcherFor(Integer... matches) {
        return new ItemMatcher(matches);
    }

    /**
     * Creates a matcher that will return false on {@link #match(Integer)} with the given matches.
     *
     * @param matches the matches that will return false
     * @return the newly created matcher
     */
    public static final ItemMatcher itemsDontMatchFor(Integer... matches) {
        return new ItemMatcher(false, matches);
    }

    @Override
    public boolean match(Integer field) {
        return positive ? matches.contains(field) : !matches.contains(field);
    }

    @Override
    public List<Integer> getAllMatches() {
        if(positive){
            return matches;
        } else {
            return getMatchesForNegative();
        }
    }

    private List<Integer> getMatchesForNegative() {
        List<Integer> allMatches = new ArrayList<>();
        for (int i = 0; i <= 255; i++) {
            if(!matches.contains(i)){
                allMatches.add(i);
            }
        }
        return allMatches;
    }
}
