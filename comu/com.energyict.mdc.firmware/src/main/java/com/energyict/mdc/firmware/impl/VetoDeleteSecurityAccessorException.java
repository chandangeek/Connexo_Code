/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.SecurityAccessorOnDeviceType;

import java.util.List;
import java.util.stream.Collectors;

public class VetoDeleteSecurityAccessorException extends LocalizedException {
    VetoDeleteSecurityAccessorException(Thesaurus thesaurus, List<SecurityAccessorOnDeviceType> securityAccessorOnDeviceTypeList) {
        super(thesaurus, MessageSeeds.VETO_SECURITY_ACCESSOR_DELETION,
                securityAccessorOnDeviceTypeList.stream()
                        .map(securityAccessorOnDeviceType -> securityAccessorOnDeviceType.getDeviceType().getName())
                        .distinct()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.joining(", ")));
    }
}
