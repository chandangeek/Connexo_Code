/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareType;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class FirmwareTypeInfos {

    public List<FirmwareTypeInfo> firmwareTypes;

    public FirmwareTypeInfos(EnumSet<FirmwareType> firmwareTypes, Thesaurus thesaurus) {
        this.firmwareTypes = firmwareTypes.stream()
                .map(firmwareType -> new FirmwareTypeInfo(firmwareType, thesaurus))
                .collect(Collectors.toList());
    }
}
