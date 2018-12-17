/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MeterReadingDocumentRequestConfirmation {
    String LOCAL_PART = "SmartMeterMeterReadingDocumentERPCreateConfirmation_E_OutService";
    String NAMESPACE_URI = "urn:webservices.wsdl.soap.sap.mdc.energyict.com:smartmetermeterreadingcreateconfirmation";
    String RESOURCE = "/wsdl/sap/SmartMeterMeterReadingDocumentERPCreateConfirmation_E_OutService.wsdl";
    String SAP_METER_READING_DOCUMENT_REQUEST_CONFIRMATION = "SapMeterReadingRequestConfirmation";

    /**
     * Invoked by the service call when the SAP meter reading document request is proceeded
     */
    void call(MeterReadingDocumentRequestConfirmationMessage confirmationMessage);
}
