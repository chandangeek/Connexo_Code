/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisterCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = UtilitiesDeviceRegisterCreateConfirmation.SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_CREATE_CONFIRMATION_C_OUT,
        service = {UtilitiesDeviceRegisterCreateConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceRegisterCreateConfirmation.SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_CREATE_CONFIRMATION_C_OUT})
public class UtilitiesDeviceRegisterCreateConfirmationProvider implements UtilitiesDeviceRegisterCreateConfirmation,
        OutboundSoapEndPointProvider {

    private final Map<String, UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut> ports = new HashMap<>();
    private Thesaurus thesaurus;

    public UtilitiesDeviceRegisterCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut port,
                                           Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut.class;
    }

    @Override
    public void call(UtilitiesDeviceRegisterCreateConfirmationMessage msg) {
        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        ports.values().stream().findFirst().get().utilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut(msg.getConfirmationMessage());
    }
}
