/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl.matchers;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeMatcherTest {

    private static final int MATCH_FIELD = 13;

    @Test
    public void singleMatcherTrueTest() {
        Matcher singleMatcher = mock(Matcher.class);
        when(singleMatcher.match(anyInt())).thenReturn(true);

        CompositeMatcher compositeMatcher = CompositeMatcher.createMatcherFor(singleMatcher);
        assertThat(compositeMatcher.match(MATCH_FIELD)).isTrue();
    }

    @Test
    public void singleMatcherFalseTest() {
        Matcher singleMatcher = mock(Matcher.class);
        when(singleMatcher.match(anyInt())).thenReturn(false);

        CompositeMatcher compositeMatcher = CompositeMatcher.createMatcherFor(singleMatcher);
        assertThat(compositeMatcher.match(MATCH_FIELD)).isFalse();
    }

    @Test
    public void dualMatcherTrueTest() {
        Matcher firstMatcher = mock(Matcher.class);
        when(firstMatcher.match(anyInt())).thenReturn(true);
        Matcher secondMatcher = mock(Matcher.class);
        when(secondMatcher.match(anyInt())).thenReturn(true);

        CompositeMatcher compositeMatcher = CompositeMatcher.createMatcherFor(firstMatcher, secondMatcher);
        assertThat(compositeMatcher.match(MATCH_FIELD)).isTrue();
    }

    @Test
    public void dualMatcherFalseTest() {
        Matcher firstMatcher = mock(Matcher.class);
        when(firstMatcher.match(anyInt())).thenReturn(false);
        Matcher secondMatcher = mock(Matcher.class);
        when(secondMatcher.match(anyInt())).thenReturn(false);

        CompositeMatcher compositeMatcher = CompositeMatcher.createMatcherFor(firstMatcher, secondMatcher);
        assertThat(compositeMatcher.match(MATCH_FIELD)).isFalse();
    }

    @Test
    public void dualMatcherFistTrueSecondFalseTest() {
        Matcher firstMatcher = mock(Matcher.class);
        when(firstMatcher.match(anyInt())).thenReturn(true);
        Matcher secondMatcher = mock(Matcher.class);
        when(secondMatcher.match(anyInt())).thenReturn(false);

        CompositeMatcher compositeMatcher = CompositeMatcher.createMatcherFor(firstMatcher, secondMatcher);
        assertThat(compositeMatcher.match(MATCH_FIELD)).isTrue();
    }

    @Test
    public void dualMatcherFistFalseSecondTrueTest() {
        Matcher firstMatcher = mock(Matcher.class);
        when(firstMatcher.match(anyInt())).thenReturn(false);
        Matcher secondMatcher = mock(Matcher.class);
        when(secondMatcher.match(anyInt())).thenReturn(true);

        CompositeMatcher compositeMatcher = CompositeMatcher.createMatcherFor(firstMatcher, secondMatcher);
        assertThat(compositeMatcher.match(MATCH_FIELD)).isTrue();
    }


}
