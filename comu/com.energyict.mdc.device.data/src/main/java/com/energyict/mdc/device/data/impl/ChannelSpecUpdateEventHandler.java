/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.energyict.mdc.device.data.impl.ChannelSpecUpdateEventHandler", service = TopicHandler.class, immediate = true)
public class ChannelSpecUpdateEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/config/channelspec/UPDATED";
    private volatile ServerDeviceService deviceService;
    private volatile Thesaurus thesaurus;

    public ChannelSpecUpdateEventHandler() {
    }

    // for testing purposes
    @Inject
    public ChannelSpecUpdateEventHandler(DeviceDataModelService deviceDataModelService, Thesaurus thesaurus) {
        this.deviceService = deviceDataModelService.deviceService();
        this.thesaurus = thesaurus;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ChannelSpec channelSpec = (ChannelSpec) localEvent.getSource();
        List<Device> deviceWithOverruledObisCodeForOtherReadingType = deviceService
                .findDeviceWithOverruledObisCodeForOtherThanChannelSpec(channelSpec);
        if (!deviceWithOverruledObisCodeForOtherReadingType.isEmpty()) {
            throw new VetoUpdateObisCodeOnConfiguration(thesaurus, deviceWithOverruledObisCodeForOtherReadingType);
        }
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceService = deviceDataModelService.deviceService();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN));
    }

    private void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }
}
