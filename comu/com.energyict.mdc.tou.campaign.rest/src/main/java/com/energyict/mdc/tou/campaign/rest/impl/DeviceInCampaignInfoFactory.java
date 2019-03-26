/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;

import static com.energyict.mdc.tou.campaign.rest.impl.RestUtil.getDeviceStatus;

public class DeviceInCampaignInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public DeviceInCampaignInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceInCampaignInfo create(Device device, ServiceCall serviceCall) {
        return new DeviceInCampaignInfo(new IdWithNameInfo(device.getId(), device.getName()),
                getDeviceStatus(serviceCall.getState(), thesaurus),
                serviceCall.getCreationTime(),
                (serviceCall.getState().equals(DefaultState.CANCELLED)
                        || serviceCall.getState().equals(DefaultState.SUCCESSFUL)) ? serviceCall.getLastModificationTime() : null);
    }
}
