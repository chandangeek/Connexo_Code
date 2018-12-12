package com.energyict.mdc.device.topology.impl.multielement;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Changing the attributes of a multi-element device will result in copying the new values to all linked multi-element slaves
 * Copyrights EnergyICT
 * Date: 10/04/2017
 * Time: 10:16
 */
@Component(name="com.energyict.mdc.device.multielement.changed.attributes", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class MultiElementAttributeChangeHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/data/device/UPDATED";

    private volatile MultiElementDeviceService multiElementDeviceService;

    public MultiElementAttributeChangeHandler() {
    }

    @Inject
    public MultiElementAttributeChangeHandler(MultiElementDeviceService multiElementDeviceService) {
        this();
        this.multiElementDeviceService = multiElementDeviceService;
    }

    @Reference
    public void setMultiElementDeviceService(MultiElementDeviceService multiElementDeviceService) {
        this.multiElementDeviceService = multiElementDeviceService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        Device device = (Device) localEvent.getSource();
        if (device.getDeviceConfiguration().isMultiElementEnabled()) {
            this.multiElementDeviceService.syncSlaves(device);
        }
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

}
