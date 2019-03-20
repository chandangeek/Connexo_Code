package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;

import java.time.Instant;
import java.util.Objects;

public class ScheduleValidator {


    // this is really ugly but this is the framework in place
    public static final String START_ON_FE_COMPONENT = "start-on";

    public static void validate(Instant nextRun, Instant now) {
        if (Objects.isNull(nextRun) || Objects.isNull(now)) {
            throw new LocalizedFieldValidationException(MessageSeeds.COULD_NOT_VALIDATE_NEXTRUN, START_ON_FE_COMPONENT);
        }

        if (now.isAfter(nextRun)) {
            throw new LocalizedFieldValidationException(MessageSeeds.SCHEDULED_BEFORE_NOW, START_ON_FE_COMPONENT);
        }
    }


}
