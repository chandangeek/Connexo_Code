/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MeterRegisterBulkChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;

@Component(name = MeterRegisterBulkChangeConfirmation.NAME,
        service = {MeterRegisterBulkChangeConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterRegisterBulkChangeConfirmation.NAME})
public class MeterRegisterBulkChangeConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut> implements MeterRegisterBulkChangeConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterRegisterBulkChangeConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut.class;
    }

    @Override
    public void call(MeterRegisterBulkChangeConfirmationMessage msg) {
        using("utilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut")
                .send(msg.getConfirmationMessage());
    }

    @Override
    protected String getName() {
        return MeterRegisterBulkChangeConfirmation.NAME;
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
