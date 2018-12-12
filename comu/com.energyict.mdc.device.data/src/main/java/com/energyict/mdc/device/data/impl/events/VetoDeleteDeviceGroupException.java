/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

public class VetoDeleteDeviceGroupException extends LocalizedException {

    public VetoDeleteDeviceGroupException(Thesaurus thesaurus,  MessageSeeds messageSeeds, EndDeviceGroup deviceGroup) {
        super(thesaurus, messageSeeds, deviceGroup.getName());
    }

}