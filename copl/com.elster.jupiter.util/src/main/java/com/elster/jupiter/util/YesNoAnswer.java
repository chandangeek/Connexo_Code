/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.Optional;
import java.util.function.Supplier;

public enum YesNoAnswer {
    YES("Yes", () -> Optional.of(Boolean.TRUE)),
    NO("No", () -> Optional.of(Boolean.FALSE)),
    UNKNOWN("Unknown", () -> Optional.empty());

    private Supplier<Optional<Boolean>> answer;
    private String answerId;

    YesNoAnswer(String answerId, Supplier<Optional<Boolean>> answer) {
        this.answerId = answerId;
        this.answer = answer;
    }

    @Override
    public String toString() {
        return answerId;
    }

    public Optional<Boolean> toOptional() {
        return answer.get();
    }
}
