package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;

/**
 * Holds the required parameters to generate a certificate. In case Connexo needs to trigger/manage the certification
 * process, additional parameters may be required to support certification with an external CA, either connected or
 * disconnected. E.g. in the EJBCA WS case, Connexo will need to provide the EJBCA parameters ca name, EE profile name and
 * certificate profile name.
 * These parameters can be added by means of custom property sets.
 */
@ConsumerType
public interface SigningParameters {
    /**
     * DB Identifier
     */
    long getId();

    /**
     * The name is the human readable identifier of the parameter set.
     */
    String getName();

    void setName(String name);
}
