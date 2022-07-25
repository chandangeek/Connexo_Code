/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface MeterEventCreateRequestProvider {

    String SAP_CREATE_UTILITIES_SMART_METER_EVENT = "SAP SmartMeterEventBulkCreateRequest";

    Optional<UtilsSmrtMtrEvtERPBulkCrteReqMsg> createBulkMessage(EndDeviceEventRecord... event);

    void send(UtilsSmrtMtrEvtERPBulkCrteReqMsg reqMsg);
}