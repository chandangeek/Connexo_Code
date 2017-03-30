/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class FancyJoinerTest {

    @Test
    public void testFancyJoinEmpty() {
        String result = Collections.<String>emptyList().stream()
                .collect(FancyJoiner.joining(", ", " and", "(", ")"));
        assertThat(result).isEqualTo("()");
    }

    @Test
    public void testFancyJoinSingle() {
        String result = Collections.singletonList("cheese").stream()
                .collect(FancyJoiner.joining(",", " and ", "(", ")"));
        assertThat(result).isEqualTo("(cheese)");
    }

    @Test
    public void testJustTwo() {
            String result = Arrays.asList("Laurel", "Hardy").stream()
                    .collect(FancyJoiner.joining(", ", " and ", "(", ")"));
            assertThat(result).isEqualTo("(Laurel and Hardy)");

    }

    @Test
    public void testMoreThanTwo() {
        String result = Arrays.asList("hook", "line", "sinker").stream()
                .collect(FancyJoiner.joining(", ", " and ", "(", ")"));
        assertThat(result).isEqualTo("(hook, line and sinker)");
    }

    @Test
    public void testALotMoreThanTwo() {
        String result = Pattern.compile("").splitAsStream("abcdefghijklmnopqrstuvwxyz")
                .collect(FancyJoiner.joining(", ", " and ", "(", ")"));
        assertThat(result).isEqualTo("(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y and z)");
    }
}