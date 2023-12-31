package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentCancellationConfirmationMessage;

public interface MeterReadingDocumentCancellationConfirmation {
    String NAME = "SAP MeterReadingCancellationConfirmation";

    /**
     * Invoked by the service call when the SAP meter reading document cancellation is proceeded
     */
    void call(MeterReadingDocumentCancellationConfirmationMessage confMsg);
}
