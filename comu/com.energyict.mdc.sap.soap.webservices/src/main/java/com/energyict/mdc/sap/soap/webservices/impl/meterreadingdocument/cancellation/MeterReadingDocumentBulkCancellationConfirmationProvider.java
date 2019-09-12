package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentBulkCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmrtMtrMtrRdngDocERPBulkCanclnConfMsg;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;


@Component(name = MeterReadingDocumentBulkCancellationConfirmation.NAME,
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
    public Class getService() {
        return SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut.class;
    }

    @Override
    public void call(MeterReadingDocumentCancellationConfirmationMessage confMsg) {
        using("smartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut")
                .send(confMsg.getBulkConfirmationMessage().get());
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
