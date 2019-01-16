/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.importers.impl.attributes.connection.ConnectionAttributesImportFactory;
import com.energyict.mdc.device.data.importers.impl.attributes.protocoldialects.ProtocolDialectAttributesImportFactory;
import com.energyict.mdc.device.data.importers.impl.attributes.protocols.ProtocolAttributesImportFactory;
import com.energyict.mdc.device.data.importers.impl.attributes.security.SecurityAttributesImportFactory;
import com.energyict.mdc.device.data.importers.impl.customattributes.CustomAttributesImportFactory;
import com.energyict.mdc.device.data.importers.impl.certificatesimport.DeviceCertificatesImporterFactory;
import com.energyict.mdc.device.data.importers.impl.devices.activation.DeviceActivationDeactivationImportFactory;
import com.energyict.mdc.device.data.importers.impl.devices.commission.DeviceCommissioningImportFactory;
import com.energyict.mdc.device.data.importers.impl.devices.decommission.DeviceDecommissioningImportFactory;
import com.energyict.mdc.device.data.importers.impl.devices.installation.DeviceInstallationImporterFactory;
import com.energyict.mdc.device.data.importers.impl.devices.remove.DeviceRemoveImportFactory;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.DeviceShipmentImporterFactory;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureDeviceKeyImporterFactory;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureDeviceKeyImporterMessageHandler;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureDeviceShipmentImporterFactory;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureDeviceShipmentImporterMessageHandler;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureHSMDeviceShipmentImporterFactory;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureHSMDeviceShipmentImporterMessageHandler;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImporterFactory;

public enum TranslationKeys implements TranslationKey {

    DATA_IMPORTER_SUBSCRIBER(DeviceDataImporterMessageHandler.SUBSCRIBER_NAME, "Handle data import"),
    SECURE_SHIPMENT_IMPORT_SUBSCRIBER(SecureDeviceShipmentImporterMessageHandler.SUBSCRIBER_NAME, "Handle secure shipment import"),
    SECURE_SHIPMENT_KEY_SUBSCRIBER(SecureDeviceKeyImporterMessageHandler.SUBSCRIBER_NAME, "Handle secure key import"),
    SECURE_HSM_SHIPMENT_IMPORT_SUBSCRIBER(SecureHSMDeviceShipmentImporterMessageHandler.SUBSCRIBER_NAME, "Handle hsm secure shipment import"),

    IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED("ImportResultNoDevicesWereProcessed", "Failed to complete, no devices have been processed."),
    IMPORT_RESULT_FAIL("ImportResultFail", "Failed to complete. {0} devices processed successfully."),
    IMPORT_RESULT_FAIL_WITH_ERRORS("ImportResultFailWithErrors", "Failed to complete. {0} devices processed successfully, {1} devices skipped due to errors."),
    IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS("ImportResultFailWithWarnAndErrors", "Failed to complete. {0} devices processed successfully of which {1} devices contain a note, {2} devices skipped due to errors."),
    IMPORT_RESULT_FAIL_WITH_WARN("ImportResultFailWithWarn", "Failed to complete. {0} devices processed successfully of which {1} devices contain a note."),
    IMPORT_RESULT_SUCCESS("ImportResultSuccess", "Finished successfully. {0} device(s) processed successfully."),
    IMPORT_RESULT_SUCCESS_WITH_ERRORS("ImportResultSuccessWithErrors", "Finished successfully with (some) failures. {0} devices processed successfully, {1} devices skipped due to errors."),
    IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS("ImportResultSuccessWithWarnAndErrors", "Finished successfully with (some) failures and notes. {0} devices processed successfully of which {1} devices contain a note, {2} devices skipped due to errors."),
    IMPORT_RESULT_SUCCESS_WITH_WARN("ImportResultSuccessWithWarn", "Finished successfully with (some) note. {0} devices processed successfully of which {1} devices contain a note."),
    IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE("ImportDefaultProcessorErrorPrefix", "Can''t process line {0}: The device {1} can''t be processed: {2}"),
    IMPORT_MISSING_MANDATORY_PROCESSOR_ERROR_TEMPLATE("ImportMissingMandatoryProcessorErrorPrefix", "Can''t process line {0}: The device can''t be processed: property ''{1}'': {2}"),
    IMPORT_MISSING_MANDATORY_PROCESSOR_ERROR_PROPERTY_TEMPLATE("ImportMissingMandatoryProcessorErrorPrefixProperty", "Can''t process line {0}: The device can''t be processed: property ''{1}'': ''{2}'': {3}"),

    IMPORT_ZIP_PROCESSOR_ERROR_TEMPLATE("ImportZipProcessorErrorPrefix", "Can''t process file {0}: The entry {1} can''t be processed: {2}"),

    READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED("ReadingsImportResultNoReadingsWereProcessed", "Failed to complete, no readings have been processed."),
    READINGS_IMPORT_RESULT_FAIL("ReadingsImportResultFail", "Failed to complete. {0} readings of {1} devices processed successfully."),
    READINGS_IMPORT_RESULT_FAIL_WITH_ERRORS("ReadingsImportResultFailWithErrors", "Failed to complete. {0} readings of {1} devices processed successfully, {2} readings of {3} devices skipped due to errors."),
    READINGS_IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS("ReadingsImportResultFailWithWarnAndErrors", "Failed to complete. {0} readings of {1} devices processed successfully of which {2} readings contain a note, {3} readings of {4} devices skipped due to errors."),
    READINGS_IMPORT_RESULT_FAIL_WITH_WARN("ReadingsImportResultFailWithWarn", "Failed to complete. {0} readings of {1} devices processed successfully of which {2} readings contain a note."),
    READINGS_IMPORT_RESULT_SUCCESS("ReadingsImportResultSuccess", "Finished successfully. {0} readings of {1} device(s) processed successfully."),
    READINGS_IMPORT_RESULT_SUCCESS_WITH_ERRORS("ReadingsImportResultSuccessWithErrors", "Finished successfully with (some) failures. {0} readings of {1} devices processed successfully, {2} readings of {3} devices skipped due to errors."),
    READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS("ReadingsImportResultSuccessWithWarnAndErrors", "Finished successfully with (some) failures and notes. {0} readings of {1} devices processed successfully of which {2} readings contain a note, {3} readings of {4} devices skipped due to errors."),
    READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN("ReadingsImportResultSuccessWithWarn", "Finished successfully with (some) notes. {0} readings of {1} devices processed successfully of which {2} readings contain a note."),

    CERTIFICATES_IMPORT_RESULT_NO_CERTIFICATES_WERE_PROCESSED("CertificatesImportImportResultNoCertificatesWereProcessed", "Failed to complete, no certificates have been processed."),
    CERTIFICATES_IMPORT_RESULT_FAIL("CertificatesImportResultFail", "Failed to complete. {0} certificates of {1} devices processed successfully."),
    CERTIFICATES_IMPORT_RESULT_FAIL_WITH_ERRORS("CertificatesImportResultFailWithErrors", "Failed to complete. {0} certificicates of {1} devices processed successfully, {2} certificicates of {3} devices skipped due to errors."),
    CERTIFICATES_IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS("CertificatesImportResultFailWithWarnAndErrors", "Failed to complete. {0} certificates of {1} devices processed successfully of which {2} certificates contain a note, {3} certificates of {4} devices skipped due to errors."),
    CERTIFICATES_IMPORT_RESULT_FAIL_WITH_WARN("CertificatesImportResultFailWithWarn", "Failed to complete. {0} certificates of {1} devices processed successfully of which {2} certificates contain a note."),
    CERTIFICATES_IMPORT_RESULT_SUCCESS("CertificatesImportResultSuccess", "Finished successfully. {0} certificates of {1} device(s) processed successfully."),
    CERTIFICATES_IMPORT_RESULT_SUCCESS_WITH_ERRORS("CertificatesImportResultSuccessWithErrors", "Finished successfully with (some) failures. {0} certificates of {1} devices processed successfully, {2} certificates of {3} devices skipped due to errors."),
    CERTIFICATES_IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS("CertificatesImportResultSuccessWithWarnAndErrors", "Finished successfully with (some) failures and notes. {0} certificates of {1} devices processed successfully of which {2} certificates contain a note, {3} certificates of {4} devices skipped due to errors."),
    CERTIFICATES_IMPORT_RESULT_SUCCESS_WITH_WARN("CertificatesImportResultSuccessWithWarn", "Finished successfully with (some) notes. {0} certificates of {1} devices processed successfully of which {2} certificates contain a note."),
    CERTIFICATE_NO_SUCH_KEY_ACCESSOR_TYPE( "CertificateNoSuchKeyAccessorType", "Can''t process certificate {0}. The security key that starts with X is not available."),


