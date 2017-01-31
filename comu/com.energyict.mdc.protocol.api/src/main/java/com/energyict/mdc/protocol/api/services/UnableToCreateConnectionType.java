/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

/**
 * Models the exceptional situation that occurs when a {@link com.energyict.mdc.protocol.api.ConnectionType}
 * could not be created due to some exception cause by the java reflection layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (10:33)
 */
public class UnableToCreateConnectionType extends RuntimeException {

    public UnableToCreateConnectionType(Throwable cause, String javaClassName) {
        super("Unable to create ConnectionType from class " + javaClassName, cause);
    }

}