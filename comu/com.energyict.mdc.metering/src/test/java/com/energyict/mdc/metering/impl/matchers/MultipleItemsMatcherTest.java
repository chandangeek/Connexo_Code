/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl.matchers;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultipleItemsMatcherTest {

    private static final int MATCH_FIELD = 13;

    @Test
    public void singleMatcherTrueTest() {
        Matcher singleMatcher = mock(Matcher.class);
        when(singleMatcher.match(anyInt())).thenReturn(true);

        MultipleItemsMatcher multiItemMatcher = MultipleItemsMatcher.createMatcherFor(singleMatcher);
        assertThat(multiItemMatcher.match(MATCH_FIELD)).isTrue();
    }

    @Test
    public void singleMatcherFalseTest() {
        Matcher singleMatcher = mock(Matcher.class);
        when(singleMatcher.match(anyInt())).thenReturn(false);

        MultipleItemsMatcher multiItemMatcher = MultipleItemsMatcher.createMatcherFor(singleMatcher);
        assertThat(multiItemMatcher.match(MATCH_FIELD)).isFalse();
    }

    @Test
    public void multipleMatchersTrueTest() {
        Matcher firstMatcher = mock(Matcher.class);
        when(firstMatcher.match(anyInt())).thenReturn(true);
        Matcher secondMatcher = mock(Matcher.class);
        when(secondMatcher.match(anyInt())).thenReturn(true);

        MultipleItemsMatcher multiItemMatcher = MultipleItemsMatcher.createMatcherFor(firstMatcher, secondMatcher);
        assertThat(multiItemMatcher.match(MATCH_FIELD, MATCH_FIELD)).isTrue();
    }

    @Test
    public void multipleMatchersToLittleArgumentsTest() {
        Matcher firstMatcher = mock(Matcher.class);
        when(firstMatcher.match(anyInt())).thenReturn(true);
        Matcher secondMatcher = mock(Matcher.class);
        when(secondMatcher.match(anyInt())).thenReturn(true);

        MultipleItemsMatcher multiItemMatcher = MultipleItemsMatcher.createMatcherFor(firstMatcher, secondMatcher);
        assertThat(multiItemMatcher.match(MATCH_FIELD)).isFalse();
    }

    @Test
    public void multipleMatchersFalseTest() {
        Matcher firstMatcher = mock(Matcher.class);
        when(firstMatcher.match(anyInt())).thenReturn(false);
        Matcher secondMatcher = mock(Matcher.class);
        when(secondMatcher.match(anyInt())).thenReturn(false);

        MultipleItemsMatcher multiItemMatcher = MultipleItemsMatcher.createMatcherFor(firstMatcher, secondMatcher);
        assertThat(multiItemMatcher.match(MATCH_FIELD, MATCH_FIELD)).isFalse();
    }

    @Test
    public void dualMatcherFistTrueSecondFalseTest() {
        Matcher firstMatcher = mock(Matcher.class);
        when(firstMatcher.match(anyInt())).thenReturn(true);
        Matcher secondMatcher = mock(Matcher.class);
        when(secondMatcher.match(anyInt())).thenReturn(false);

        MultipleItemsMatcher multiItemMatcher = MultipleItemsMatcher.createMatcherFor(firstMatcher, secondMatcher);
        assertThat(multiItemMatcher.match(MATCH_FIELD, MATCH_FIELD)).isFalse();
    }

    @Test
    public void dualMatcherFistFalseSecondTrueTest() {
        Matcher firstMatcher = mock(Matcher.class);
        when(firstMatcher.match(anyInt())).thenReturn(false);
        Matcher secondMatcher = mock(Matcher.class);
        when(secondMatcher.match(anyInt())).thenReturn(true);

        MultipleItemsMatcher multiItemMatcher = MultipleItemsMatcher.createMatcherFor(firstMatcher, secondMatcher);
        assertThat(multiItemMatcher.match(MATCH_FIELD, MATCH_FIELD)).isFalse();
    }

}
