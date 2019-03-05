/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.cim.webservices.inbound.soap.FailedMeterOperation;
import com.energyict.mdc.cim.webservices.inbound.soap.SendMeterConfigService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS GetMeterConfig
 */
@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.servicecall.GetMeterConfigMasterServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + GetMeterConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class GetMeterConfigMasterServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "GetMeterConfigMasterServiceCallHandler";
    public static final String VERSION = "v1.0";

    private volatile DeviceService deviceService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private static final List<SendMeterConfigService> METER_CONFIG_SERVICES = new CopyOnWriteArrayList<>();

    public GetMeterConfigMasterServiceCallHandler() {
        // for test purposes
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case ONGOING:
                serviceCall.findChildren().stream().forEach(child -> child.requestTransition(DefaultState.PENDING));
                break;
            case SUCCESSFUL:
                sendResponseToOutboundEndPoint(serviceCall);
                break;
            case FAILED:
                sendResponseToOutboundEndPoint(serviceCall);
                break;
            case PARTIAL_SUCCESS:
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
                updateCounter(parentServiceCall, newState);
                break;
            case FAILED:
                updateCounter(parentServiceCall, newState);
                break;
            case CANCELLED:
            case REJECTED:
            default:
                // No specific action required for these states
                break;
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSendMeterConfigService(SendMeterConfigService sendMeterConfigService) {
        METER_CONFIG_SERVICES.add(sendMeterConfigService);
    }

    public void removeSendMeterConfigService(SendMeterConfigService sendMeterConfigService) {
        METER_CONFIG_SERVICES.remove(sendMeterConfigService);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    private void updateCounter(ServiceCall serviceCall, DefaultState state) {
        GetMeterConfigMasterDomainExtension extension = serviceCall.getExtension(GetMeterConfigMasterDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        Long successfulCalls = extension.getActualNumberOfSuccessfulCalls();
        Long failedCalls = extension.getActualNumberOfFailedCalls();
        Long expectedCalls = extension.getExpectedNumberOfCalls();

        if (DefaultState.SUCCESSFUL.equals(state)) {
            successfulCalls++;
            extension.setActualNumberOfSuccessfulCalls(successfulCalls);
        } else {
            failedCalls++;
            extension.setActualNumberOfFailedCalls(failedCalls);
        }
        serviceCall.update(extension);

        if (extension.getExpectedNumberOfCalls().compareTo(successfulCalls + failedCalls) <= 0) {
            if (successfulCalls.compareTo(expectedCalls) >= 0 && serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (failedCalls.compareTo(expectedCalls) >= 0 && serviceCall.canTransitionTo(DefaultState.FAILED)) {
                serviceCall.requestTransition(DefaultState.FAILED);
            } else if (serviceCall.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
            }
        }
    }

    private void sendResponseToOutboundEndPoint(ServiceCall serviceCall) {
        GetMeterConfigMasterDomainExtension extensionFor = serviceCall.getExtensionFor(new GetMeterConfigMasterCustomPropertySet()).get();
        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService.findEndPointConfigurations().find()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(epc -> !epc.isInbound())
                .filter(epc -> epc.getUrl().equals(extensionFor.getCallbackURL()))
                .findAny();

        METER_CONFIG_SERVICES.stream().forEach(service -> service.call(getSuccessfullyProceededDevices(serviceCall),
                getUnsuccessfullyProceededDevices(serviceCall),
                extensionFor.getExpectedNumberOfCalls(),
                endPointConfiguration.get().getUrl()));
    }

    private List<Device> getSuccessfullyProceededDevices(ServiceCall serviceCall) {
        List<Device> devices = new ArrayList<>();
        serviceCall.findChildren()
                .stream()
                .filter(child -> child.getState().equals(DefaultState.SUCCESSFUL))
                .forEach(child ->  {
                    GetMeterConfigItemDomainExtension extensionFor = child.getExtensionFor(new GetMeterConfigItemCustomPropertySet()).get();
                    Optional<Device> device = findDevice(Optional.ofNullable(extensionFor.getMeterMrid()), extensionFor.getMeterName());
                    if (device.isPresent()) {
                        devices.add(device.get());
                    }
                });
        return devices;
    }

    private Optional<Device> findDevice(Optional<String> mrid, String deviceName) {
        return mrid.isPresent() ? deviceService.findDeviceByMrid(mrid.get()) : deviceService.findDeviceByName(deviceName);
    }

    private List<FailedMeterOperation> getUnsuccessfullyProceededDevices(ServiceCall serviceCall) {
        List<FailedMeterOperation> failedMeterOperations = new ArrayList<>();
        serviceCall.findChildren()
                .stream()
                .filter(child -> child.getState().equals(DefaultState.FAILED))
                .forEach(child ->  {
                    GetMeterConfigItemDomainExtension extensionFor = child.getExtensionFor(new GetMeterConfigItemCustomPropertySet()).get();
                    FailedMeterOperation failedMeterOperation = new FailedMeterOperation();
                    failedMeterOperation.setErrorCode(extensionFor.getErrorCode());
                    failedMeterOperation.setErrorMessage(extensionFor.getErrorMessage());
                    failedMeterOperation.setmRID(extensionFor.getMeterMrid());
                    failedMeterOperation.setMeterName(extensionFor.getMeterName());
                    failedMeterOperations.add(failedMeterOperation);
                });
        return failedMeterOperations;
    }
}
