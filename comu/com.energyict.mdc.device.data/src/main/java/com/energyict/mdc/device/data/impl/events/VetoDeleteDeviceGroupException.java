package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 11:19
 */
public class VetoDeleteDeviceGroupException extends LocalizedException {

    public VetoDeleteDeviceGroupException(Thesaurus thesaurus, EndDeviceGroup deviceGroup) {
        super(thesaurus, MessageSeeds.VETO_DEVICEGROUP_DELETION, deviceGroup.getName());
    }

}