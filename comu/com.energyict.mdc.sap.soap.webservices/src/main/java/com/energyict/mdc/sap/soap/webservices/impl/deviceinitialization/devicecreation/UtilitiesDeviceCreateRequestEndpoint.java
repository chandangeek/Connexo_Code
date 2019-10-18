/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilitiesDeviceERPSmartMeterCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqMsg;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class UtilitiesDeviceCreateRequestEndpoint extends AbstractCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterCreateRequestCIn {

    @Inject
    UtilitiesDeviceCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                         Clock clock, SAPCustomPropertySets sapCustomPropertySets, OrmService ormService, WebServiceActivator webServiceActivator) {
        super(serviceCallCommands, endPointConfigurationService, clock, sapCustomPropertySets, ormService, webServiceActivator);
    }

    @Override
    public void utilitiesDeviceERPSmartMeterCreateRequestCIn(UtilsDvceERPSmrtMtrCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {

            Optional.ofNullable(request).ifPresent(requestMsg->{
                saveRelatedAttribute(CimAttributeNames.SERIAL_ID.getAttributeName(), getSerialId(requestMsg));
                saveRelatedAttribute(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), getDeviceId(requestMsg));
            });

            if (!isAnyActiveEndpoint(UtilitiesDeviceCreateConfirmation.NAME)) {
                throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                        UtilitiesDeviceCreateConfirmation.NAME);
            }

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> createServiceCallAndTransition(UtilitiesDeviceCreateRequestMessage.builder()
                            .from(requestMessage)
                            .build()));
            return null;
        });
    }
}
