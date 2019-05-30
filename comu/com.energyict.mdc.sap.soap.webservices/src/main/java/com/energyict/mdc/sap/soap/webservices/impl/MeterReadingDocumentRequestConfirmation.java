/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MeterReadingDocumentRequestConfirmation {
    String SAP_METER_READING_DOCUMENT_REQUEST_CONFIRMATION = "SapMeterReadingRequestConfirmation";

    /**
     * Invoked by the service call when the SAP meter reading document request is proceeded
     */
    void call(MeterReadingDocumentRequestConfirmationMessage confirmationMessage);
}
