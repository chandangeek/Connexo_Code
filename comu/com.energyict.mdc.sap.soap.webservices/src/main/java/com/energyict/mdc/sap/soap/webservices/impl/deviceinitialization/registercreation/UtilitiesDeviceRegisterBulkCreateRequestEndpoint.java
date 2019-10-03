/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisterBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisteredBulkNotification;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilitiesDeviceERPSmartMeterRegisterBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegBulkCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class UtilitiesDeviceRegisterBulkCreateRequestEndpoint extends AbstractRegisterCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterRegisterBulkCreateRequestCIn {


    @Inject
    UtilitiesDeviceRegisterBulkCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                                     Clock clock, SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus, OrmService ormService) {
        super(serviceCallCommands, endPointConfigurationService, clock, sapCustomPropertySets, thesaurus, ormService);
    }

    @Override
    public void utilitiesDeviceERPSmartMeterRegisterBulkCreateRequestCIn(UtilsDvceERPSmrtMtrRegBulkCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {

            SetMultimap<String, String> values = HashMultimap.create();

            request.getUtilitiesDeviceERPSmartMeterRegisterCreateRequestMessage().forEach(req -> {
                values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), req.getUtilitiesDevice().getID().getValue());
                req.getUtilitiesDevice().getRegister().forEach(reg -> {
                    values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), reg.getUtilitiesMeasurementTaskID().getValue());
                });
            });

            createRelatedObjects(values);

            if (!isAnyActiveEndpoint(UtilitiesDeviceRegisterBulkCreateConfirmation.NAME)) {
                throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                        UtilitiesDeviceRegisterBulkCreateConfirmation.NAME);
            }

            if (!isAnyActiveEndpoint(UtilitiesDeviceRegisteredBulkNotification.NAME)) {
                throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                        UtilitiesDeviceRegisteredBulkNotification.NAME);
            }

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> createServiceCallAndTransition(UtilitiesDeviceRegisterCreateRequestMessage.builder()
                            .from(requestMessage)
                            .build()));
            return null;
        });

    }
}
