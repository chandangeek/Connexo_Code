package com.elster.jupiter.license;

import com.elster.jupiter.util.time.UtcInstant;

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

    UtcInstant getExpiration();

    UtcInstant getActivation();

    int getGracePeriodInDays();

    Type getType();

    Properties getLicensedValues();

}
