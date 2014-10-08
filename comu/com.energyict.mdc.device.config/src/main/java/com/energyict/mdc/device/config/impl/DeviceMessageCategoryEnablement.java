package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 9/30/14
 * Time: 1:38 PM
 */
public class DeviceMessageCategoryEnablement extends DeviceMessageEnablementImpl<DeviceMessageCategoryEnablement> {

    @Inject
    public DeviceMessageCategoryEnablement(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(DeviceMessageCategoryEnablement.class, dataModel, eventService, thesaurus);
    }

    @Override
    public boolean isCategory() {
        return true;
    }

    @Override
    public DeviceMessageId getDeviceMessageId() {
        return null;
    }
}
