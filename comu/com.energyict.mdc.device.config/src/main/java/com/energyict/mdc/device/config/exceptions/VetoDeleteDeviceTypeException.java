/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.MessageSeeds;

public class VetoDeleteDeviceTypeException extends LocalizedException {
    public VetoDeleteDeviceTypeException(Thesaurus thesaurus, DeviceType deviceType) {
        super(thesaurus, MessageSeeds.VETO_DEVICETYPE_DELETION, deviceType.getName());
    }
}
