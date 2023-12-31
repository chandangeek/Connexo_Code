/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisterCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilitiesDeviceERPSmartMeterRegisterCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqMsg;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class UtilitiesDeviceRegisterCreateRequestEndpoint extends AbstractRegisterCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterRegisterCreateRequestCIn {

    @Inject
    UtilitiesDeviceRegisterCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                                 Clock clock, SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus, WebServiceActivator webServiceActivator,
                                                 ServiceCallService serviceCallService) {
        super(serviceCallCommands, endPointConfigurationService, clock, sapCustomPropertySets, thesaurus, webServiceActivator, serviceCallService);
    }

    @Override
    void validateConfiguredEndpoints() {
        if (!isAnyActiveEndpoint(UtilitiesDeviceRegisterCreateConfirmation.NAME)) {
            throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                    UtilitiesDeviceRegisterCreateConfirmation.NAME);
        }

        if (!isAnyActiveEndpoint(UtilitiesDeviceRegisteredNotification.NAME)) {
            throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                    UtilitiesDeviceRegisteredNotification.NAME);
        }
    }

    @Override
    public void utilitiesDeviceERPSmartMeterRegisterCreateRequestCIn(UtilsDvceERPSmrtMtrRegCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        UtilitiesDeviceRegisterCreateRequestMessage message = UtilitiesDeviceRegisterCreateRequestMessage.builder(getThesaurus())
                                .from(requestMessage, getWebServiceActivator().getSapProperty(AdditionalProperties.LRN_END_INTERVAL))
                                .build(getThesaurus());

                        handleRequestMessage(message);
                    });
            return null;
        });
    }
}
