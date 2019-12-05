/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.MasterConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.MasterConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.MasterConnectionStatusChangeServiceCallHandler;
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
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequest;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequest;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequest;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestDomainExtension;

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
            MeterReadingDocumentCreateResultDomainExtension.class.getName()),
    MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST(
            MasterUtilitiesDeviceRegisterCreateRequestCallHandler.NAME,
            MasterUtilitiesDeviceRegisterCreateRequestCallHandler.VERSION,
            MasterUtilitiesDeviceRegisterCreateRequestCallHandler.APPLICATION,
            MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.class.getSimpleName(),
            MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class.getName()),
    SUB_MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST(
            SubMasterUtilitiesDeviceRegisterCreateRequestCallHandler.NAME,
            SubMasterUtilitiesDeviceRegisterCreateRequestCallHandler.VERSION,
            SubMasterUtilitiesDeviceRegisterCreateRequestCallHandler.APPLICATION,
            SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.class.getSimpleName(),
            SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class.getName()),
    UTILITIES_DEVICE_REGISTER_CREATE_REQUEST(
            UtilitiesDeviceRegisterCreateRequestCallHandler.NAME,
            UtilitiesDeviceRegisterCreateRequestCallHandler.VERSION,
            UtilitiesDeviceRegisterCreateRequestCallHandler.APPLICATION,
            UtilitiesDeviceRegisterCreateRequestCustomPropertySet.class.getSimpleName(),
            UtilitiesDeviceRegisterCreateRequestDomainExtension.class.getName()),
    MASTER_UTILITIES_DEVICE_CREATE_REQUEST(
            MasterUtilitiesDeviceCreateRequestCallHandler.NAME,
            MasterUtilitiesDeviceCreateRequestCallHandler.VERSION,
            MasterUtilitiesDeviceCreateRequestCallHandler.APPLICATION,
            MasterUtilitiesDeviceCreateRequestCustomPropertySet.class.getSimpleName(),
            MasterUtilitiesDeviceCreateRequestDomainExtension.class.getName()),
    UTILITIES_DEVICE_CREATE_REQUEST(
            UtilitiesDeviceCreateRequestCallHandler.NAME,
            UtilitiesDeviceCreateRequestCallHandler.VERSION,
            UtilitiesDeviceCreateRequestCallHandler.APPLICATION,
            UtilitiesDeviceCreateRequestCustomPropertySet.class.getSimpleName(),
            UtilitiesDeviceCreateRequestDomainExtension.class.getName()),
    MASTER_METER_REGISTER_CHANGE_REQUEST(
            MasterMeterRegisterChangeRequest.NAME,
            MasterMeterRegisterChangeRequest.VERSION,
            MasterMeterRegisterChangeRequest.APPLICATION,
            MasterMeterRegisterChangeRequestCustomPropertySet.class.getSimpleName(),
            MasterMeterRegisterChangeRequestDomainExtension.class.getName()),
    SUB_MASTER_METER_REGISTER_CHANGE_REQUEST(
            SubMasterMeterRegisterChangeRequest.NAME,
            SubMasterMeterRegisterChangeRequest.VERSION,
            SubMasterMeterRegisterChangeRequest.APPLICATION,
            SubMasterMeterRegisterChangeRequestCustomPropertySet.class.getSimpleName(),
            SubMasterMeterRegisterChangeRequestDomainExtension.class.getName()),
    METER_REGISTER_CHANGE_REQUEST(
            MeterRegisterChangeRequest.NAME,
            MeterRegisterChangeRequest.VERSION,
            MeterRegisterChangeRequest.APPLICATION,
            MeterRegisterChangeRequestCustomPropertySet.class.getSimpleName(),
            MeterRegisterChangeRequestDomainExtension.class.getName()),
    MASTER_CONNECTION_STATUS_CHANGE(
            MasterConnectionStatusChangeServiceCallHandler.NAME,
            MasterConnectionStatusChangeServiceCallHandler.VERSION,
            MasterConnectionStatusChangeServiceCallHandler.APPLICATION,
            MasterConnectionStatusChangeCustomPropertySet.class.getSimpleName(),
            MasterConnectionStatusChangeDomainExtension.class.getName())
    ;

    private final String typeName;
    private final String typeVersion;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final String reservedByApplication;
    private final String customPropertySetClass;
    private final String persistenceSupportClass;

    ServiceCallTypes(String typeName, String typeVersion, String application, String customPropertySetClass, String persistenceSupportClass) {
        this.typeName = typeName;
        this.typeVersion = typeVersion;
        this.reservedByApplication = application;
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
        return Optional.ofNullable(reservedByApplication);
    }

    public String getCustomPropertySetClass() {
        return customPropertySetClass;
    }

    public String getPersistenceSupportClass() {
        return persistenceSupportClass;
    }
}