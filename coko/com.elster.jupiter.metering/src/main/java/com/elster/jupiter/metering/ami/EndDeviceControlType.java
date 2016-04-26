package com.elster.jupiter.metering.ami;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;

public interface EndDeviceControlType {
    EndDeviceType getType();
    EndDeviceDomain getDomain();
    EndDeviceSubDomain getSubDomain();
    EndDeviceEventOrAction getEventOrAction();
}
