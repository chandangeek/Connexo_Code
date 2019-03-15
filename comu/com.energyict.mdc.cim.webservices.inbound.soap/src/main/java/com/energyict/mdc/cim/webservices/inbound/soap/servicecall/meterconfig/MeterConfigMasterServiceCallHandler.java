/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.FailedMeterOperation;
import com.energyict.mdc.cim.webservices.inbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.ReplyMeterConfigWebService;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset.CasHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.DeviceBuilder;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.Clock;
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
    private volatile BatchService batchService;
    private volatile Clock clock;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile Thesaurus thesaurus;

    private ReplyTypeFactory replyTypeFactory;
    private MeterConfigFaultMessageFactory messageFactory;
    private DeviceBuilder deviceBuilder;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case ONGOING:
                serviceCall.findChildren().stream().forEach(child -> processChild(child));
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

    @Reference
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    private void processChild(ServiceCall child) {
        MeterConfigDomainExtension extensionFor = child.getExtensionFor(new MeterConfigCustomPropertySet()).get();
        OperationEnum operation = OperationEnum.getFromString(extensionFor.getOperation());
        if (OperationEnum.GET.equals(operation)) {
            try {
                getDeviceBuilder().findDevice(Optional.ofNullable(extensionFor.getMeterMrid()), extensionFor.getMeterName());
            } catch (Exception faultMessage) {
                if (faultMessage instanceof FaultMessage) {
                    Optional<ErrorType> errorType = ((FaultMessage) faultMessage).getFaultInfo().getReply().getError().stream().findFirst();
                    if (errorType.isPresent()) {
                        extensionFor.setErrorMessage(errorType.get().getDetails());
                        extensionFor.setErrorCode(errorType.get().getCode());
                    } else {
                        extensionFor.setErrorMessage(faultMessage.getLocalizedMessage());
                    }
                } else if (faultMessage instanceof ConstraintViolationException) {
                    extensionFor.setErrorMessage(((ConstraintViolationException) faultMessage).getConstraintViolations().stream()
                            .findFirst()
                            .map(ConstraintViolation::getMessage)
                            .orElseGet(faultMessage::getMessage));
                } else {
                    extensionFor.setErrorMessage(faultMessage.getLocalizedMessage());
                }
                child.update(extensionFor);
                child.requestTransition(DefaultState.FAILED);
            }
        }
    }

    private void updateCounter(ServiceCall serviceCall, DefaultState state) {
        MeterConfigMasterDomainExtension extension = serviceCall.getExtension(MeterConfigMasterDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        BigDecimal successfulCalls = extension.getActualNumberOfSuccessfulCalls();
        BigDecimal failedCalls = extension.getActualNumberOfFailedCalls();
        BigDecimal expectedCalls = extension.getExpectedNumberOfCalls();

        if (DefaultState.SUCCESSFUL.equals(state)) {
            successfulCalls = successfulCalls.add(BigDecimal.ONE);
            extension.setActualNumberOfSuccessfulCalls(successfulCalls);
        } else {
            failedCalls = failedCalls.add(BigDecimal.ONE);
            extension.setActualNumberOfFailedCalls(failedCalls);
        }
        serviceCall.update(extension);

        if (extension.getExpectedNumberOfCalls().compareTo(successfulCalls.add(failedCalls)) <= 0) {
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
        MeterConfigMasterDomainExtension extensionFor = serviceCall.getExtensionFor(new MeterConfigMasterCustomPropertySet()).get();
        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService.findEndPointConfigurations().find()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(epc -> !epc.isInbound())
                .filter(epc -> epc.getUrl().equals(extensionFor.getCallbackURL()))
                .findAny();

        ServiceCall child = serviceCall.findChildren().stream().findFirst().get();
        MeterConfigDomainExtension extensionForChild = child.getExtensionFor(new MeterConfigCustomPropertySet()).get();
        OperationEnum operation = OperationEnum.getFromString(extensionForChild.getOperation());

        replyMeterConfigWebService.call(endPointConfiguration.get(), operation,
                getSuccessfullyProceededDevices(serviceCall),
                getUnsuccessfullyProceededDevices(serviceCall),
                extensionFor.getExpectedNumberOfCalls());
    }

    private List<Device> getSuccessfullyProceededDevices(ServiceCall serviceCall) {
        List<Device> devices = new ArrayList<>();
        serviceCall.findChildren()
                .stream()
                .filter(child -> child.getState().equals(DefaultState.SUCCESSFUL))
                .forEach(child ->  {
                    MeterConfigDomainExtension extensionFor = child.getExtensionFor(new MeterConfigCustomPropertySet()).get();
                    Optional<Device> device = findDevice(Optional.ofNullable(extensionFor.getMeterMrid()), extensionFor.getMeterName());
                    if (device.isPresent()) {
                        devices.add(device.get());
                    }
                });
        return devices;
    }

    private List<FailedMeterOperation> getUnsuccessfullyProceededDevices(ServiceCall serviceCall) {
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

    private Optional<Device> findDevice(Optional<String> mrid, String deviceName) {
        return mrid.isPresent() ? deviceService.findDeviceByMrid(mrid.get()) : deviceService.findDeviceByName(deviceName);
    }

    private DeviceBuilder getDeviceBuilder() {
        if (deviceBuilder == null) {
            deviceBuilder = new DeviceBuilder(batchService, clock, deviceLifeCycleService, deviceConfigurationService,
                    deviceService, getMessageFactory(), deviceLifeCycleConfigurationService);
        }
        return deviceBuilder;
    }

    private MeterConfigFaultMessageFactory getMessageFactory() {
        if (messageFactory == null) {
            messageFactory = new MeterConfigFaultMessageFactory(thesaurus, getReplyTypeFactory());
        }
        return messageFactory;
    }

    private ReplyTypeFactory getReplyTypeFactory() {
        if (replyTypeFactory == null) {
            replyTypeFactory = new ReplyTypeFactory(thesaurus);
        }
        return replyTypeFactory;
    }

}
