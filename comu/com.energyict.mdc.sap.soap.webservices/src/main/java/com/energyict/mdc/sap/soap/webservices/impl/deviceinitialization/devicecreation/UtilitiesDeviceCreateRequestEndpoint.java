/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilitiesDeviceERPSmartMeterCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;

public class UtilitiesDeviceCreateRequestEndpoint extends AbstractCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterCreateRequestCIn {

    @Inject
    UtilitiesDeviceCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                         Clock clock, SAPCustomPropertySets sapCustomPropertySets, OrmService ormService, WebServiceActivator webServiceActivator) {
        super(serviceCallCommands, endPointConfigurationService, clock, sapCustomPropertySets, ormService, webServiceActivator);
    }

    @Override
    public void utilitiesDeviceERPSmartMeterCreateRequestCIn(UtilsDvceERPSmrtMtrCrteReqMsg requestMessage) {
        runInTransactionWithOccurrence(() -> {
            if (requestMessage != null) {
                UtilitiesDeviceCreateRequestMessage request = UtilitiesDeviceCreateRequestMessage.builder()
                        .from(requestMessage)
                        .build();

                if (request.getUtilitiesDeviceCreateMessages().get(0) != null) {
                    SetMultimap<String, String> values = HashMultimap.create();
                    values.put(CimAttributeNames.SERIAL_ID.getAttributeName(), request.getUtilitiesDeviceCreateMessages().get(0).getSerialId());
                    values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), request.getUtilitiesDeviceCreateMessages().get(0).getDeviceId());
                    saveRelatedAttributes(values);
                }

                if (!isAnyActiveEndpoint(UtilitiesDeviceCreateConfirmation.NAME)) {
                    throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                            UtilitiesDeviceCreateConfirmation.NAME);
                }
                createServiceCallAndTransition(request);
            }
            return null;
        });
    }
}
