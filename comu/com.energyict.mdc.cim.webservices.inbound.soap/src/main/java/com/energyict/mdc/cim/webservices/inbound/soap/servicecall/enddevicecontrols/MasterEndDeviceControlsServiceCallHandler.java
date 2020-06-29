/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols;

import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.Name;
import ch.iec.tc57._2011.enddeviceevents.NameType;
import com.energyict.cim.EndDeviceEventOrAction;

import javax.inject.Inject;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.servicecall.DefaultState.SUCCESSFUL;
import static com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH;
import static com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH;

public class MasterEndDeviceControlsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MasterEndDeviceControls";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private final EndPointConfigurationService endPointConfigurationService;
    private final EndDeviceEventsServiceProvider endDeviceEventsServiceProvider;
    private final MeteringService meteringService;
    private final ServiceCallService serviceCallService;
    private final Clock clock;
    private final Thesaurus thesaurus;

    @Inject
    public MasterEndDeviceControlsServiceCallHandler(EndPointConfigurationService endPointConfigurationService,
                                                     EndDeviceEventsServiceProvider endDeviceEventsServiceProvider,
                                                     MeteringService meteringService, ServiceCallService serviceCallService,
                                                     Clock clock, Thesaurus thesaurus) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.endDeviceEventsServiceProvider = endDeviceEventsServiceProvider;
        this.meteringService = meteringService;
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Service call is switched to state " + newState.getDefaultFormat());
        switch (newState) {
            case SUCCESSFUL:
            case PARTIAL_SUCCESS:
            case REJECTED:
            case FAILED:
            case CANCELLED:
                sendResponse(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall subParentServiceCall, DefaultState oldState, DefaultState newState) {
        ServiceCallTransitionUtils.resultTransition(parentServiceCall, serviceCallService);
    }

    private void sendResponse(ServiceCall serviceCall) {
        MasterEndDeviceControlsDomainExtension extension = serviceCall.getExtension(MasterEndDeviceControlsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        Optional<EndPointConfiguration> endPointConfigurationOptional = getEndPointConfiguration(extension.getCallbackUrl());
        if (!endPointConfigurationOptional.isPresent()) {
            serviceCall.log(LogLevel.SEVERE, MessageSeeds.NO_END_POINT_WITH_URL.translate(thesaurus, extension.getCallbackUrl()));
            return;
        }

        List<EndDeviceEvent> events = new ArrayList<>();

        serviceCall.findChildren().stream().forEach(subSC -> {
            SubMasterEndDeviceControlsDomainExtension subExtension = subSC.getExtension(SubMasterEndDeviceControlsDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

            subSC.findChildren().stream().forEach(childSC -> {
                EndDeviceControlsDomainExtension childExtension = childSC.getExtension(EndDeviceControlsDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

                if (childExtension.getCancellationReason() != CancellationReason.CREATE_ERROR) {
                    Device device = (Device) childSC.getTargetObject()
                            .orElseThrow(() -> new IllegalStateException("Unable to get device"));

                    EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
                    endDeviceEvent.setAssets(createAsset(device));
                    endDeviceEvent.setCreatedDateTime(clock.instant());

                    EndDeviceEvent.EndDeviceEventType eventType = new EndDeviceEvent.EndDeviceEventType();

                    String ref = createRef(subExtension.getCommandCode(), childSC.getState());
                    eventType.setRef(ref);
                    endDeviceEvent.setEndDeviceEventType(eventType);

                    events.add(endDeviceEvent);
                }
            });
        });

        if (!events.isEmpty()) {
            serviceCall.log(LogLevel.INFO, "Sending " + events.size() + " event(s).");
            endDeviceEventsServiceProvider.call(events, endPointConfigurationOptional.get(), extension.getCorrelationId());
        } else {
            serviceCall.log(LogLevel.INFO, "No events to send.");
        }
    }

    /**
     * Create ref based on command code: we change the last digit of command code
     * to {@link EndDeviceEventOrAction} according state of service call and {@link CommandEventCodeMapping}
     */
    private String createRef(String commandCode, DefaultState state) {
        EndDeviceEventOrAction endDeviceEventOrAction;
        if (state.equals(DefaultState.CANCELLED)) {
            endDeviceEventOrAction = EndDeviceEventOrAction.CANCELLED;
        } else {
            Optional<EndDeviceControlType> endDeviceControlType = meteringService.getEndDeviceControlType(commandCode);
            CommandEventCodeMapping commandEventCodeMapping;
            if (endDeviceControlType.isPresent()) {
                commandEventCodeMapping = CommandEventCodeMapping.getMappingFor(endDeviceControlType.get());
            } else {
                commandEventCodeMapping = CommandEventCodeMapping.DEFAULT;
            }

            if (state.equals(SUCCESSFUL)) {
                endDeviceEventOrAction = commandEventCodeMapping.getSuccessEvent();
            } else {
                endDeviceEventOrAction = commandEventCodeMapping.getFailureEvent();
            }
        }
        int lastPeriodIndex = commandCode.lastIndexOf('.');
        return commandCode.substring(0, lastPeriodIndex + 1) + endDeviceEventOrAction.getValue();
    }

    private Asset createAsset(Device device) {
        Asset asset = new Asset();
        asset.setMRID(device.getmRID());
        asset.getNames().add(createName(device));
        return asset;
    }

    private Name createName(Device device) {
        NameType nameType = new NameType();
        nameType.setName("EndDevice");
        Name name = new Name();
        name.setNameType(nameType);
        name.setName(device.getName());
        return name;
    }

    private Optional<EndPointConfiguration> getEndPointConfiguration(String url) {
        return  endPointConfigurationService
                .getEndPointConfigurationsForWebService(EndDeviceEventsServiceProvider.NAME)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst();
    }

    enum CommandEventCodeMapping {

        DEFAULT(null, EndDeviceEventOrAction.PROCESSED, EndDeviceEventOrAction.ERROR),
        CONTACTOR_CLOSE(CLOSE_REMOTE_SWITCH, EndDeviceEventOrAction.CONNECTED, EndDeviceEventOrAction.CONNECTFAILED),
        CONTACTOR_OPEN(OPEN_REMOTE_SWITCH, EndDeviceEventOrAction.DISCONNECTED, EndDeviceEventOrAction.DISCONNECTFAILED),
        ;

        private final EndDeviceControlTypeMapping endDeviceControlTypeMapping;
        private final EndDeviceEventOrAction successEvent;
        private final EndDeviceEventOrAction failureEvent;

        CommandEventCodeMapping(EndDeviceControlTypeMapping endDeviceControlTypeMapping, EndDeviceEventOrAction successEvent, EndDeviceEventOrAction failureEvent) {
            this.endDeviceControlTypeMapping = endDeviceControlTypeMapping;
            this.successEvent = successEvent;
            this.failureEvent = failureEvent;
        }

        public EndDeviceControlTypeMapping getEndDeviceControlTypeMapping() {
            return endDeviceControlTypeMapping;
        }

        public EndDeviceEventOrAction getSuccessEvent() {
            return successEvent;
        }

        public EndDeviceEventOrAction getFailureEvent() {
            return failureEvent;
        }

        public static CommandEventCodeMapping getMappingFor(EndDeviceControlType endDeviceControlType) {
            EndDeviceControlTypeMapping endDeviceControlTypeMapping = EndDeviceControlTypeMapping.getMappingWithoutDeviceTypeFor(endDeviceControlType);
            for (CommandEventCodeMapping mapping : values()) {
                if (endDeviceControlTypeMapping.equals(mapping.getEndDeviceControlTypeMapping())) {
                    return mapping;
                }
            }
            return CommandEventCodeMapping.DEFAULT;
        }
    }
}
