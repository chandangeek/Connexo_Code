package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmartMeterMeterReadingDocumentERPCancellationConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmartMeterMeterReadingDocumentERPCancellationConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentCancellationConfirmationProvider",
        service = {MeterReadingDocumentCancellationConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentCancellationConfirmation.NAME})
public class MeterReadingDocumentCancellationConfirmationProvider extends AbstractOutboundEndPointProvider<SmartMeterMeterReadingDocumentERPCancellationConfirmationCOut>
        implements MeterReadingDocumentCancellationConfirmation, OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterReadingDocumentCancellationConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService){
        // No action, just for binding WebServicesService
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(SmartMeterMeterReadingDocumentERPCancellationConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(SmartMeterMeterReadingDocumentERPCancellationConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new SmartMeterMeterReadingDocumentERPCancellationConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return SmartMeterMeterReadingDocumentERPCancellationConfirmationCOut.class;
    }

    @Override
    public void call(MeterReadingDocumentCancellationConfirmationMessage confMsg) {
        SetMultimap<String, String> values = HashMultimap.create();
        SmrtMtrMtrRdngDocERPCanclnConfMsg message = confMsg.getConfirmationMessage()
                .orElseThrow(() -> new IllegalStateException("Confirmation message is empty."));

        getMeterReadingDocumentId(message).ifPresent(value -> values.put(SapAttributeNames.SAP_METER_READING_DOCUMENT_ID.getAttributeName(), value));
        
        using("smartMeterMeterReadingDocumentERPCancellationConfirmationCOut")
                .withRelatedAttributes(values)
                .send(message);
    }

    private static Optional<String> getMeterReadingDocumentId(SmrtMtrMtrRdngDocERPCanclnConfMsg msg) {
        return Optional.ofNullable(msg)
                .map(SmrtMtrMtrRdngDocERPCanclnConfMsg::getMeterReadingDocument)
                .map(SmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc::getID)
                .map(MeterReadingDocumentID::getValue);
    }

    @Override
    protected String getName() {
        return MeterReadingDocumentCancellationConfirmation.NAME;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
