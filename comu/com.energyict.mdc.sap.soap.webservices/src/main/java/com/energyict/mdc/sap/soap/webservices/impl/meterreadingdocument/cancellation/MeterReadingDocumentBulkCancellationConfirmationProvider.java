package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentBulkCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmrtMtrMtrRdngDocERPBulkCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentBulkCancellationConfirmationProvider",
        service = {MeterReadingDocumentBulkCancellationConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentBulkCancellationConfirmation.NAME})
public class MeterReadingDocumentBulkCancellationConfirmationProvider extends AbstractOutboundEndPointProvider<SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut>
        implements MeterReadingDocumentBulkCancellationConfirmation, OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterReadingDocumentBulkCancellationConfirmationProvider() {
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
    public void addRequestConfirmationPort(SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOutService();
    }

    @Override
    public Class<SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut> getService() {
        return SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut.class;
    }

    @Override
    public void call(MeterReadingDocumentCancellationConfirmationMessage confMsg) {
        SetMultimap<String, String> values = HashMultimap.create();
        SmrtMtrMtrRdngDocERPBulkCanclnConfMsg message = confMsg.getBulkConfirmationMessage()
                .orElseThrow(() -> new IllegalStateException("Bulk confirmation message is empty."));

        message.getSmartMeterMeterReadingDocumentERPCancellationConfirmationMessage().forEach(cnfMsg -> {
            getMeterReadingDocumentId(cnfMsg).ifPresent(value -> values.put(SapAttributeNames.SAP_METER_READING_DOCUMENT_ID.getAttributeName(), value));
        });

        using("smartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut")
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
        return MeterReadingDocumentBulkCancellationConfirmation.NAME;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
