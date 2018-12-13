/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;

public interface EndDeviceControlType {
    EndDeviceType getType();

    EndDeviceDomain getDomain();

    EndDeviceSubDomain getSubDomain();

    EndDeviceEventOrAction getEventOrAction();

    String getAliasName();

    String getDescription();

    String getMRID();

    String getName();
}