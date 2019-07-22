/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrBlkCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class UtilitiesDeviceBulkCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;

    @Inject
    UtilitiesDeviceBulkCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                             Thesaurus thesaurus) {
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void utilitiesDeviceERPSmartMeterBulkCreateRequestCIn(UtilsDvceERPSmrtMtrBlkCrteReqMsg request) {
        if (!isAnyActiveEndpoint(UtilitiesDeviceBulkCreateConfirmation.NAME)) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_NECESSARY_OUTBOUND_END_POINT,
                    UtilitiesDeviceBulkCreateConfirmation.NAME);
        }

        Optional.ofNullable(request)
                .ifPresent(requestMessage -> serviceCallCommands.createServiceCallAndTransition(UtilitiesDeviceCreateRequestMessage.builder()
                        .from(requestMessage)
                        .build()));

    }

    private boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .findEndPointConfigurations().find().stream()
                .filter(epc -> epc.getWebServiceName().equals(name))
                .filter(EndPointConfiguration::isActive)
                .findAny().isPresent();
    }
}
