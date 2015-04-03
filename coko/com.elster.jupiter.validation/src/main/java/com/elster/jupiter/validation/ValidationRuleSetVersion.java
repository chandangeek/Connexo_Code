package com.elster.jupiter.validation;

import java.time.Instant;

public interface ValidationRuleSetVersion {

    long getId();

    String getName();

    Instant getStartDate();

    void save();

    void delete();
}
