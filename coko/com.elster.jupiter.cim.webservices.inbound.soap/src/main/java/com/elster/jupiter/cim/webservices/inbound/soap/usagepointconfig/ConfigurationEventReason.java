/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import java.util.Arrays;
import java.util.Optional;

enum ConfigurationEventReason {
    PURPOSE_ACTIVE("Purpose Active"),
    PURPOSE_INACTIVE("Purpose Inactive"),
    CHANGE_STATUS("Change Status");

    private String reasonText;

    ConfigurationEventReason(String reasonText) {
        this.reasonText = reasonText;
    }

    static Optional<ConfigurationEventReason> forReason(String reason) {
        return Arrays.stream(values())
                .filter(value -> value.reasonText.equals(reason))
                .findAny();
    }

    static String[] supportedReasons() {
        return Arrays.stream(values())
                .map(ConfigurationEventReason::getReason)
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
