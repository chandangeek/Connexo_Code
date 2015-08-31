package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class VetoDeleteDeviceGroupException extends LocalizedException {

    public VetoDeleteDeviceGroupException(Thesaurus thesaurus, EndDeviceGroup deviceGroup) {
        super(thesaurus, MessageSeeds.VETO_DEVICEGROUP_DELETION, deviceGroup.getName());
    }

}
