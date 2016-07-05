package com.energyict.mdc.device.data;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface PassiveEffectiveCalendar extends HasId{

    Instant getActivationDate();

    AllowedCalendar getAllowedCalendar();

    Optional<DeviceMessage> getDeviceMessage();

    void setDeviceMessage(DeviceMessage deviceMessage);

}
