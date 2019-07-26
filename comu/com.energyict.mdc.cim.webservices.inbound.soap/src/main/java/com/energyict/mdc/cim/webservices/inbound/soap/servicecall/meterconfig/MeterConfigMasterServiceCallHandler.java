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
import com.energyict.mdc.cim.webservices.outbound.soap.ReplyMeterConfigWebService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private void updateCounter(ServiceCall serviceCall, DefaultState state) {
        MeterConfigMasterDomainExtension extension = serviceCall.getExtension(MeterConfigMasterDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        long successfulCalls = extension.getActualNumberOfSuccessfulCalls();
        long failedCalls = extension.getActualNumberOfFailedCalls();
        long expectedCalls = extension.getExpectedNumberOfCalls();

        if (DefaultState.SUCCESSFUL.equals(state)) {
            successfulCalls++;
            extension.setActualNumberOfSuccessfulCalls(successfulCalls);
        } else {
            failedCalls++;
            extension.setActualNumberOfFailedCalls(failedCalls);
        }
        serviceCall.update(extension);

        if (expectedCalls <= successfulCalls + failedCalls) {
            if (successfulCalls >= expectedCalls && serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (failedCalls >= expectedCalls && serviceCall.canTransitionTo(DefaultState.FAILED)) {
                serviceCall.requestTransition(DefaultState.FAILED);
            } else if (serviceCall.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
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
			
			replyMeterConfigWebService.call(endPointConfiguration.get(), operation,
					getSuccessfullyProcessedDevices(serviceCall), getUnsuccessfullyProcessedDevices(serviceCall),
					extensionFor.getExpectedNumberOfCalls(), extensionFor.getCorrelationId());
		}
    }

    private List<Device> getSuccessfullyProcessedDevices(ServiceCall serviceCall) {
        List<Device> devices = new ArrayList<>();
        serviceCall.findChildren()
                .stream()
                .filter(child -> child.getState().equals(DefaultState.SUCCESSFUL))
                .forEach(child ->  {
                    MeterConfigDomainExtension extensionFor = child.getExtensionFor(new MeterConfigCustomPropertySet()).get();
                    Optional<Device> device = findDevice(extensionFor.getMeterMrid(), extensionFor.getMeterName());
                    if (device.isPresent()) {
                        devices.add(device.get());
                    }
                });
        return devices;
    }

    private List<FailedMeterOperation> getUnsuccessfullyProcessedDevices(ServiceCall serviceCall) {
        List<FailedMeterOperation> failedMeterOperations = new ArrayList<>();
        serviceCall.findChildren()
                .stream()
                .filter(child -> child.getState().equals(DefaultState.FAILED))
                .forEach(child ->  {
                    MeterConfigDomainExtension extensionFor = child.getExtensionFor(new MeterConfigCustomPropertySet()).get();
                    FailedMeterOperation failedMeterOperation = new FailedMeterOperation();
                    failedMeterOperation.setErrorCode(extensionFor.getErrorCode());
                    failedMeterOperation.setErrorMessage(extensionFor.getErrorMessage());
                    failedMeterOperation.setmRID(extensionFor.getMeterMrid());
                    failedMeterOperation.setMeterName(extensionFor.getMeterName());
                    failedMeterOperations.add(failedMeterOperation);
                });
        return failedMeterOperations;
    }

    private Optional<Device> findDevice(String mrid, String deviceName) {
        return (mrid != null && !mrid.isEmpty()) ? deviceService.findDeviceByMrid(mrid) : deviceService.findDeviceByName(deviceName);
    }
}
