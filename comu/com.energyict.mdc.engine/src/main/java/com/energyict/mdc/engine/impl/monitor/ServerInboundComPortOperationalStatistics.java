/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.monitor.InboundComPortOperationalStatistics;

public interface ServerInboundComPortOperationalStatistics extends InboundComPortOperationalStatistics {

    void notifyConnection();
    void deviceRecognized(String deviceId);

}
