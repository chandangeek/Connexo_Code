package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentCancellationConfirmationMessage;

public interface MeterReadingDocumentBulkCancellationConfirmation {
    String NAME = "SAP MeterReadingBulkCancellationConfirmation";

    /**
     * Invoked by the service call when the SAP meter reading document bulk cancellation is proceeded
     */
    void call(MeterReadingDocumentCancellationConfirmationMessage confMsg);
}
