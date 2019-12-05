/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;


@Component(name = MeterRegisterChangeRequest.NAME, service = ServiceCallHandler.class,
        property = "name=" + MeterRegisterChangeRequest.NAME, immediate = true)
public class MeterRegisterChangeRequest implements ServiceCallHandler {

    public static final String NAME = "MeterRegisterChangeRequest";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile Thesaurus thesaurus;
    private volatile SAPCustomPropertySets sapCustomPropertySets;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case ONGOING:
                processServiceCall(serviceCall);
                break;
            case CANCELLED:
                cancelServiceCall(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    private void processServiceCall(ServiceCall serviceCall) {
        ServiceCall subParent = serviceCall.getParent().orElseThrow(() -> new IllegalStateException("Can not find parent for service call"));
        SubMasterMeterRegisterChangeRequestDomainExtension subParentExtension = subParent.getExtensionFor(new SubMasterMeterRegisterChangeRequestCustomPropertySet())
                .orElseThrow(() -> new IllegalStateException("Can not find domain extension for parent service call"));
        MeterRegisterChangeRequestDomainExtension extension = serviceCall.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet())
                .orElseThrow(() -> new IllegalStateException("Can not find domain extension for service call"));

        Optional<Device> device = sapCustomPropertySets.getDevice(subParentExtension.getDeviceId());
        if (device.isPresent()) {
            try {
                sapCustomPropertySets.truncateCpsInterval(device.get(), extension.getLrn(), WebServiceActivator.getZonedDate(extension.getEndDate(), extension.getTimeZone()));
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } catch (SAPWebServiceException sapEx) {
                failServiceCallWithException(extension, sapEx);
            } catch (Exception e) {
                failServiceCallWithException(extension, new SAPWebServiceException(thesaurus, MessageSeeds.ERROR_PROCESSING_METER_REPLACEMENT_REQUEST, e.getLocalizedMessage()));
            }
        } else {
            failServiceCall(extension, MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, subParentExtension.getDeviceId());
        }
    }

    private void cancelServiceCall(ServiceCall serviceCall) {
        MeterRegisterChangeRequestDomainExtension extension = serviceCall.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet()).get();
        extension.setError(MessageSeeds.REGISTER_LRN_SERVICE_CALL_WAS_CANCELLED, extension.getLrn());
        serviceCall.update(extension);
    }

    private void failServiceCall(MeterRegisterChangeRequestDomainExtension extension, MessageSeed messageSeed, Object... args){
        ServiceCall serviceCall = extension.getServiceCall();

        extension.setError(messageSeed, args);
        serviceCall.update(extension);
        serviceCall.requestTransition(DefaultState.FAILED);
    }

    private void failServiceCallWithException(MeterRegisterChangeRequestDomainExtension extension, SAPWebServiceException e){
        ServiceCall serviceCall = extension.getServiceCall();

        extension.setErrorCode(e.getErrorCode());
        extension.setErrorMessage(e.getLocalizedMessage());
        serviceCall.update(extension);
        serviceCall.requestTransition(DefaultState.FAILED);
    }
}
