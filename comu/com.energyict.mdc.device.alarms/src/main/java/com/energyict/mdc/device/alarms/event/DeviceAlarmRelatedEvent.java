package com.energyict.mdc.device.alarms.event;


import com.elster.jupiter.metering.events.EndDeviceEventRecord;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DeviceAlarmRelatedEvent {
    EndDeviceEventRecord getEventRecord();

}