    // Properties translations
    DEVICE_DATA_IMPORTER_DELIMITER("delimiter", "Delimiter"),
    DEVICE_DATA_IMPORTER_DELIMITER_DESCRIPTION("delimiter.description", "The character that delimits the values for the different properties to import"),
    DEVICE_DATA_IMPORTER_DATE_FORMAT("dateFormat", "Date format"),
    DEVICE_DATA_IMPORTER_DATE_FORMAT_DESCRIPTION("dateFormat.description", "The format that is used for date properties"),
    DEVICE_DATA_IMPORTER_TIMEZONE("timeZone", "Time zone"),
    DEVICE_DATA_IMPORTER_TIMEZONE_DESCRIPTION("timeZone.description", "The unique identifier of the Timezone in which date properties are specified"),
    DEVICE_DATA_IMPORTER_NUMBER_FORMAT("numberFormat", "Number format"),
    DEVICE_DATA_IMPORTER_NUMBER_FORMAT_DESCRIPTION("numberFormat.description", "The format that is used for numerical properties"),
    DEVICE_DATA_IMPORTER_TRUSTSTORE("truststore", "Trust store"),
    DEVICE_DATA_IMPORTER_TRUSTSTORE_DESCRIPTION("truststore.description", "The trust store used to build a chain of trust for the certificate in the shipment file"),
    DEVICE_DATA_IMPORTER_PUBLICKEY("publickey", "Public key"),
    DEVICE_DATA_IMPORTER_PUBLICKEY_DESCRIPTION("publickey.description", "The public key to be used to verify the shipment file's signature. Only to be filled in if the shipment file signature does not contain a certificate"),

    DEVICE_CERTIFICATES_IMPORTER_PUBLIC_KEY("publicKey","Public Key"),
    DEVICE_CERTIFICATES_IMPORTER_PUBLIC_KEY_DESCRIPTION("publicKey.description","The public key needed to access the certificates"),

    MASTER_WILL_BE_OVERRIDDEN("MasterWillBeOverridden", "Note for line {0}: Master device (name: {1}) was overridden by new one (name: {2})"),
    NEW_USAGE_POINT_WILL_BE_CREATED("NewUsagePointWillBeCreated", "Note for line {0}: Usage point {1} is not found. New usage point was created based on Service Category value"),

    //Translations for importer names
    DEVICE_READINGS_IMPORTER(DeviceReadingsImporterFactory.NAME, "Device readings importer [STD]"),
    DEVICE_SHIPMENT_IMPORTER(DeviceShipmentImporterFactory.NAME, "Devices shipment importer [STD]"),
    SECURE_DEVICE_SHIPMENT_IMPORTER(SecureDeviceShipmentImporterFactory.NAME, "Secure device shipment importer [STD]"),
    SECURE_HSM_DEVICE_SHIPMENT_IMPORTER(SecureHSMDeviceShipmentImporterFactory.NAME, "Secure HSM device shipment importer [STD]"),
    SECURE_DEVICE_KEY_IMPORTER(SecureDeviceKeyImporterFactory.NAME, "Secured device key importer [STD]"),
    DEVICE_INSTALLATION_IMPORTER(DeviceInstallationImporterFactory.NAME, "Devices installation importer [STD]"),
    DEVICE_COMMISSIONING_IMPORTER(DeviceCommissioningImportFactory.NAME, "Devices commissioning  importer [STD]"),
    DEVICE_DECOMMISSIONING_IMPORTER(DeviceDecommissioningImportFactory.NAME, "Devices decommissioning  importer [STD]"),
    DEVICE_ACTIVATION_DEACTIVATION_IMPORTER(DeviceActivationDeactivationImportFactory.NAME, "Devices activation / deactivation importer [STD]"),
    DEVICE_REMOVE_IMPORTER(DeviceRemoveImportFactory.NAME, "Devices remove importer [STD]"),
    DEVICE_CONNECTION_ATTRIBUTES_IMPORTER(ConnectionAttributesImportFactory.NAME, "Device connection attributes importer [STD]"),
    DEVICE_SECURITY_ATTRIBUTES_IMPORTER(SecurityAttributesImportFactory.NAME, "Device security attributes importer [STD]"),
    DEVICE_COUSTOM_ATTRIBUTES_IMPORTER(CustomAttributesImportFactory.NAME, "Device custom attributes importer [STD]"),
    DEVICE_CERTIFICATES_IMPORTER(DeviceCertificatesImporterFactory.NAME, "Certifcate importer [STD]"),
    DEVICE_PROTOCOL_ATTRIBUTES_IMPORTER(ProtocolAttributesImportFactory.NAME, "Device protocol attributes importer [STD]"),
    DEVICE_PROTOCOL_DIALECT_ATTRIBUTES_IMPORTER(ProtocolDialectAttributesImportFactory.NAME, "Device protocol dialect attributes importer [STD]"),

    STRING_FORMAT("StringFormat", "string"),
    NUMBER_FORMAT("NumberFormat", "number"),
    HEX_STRING_FORMAT("HexStringFormat", "hex string"),
    BOOLEAN_FORMAT("BooleanFormat", "boolean: 0 or 1"),
    INTEGER_FORMAT("IntegerFormat", "integer"),
    OBIS_CODE_FORMAT("ObisCodeFormat", "obis code");


    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}