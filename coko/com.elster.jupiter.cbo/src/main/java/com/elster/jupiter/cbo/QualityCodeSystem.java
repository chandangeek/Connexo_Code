package com.elster.jupiter.cbo;

import java.util.Arrays;
import java.util.Optional;

public enum QualityCodeSystem {
    NOTAPPLICABLE,
    ENDDEVICE,
    MDC,
    MDM,
    OTHER,
    EXTERNAL;

    public static Optional<QualityCodeSystem> get(int ordinal) {
        return Optional.ofNullable(ordinal < values().length ? values()[ordinal] : null);
    }

    public static QualityCodeSystem of(String systemName) {
        return Arrays.stream(values())
                .filter(system -> system.name().equalsIgnoreCase(systemName))
                .findAny()
                .orElse(NOTAPPLICABLE);
    }

    public static QualityCodeSystem ofApplication(String applicationName) {
        if(applicationName == null) {
            return NOTAPPLICABLE;
        }
        switch(applicationName) {
            case "MDC":
                return MDC;
            case "INS":
                return MDM;
            case "":
                return NOTAPPLICABLE;
            default:
                return OTHER;
        }
    }
}
