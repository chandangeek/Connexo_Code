/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;

import java.util.List;
import java.util.stream.Collectors;

class VetoDeleteSecurityAccessorTypeException extends LocalizedException {
    VetoDeleteSecurityAccessorTypeException(Thesaurus thesaurus, List<DeviceType> blockingDeviceTypes) {
        super(thesaurus, MessageSeeds.VETO_SECURITY_ACCESSOR_TYPE_DELETION,
                blockingDeviceTypes.stream()
                        .map(DeviceType::getName)
                        .distinct()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.joining(", ")));
    }
}
