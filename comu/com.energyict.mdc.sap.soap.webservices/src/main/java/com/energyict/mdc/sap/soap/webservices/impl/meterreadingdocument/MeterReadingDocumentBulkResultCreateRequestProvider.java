/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentBulkResult;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestCOutService;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentbulkresult.outbound.provider",
        service = {MeterReadingDocumentBulkResult.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentBulkResult.SAP_METER_READING_DOCUMENT_BULK_RESULT})
public class MeterReadingDocumentBulkResultCreateRequestProvider extends AbstractOutboundEndPointProvider<MeterReadingDocumentERPResultBulkCreateRequestCOut> implements MeterReadingDocumentBulkResult, OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterReadingDocumentBulkResultCreateRequestProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addBulkResultsPort(MeterReadingDocumentERPResultBulkCreateRequestCOut port,
                                   Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeBulkResultsPort(MeterReadingDocumentERPResultBulkCreateRequestCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Override
    public Service get() {
        return new MeterReadingDocumentERPResultBulkCreateRequestCOutService();
    }

    @Override
    public Class getService() {
        return MeterReadingDocumentERPResultBulkCreateRequestCOut.class;
    }

    @Override
    protected String getName() {
        return MeterReadingDocumentBulkResult.SAP_METER_READING_DOCUMENT_BULK_RESULT;
    }

    @Override
    public void call(MeterReadingDocumentCreateResultMessage resultMessage) {
        SetMultimap<String, String> values = HashMultimap.create();
        resultMessage.getBulkResultMessage().getMeterReadingDocumentERPResultCreateRequestMessage().forEach(reading->{
            values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(),
                    reading.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtilitiesMeasurementTaskID().getValue());
            values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                    reading.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtiltiesDevice().getUtilitiesDeviceID().getValue());
        });

        using("meterReadingDocumentERPResultBulkCreateRequestCOut")
                .withRelatedAttributes(values)
                .send(resultMessage.getBulkResultMessage());
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}