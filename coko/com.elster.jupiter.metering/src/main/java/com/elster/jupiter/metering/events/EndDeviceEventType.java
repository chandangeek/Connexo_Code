/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.events;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.cbo.IdentifiedObject;

public interface EndDeviceEventType extends IdentifiedObject {

    EndDeviceType getType();
    EndDeviceDomain getDomain();
    EndDeviceSubDomain getSubDomain();
    EndDeviceEventOrAction getEventOrAction();

}
