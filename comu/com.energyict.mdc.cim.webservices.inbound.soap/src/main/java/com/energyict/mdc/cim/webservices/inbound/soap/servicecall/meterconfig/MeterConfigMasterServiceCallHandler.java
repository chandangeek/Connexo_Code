/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.cim.webservices.outbound.soap.FailedMeterOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.outbound.soap.PingResult;
import com.energyict.mdc.cim.webservices.outbound.soap.ReplyMeterConfigWebService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import com.google.common.base.Strings;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MeterConfig
 */
@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.servicecall.MeterConfigMasterServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + MeterConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class MeterConfigMasterServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MeterConfigMasterServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile DeviceService deviceService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile ReplyMeterConfigWebService replyMeterConfigWebService;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case ONGOING:
                serviceCall.findChildren().stream().forEach(child -> child.requestTransition(DefaultState.PENDING));
                break;
            case SUCCESSFUL:
            case FAILED:
            case PARTIAL_SUCCESS:
            case CANCELLED:
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

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case SUCCESSFUL:
            case FAILED:
            case CANCELLED:
            case REJECTED:
                processChildren(parentServiceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Reference
    public void addReplyMeterConfigWebServiceClient(ReplyMeterConfigWebService webService) {
        this.replyMeterConfigWebService = webService;
    }

    public void removeReplyMeterConfigWebServiceClient(ReplyMeterConfigWebService webService) {
        this.replyMeterConfigWebService = null;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    private void processChildren(ServiceCall serviceCall) {
        List<ServiceCall> children = findChildren(serviceCall);
        if (areAllClosed(children)) {
            if (hasAllChildrenInState(children, DefaultState.SUCCESSFUL) && serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (hasAnyChildInState(children, DefaultState.SUCCESSFUL) && serviceCall.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
            } else if (hasAnyChildInState(children, DefaultState.CANCELLED) && serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
                serviceCall.requestTransition(DefaultState.CANCELLED);
            } else if (hasAnyChildInState(children, DefaultState.REJECTED) && serviceCall.canTransitionTo(DefaultState.REJECTED)) {
                serviceCall.requestTransition(DefaultState.REJECTED);
            } else if (serviceCall.canTransitionTo(DefaultState.FAILED)) {
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        }
    }

    private void sendResponseToOutboundEndPoint(ServiceCall serviceCall) {
        MeterConfigMasterDomainExtension extensionFor = serviceCall.getExtensionFor(new MeterConfigMasterCustomPropertySet()).get();
        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService.findEndPointConfigurations().find()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(epc -> !epc.isInbound())
                .filter(epc -> epc.getUrl().equals(extensionFor.getCallbackURL()))
                .findAny();
        if (endPointConfiguration.isPresent()) {
            ServiceCall child = serviceCall.findChildren().stream().findFirst().get();
            MeterConfigDomainExtension extensionForChild = child.getExtensionFor(new MeterConfigCustomPropertySet())
                    .get();
            OperationEnum operation = OperationEnum.getFromString(extensionForChild.getOperation());
            boolean meterStatusRequired = !Strings.isNullOrEmpty(extensionFor.getMeterStatusSource());
            replyMeterConfigWebService.call(endPointConfiguration.get(), operation,
                    getSuccessfullyProcessedDevices(serviceCall),
                    getFailedMeterOperations(serviceCall, false), getFailedMeterOperations(serviceCall, true),
                    getExpectedNumberOfCalls(serviceCall), meterStatusRequired, extensionFor.getCorrelationId());
        }
    }

    private long getExpectedNumberOfCalls(ServiceCall serviceCall) {
        return findChildren(serviceCall).size();
    }

    private Map<Device, PingResult> getSuccessfullyProcessedDevices(ServiceCall serviceCall) {
        Map<Device, PingResult> map = new HashMap<>();
        serviceCall.findChildren()
                .stream()
                .filter(child -> child.getState().equals(DefaultState.SUCCESSFUL))
                .forEach(child -> {
                    MeterConfigDomainExtension extensionFor = child.getExtensionFor(new MeterConfigCustomPropertySet()).get();
                    Optional<Device> device = findDevice(extensionFor.getMeterMrid(), extensionFor.getMeterName());
                    device.ifPresent(value -> map.put(value, PingResult.valueFor(extensionFor.getPingResult())));
                });
        return map;
    }

    private List<FailedMeterOperation> getFailedMeterOperations(ServiceCall serviceCall, boolean warning) {
        List<FailedMeterOperation> failedMeterOperations = new ArrayList<>();
        serviceCall.findChildren()
                .stream()
                .filter(child -> {
                    if (warning) {
                        return child.getState().equals(DefaultState.SUCCESSFUL);
                    } else {
                        return child.getState().equals(DefaultState.FAILED)
                                || child.getState().equals(DefaultState.CANCELLED);
                    }
                })
                .forEach(child -> {
                    MeterConfigDomainExtension extensionFor = child.getExtensionFor(new MeterConfigCustomPropertySet()).get();
                    if (warning && Strings.isNullOrEmpty(extensionFor.getErrorMessage())) {
                        return;
                    } else {
                        FailedMeterOperation failedMeterOperation = new FailedMeterOperation();
                        failedMeterOperation.setErrorCode(extensionFor.getErrorCode());
                        failedMeterOperation.setErrorMessage(extensionFor.getErrorMessage());
                        failedMeterOperation.setmRID(extensionFor.getMeterMrid());
                        failedMeterOperation.setMeterName(extensionFor.getMeterName());
                        failedMeterOperations.add(failedMeterOperation);
                    }
                });
        return failedMeterOperations;
    }

    private boolean hasAllChildrenInState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean hasAnyChildInState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }

    private boolean areAllClosed(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream().noneMatch(sc -> sc.getState().isOpen());
    }

    private Optional<Device> findDevice(String mrid, String deviceName) {
        return (mrid != null && !mrid.isEmpty()) ? deviceService.findDeviceByMrid(mrid) : deviceService.findDeviceByName(deviceName);
    }
}
