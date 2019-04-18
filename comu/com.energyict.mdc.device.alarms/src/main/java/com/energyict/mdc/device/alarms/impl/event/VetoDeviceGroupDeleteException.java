package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;

public class VetoDeviceGroupDeleteException extends LocalizedException {

    public VetoDeviceGroupDeleteException(Thesaurus thesaurus, EndDeviceGroup endDeviceGroup) {
        super(thesaurus, MessageSeeds.DEVICE_GROUP_IN_USE, endDeviceGroup.getName());
    }
}
