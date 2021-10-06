/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;

import javax.inject.Inject;

public class DeviceInFirmwareCampaignInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public DeviceInFirmwareCampaignInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceInFirmwareCampaignInfo createInfo(DeviceInFirmwareCampaign deviceInFirmwareCampaign) {
        Device device = deviceInFirmwareCampaign.getDevice();
        DefaultState defaultState = deviceInFirmwareCampaign.getServiceCall().getState();
        return new DeviceInFirmwareCampaignInfo(deviceInFirmwareCampaign.getId(),
                new IdWithNameInfo(device.getId(), device.getName()),
                StatusInfoFactory.getDeviceStatus(defaultState, thesaurus),
                deviceInFirmwareCampaign.getStartedOn(),
                deviceInFirmwareCampaign.getFinishedOn());
    }
}
