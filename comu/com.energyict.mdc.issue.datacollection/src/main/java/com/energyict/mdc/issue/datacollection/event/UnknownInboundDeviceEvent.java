package com.energyict.mdc.issue.datacollection.event;

import java.util.Map;

import javax.inject.Inject;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.google.inject.Injector;

public class UnknownInboundDeviceEvent extends UnknownSlaveDeviceEvent {

    @Inject
    public UnknownInboundDeviceEvent(IssueDataCollectionService issueDataCollectionService, MeteringService meteringService,
            DeviceService deviceService, TopologyService topologyService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, meteringService, deviceService, topologyService, communicationTaskService, thesaurus, injector);
    }

    @Override
    protected void getEventDevice(Map<?, ?> rawEvent) {
        //nothing to do because the event doesn't contain info about device, only mrid which is parsed in super.wrapInternal(...)
    }

}