/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.sendmeterread;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingResultCreateConfirmation;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.SmartMeterMeterReadingDocumentERPResultCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.SmartMeterMeterReadingDocumentERPResultCreateConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.sendmeterread.MeterReadingResultCreateConfirmationProvider",
        service = {MeterReadingResultCreateConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingResultCreateConfirmation.NAME})
public class MeterReadingResultCreateConfirmationProvider extends AbstractOutboundEndPointProvider<SmartMeterMeterReadingDocumentERPResultCreateConfirmationCOut> implements MeterReadingResultCreateConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterReadingResultCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService){
        // No action, just for binding WebServicesService
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(SmartMeterMeterReadingDocumentERPResultCreateConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(SmartMeterMeterReadingDocumentERPResultCreateConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new SmartMeterMeterReadingDocumentERPResultCreateConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return SmartMeterMeterReadingDocumentERPResultCreateConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return MeterReadingResultCreateConfirmation.NAME;
    }

    @Override
    public void call(MeterReadingResultCreateConfirmationMessage msg) {
        using("smartMeterMeterReadingDocumentERPResultCreateConfirmationCOut").send(msg.getConfirmationMessage());
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
