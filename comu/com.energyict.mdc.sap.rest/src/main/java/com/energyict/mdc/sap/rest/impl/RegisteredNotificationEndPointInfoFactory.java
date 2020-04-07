/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

public class RegisteredNotificationEndPointInfoFactory {

    public RegisteredNotificationEndPointInfo from(EndPointConfiguration endPointConfiguration) {
        return new RegisteredNotificationEndPointInfo(endPointConfiguration.getId(),
                endPointConfiguration.getName(),
                endPointConfiguration.getVersion());
    }
}
