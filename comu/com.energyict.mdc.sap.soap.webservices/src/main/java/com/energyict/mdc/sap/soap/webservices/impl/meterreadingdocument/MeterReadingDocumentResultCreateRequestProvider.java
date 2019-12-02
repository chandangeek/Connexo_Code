/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentResult;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqMtrRdngDoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqUtilsMsmtTsk;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.UtilitiesMeasurementTaskID;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentresult.outbound.provider",
        service = {MeterReadingDocumentResult.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentResult.SAP_METER_READING_DOCUMENT_RESULT})
public class MeterReadingDocumentResultCreateRequestProvider extends AbstractOutboundEndPointProvider<MeterReadingDocumentERPResultCreateRequestCOut> implements MeterReadingDocumentResult, OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterReadingDocumentResultCreateRequestProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResultPort(MeterReadingDocumentERPResultCreateRequestCOut port,
                              Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeResultPort(MeterReadingDocumentERPResultCreateRequestCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Override
    public Service get() {
        return new MeterReadingDocumentERPResultCreateRequestCOutService();
    }

    @Override
    public Class getService() {
        return MeterReadingDocumentERPResultCreateRequestCOut.class;
    }

    @Override
    protected String getName() {
        return MeterReadingDocumentResult.SAP_METER_READING_DOCUMENT_RESULT;
    }

    @Override
    public void call(MeterReadingDocumentCreateResultMessage resultMessage) {
        SetMultimap<String, String> values = HashMultimap.create();
        getTaskId(resultMessage.getResultMessage()).ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), value));
        getDeviceId(resultMessage.getResultMessage()).ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
        using("meterReadingDocumentERPResultCreateRequestCOut")
                .withRelatedAttributes(values)
                .send(resultMessage.getResultMessage());
    }

    private static Optional<String> getTaskId(MtrRdngDocERPRsltCrteReqMsg msg) {
        return Optional.ofNullable(msg)
                .map(MtrRdngDocERPRsltCrteReqMsg::getMeterReadingDocument)
                .map(MtrRdngDocERPRsltCrteReqMtrRdngDoc::getUtiltiesMeasurementTask)
                .map(MtrRdngDocERPRsltCrteReqUtilsMsmtTsk::getUtilitiesMeasurementTaskID)
                .map(UtilitiesMeasurementTaskID::getValue);
    }

    private static Optional<String> getDeviceId(MtrRdngDocERPRsltCrteReqMsg msg) {
        return Optional.ofNullable(msg)
                .map(MtrRdngDocERPRsltCrteReqMsg::getMeterReadingDocument)
                .map(MtrRdngDocERPRsltCrteReqMtrRdngDoc::getUtiltiesMeasurementTask)
                .map(MtrRdngDocERPRsltCrteReqUtilsMsmtTsk::getUtiltiesDevice)
                .map(MtrRdngDocERPRsltCrteReqUtilsDvce::getUtilitiesDeviceID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}