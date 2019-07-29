/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.cim.webservices.outbound.soap.ReplyGetEndDeviceEventsWebService;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.servicecall.GetEndDeviceEventsServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + GetEndDeviceEventsServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class GetEndDeviceEventsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "GetEndDeviceEventsServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile MeteringService meteringService;

    private ReplyGetEndDeviceEventsWebService replyGetEndDeviceEventsWebService;
    private List<EndDeviceEventRecord> endDeviceEvents = new ArrayList<>();

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case ONGOING:
                processServiceCall(serviceCall);
                break;
            case SUCCESSFUL:
                sendResponseToOutboundEndPoint(serviceCall);
                break;
            case FAILED:
                sendResponseToOutboundEndPoint(serviceCall);
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void addReplyGetEndDeviceEventsWebServiceClient(ReplyGetEndDeviceEventsWebService webService) {
        this.replyGetEndDeviceEventsWebService = webService;
    }

    public void removeReplyGetEndDeviceEventsWebServiceClient(ReplyGetEndDeviceEventsWebService webService) {
        this.replyGetEndDeviceEventsWebService = null;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    private void processServiceCall(ServiceCall serviceCall) {
        GetEndDeviceEventsDomainExtension extensionFor = serviceCall.getExtensionFor(new GetEndDeviceEventsCustomPropertySet()).get();
        try {
            setEndDeviceEvents(extensionFor);
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } catch (Exception faultMessage) {
            GetEndDeviceEventsDomainExtension extension = serviceCall.getExtension(GetEndDeviceEventsDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
            extension.setErrorMessage(faultMessage.getLocalizedMessage());
            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    private void sendResponseToOutboundEndPoint(ServiceCall serviceCall) {
        GetEndDeviceEventsDomainExtension extension = serviceCall.getExtensionFor(new GetEndDeviceEventsCustomPropertySet()).get();
        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService.findEndPointConfigurations().find()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(epc -> !epc.isInbound())
                .filter(epc -> epc.getUrl().equals(extension.getCallbackURL()))
                .findAny();

        replyGetEndDeviceEventsWebService.call(endPointConfiguration.get(), endDeviceEvents);
    }

    private void setEndDeviceEvents(GetEndDeviceEventsDomainExtension extension) {
        Set<String> meterIds = new HashSet<>();
        for (String identifier : extension.getMeter().split(",")) {
            if (!identifier.isEmpty()) {
                meterIds.add(identifier);
            }
        }

        endDeviceEvents.clear();
        Range<Instant> range = Range.openClosed(extension.getFromDate(), extension.getToDate());
        meteringService.findEndDevices(meterIds).stream().forEach(
                endDevice -> endDevice.getDeviceEvents(range)
                        .stream()
                        .forEach(
                                deviceEvent -> endDeviceEvents.add(deviceEvent)
                        ));
    }
}
