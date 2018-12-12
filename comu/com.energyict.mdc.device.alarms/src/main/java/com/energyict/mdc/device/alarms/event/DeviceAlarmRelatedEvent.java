/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.event;


import com.elster.jupiter.metering.events.EndDeviceEventRecord;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DeviceAlarmRelatedEvent {
    EndDeviceEventRecord getEventRecord();

}
