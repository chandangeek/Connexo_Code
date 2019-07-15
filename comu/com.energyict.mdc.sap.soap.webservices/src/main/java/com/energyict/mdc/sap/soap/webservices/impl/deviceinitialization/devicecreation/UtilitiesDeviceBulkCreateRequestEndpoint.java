/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrBlkCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class UtilitiesDeviceBulkCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    UtilitiesDeviceBulkCreateRequestEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void utilitiesDeviceERPSmartMeterBulkCreateRequestCIn(UtilsDvceERPSmrtMtrBlkCrteReqMsg request) {
        Optional.ofNullable(request)
                .ifPresent(requestMessage -> serviceCallCommands.createServiceCallAndTransition(UtilitiesDeviceCreateRequestMessage.builder()
                        .from(requestMessage)
                        .build()));

    }
}
