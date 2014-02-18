package com.energyict.mdc.metering.impl.matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Special case of a compositeMatcher. All matchers should match with the respective given fields.
 * <p/>
 * <b>The <i>ORDER</i> in which the fields are given in {@link #match(Integer...)} should be the same as the order
 * of the matchers that were added through {@link #createMatcherFor(Matcher[])}.
 * Eg. field[0] will be matched with matchers[0], field[1] will be matched with matchers[1], etc...</b>
 * <p/>
 *
 * Copyrights EnergyICT
 * Date: 19/12/13
 * Time: 11:41
 */
public class MultipleItemsMatcher implements Matcher<Integer[]>{

    private List<Matcher<Integer>> matchers = new ArrayList<>();

    @SafeVarargs
    public MultipleItemsMatcher(Matcher<Integer>... matchers) {
        Collections.addAll(this.matchers, matchers);
    }

    @Override
    public boolean match(Integer... fields) {
        if(fields.length != matchers.size()){
            return false;
        }
        boolean multiMatch = true;
        for (int i = 0; i < fields.length; i++) {
            multiMatch &= matchers.get(i).match(fields[i]);
        }
        return multiMatch;
    }

    @Override
    public List<Integer[]> getAllMatches() {
        List<Integer[]> allMatches = new ArrayList<>();
        for (Matcher<Integer> matcher : matchers) {
            allMatches.add(matcher.getAllMatches().toArray(new Integer[matcher.getAllMatches().size()]));
        }
        return allMatches;
    }

    /**
     * Creates a <i>Matcher</i> which will require multiple fields to match.
     * The amount of matchers that are provided should be the same amount as the number of
     * fields that will be passed to {@link #match(Integer...)}. It this is not the case,
     * then this matcher will always return false.
     *
     * @param matchers the matchers that need to match
     * @return the newly created MultipleItemsMatcher
     */
    public static final MultipleItemsMatcher createMatcherFor(Matcher<Integer>... matchers){
        return new MultipleItemsMatcher(matchers);
    }
}
