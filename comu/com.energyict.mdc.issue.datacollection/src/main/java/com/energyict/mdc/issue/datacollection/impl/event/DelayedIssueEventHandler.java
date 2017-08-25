/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.event.UnregisteredFromGatewayDelayedEvent;

import com.google.inject.Injector;

import javax.swing.text.html.Option;
import javax.validation.MessageInterpolator;
import java.util.Map;
import java.util.Optional;

public class DelayedIssueEventHandler implements MessageHandler {
    private final Injector injector;

    public DelayedIssueEventHandler(Injector injector) {
        this.injector = injector;
    }

    protected JsonService getJsonService() {
        return injector.getInstance(JsonService.class);
    }

    protected DeviceService getDeviceService() {
        return injector.getInstance(DeviceService.class);
    }

    private TopologyService getTopologyService() {
        return injector.getInstance(TopologyService.class);
    }

    private IssueCreationService getIssueCreationService() {
        return injector.getInstance(IssueCreationService.class);
    }

    public IssueDataCollectionService getIssueDataCollectionService() {
        return injector.getInstance(IssueDataCollectionService.class);
    }

    public MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    public CommunicationTaskService getCommunicationTaskService() {
        return injector.getInstance(CommunicationTaskService.class);
    }

    public Thesaurus getThesaurus() {
        return injector.getInstance(Thesaurus.class);
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = getJsonService().deserialize(message.getPayload(), Map.class);
        long deviceIdentifier = ((Number) map.get("deviceIdentifier")).longValue();
        long gatewayIdentifier  = ((Number) map.get("gatewayIdentifier")).longValue();
        long ruleId = ((Number) map.get("ruleId")).longValue();
        Optional<Device> device = getDeviceService().findDeviceById(deviceIdentifier);
        Optional<Device> gateway = getDeviceService().findDeviceById(gatewayIdentifier);
        device.ifPresent(dev -> this.checkPhysicalGateway(dev, ruleId, gateway));
    }

    private void checkPhysicalGateway(Device device, long ruleId, Optional<Device> gateway) {
        Optional<Device> physicalGateway = getTopologyService().getPhysicalGateway(device);
        if (!physicalGateway.isPresent()) {
            UnregisteredFromGatewayDelayedEvent unregisteredFromGatewayDelayedEvent = new UnregisteredFromGatewayDelayedEvent(device, gateway, getIssueDataCollectionService(), getMeteringService(), getDeviceService(),
                    getCommunicationTaskService(), getTopologyService(), getThesaurus(), injector);
            getIssueCreationService().processIssueCreationEvent(ruleId,unregisteredFromGatewayDelayedEvent);
        }
    }
}