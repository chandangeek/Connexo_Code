/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
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
                                         Clock clock, WebServiceActivator webServiceActivator, DeviceService deviceService, ServiceCallService serviceCallService) {
        super(serviceCallCommands, endPointConfigurationService, clock, webServiceActivator, deviceService, serviceCallService);
    }

    @Override
    void validateConfiguredEndpoints() {
        if (!isAnyActiveEndpoint(UtilitiesDeviceCreateConfirmation.NAME)) {
            throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                    UtilitiesDeviceCreateConfirmation.NAME);
        }
    }

    @Override
    public void utilitiesDeviceERPSmartMeterCreateRequestCIn(UtilsDvceERPSmrtMtrCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                                UtilitiesDeviceCreateRequestMessage message = UtilitiesDeviceCreateRequestMessage.builder(getThesaurus())
                                        .from(requestMessage)
                                        .build();

                                handleRequestMessage(message);
                            }
                    );
            return null;
        });
    }
}
