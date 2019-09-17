package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmartMeterMeterReadingDocumentERPCancellationConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmartMeterMeterReadingDocumentERPCancellationConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;

@Component(name = MeterReadingDocumentCancellationConfirmation.NAME,
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
        using("smartMeterMeterReadingDocumentERPCancellationConfirmationCOut")
                .send(confMsg.getConfirmationMessage().get());
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
