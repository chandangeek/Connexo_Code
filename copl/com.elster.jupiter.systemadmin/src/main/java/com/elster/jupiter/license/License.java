/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.license;

import java.time.Instant;

import java.util.Properties;

public interface License {

    enum Status {
        ACTIVE, EXPIRED
    }

    enum Type {
        EVALUATION, FULL
    }

    String getApplicationKey();

    Status getStatus();

    String getDescription();

    Instant getExpiration();

    Instant getActivation();

    int getGracePeriodInDays();

    Type getType();

    Properties getLicensedValues();

}
