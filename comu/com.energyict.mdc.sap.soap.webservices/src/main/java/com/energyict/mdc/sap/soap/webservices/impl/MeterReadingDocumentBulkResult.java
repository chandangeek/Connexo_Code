/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateResultMessage;

public interface MeterReadingDocumentBulkResult {
    String SAP_METER_READING_DOCUMENT_BULK_RESULT = "SAP MeterReadingBulkResultRequest";

    /**
     * Invoked by the service call when the meter reading document is ready to be sent to SAP
     */
    void call(MeterReadingDocumentCreateResultMessage resultMessage);
}
