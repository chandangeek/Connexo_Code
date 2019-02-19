/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MeterReadingDocumentBulkRequestConfirmation {
    String LOCAL_PART = "SmartMeterMeterReadingDocumentERPBulkCreateConfirmation_E_OutService";
    String NAMESPACE_URI = "urn:webservices.wsdl.soap.sap.mdc.energyict.com:smartmetermeterreadingbulkcreateconfirmation";
    String RESOURCE = "/wsdl/sap/SmartMeterMeterReadingDocumentERPBulkCreateConfirmation_E_OutService.wsdl";
    String SAP_METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATION = "SapMeterReadingBulkRequestConfirmation";

    /**
     * Invoked by the service call when the SAP meter reading document request is proceeded
     */
    void call(MeterReadingDocumentRequestConfirmationMessage confirmationMessage);
}
