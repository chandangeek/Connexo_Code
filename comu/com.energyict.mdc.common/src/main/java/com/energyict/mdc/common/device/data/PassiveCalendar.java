/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.protocol.DeviceMessage;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Optional;

@ConsumerType
public interface PassiveCalendar extends HasId {

    Instant getActivationDate();

    AllowedCalendar getAllowedCalendar();

    Optional<DeviceMessage> getDeviceMessage();

    void setDeviceMessage(DeviceMessage deviceMessage);

}