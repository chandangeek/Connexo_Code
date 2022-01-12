/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.location;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilitiesDeviceERPSmartMeterLocationBulkNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocBulkNotifMsg;

import javax.inject.Inject;
import java.util.Optional;

public class UtilitiesDeviceLocationBulkNotificationEndpoint extends AbstractLocationNotificationEndpoint implements UtilitiesDeviceERPSmartMeterLocationBulkNotificationCIn {

    @Inject
    UtilitiesDeviceLocationBulkNotificationEndpoint(ServiceCallService serviceCallService, Thesaurus thesaurus, ServiceCallCommands serviceCallCommands) {
        super(serviceCallService, thesaurus, serviceCallCommands);
    }

    @Override
    public void utilitiesDeviceERPSmartMeterLocationBulkNotificationCIn(UtilsDvceERPSmrtMtrLocBulkNotifMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        LocationNotificationMessage message = LocationNotificationMessage.builder(getThesaurus())
                                .from(requestMessage)
                                .build();

                        handleMessage(message);
                    });
            return null;
        });
    }
}
