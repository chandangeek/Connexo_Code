package com.elster.jupiter.util;

import java.util.Optional;
import java.util.function.Supplier;

public enum YesNoAnswer {
    YES(() -> Optional.of(Boolean.TRUE)),
    NO(() -> Optional.of(Boolean.FALSE)),
    UNKNOWN(() -> Optional.empty());

    private Supplier<Optional<Boolean>> answer;

    YesNoAnswer(Supplier<Optional<Boolean>> answer) {
        this.answer = answer;
    }

    public Optional<Boolean> toOptional() {
        return answer.get();
    }
}
