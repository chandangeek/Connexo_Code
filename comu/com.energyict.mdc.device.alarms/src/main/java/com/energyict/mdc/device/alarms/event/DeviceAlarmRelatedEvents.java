package com.energyict.mdc.device.alarms.event;


import com.elster.jupiter.metering.readings.EndDeviceEvent;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DeviceAlarmRelatedEvents {
    EndDeviceEvent getEvent();

}
