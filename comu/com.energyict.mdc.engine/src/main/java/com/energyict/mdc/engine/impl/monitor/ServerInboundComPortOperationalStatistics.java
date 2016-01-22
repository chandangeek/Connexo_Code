package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.monitor.InboundComPortOperationalStatistics;

public interface ServerInboundComPortOperationalStatistics extends InboundComPortOperationalStatistics {

    void notifyConnection();
    void deviceRecognized(String deviceId);

}
