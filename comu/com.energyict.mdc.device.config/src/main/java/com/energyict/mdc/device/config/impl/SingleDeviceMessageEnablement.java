package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 9/30/14
 * Time: 1:38 PM
 */
public class SingleDeviceMessageEnablement extends DeviceMessageEnablementImpl<SingleDeviceMessageEnablement> {

    static SingleDeviceMessageEnablement from(DataModel dataModel, DeviceCommunicationConfigurationImpl deviceCommunicationConfiguration, DeviceMessageId deviceMessageId) {
        return dataModel.getInstance(SingleDeviceMessageEnablement.class).init(deviceCommunicationConfiguration, deviceMessageId);
    }

    private DeviceMessageId deviceMessageId;

    @Inject
    public SingleDeviceMessageEnablement(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(SingleDeviceMessageEnablement.class, dataModel, eventService, thesaurus);
    }

    private SingleDeviceMessageEnablement init(DeviceCommunicationConfiguration deviceCommunicationConfiguration, DeviceMessageId deviceMessageId) {
        setDeviceCommunicationConfiguration(deviceCommunicationConfiguration);
        this.deviceMessageId = deviceMessageId;
        return this;
    }

    @Override
    public DeviceMessageId getDeviceMessageId() {
        return deviceMessageId;
    }

    @Override
    public boolean isSpecificMessage() {
        return true;
    }

}
