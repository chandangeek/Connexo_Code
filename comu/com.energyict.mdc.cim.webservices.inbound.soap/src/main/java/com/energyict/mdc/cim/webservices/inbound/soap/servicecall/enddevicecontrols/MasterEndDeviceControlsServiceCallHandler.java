/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.Name;
import ch.iec.tc57._2011.enddeviceevents.NameType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping;

import ch.iec.tc57._2011.schema.message.ErrorType;
import com.energyict.cim.EndDeviceEventOrAction;

import javax.inject.Inject;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH;
import static com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH;

public class MasterEndDeviceControlsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MasterEndDeviceControls";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private static final String CONTACTOR_OPEN_COMMAND = "3.31.0.23";
    private static final String CONTACTOR_CLOSE_COMMAND = "3.31.0.18";
    private static final String UPDATE_CREDIT_COMMAND = "3.20.22.13";

    private final EndPointConfigurationService endPointConfigurationService;
    private final EndDeviceEventsServiceProvider endDeviceEventsServiceProvider;
    private final ServiceCallService serviceCallService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final ReplyTypeFactory replyTypeFactory;

    @Inject
    public MasterEndDeviceControlsServiceCallHandler(EndPointConfigurationService endPointConfigurationService,
                                                     EndDeviceEventsServiceProvider endDeviceEventsServiceProvider, ServiceCallService serviceCallService,
                                                     Clock clock, Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.endDeviceEventsServiceProvider = endDeviceEventsServiceProvider;
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Service call is switched to state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall = ServiceCallTransitionUtils.lock(serviceCall, serviceCallService);
                serviceCall.findChildren().stream().forEach(child -> child.transitionWithLockIfPossible(DefaultState.PENDING));
                ServiceCallTransitionUtils.transitToStateAfterOngoing(serviceCall, DefaultState.WAITING);
                break;
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
        if (!newState.isOpen()) {
            ServiceCallTransitionUtils.resultTransition(parentServiceCall, serviceCallService);
        }
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
        List<ErrorType> errorTypes = new ArrayList<>();

        serviceCall.findChildren().stream().forEach(subSC -> {
            SubMasterEndDeviceControlsDomainExtension subExtension = subSC.getExtension(SubMasterEndDeviceControlsDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

            subSC.findChildren().stream().forEach(childSC -> {
                EndDeviceControlsDomainExtension childExtension = childSC.getExtension(EndDeviceControlsDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

                Optional<Device> device = (Optional<Device>) childSC.getTargetObject();

                String commandCode =  subExtension.getCommandCode();
                EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
                Optional<EndDeviceEventDetail> endDeviceEventDetailOptional = Optional.empty();
                switch (commandCode) {
                    case CONTACTOR_OPEN_COMMAND:
                    case CONTACTOR_CLOSE_COMMAND:
                        endDeviceEventDetailOptional = createEndDeviceDetailsForContactorStatus(childSC);
                        break;
                    case UPDATE_CREDIT_COMMAND:
                        endDeviceEventDetailOptional = createEndDeviceDetailsForCreditStatus(childSC);
                        break;
                    default:
                        //nothing special to do
                }

                endDeviceEventDetailOptional.ifPresent(endDeviceEventDetail -> endDeviceEvent.getEndDeviceEventDetails()
                        .add(endDeviceEventDetail));

                if (childExtension.getError() != null) {
                    String name;
                    String mrid;
                    if (device.isPresent()) {
                        name = device.get().getName();
                        mrid = device.get().getmRID();
                    } else {
                        name = childExtension.getDeviceName();
                        mrid = childExtension.getDeviceMrid();
                    }
                    errorTypes.add(replyTypeFactory.errorType(name, mrid, childExtension.getError(),
                            MessageSeeds.END_DEVICE_ERROR.getErrorCode(), MessageSeeds.END_DEVICE_ERROR.getErrorTypeLevel()));
                } else {
                    Asset asset;
                    if (device.isPresent()) {
                        asset = createAsset(device.get());
                    } else {
                        asset = createAsset(childExtension.getDeviceMrid(), childExtension.getDeviceName());
                    }
                    endDeviceEvent.setAssets(asset);
                    endDeviceEvent.setCreatedDateTime(clock.instant());

                    EndDeviceEvent.EndDeviceEventType eventType = new EndDeviceEvent.EndDeviceEventType();

                    String ref = createRef(subExtension.getCommandCode(), childSC.getState(), childExtension.getCancellationReason());
                    eventType.setRef(ref);
                    endDeviceEvent.setEndDeviceEventType(eventType);

                    events.add(endDeviceEvent);
                }

            });
        });

        if (events.isEmpty() && errorTypes.isEmpty()) {
            serviceCall.log(LogLevel.INFO, "No events/errors to send.");
        } else {
            serviceCall.log(LogLevel.INFO, "Sending " + events.size() + " event(s) and " + errorTypes.size() + " error(s).");
            endDeviceEventsServiceProvider.call(events, errorTypes, endPointConfigurationOptional.get(), extension.getCorrelationId());
        }
    }

    /**
     *  Create {@link EndDeviceEventDetail} with information about breaker status
     *  in case service call contains command to change breaker status
     */
    public Optional<EndDeviceEventDetail> createEndDeviceDetailsForContactorStatus(ServiceCall serviceCall) {
        Device device = (Device) serviceCall.getTargetObject().get();
        return device.getRegisters().stream()
                .filter(register -> register.getRegisterTypeObisCode().equals(ActivatedBreakerStatus.BREAKER_STATUS))
                .findAny()
                .map(Register::getLastReading)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(NumericalReading.class::cast)
                .map(this::wrapContactorStatusToEndDeviceEventDetail);
    }

    /**
     *  Create {@link EndDeviceEventDetail} with information about credit amount
     *  in case service call contains command to change credit amount
     */
    public Optional<EndDeviceEventDetail> createEndDeviceDetailsForCreditStatus (ServiceCall serviceCall) {
        Device device = (Device) serviceCall.getTargetObject().get();
        return device.getRegisters().stream()
                .filter(register -> (register.getRegisterTypeObisCode().equals(CreditAmount.IMPORT_CREDIT) ||
                        register.getRegisterTypeObisCode().equals(CreditAmount.EMERGENCY_CREDIT)))
                .findAny()
                .map(Register::getLastReading)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(NumericalReading.class::cast)
                .map(this::wrapCreditAmountToEndDeviceEventDetail);
    }

    private EndDeviceEventDetail wrapContactorStatusToEndDeviceEventDetail(NumericalReading reading) {
        EndDeviceEventDetail endDeviceEventDetail = new EndDeviceEventDetail();
        endDeviceEventDetail.setName("Contactor status");
        endDeviceEventDetail.setValue(reading.getValue().intValue() == 0 ? "Opened" : "Closed");
        return endDeviceEventDetail;
    }

    private EndDeviceEventDetail wrapCreditAmountToEndDeviceEventDetail(NumericalReading reading) {
        EndDeviceEventDetail endDeviceEventDetail = new EndDeviceEventDetail();
        endDeviceEventDetail.setName("Credit amount");
        endDeviceEventDetail.setValue(reading.getValue().toString());
        return endDeviceEventDetail;
    }

    /**
     * Create ref based on command code: we change the last digit of command code
     * to {@link EndDeviceEventOrAction} according state of service call and {@link CommandEventCodeMapping}
     */
    private String createRef(String commandCode, DefaultState state, CancellationReason cancellationReason) {
        EndDeviceEventOrAction endDeviceEventOrAction;
        if (state.equals(DefaultState.CANCELLED)) {
            endDeviceEventOrAction = cancellationReason == CancellationReason.TIMEOUT ? EndDeviceEventOrAction.EXPIRED :
                    EndDeviceEventOrAction.CANCELLED;
        } else {
            CommandEventCodeMapping commandEventCodeMapping = CommandEventCodeMapping.getMappingFor(commandCode);

            if (state.equals(DefaultState.SUCCESSFUL)) {
                endDeviceEventOrAction = commandEventCodeMapping.getSuccessEvent();
            } else {
                endDeviceEventOrAction = commandEventCodeMapping.getFailureEvent();
            }
        }
        int lastPeriodIndex = commandCode.lastIndexOf('.');
        return commandCode.substring(0, lastPeriodIndex + 1) + endDeviceEventOrAction.getValue();
    }

    private Asset createAsset(Device device) {
        return createAsset(device.getmRID(), device.getName());
    }

    private Asset createAsset(String deviceMrid, String deviceName) {
        Asset asset = new Asset();
        if (deviceMrid != null) {
            asset.setMRID(deviceMrid);
        }
        if (deviceName != null) {
            asset.getNames().add(createName(deviceName));
        }
        return asset;
    }

    private Name createName(String deviceName) {
        NameType nameType = new NameType();
        nameType.setName("EndDevice");
        Name name = new Name();
        name.setNameType(nameType);
        name.setName(deviceName);
        return name;
    }

    private Optional<EndPointConfiguration> getEndPointConfiguration(String url) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(EndDeviceEventsServiceProvider.NAME)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst();
    }

    enum CommandEventCodeMapping {

        DEFAULT(null, EndDeviceEventOrAction.PROCESSED, EndDeviceEventOrAction.ERROR),
        CONTACTOR_CLOSE(CLOSE_REMOTE_SWITCH, EndDeviceEventOrAction.PROCESSED, EndDeviceEventOrAction.ERROR),
        CONTACTOR_OPEN(OPEN_REMOTE_SWITCH, EndDeviceEventOrAction.PROCESSED, EndDeviceEventOrAction.ERROR),
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

        public static CommandEventCodeMapping getMappingFor(String commandCode) {
            EndDeviceControlTypeMapping endDeviceControlTypeMapping = EndDeviceControlTypeMapping.getMappingWithoutDeviceTypeFor(commandCode);
            for (CommandEventCodeMapping mapping : values()) {
                if (endDeviceControlTypeMapping.equals(mapping.getEndDeviceControlTypeMapping())) {
                    return mapping;
                }
            }
            return CommandEventCodeMapping.DEFAULT;
        }
    }
}
