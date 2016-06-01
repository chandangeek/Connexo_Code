package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.VetoUpdateObisCodeOnConfiguration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 31.05.16
 * Time: 10:34
 */
@Component(name = "com.energyict.mdc.device.data.impl.ChannelSpecUpdateEventHandler", service = TopicHandler.class, immediate = true)
public class ChannelSpecUpdateEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/config/channelspec/UPDATED";
    private volatile DeviceService deviceService;
    private volatile Thesaurus thesaurus;

    public ChannelSpecUpdateEventHandler() {
    }

    // for testing purposes
    @Inject
    public ChannelSpecUpdateEventHandler(DeviceService deviceService, Thesaurus thesaurus) {
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ChannelSpec channelSpec = (ChannelSpec) localEvent.getSource();
        List<Device> deviceWithOverruledObisCodeForOtherReadingType = ((ServerDeviceService) deviceService)
                .findDeviceWithOverruledObisCodeForOtherThanChannelSpec(channelSpec);
        if (deviceWithOverruledObisCodeForOtherReadingType.size() != 0) {
            throw new VetoUpdateObisCodeOnConfiguration(thesaurus, deviceWithOverruledObisCodeForOtherReadingType);
        }
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN));
    }

    private void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }
}
