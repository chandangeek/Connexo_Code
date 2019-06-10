/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultServiceCallHandler;

import java.util.Optional;

public enum ServiceCallTypes {

    CONNECTION_STATUS_CHANGE(
            ConnectionStatusChangeServiceCallHandler.NAME,
            ConnectionStatusChangeServiceCallHandler.VERSION,
            ConnectionStatusChangeServiceCallHandler.APPLICATION,
            ConnectionStatusChangeCustomPropertySet.class.getSimpleName(),
            ConnectionStatusChangeDomainExtension.class.getName()),
    MASTER_METER_READING_DOCUMENT_CREATE_REQUEST(
            MasterMeterReadingDocumentCreateRequestServiceCallHandler.NAME,
            MasterMeterReadingDocumentCreateRequestServiceCallHandler.VERSION,
            MasterMeterReadingDocumentCreateRequestServiceCallHandler.APPLICATION,
            MasterMeterReadingDocumentCreateRequestCustomPropertySet.class.getSimpleName(),
            MasterMeterReadingDocumentCreateRequestDomainExtension.class.getName()),
    METER_READING_DOCUMENT_CREATE_REQUEST(
            MeterReadingDocumentCreateRequestServiceCallHandler.NAME,
            MeterReadingDocumentCreateRequestServiceCallHandler.VERSION,
            MeterReadingDocumentCreateRequestServiceCallHandler.APPLICATION,
            MeterReadingDocumentCreateRequestCustomPropertySet.class.getSimpleName(),
            MeterReadingDocumentCreateRequestDomainExtension.class.getName()),
    MASTER_METER_READING_DOCUMENT_CREATE_RESULT(
            MasterMeterReadingDocumentCreateResultServiceCallHandler.NAME,
            MasterMeterReadingDocumentCreateResultServiceCallHandler.VERSION,
            MasterMeterReadingDocumentCreateResultServiceCallHandler.APPLICATION,
            MasterMeterReadingDocumentCreateResultCustomPropertySet.class.getSimpleName(),
            MasterMeterReadingDocumentCreateResultDomainExtension.class.getName()),
    METER_READING_DOCUMENT_CREATE_RESULT(
            MeterReadingDocumentCreateResultServiceCallHandler.NAME,
            MeterReadingDocumentCreateResultServiceCallHandler.VERSION,
            MeterReadingDocumentCreateResultServiceCallHandler.APPLICATION,
            MeterReadingDocumentCreateResultCustomPropertySet.class.getSimpleName(),
            MeterReadingDocumentCreateResultDomainExtension.class.getName())
    ;

    private final String typeName;
    private final String typeVersion;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<String> reservedByApplication;
    private final String customPropertySetClass;
    private final String persistenceSupportClass;

    ServiceCallTypes(String typeName, String typeVersion, String application, String customPropertySetClass, String persistenceSupportClass) {
        this.typeName = typeName;
        this.typeVersion = typeVersion;
        this.reservedByApplication = Optional.ofNullable(application);
        this.customPropertySetClass = customPropertySetClass;
        this.persistenceSupportClass = persistenceSupportClass;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeVersion() {
        return typeVersion;
    }

    public Optional<String> getApplication() {
        return reservedByApplication;
    }

    public String getCustomPropertySetClass() {
        return customPropertySetClass;
    }

    public String getPersistenceSupportClass() {
        return persistenceSupportClass;
    }
}