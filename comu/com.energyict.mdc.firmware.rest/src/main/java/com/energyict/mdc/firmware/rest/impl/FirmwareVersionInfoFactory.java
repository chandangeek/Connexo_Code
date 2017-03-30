/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class FirmwareVersionInfoFactory {
    private final Thesaurus thesaurus;
    private final FirmwareService firmwareService;

    @Inject
    public FirmwareVersionInfoFactory(Thesaurus thesaurus, FirmwareService firmwareService) {
        this.thesaurus = thesaurus;
        this.firmwareService = firmwareService;
    }

    public List<FirmwareVersionInfo> from(List<FirmwareVersion> firmwareVersions){
        return firmwareVersions.stream().map(this::from).collect(Collectors.toList());
    }

    public FirmwareVersionInfo from(FirmwareVersion firmwareVersion){
        FirmwareVersionInfo info = new FirmwareVersionInfo();
        info.id = firmwareVersion.getId();
        info.firmwareVersion = firmwareVersion.getFirmwareVersion();
        info.firmwareStatus = new FirmwareStatusInfo(firmwareVersion.getFirmwareStatus(), thesaurus);
        info.firmwareType = new FirmwareTypeInfo(firmwareVersion.getFirmwareType(), thesaurus);
        info.version = firmwareVersion.getVersion();
        return info;
    }

    public FirmwareVersionInfo fullInfo(FirmwareVersion firmwareVersion){
        FirmwareVersionInfo info = from(firmwareVersion);
        info.isInUse = firmwareService.isFirmwareVersionInUse(firmwareVersion.getId());
        return info;
    }
}
