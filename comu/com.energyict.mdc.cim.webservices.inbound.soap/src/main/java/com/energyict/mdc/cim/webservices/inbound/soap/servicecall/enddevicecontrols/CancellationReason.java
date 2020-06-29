/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols;

import java.util.stream.Stream;

public enum CancellationReason {
    NOT_CANCELLED("-"),
    UNKNOWN_REASON("Unknown reason"),
    TIMEOUT("Timeout"),
    CREATE_ERROR("Create error"),
    MANUALLY("Manually"),
    CANCEL_END_DEVICE_CONTROLS("CancelEndDeviceControls request");

    private String name;

    CancellationReason(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CancellationReason valueFor(String strValue) {
        return Stream
                .of(values())
                .filter(each -> each.getName().equals(strValue))
                .findAny()
                .orElse(UNKNOWN_REASON);
    }
}
