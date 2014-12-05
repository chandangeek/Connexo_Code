package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.StillGatewayException;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Listens for delete events of {@link Device}s and will
 * veto the delete if the Device is the communication gateway
 * for at least one slave device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:43)
 */
@Component(name="com.energyict.mdc.device.topology.delete.physicalgateway", service = TopicHandler.class, immediate = true)
public class CannotDeleteCommunicationGatewayEventHandler implements TopicHandler {

    private volatile TopologyService topologyService;
    private volatile Thesaurus thesaurus;

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/device/BEFORE_DELETE";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.validateGatewayUsage((Device) localEvent.getSource());
    }

    private void validateGatewayUsage(Device gateway) {
        List<Device> communicationReferencingDevices = this.topologyService.getCommunicationReferencingDevices();
        if (!communicationReferencingDevices.isEmpty()) {
            throw StillGatewayException.forCommunicationGateway(this.thesaurus, gateway, communicationReferencingDevices.toArray(new Device[communicationReferencingDevices.size()]));
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