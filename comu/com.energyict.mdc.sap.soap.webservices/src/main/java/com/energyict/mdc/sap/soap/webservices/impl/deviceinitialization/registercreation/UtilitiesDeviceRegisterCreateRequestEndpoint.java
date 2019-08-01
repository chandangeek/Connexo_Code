/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisterCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilitiesDeviceERPSmartMeterRegisterCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqMsg;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class UtilitiesDeviceRegisterCreateRequestEndpoint extends AbstractRegisterCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterRegisterCreateRequestCIn {

    @Inject
    UtilitiesDeviceRegisterCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                                 Clock clock, SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus) {
        super(serviceCallCommands, endPointConfigurationService, clock, sapCustomPropertySets, thesaurus);
    }

    @Override
    public void utilitiesDeviceERPSmartMeterRegisterCreateRequestCIn(UtilsDvceERPSmrtMtrRegCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            if (!isAnyActiveEndpoint(UtilitiesDeviceRegisterCreateConfirmation.NAME)) {
                throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_NECESSARY_OUTBOUND_END_POINT,
                        UtilitiesDeviceRegisterCreateConfirmation.NAME);
            }

            if (!isAnyActiveEndpoint(UtilitiesDeviceRegisteredNotification.NAME)) {
                throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_NECESSARY_OUTBOUND_END_POINT,
                        UtilitiesDeviceRegisteredNotification.NAME);
            }

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> createServiceCallAndTransition(UtilitiesDeviceRegisterCreateRequestMessage.builder()
                            .from(requestMessage)
                            .build()));
            return null;
        });
    }


}
