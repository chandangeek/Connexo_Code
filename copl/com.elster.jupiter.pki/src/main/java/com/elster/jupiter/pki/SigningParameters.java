package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;

/**
 * Holds the required parameters to generate a certificate. E.g. in case
 */
@ConsumerType
public interface SigningParameters {
    long getId();

    String getName();

    void setName(String name);

}
