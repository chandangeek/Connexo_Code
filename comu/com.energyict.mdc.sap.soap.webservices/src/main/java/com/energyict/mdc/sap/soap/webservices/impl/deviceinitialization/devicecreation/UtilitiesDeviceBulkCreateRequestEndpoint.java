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
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrBlkCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

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

                                SetMultimap<String, String> values = HashMultimap.create();

                                message.getUtilitiesDeviceCreateMessages().forEach(req -> {
                                    values.put(CimAttributeNames.SERIAL_ID.getAttributeName(), req.getSerialId());
                                    values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), req.getDeviceId());
                                });

                                saveRelatedAttributes(values);

                                if (!isAnyActiveEndpoint(UtilitiesDeviceBulkCreateConfirmation.NAME)) {
                                    throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                            UtilitiesDeviceBulkCreateConfirmation.NAME);
                                }

                                createServiceCallAndTransition(message);
                            }
                    );
            return null;
        });
    }
}
