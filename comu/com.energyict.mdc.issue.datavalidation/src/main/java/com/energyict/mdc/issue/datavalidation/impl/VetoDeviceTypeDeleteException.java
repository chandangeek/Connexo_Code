/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.config.DeviceType;

public class VetoDeviceTypeDeleteException extends LocalizedException {
    public VetoDeviceTypeDeleteException(Thesaurus thesaurus, DeviceType deviceType, MessageSeeds message) {
        super(thesaurus, message, deviceType.getName());
    }
}
