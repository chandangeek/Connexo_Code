/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceInFirmwareCampaignInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public DeviceInFirmwareCampaignInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public List<DeviceInFirmwareCampaignInfo> from(List<DeviceInFirmwareCampaign> devices){
        return devices.stream().map(device -> new DeviceInFirmwareCampaignInfo(device, thesaurus)).collect(Collectors.toList());
    }
}
