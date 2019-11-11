/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisterCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilitiesDeviceERPSmartMeterRegisterCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class UtilitiesDeviceRegisterCreateRequestEndpoint extends AbstractRegisterCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterRegisterCreateRequestCIn {

    @Inject
    UtilitiesDeviceRegisterCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                                 Clock clock, SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus, OrmService ormService) {
        super(serviceCallCommands, endPointConfigurationService, clock, sapCustomPropertySets, thesaurus, ormService);
    }

    @Override
    public void utilitiesDeviceERPSmartMeterRegisterCreateRequestCIn(UtilsDvceERPSmrtMtrRegCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        UtilitiesDeviceRegisterCreateRequestMessage message = UtilitiesDeviceRegisterCreateRequestMessage.builder()
                                .from(requestMessage)
                                .build();

                        if (message.getUtilitiesDeviceRegisterCreateMessages().get(0) != null) {
                            SetMultimap<String, String> values = HashMultimap.create();
                            values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), message.getUtilitiesDeviceRegisterCreateMessages().get(0).getDeviceId());
                            message.getUtilitiesDeviceRegisterCreateMessages().get(0).getUtilitiesDeviceRegisterMessages().forEach(register -> {
                                values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(),
                                        register.getLrn());
                            });

                            saveRelatedAttributes(values);
                        }

                        if (!isAnyActiveEndpoint(UtilitiesDeviceRegisterCreateConfirmation.NAME)) {
                            throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                    UtilitiesDeviceRegisterCreateConfirmation.NAME);
                        }

                        if (!isAnyActiveEndpoint(UtilitiesDeviceRegisteredNotification.NAME)) {
                            throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                    UtilitiesDeviceRegisteredNotification.NAME);
                        }

                        createServiceCallAndTransition(message);

                    });
            return null;
        });
    }


}
