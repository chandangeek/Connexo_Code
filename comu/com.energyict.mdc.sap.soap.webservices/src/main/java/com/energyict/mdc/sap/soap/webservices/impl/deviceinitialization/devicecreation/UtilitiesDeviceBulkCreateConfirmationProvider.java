/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = UtilitiesDeviceBulkCreateConfirmation.NAME,
        service = {UtilitiesDeviceBulkCreateConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceBulkCreateConfirmation.NAME})
public class UtilitiesDeviceBulkCreateConfirmationProvider implements UtilitiesDeviceBulkCreateConfirmation,
        OutboundSoapEndPointProvider {

    private final Map<String, UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut> ports = new HashMap<>();

    private volatile Thesaurus thesaurus;

    public UtilitiesDeviceBulkCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut port,
                                           Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut.class;
    }

    @Override
    public void call(UtilitiesDeviceCreateConfirmationMessage msg) {
        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        ports.values().stream().findFirst().get().utilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut(msg.getConfirmationMessage());
    }
}
