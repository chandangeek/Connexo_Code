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
        return systemName == null || systemName.isEmpty() ?
                NOTAPPLICABLE :
                Arrays.stream(values())
                        .filter(system -> system.name().equalsIgnoreCase(systemName))
                        .findAny()
                        .orElse(OTHER);
    }

    public static QualityCodeSystem ofApplication(String applicationName) {
        // TODO: bad architecture; somehow need to move such a mapper out of kore
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
