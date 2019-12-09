package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentBulkCancellationConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentCancellationConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmrtMtrMtrRdngDocERPBulkCanclnConfMsg;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterReadingDocumentBulkCancellationConfirmationTest extends AbstractOutboundWebserviceTest<SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut> {
    private SmrtMtrMtrRdngDocERPBulkCanclnConfMsg confirmationMessage = new SmrtMtrMtrRdngDocERPBulkCanclnConfMsg();
    @Mock
    private MeterReadingDocumentCancellationConfirmationMessage outboundMessage;

    private MeterReadingDocumentBulkCancellationConfirmationProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getBulkConfirmationMessage()).thenReturn(Optional.of(confirmationMessage));

        provider = getProviderInstance(MeterReadingDocumentBulkCancellationConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        verify(endpoint).smartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut(confirmationMessage);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP MeterReadingBulkCancellationConfirmation'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(SmartMeterMeterReadingDocumentERPBulkCancellationConfirmationCOutService.class);
    }

}
