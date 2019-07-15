/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.data.Device;

import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

@Component(name = UtilitiesDeviceRegisterCreateRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + UtilitiesDeviceRegisterCreateRequestCallHandler.NAME, immediate = true)
public class UtilitiesDeviceRegisterCreateRequestCallHandler implements ServiceCallHandler {

    public static final String NAME = "UtilitiesDeviceRegisterCreateRequestCallHandler";
    public static final String VERSION = "v1.0";

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
            default:
                // No specific action required for these states
                break;
        }
    }

    private void processServiceCall(ServiceCall serviceCall) {
        UtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
        Optional<Device> device = sapCustomPropertySets.getDevice(extension.getDeviceId());
        if (device.isPresent()) {
            //TODO: we can set LRN for channels
            Optional<Register> register = device.get().getRegisterWithDeviceObisCode(ObisCode.fromString(extension.getRegisterId()));
            if(register.isPresent()){
                sapCustomPropertySets.setLrn(register.get(), extension.getLrn(), extension.getStartDate(), extension.getEndDate());
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        } else {
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }
}
