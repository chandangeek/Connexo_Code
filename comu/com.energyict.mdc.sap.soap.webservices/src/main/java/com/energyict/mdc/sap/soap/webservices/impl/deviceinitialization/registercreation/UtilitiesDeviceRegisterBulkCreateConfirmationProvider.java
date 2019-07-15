/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisterBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = UtilitiesDeviceRegisterBulkCreateConfirmation.SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_BULK_CREATE_CONFIRMATION_C_OUT,
        service = {UtilitiesDeviceRegisterBulkCreateConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceRegisterBulkCreateConfirmation.SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_BULK_CREATE_CONFIRMATION_C_OUT})
public class UtilitiesDeviceRegisterBulkCreateConfirmationProvider implements UtilitiesDeviceRegisterBulkCreateConfirmation,
        OutboundSoapEndPointProvider {

    private final Map<String, UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut> ports = new HashMap<>();
    private Thesaurus thesaurus;

    public UtilitiesDeviceRegisterBulkCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut port,
                                           Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut.class;
    }

    @Override
    public void call(UtilitiesDeviceRegisterCreateConfirmationMessage msg) {
        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        ports.values().stream().findFirst().get().utilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut(msg.getBulkConfirmationMessage());
    }
}
