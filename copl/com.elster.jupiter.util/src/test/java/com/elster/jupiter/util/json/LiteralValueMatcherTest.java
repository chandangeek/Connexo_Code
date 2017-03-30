/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class LiteralValueMatcherTest {

    @Test
    public void testMatchesTrue() throws Exception {
        assertThat(new LiteralValueMatcher(asList("A", "B", "C")).matches(asList("A", "B", "C"))).isTrue();
    }

    @Test
    public void testMatchesFalse() throws Exception {
        assertThat(new LiteralValueMatcher(asList("A", "B", "C")).matches(asList("A", "B"))).isFalse();
    }

}
