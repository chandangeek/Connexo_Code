/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.StillGatewayException;
import com.energyict.mdc.device.topology.TopologyService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Listens for delete events of {@link Device}s and will
 * veto the delete if the Device is the physical gateway
 * for at least one slave device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:43)
 */
@Component(name="com.energyict.mdc.device.topology.delete.physicalgateway", service = TopicHandler.class, immediate = true)
public class CannotDeletePhysicalGatewayEventHandler implements TopicHandler {

    private volatile TopologyService topologyService;
    private volatile Thesaurus thesaurus;

    // For OSGi purposes
    public CannotDeletePhysicalGatewayEventHandler() {
        super();
    }

    // For unit testing purposes
    public CannotDeletePhysicalGatewayEventHandler(TopologyService topologyService, Thesaurus thesaurus) {
        this();
        this.topologyService = topologyService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/device/BEFORE_DELETE";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.validateGatewayUsage((Device) localEvent.getSource());
    }

    private void validateGatewayUsage(Device gateway) {
        List<Device> physicalConnectedDevices = this.topologyService.findPhysicalConnectedDevices(gateway);
        if (!physicalConnectedDevices.isEmpty()) {
            throw StillGatewayException.forPhysicalGateway(this.thesaurus, gateway, MessageSeeds.DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY, physicalConnectedDevices.toArray(new Device[physicalConnectedDevices.size()]));
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TopologyService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

}