/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateResultMessage;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MeterReadingDocumentResult {
    String LOCAL_PART = "MeterReadingDocumentERPResultCreateRequest_E_OutService";
    String NAMESPACE_URI = "urn:webservices.wsdl.soap.sap.mdc.energyict.com:meterreadingresultcreaterequest";
    String RESOURCE = "/wsdl/sap/MeterReadingDocumentERPResultCreateRequest_E_OutService.wsdl";
    String SAP_METER_READING_DOCUMENT_RESULT = "SapMeterReadingResult";

    /**
     * Invoked by the service call when the meter reading document is ready to be sent to SAP
     */
    void call(MeterReadingDocumentCreateResultMessage resultMessage);
}
