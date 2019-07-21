/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = UtilitiesDeviceCreateConfirmation.SAP_UTILITIES_DEVICE_ERP_SMART_METER_CREATE_CONFIRMATION_C_OUT,
        service = {UtilitiesDeviceCreateConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceCreateConfirmation.SAP_UTILITIES_DEVICE_ERP_SMART_METER_CREATE_CONFIRMATION_C_OUT})
public class UtilitiesDeviceCreateConfirmationProvider implements UtilitiesDeviceCreateConfirmation,
        OutboundSoapEndPointProvider {

    private final Map<String, UtilitiesDeviceERPSmartMeterCreateConfirmationCOut> ports = new HashMap<>();

    private volatile Thesaurus thesaurus;

    public UtilitiesDeviceCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterCreateConfirmationCOut port,
                                           Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterCreateConfirmationCOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterCreateConfirmationCOut.class;
    }

    @Override
    public void call(UtilsDvceERPSmrtMtrCrteConfMsg msg) {
        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        ports.values().stream().findFirst().get().utilitiesDeviceERPSmartMeterCreateConfirmationCOut(msg);
    }
}
