/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrBlkCrteReqMsg;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class UtilitiesDeviceBulkCreateRequestEndpoint extends AbstractCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn {

    @Inject
    UtilitiesDeviceBulkCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                             Clock clock, SAPCustomPropertySets sapCustomPropertySets, OrmService ormService, WebServiceActivator webServiceActivator) {
        super(serviceCallCommands, endPointConfigurationService, clock, sapCustomPropertySets, ormService, webServiceActivator);
    }

    @Override
    public void utilitiesDeviceERPSmartMeterBulkCreateRequestCIn(UtilsDvceERPSmrtMtrBlkCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                                UtilitiesDeviceCreateRequestMessage message = UtilitiesDeviceCreateRequestMessage.builder()
                                        .from(requestMessage)
                                        .build();

                                handleRequestMessage(message);
                            }
                    );
            return null;
        });
    }
}
