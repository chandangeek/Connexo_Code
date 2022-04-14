/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.meterchange;

import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceMeterChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilitiesDeviceERPSmartMeterChangeRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqMsg;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class UtilitiesDeviceMeterChangeRequestEndpoint extends AbstractChangeRequestEndpoint implements UtilitiesDeviceERPSmartMeterChangeRequestCIn {

    @Inject
    UtilitiesDeviceMeterChangeRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                              Clock clock, WebServiceActivator webServiceActivator, DeviceService deviceService, ServiceCallService serviceCallService) {
        super(serviceCallCommands, endPointConfigurationService, clock, webServiceActivator, deviceService, serviceCallService);
    }

    @Override
    void validateConfiguredEndpoints() {
        if (!isAnyActiveEndpoint(UtilitiesDeviceMeterChangeConfirmation.NAME)) {
            throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                    UtilitiesDeviceMeterChangeConfirmation.NAME);
        }
    }

    @Override
    public void utilitiesDeviceERPSmartMeterChangeRequestCIn(UtilsDvceERPSmrtMtrChgReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                                UtilitiesDeviceMeterChangeRequestMessage message = UtilitiesDeviceMeterChangeRequestMessage.builder(getThesaurus())
                                        .from(requestMessage)
                                        .build();

                                handleRequestMessage(message);
                            }
                    );
            return null;
        });
    }
}
