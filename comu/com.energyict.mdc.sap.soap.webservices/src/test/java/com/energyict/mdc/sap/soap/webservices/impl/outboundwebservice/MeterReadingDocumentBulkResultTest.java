/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentBulkResultCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateResultMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterReadingDocumentBulkResultTest extends AbstractOutboundWebserviceTest<MeterReadingDocumentERPResultBulkCreateRequestCOut> {
    @Mock
    private MeterReadingDocumentCreateResultMessage outboundMessage;
    @Mock
    private MtrRdngDocERPRsltBulkCrteReqMsg resultMessage;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MtrRdngDocERPRsltCrteReqMsg crteReqMsg;

    private MeterReadingDocumentBulkResultCreateRequestProvider provider;
    private List<MtrRdngDocERPRsltCrteReqMsg> meterReadingDocumentERPResultCreateRequestMessage = new ArrayList<>();

    @Before
    public void setUp() {
        meterReadingDocumentERPResultCreateRequestMessage.add(crteReqMsg);
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getBulkResultMessage()).thenReturn(resultMessage);
        when(resultMessage.getMeterReadingDocumentERPResultCreateRequestMessage()).thenReturn(meterReadingDocumentERPResultCreateRequestMessage);
        when(crteReqMsg.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtilitiesMeasurementTaskID().getValue()).thenReturn("UtilMesurmentTaskId");
        when(crteReqMsg.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtiltiesDevice().getUtilitiesDeviceID().getValue()).thenReturn("UtilDeviceId");

        provider = getProviderInstance(MeterReadingDocumentBulkResultCreateRequestProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(),
                "UtilMesurmentTaskId");
        values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                "UtilDeviceId");

        verify(endpoint).meterReadingDocumentERPResultBulkCreateRequestCOut(resultMessage);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP MeterReadingBulkResultRequest'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(MeterReadingDocumentERPResultBulkCreateRequestCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(MeterReadingDocumentERPResultBulkCreateRequestCOutService.class);
    }
}
