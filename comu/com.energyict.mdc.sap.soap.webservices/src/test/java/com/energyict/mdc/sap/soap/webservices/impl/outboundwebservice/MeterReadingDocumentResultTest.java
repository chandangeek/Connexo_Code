/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateResultMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentResultCreateRequestProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterReadingDocumentResultTest extends AbstractOutboundWebserviceTest<MeterReadingDocumentERPResultCreateRequestCOut> {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MtrRdngDocERPRsltCrteReqMsg resultMessage;
    @Mock
    private MeterReadingDocumentCreateResultMessage outboundMessage;

    private MeterReadingDocumentResultCreateRequestProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getResultMessage()).thenReturn(resultMessage);
        when(resultMessage.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtilitiesMeasurementTaskID().getValue()).thenReturn("UtilMeasurmentTaskID");
        when(resultMessage.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtiltiesDevice().getUtilitiesDeviceID().getValue()).thenReturn("UtilDeviceID");

        provider = getProviderInstance(MeterReadingDocumentResultCreateRequestProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        SetMultimap<String, String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), "UtilMeasurmentTaskID");
        values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),"UtilDeviceID");

        verify(endpoint).meterReadingDocumentERPResultCreateRequestCOut(resultMessage);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SapMeterReadingResult'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(MeterReadingDocumentERPResultCreateRequestCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(MeterReadingDocumentERPResultCreateRequestCOutService.class);
    }
}
