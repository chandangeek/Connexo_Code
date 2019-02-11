/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import javax.inject.Inject;

import static com.energyict.mdc.tou.campaign.rest.impl.RestUtil.getStatus;

public class DeviceInCampaignInfoFactory {

    private final TimeOfUseCampaignService timeOfUseCampaignService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;
    private ExceptionFactory exceptionFactory;

    @Inject
    public DeviceInCampaignInfoFactory(TimeOfUseCampaignService timeOfUseCampaignService, Thesaurus thesaurus,
                                       DeviceService deviceService, ExceptionFactory exceptionFactory) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
        this.exceptionFactory = exceptionFactory;
    }

    public DeviceInCampaignInfo createOnCancel(IdWithNameInfo idWithNameInfo) {
        long id = ((Number) idWithNameInfo.id).longValue();
        Device device = deviceService.findDeviceById(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_WITH_ID_NOT_FOUND, id));
        ServiceCall serviceCall = timeOfUseCampaignService.cancelDevice(device);
        return create(device, serviceCall);
    }

    public DeviceInCampaignInfo createOnRetry(IdWithNameInfo idWithNameInfo) {
        long id = ((Number) idWithNameInfo.id).longValue();
        Device device = deviceService.findDeviceById(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_WITH_ID_NOT_FOUND, id));
        ServiceCall serviceCall = timeOfUseCampaignService.retryDevice(device);
        return create(device, serviceCall);
    }

    public DeviceInCampaignInfo create(Device device, ServiceCall serviceCall) {
        return new DeviceInCampaignInfo(new IdWithNameInfo(device.getId(), device.getName()),
                getStatus(serviceCall.getState(), thesaurus),
                serviceCall.getCreationTime(),
                (serviceCall.getState().equals(DefaultState.CANCELLED)
                        || serviceCall.getState().equals(DefaultState.SUCCESSFUL)) ? serviceCall.getLastModificationTime() : null);
    }
}
