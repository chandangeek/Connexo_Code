/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

/**
 * Defines constants for properties of serial {@link ConnectionType}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-04 (17:17)
 */
public enum SerialConnectionPropertyNames {

    /**
     * The name of the {@link ConnectionProperty}
     * that will hold the name of the serial port that is used to connect.
     */
    COMPORT_NAME_PROPERTY_NAME {
        @Override
        public String propertyName () {
            return "SERIAL_COMPORT_NAME";
        }
    };

    public abstract String propertyName ();

}