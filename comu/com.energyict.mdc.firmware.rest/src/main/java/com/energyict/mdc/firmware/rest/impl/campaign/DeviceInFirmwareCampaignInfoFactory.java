/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;

import static com.energyict.mdc.firmware.rest.impl.campaign.CampaignRestUtil.getDeviceStatus;

public class DeviceInFirmwareCampaignInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public DeviceInFirmwareCampaignInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceInFirmwareCampaignInfo create(Device device, ServiceCall serviceCall) {
        return new DeviceInFirmwareCampaignInfo(new IdWithNameInfo(device.getId(), device.getName()),
                getDeviceStatus(serviceCall.getState(), thesaurus),
                serviceCall.getCreationTime(),
                (serviceCall.getState().equals(DefaultState.CANCELLED)
                        || serviceCall.getState().equals(DefaultState.SUCCESSFUL)) ? serviceCall.getLastModificationTime() : null);
    }
}
