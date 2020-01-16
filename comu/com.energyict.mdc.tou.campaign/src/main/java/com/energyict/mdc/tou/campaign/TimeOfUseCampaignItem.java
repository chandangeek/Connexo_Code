/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface TimeOfUseCampaignItem {
    Device getDevice();

    Optional<DeviceMessage> getDeviceMessage();

    ServiceCall getServiceCall();

    ServiceCall cancel(boolean initFromCampaign);

    ServiceCall retry();

    long getParentServiceCallId();

    long getStepOfUpdate();

    Optional<ComTaskExecution> findOrCreateVerificationComTaskExecution();

    Optional<ComTaskExecution> findOrCreateUploadComTaskExecution();
}
