package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentCancellationConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentCancellationConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmartMeterMeterReadingDocumentERPCancellationConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmartMeterMeterReadingDocumentERPCancellationConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterReadingDocumentCancellationConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private SmartMeterMeterReadingDocumentERPCancellationConfirmationCOut port;
    @Mock
    private SmrtMtrMtrRdngDocERPCanclnConfMsg confirmationMessage;
    @Mock
    private MeterReadingDocumentCancellationConfirmationMessage outboundMessage;

    MeterReadingDocumentCancellationConfirmationProvider provider;

    @Before
    public void setUp() {
        provider = spy(new MeterReadingDocumentCancellationConfirmationProvider());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(outboundMessage.getConfirmationMessage()).thenReturn(Optional.of(confirmationMessage));
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());
        properties.put("epcId", 1l);

        provider.addRequestConfirmationPort(port, properties);
        provider.call(outboundMessage);

        verify(provider).using("smartMeterMeterReadingDocumentERPCancellationConfirmationCOut");
        verify(requestSender).send(confirmationMessage);
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SAP MeterReadingCancellationConfirmation'.");

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), SmartMeterMeterReadingDocumentERPCancellationConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), SmartMeterMeterReadingDocumentERPCancellationConfirmationCOutService.class);
    }

}
