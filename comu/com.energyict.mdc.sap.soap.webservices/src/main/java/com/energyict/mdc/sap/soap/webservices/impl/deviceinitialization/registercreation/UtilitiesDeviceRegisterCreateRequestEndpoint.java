/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilitiesDeviceERPSmartMeterRegisterCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class UtilitiesDeviceRegisterCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterRegisterCreateRequestCIn {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    UtilitiesDeviceRegisterCreateRequestEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void utilitiesDeviceERPSmartMeterRegisterCreateRequestCIn(UtilsDvceERPSmrtMtrRegCrteReqMsg request) {
        Optional.ofNullable(request)
                .ifPresent(requestMessage -> serviceCallCommands.createServiceCallAndTransition(UtilitiesDeviceRegisterCreateRequestMessage.builder()
                        .from(requestMessage)
                        .build()));
    }
}
