package com.elster.jupiter.devtools.tests.assertions;

import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import org.assertj.core.api.Assertions;

import java.util.Optional;

public class JupiterAssertions extends Assertions {

    public static <T> OptionalAssert<T> assertThat(Optional<T> optional) {
        return new OptionalAssert<>(optional);
    }

    public static LogRecorderAssert assertThat(LogRecorder logRecorder) {
        return new LogRecorderAssert(logRecorder);
    }
}
