/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.InboundCIMWebServiceExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.DeviceBuilder;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigParser;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.util.Optional;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MeterConfig
 */
@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.servicecall.MeterConfigServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + MeterConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class MeterConfigServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MeterConfigServiceCallHandler";
    public static final String VERSION = "v1.0";

    private volatile BatchService batchService;
    private volatile Clock clock;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile MeterConfigParser meterConfigParser;

    private ReplyTypeFactory replyTypeFactory;
    private MeterConfigFaultMessageFactory messageFactory;
    private DeviceBuilder deviceBuilder;
    private Optional<InboundCIMWebServiceExtension> webServiceExtension = Optional.empty();

    public MeterConfigServiceCallHandler(){

    }
    @Inject
    public MeterConfigServiceCallHandler(MeterConfigParser meterConfigParser, Thesaurus thesaurus, Clock clock, BatchService batchService,
            DeviceLifeCycleService deviceLifeCycleService,
            DeviceConfigurationService deviceConfigurationService,
            DeviceService deviceService,
            JsonService jsonService){
        this.meterConfigParser = meterConfigParser;
        this.batchService = batchService;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.clock = clock;
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
    }
    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                processMeterConfigServiceCall(serviceCall);
                break;
            case SUCCESSFUL:
                break;
            case FAILED:
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
    public void addInboundCIMWebServiceExtension(InboundCIMWebServiceExtension webServiceExtension) {
        this.webServiceExtension = Optional.of(webServiceExtension);
    }

    public void removeInboundCIMWebServiceExtension(InboundCIMWebServiceExtension webServiceExtension) {
        this.webServiceExtension = Optional.empty();
    }

    private void processMeterConfigServiceCall(ServiceCall serviceCall)  {
        MeterConfigDomainExtension extensionFor = serviceCall.getExtensionFor(new MeterConfigCustomPropertySet()).get();
        MeterInfo meterInfo = jsonService.deserialize(extensionFor.getMeter(), MeterInfo.class);
        try {
            Device device;
            switch (OperationEnum.getFromString(extensionFor.getOperation())) {
                case CREATE:
                    device = getDeviceBuilder().prepareCreateFrom(meterInfo).build();
                    postProcessDevice(device, meterInfo);
                    break;
                case UPDATE:
                    device = getDeviceBuilder().prepareChangeFrom(meterInfo).build();
                    postProcessDevice(device, meterInfo);
                    break;
                default:
                    break;
            }
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } catch (Exception faultMessage) {
            MeterConfigDomainExtension extension = serviceCall.getExtension(MeterConfigDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
            extension.setErrorCode(OperationEnum.getFromString(extension.getOperation()).getDefaultErrorCode());
            if (faultMessage instanceof FaultMessage) {
                Optional<ErrorType> errorType = ((FaultMessage) faultMessage).getFaultInfo().getReply().getError().stream().findFirst();
                if (errorType.isPresent()) {
                    extension.setErrorMessage(errorType.get().getDetails());
                    extension.setErrorCode(errorType.get().getCode());
                } else {
                    extension.setErrorMessage(faultMessage.getLocalizedMessage());
                }
            } else if (faultMessage instanceof ConstraintViolationException) {
                extension.setErrorMessage(((ConstraintViolationException) faultMessage).getConstraintViolations().stream()
                        .findFirst()
                        .map(ConstraintViolation::getMessage)
                        .orElseGet(faultMessage::getMessage));
            } else {
                extension.setErrorMessage(faultMessage.getLocalizedMessage());
            }
            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
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
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
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
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    private void postProcessDevice(Device device, MeterInfo meterInfo){
        webServiceExtension.ifPresent(inboundCIMWebServiceExtension -> inboundCIMWebServiceExtension.extendMeterInfo(device, meterInfo));
    }

    private DeviceBuilder getDeviceBuilder() {
        if (deviceBuilder == null) {
            deviceBuilder = new DeviceBuilder(batchService, clock, deviceLifeCycleService, deviceConfigurationService, deviceService, getMessageFactory());
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
