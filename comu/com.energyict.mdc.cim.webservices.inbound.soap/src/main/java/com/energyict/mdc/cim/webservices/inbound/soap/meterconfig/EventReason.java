package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import java.util.Arrays;
import java.util.Optional;

enum EventReason {

    CHANGE_MULTIPLIER("changeMultiplier"),
    CHANGE_STATUS("changeStatus"),
    ;

    private String reasonText;

    EventReason(String reasonText) {
        this.reasonText = reasonText;
    }

    static Optional<EventReason> forReason(String reason) {
        return Arrays.stream(values())
                .filter(value -> value.reasonText.equals(reason))
                .findAny();
    }

    static String[] supportedReasons() {
        return Arrays.stream(values())
                .map(EventReason::getReason)
                .toArray(String[]::new);
    }

    String getReason() {
        return reasonText;
    }

    @Override
    public String toString() {
        return getReason();
    }
}