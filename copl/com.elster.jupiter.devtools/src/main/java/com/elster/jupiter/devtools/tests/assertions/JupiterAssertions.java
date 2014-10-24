package com.elster.jupiter.devtools.tests.assertions;

import org.assertj.core.api.Assertions;

import java.util.Optional;

public class JupiterAssertions extends Assertions {

    public static <T> OptionalAssert<T> assertThat(Optional<T> optional) {
        return new OptionalAssert<>(optional);
    }
}
