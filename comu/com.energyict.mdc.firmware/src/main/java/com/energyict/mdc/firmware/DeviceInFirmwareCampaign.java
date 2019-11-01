/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface DeviceInFirmwareCampaign {

    Device getDevice();

    Optional<DeviceMessage> getDeviceMessage();

    ServiceCall getServiceCall();

    ServiceCall cancel(boolean initFromCampaign);

    ServiceCall retry();

    ServiceCall getParent();

    Instant getStartedOn();

    Instant getFinishedOn();

    long getId();

    boolean doesDeviceAlreadyHaveTheSameVersion();

    Optional<ComTaskExecution> findOrCreateFirmwareComTaskExecution();

    Optional<ComTaskExecution> findOrCreateVerificationComTaskExecution();

    void delete();
}
