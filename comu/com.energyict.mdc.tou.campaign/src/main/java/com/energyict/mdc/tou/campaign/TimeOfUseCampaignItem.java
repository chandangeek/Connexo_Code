/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import java.util.Optional;

public interface TimeOfUseCampaignItem {
    Device getDevice();

    Optional<DeviceMessage> getDeviceMessage();

    ServiceCall getServiceCall();

    ServiceCall retry();

    ServiceCall cancel();

    long getParentServiceCallId();
}
