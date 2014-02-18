package com.energyict.mdc.metering.impl.matchers;

import java.util.Collections;
import java.util.List;

/**
 * Matcher can be used to match ObisCode fields.
 * The implementor should call {@link #match(T)} to validate if the given field matches this matcher.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/12/13
 * Time: 13:11
 */
public interface Matcher<T> {

    /**
     * Use this matcher if the outcome of the match doesn't affect the result.
     * Every field will match (true).
     */
    public static final Matcher DONT_CARE = new Matcher<Integer>() {
        @Override
        public boolean match(Integer field) {
            return true;
        }

        @Override
        public List<Integer> getAllMatches() {
            return Collections.emptyList();
        }
    };

    /**
     * Match the given field to this matcher
     *
     * @param field the field to match
     * @return true if the field matches, false otherwise
     */
    public boolean match(T field);

    /**
     * Returns a List with all possible match values
     *
     * @return the list with all possible match values
     */
    public List<T> getAllMatches();

}
