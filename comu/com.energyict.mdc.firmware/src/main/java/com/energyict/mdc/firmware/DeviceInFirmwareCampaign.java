/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface DeviceInFirmwareCampaign {

    Device getDevice();

    Optional<DeviceMessage> getDeviceMessage();

    ServiceCall getServiceCall();

    ServiceCall cancel();

    ServiceCall retry();

    ServiceCall getParent();

    Instant getStartedOn();

    Instant getFinishedOn();

    long getId();

    void delete();
}
