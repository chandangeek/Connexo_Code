/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.eventmanagement;

import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractInboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.UtilsSmrtMtrEvtERPBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.UtilsSmrtMtrEvtERPCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.UtilsSmrtMtrEvtERPCrteConfUtilsSmrtMtrEvt;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class MeterEventCreateRequestConfirmationReceiverTest extends AbstractInboundWebserviceTest {
    private static final String REF_UUID = java.util.UUID.randomUUID().toString();
    private UtilsSmrtMtrEvtERPBulkCrteConfMsg message;
    private MeterEventCreateRequestConfirmationReceiver service;

    @Before
    public void setUp() {
        message = new UtilsSmrtMtrEvtERPBulkCrteConfMsg();
        BusinessDocumentMessageHeader header = new BusinessDocumentMessageHeader();
        UUID uuid = new UUID();
        uuid.setValue(REF_UUID);
        header.setReferenceUUID(uuid);
        message.setMessageHeader(header);

        service = getProviderInstance(MeterEventCreateRequestConfirmationReceiver.class);
    }

    @Test
    public void testApplication() {
        assertThat(service.getApplication()).isEqualTo("MultiSense");
    }

    @Test
    public void testPassedConfirmation() {
        Log log = new Log();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        message.setLog(log);

        UtilsSmrtMtrEvtERPCrteConfMsg confMsg = new UtilsSmrtMtrEvtERPCrteConfMsg();
        UtilsSmrtMtrEvtERPCrteConfUtilsSmrtMtrEvt event = new UtilsSmrtMtrEvtERPCrteConfUtilsSmrtMtrEvt();
        UtilitiesDeviceID utilitiesDeviceID = new UtilitiesDeviceID();
        utilitiesDeviceID.setValue("1");
        event.setUtilitiesDeviceID(utilitiesDeviceID);
        confMsg.setUtilitiesSmartMeterEvent(event);
        message.getUtilitiesSmartMeterEventERPCreateConfirmationMessage().add(confMsg);

        service.utilitiesSmartMeterEventERPBulkCreateConfirmationCIn(message);

        SetMultimap<String, String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                "1");

        verify(webServiceCallOccurrenceService).passOccurrence(webServiceCallOccurrence.getId());
        verify(webServiceCallOccurrence).log(LogLevel.INFO, "Confirmed smart meter event creation request with UUID " + REF_UUID + ".");
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testFailedConfirmationNoError() {
        Log log = new Log();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        message.setLog(log);

        assertThrowsException(() -> service.utilitiesSmartMeterEventERPBulkCreateConfirmationCIn(message),
                SAPWebServiceException.class,
                "Failed to confirm smart meter event creation request with UUID " + REF_UUID + ": No message provided.");
    }

    @Test
    public void testFailedConfirmationNoUUID() {
        Log log = new Log();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        message.setLog(log);
        message.getMessageHeader().setReferenceUUID(null);

        assertThrowsException(() -> service.utilitiesSmartMeterEventERPBulkCreateConfirmationCIn(message),
                SAPWebServiceException.class,
                "Failed to confirm smart meter event creation request with UUID null: No message provided.");
    }

    @Test
    public void testFailedConfirmationWithError() {
        Log log = new Log();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        message.setLog(log);

        LogItem logItem = new LogItem();
        logItem.setNote("Custom error.");
        log.getItem().add(logItem);

        assertThrowsException(() -> service.utilitiesSmartMeterEventERPBulkCreateConfirmationCIn(message),
                SAPWebServiceException.class,
                "Failed to confirm smart meter event creation request with UUID " + REF_UUID + ": Custom error.");
    }

    @Test
    public void testFailedConfirmationWithSeveralErrors() {
        Log log = new Log();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        message.setLog(log);

        LogItem logItem = new LogItem();
        logItem.setNote("Custom info.");
        logItem.setSeverityCode(SeverityCode.INFORMATION.getCode());
        log.getItem().add(logItem);

        logItem = new LogItem();
        logItem.setNote("Custom warning.");
        logItem.setSeverityCode(SeverityCode.WARNING.getCode());
        log.getItem().add(logItem);

        logItem = new LogItem();
        logItem.setNote("Custom error.");
        logItem.setSeverityCode(SeverityCode.ERROR.getCode());
        log.getItem().add(logItem);

        logItem = new LogItem();
        logItem.setNote("Custom abort error.");
        logItem.setSeverityCode(SeverityCode.ABORT.getCode());
        log.getItem().add(logItem);

        assertThrowsException(() -> service.utilitiesSmartMeterEventERPBulkCreateConfirmationCIn(message),
                SAPWebServiceException.class,
                "Failed to confirm smart meter event creation request with UUID " + REF_UUID + ": Custom abort error.");
    }

    @Test
    public void testFailedConfirmationWithSeveralIncompleteErrors() {
        Log log = new Log();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        message.setLog(log);

        LogItem logItem = new LogItem();
        logItem.setNote("Custom error 1.");
        log.getItem().add(logItem);

        logItem = new LogItem();
        logItem.setSeverityCode(SeverityCode.ERROR.getCode());
        log.getItem().add(logItem);

        logItem = new LogItem();
        logItem.setNote("Custom error 2.");
        logItem.setSeverityCode(SeverityCode.ERROR.getCode());
        log.getItem().add(logItem);

        assertThrowsException(() -> service.utilitiesSmartMeterEventERPBulkCreateConfirmationCIn(message),
                SAPWebServiceException.class,
                "Failed to confirm smart meter event creation request with UUID " + REF_UUID + ": Custom error 2.");
    }
}
