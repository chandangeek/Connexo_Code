package com.elster.jupiter.license;

import java.time.Instant;

import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 8/04/2014
 * Time: 17:05
 */
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
