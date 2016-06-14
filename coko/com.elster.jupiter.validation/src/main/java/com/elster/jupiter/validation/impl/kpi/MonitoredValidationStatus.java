package com.elster.jupiter.validation.impl.kpi;


import com.elster.jupiter.validation.ValidationResult;

import java.util.EnumSet;
import java.util.Set;

public enum MonitoredValidationStatus {

    SUSPECT {
        @Override
        public Set<ValidationResult> monitoredStatusses() {
            return EnumSet.complementOf(EnumSet.of(ValidationResult.SUSPECT));
        }
    };

    public abstract Set<ValidationResult> monitoredStatusses();

}

