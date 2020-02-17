/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;

public interface MeterReadingDocumentBulkRequestConfirmation {
    String SAP_METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATION = "SAP MeterReadingBulkRequestConfirmation";

    /**
     * Invoked by the service call when the SAP meter reading document request is proceeded
     */
    void call(MeterReadingDocumentRequestConfirmationMessage confirmationMessage);
}
