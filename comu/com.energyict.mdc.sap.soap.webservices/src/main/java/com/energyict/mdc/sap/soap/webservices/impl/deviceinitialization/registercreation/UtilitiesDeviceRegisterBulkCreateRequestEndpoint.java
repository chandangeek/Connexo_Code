/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisterBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisteredBulkNotification;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilitiesDeviceERPSmartMeterRegisterBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegBulkCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class UtilitiesDeviceRegisterBulkCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterRegisterBulkCreateRequestCIn {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;

    @Inject
    UtilitiesDeviceRegisterBulkCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                                     Thesaurus thesaurus) {
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void utilitiesDeviceERPSmartMeterRegisterBulkCreateRequestCIn(UtilsDvceERPSmrtMtrRegBulkCrteReqMsg request) {
        if (!isAnyActiveEndpoint(UtilitiesDeviceRegisterBulkCreateConfirmation.NAME)) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_NECESSARY_OUTBOUND_END_POINT,
                    UtilitiesDeviceRegisterBulkCreateConfirmation.NAME);
        }

        if (!isAnyActiveEndpoint(UtilitiesDeviceRegisteredBulkNotification.NAME)) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_NECESSARY_OUTBOUND_END_POINT,
                    UtilitiesDeviceRegisteredBulkNotification.NAME);
        }

        Optional.ofNullable(request)
                .ifPresent(requestMessage -> serviceCallCommands.createServiceCallAndTransition(UtilitiesDeviceRegisterCreateRequestMessage.builder()
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
