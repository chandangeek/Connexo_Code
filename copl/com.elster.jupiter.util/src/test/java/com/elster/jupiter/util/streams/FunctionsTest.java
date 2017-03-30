/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static java.util.stream.Collectors.toList;

public class FunctionsTest {

    @Test
    public void testAsStream() throws Exception {
        List<Wrapper> wrappers = Arrays.asList(new Wrapper("A"), new Wrapper("B"), new Wrapper(null), new Wrapper("D"), new Wrapper(null), new Wrapper("F"));

        List<String> strings = wrappers.stream()
                .map(Wrapper::getValue)
                .flatMap(Functions.asStream())
                .collect(toList());

        assertThat(strings).containsExactly("A", "B", "D", "F");
    }

    private class Wrapper {
        private String value;

        private Wrapper(String value) {
            this.value = value;
        }

        public Optional<String> getValue() {
            return Optional.ofNullable(value);
        }
    }
}