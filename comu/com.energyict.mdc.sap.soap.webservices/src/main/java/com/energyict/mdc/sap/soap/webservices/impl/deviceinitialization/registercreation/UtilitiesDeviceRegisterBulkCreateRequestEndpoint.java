/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilitiesDeviceERPSmartMeterRegisterBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegBulkCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class UtilitiesDeviceRegisterBulkCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterRegisterBulkCreateRequestCIn {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    UtilitiesDeviceRegisterBulkCreateRequestEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void utilitiesDeviceERPSmartMeterRegisterBulkCreateRequestCIn(UtilsDvceERPSmrtMtrRegBulkCrteReqMsg request) {
        Optional.ofNullable(request)
                .ifPresent(requestMessage -> serviceCallCommands.createServiceCallAndTransition(UtilitiesDeviceRegisterCreateRequestMessage.builder()
                        .from(requestMessage)
                        .build()));

    }
}
