/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig;

import ch.iec.tc57._2011.getmeterconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.getmeterconfig.DeviceBuilder;
import com.energyict.mdc.cim.webservices.inbound.soap.getmeterconfig.GetMeterConfigFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.device.data.DeviceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Optional;


/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS GetMeterConfig
 */
@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.servicecall.GetMeterConfigItemServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + GetMeterConfigItemServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class GetMeterConfigItemServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "GetMeterConfigItemServiceCallHandler";
    public static final String VERSION = "v1.0";
    private volatile DeviceService deviceService;
    private volatile Thesaurus thesaurus;

    private ReplyTypeFactory replyTypeFactory;
    private GetMeterConfigFaultMessageFactory messageFactory;
    private DeviceBuilder deviceBuilder;

    public GetMeterConfigItemServiceCallHandler() {
        // for test purposes
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

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }


    private void processMeterConfigServiceCall(ServiceCall serviceCall)  {
        GetMeterConfigItemDomainExtension extensionFor = serviceCall.getExtensionFor(new GetMeterConfigItemCustomPropertySet()).get();
        try {
            getDeviceBuilder().findDevice(Optional.ofNullable(extensionFor.getMeterMrid()), extensionFor.getMeterName());
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
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
            serviceCall.update(extensionFor);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    private DeviceBuilder getDeviceBuilder() {
        if (deviceBuilder == null) {
            deviceBuilder = new DeviceBuilder(deviceService, getMessageFactory());
        }
        return deviceBuilder;
    }

    private GetMeterConfigFaultMessageFactory getMessageFactory() {
        if (messageFactory == null) {
            messageFactory = new GetMeterConfigFaultMessageFactory(thesaurus, getReplyTypeFactory());
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
