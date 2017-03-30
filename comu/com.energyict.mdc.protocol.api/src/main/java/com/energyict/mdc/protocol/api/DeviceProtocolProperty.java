/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

/**
 * Provides the names of commonly used properties
 * of {@link DeviceProtocol}s.
 * Note that those properties could be general,
 * related to a protocol dialect or related to
 * the connection that is established
 * to talk to the actual Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-15 (16:23)
 */
public enum DeviceProtocolProperty {

    CALL_HOME_ID("callHomeId"),
    DEVICE_TIME_ZONE("deviceTimeZone"),
    PHONE_NUMBER("phoneNumber");

    DeviceProtocolProperty(String javaName) {
        this.javaName = javaName;
    }

    private final String javaName;

    public String javaFieldName() {
        return javaName;
    }

}