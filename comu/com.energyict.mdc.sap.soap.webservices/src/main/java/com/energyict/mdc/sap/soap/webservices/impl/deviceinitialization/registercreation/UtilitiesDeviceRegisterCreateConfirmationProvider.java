/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisterCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component(name = UtilitiesDeviceRegisterCreateConfirmation.NAME,
        service = {UtilitiesDeviceRegisterCreateConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceRegisterCreateConfirmation.NAME})
public class UtilitiesDeviceRegisterCreateConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut> implements UtilitiesDeviceRegisterCreateConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public UtilitiesDeviceRegisterCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut port) {
        super.doRemoveEndpoint(port);
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
        Set<String> values = new HashSet<>();
        values.add(msg.getConfirmationMessage().get().getUtilitiesDevice().getID().getValue());
        using("utilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut")
                //.withRelatedObject("DeviceX", values)
                .send(msg.getConfirmationMessage().get());
    }

    @Override
    protected String getName() {
        return UtilitiesDeviceRegisterCreateConfirmation.NAME;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}