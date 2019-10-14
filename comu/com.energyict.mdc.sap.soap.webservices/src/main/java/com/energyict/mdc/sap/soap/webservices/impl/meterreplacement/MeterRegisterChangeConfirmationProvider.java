/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MeterRegisterChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;

@Component(name = MeterRegisterChangeConfirmation.NAME,
        service = {MeterRegisterChangeConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterRegisterChangeConfirmation.NAME})
public class MeterRegisterChangeConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut> implements MeterRegisterChangeConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterRegisterChangeConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return MeterRegisterChangeConfirmation.NAME;
    }

    @Override
    public void call(MeterRegisterChangeConfirmationMessage msg) {
        using("utilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut")
                .send(msg.getConfirmationMessage());
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
