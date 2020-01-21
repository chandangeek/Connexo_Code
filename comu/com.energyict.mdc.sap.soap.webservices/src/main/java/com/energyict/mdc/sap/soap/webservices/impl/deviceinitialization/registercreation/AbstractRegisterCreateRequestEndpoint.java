package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;

public abstract class AbstractRegisterCreateRequestEndpoint extends AbstractInboundEndPoint implements ApplicationSpecific {

    private final RegisterCreateRequestHandler registerCreateRequestHandler;

    @Inject
    AbstractRegisterCreateRequestEndpoint(RegisterCreateRequestHandler registerCreateRequestHandler) {
        this.registerCreateRequestHandler = registerCreateRequestHandler;

    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    void handleRequestMessage(UtilitiesDeviceRegisterCreateRequestMessage requestMessage) {

        SetMultimap<String, String> values = HashMultimap.create();

        requestMessage.getUtilitiesDeviceRegisterCreateMessages().forEach(device -> {
            values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), device.getDeviceId());
            device.getUtilitiesDeviceRegisterMessages().forEach(reg -> {
                values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), reg.getLrn());
            });
        });

        saveRelatedAttributes(values);

        registerCreateRequestHandler.handleRequestMessage(requestMessage);

    }
}
