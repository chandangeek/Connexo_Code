/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.pod;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryBulkAssignedNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg;

import javax.inject.Inject;
import java.util.Optional;

public class PointOfDeliveryBulkAssignedNotificationEndpoint extends AbstractPodNotification
        implements SmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryBulkAssignedNotificationCIn {

    @Inject
    PointOfDeliveryBulkAssignedNotificationEndpoint(ServiceCallService serviceCallService, Thesaurus thesaurus, ServiceCallCommands serviceCallCommands) {
        super(serviceCallService, thesaurus, serviceCallCommands);
    }

    @Override
    public void smartMeterUtilitiesMeasurementTaskERPPointOfDeliveryBulkAssignedNotificationCIn(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        PodNotificationMessage message = PodNotificationMessage.builder(getThesaurus())
                                .from(requestMessage)
                                .build();

                        handleMessage(message);
                    });
            return null;
        });
    }
}
