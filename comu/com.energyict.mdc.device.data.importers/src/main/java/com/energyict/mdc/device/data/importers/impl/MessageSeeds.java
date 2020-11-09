/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    DATE_FORMAT_IS_NOT_VALID(1, "DateFormatIsNotValid", "Invalid date format", Level.SEVERE),
    TIME_ZONE_IS_NOT_VALID(2, "TimeZoneIsNotValid", "Invalid time zone", Level.SEVERE),
    NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER(3, "NumberFormatIncompatibleWithDelimiter", "Number format is incompatible with delimiter", Level.SEVERE),

    MISSING_TITLE_ERROR(4, "TitleMissingError", "File format error: wrong number of title columns in the first line. Importer service expects {0} but was {1}.", Level.SEVERE),
    FILE_FORMAT_ERROR(5, "FileFormatError", "File format error: wrong number of columns in the line {0}. Importer service expects {1} but was {2}.", Level.SEVERE),
    LINE_MISSING_VALUE_ERROR(6, "LineMissingValueError", "Format error for line {0}: missing value for column ''{1}''.", Level.SEVERE),
    LINE_FORMAT_ERROR(7, "LineFormatError", "Format error for line {0}: wrong value format for column ''{1}'' (expected format = ''{2}'')", Level.SEVERE),
    WRONG_LINE_SIZE(8, "HeaderSizeDoesNotMatchLineSize", "Format error for line {0}: header size doesn''t match line size.", Level.SEVERE),

    NO_DEVICE_TYPE(101, "ImportProcessorNoDeviceType", "Can''t process line {0}: No device type found with name: {1}.", Level.WARNING),
    NO_DEVICE_CONFIGURATION(102, "ImportProcessorNoDeviceConfiguration", "Can''t process line {0}: No device configuration found with name: {1}.", Level.WARNING),
    NO_DEVICE(103, "ImportProcessorNoDevice", "Can''t process line {0}: No device found: {1}.", Level.WARNING),
    NO_MASTER_DEVICE(104, "ImportProcessorNoMasterDevice", "Can''t process line {0}: Master device {1} was not found.", Level.WARNING),
    DEVICE_CAN_NOT_BE_MASTER(105, "ImportProcessorDeviceCanNotBeMaster", "Can''t process line {0}: Master device {0} is not configured to act as master device", Level.WARNING),
    NO_USAGE_POINT(106, "ImportProcessorNoUsagePoint", "Can''t process line {0}: Usage point {1} is not found. " +
            "New usage point can''t be created because of incorrect value of Service category. The list of available Service categories: {2}", Level.WARNING),
    DEVICE_ALREADY_EXISTS(107, "ImportProcessorDeviceAlreadyExists", "Can''t process line {0}: The device (name: {1}) is already in use", Level.WARNING),
    DEVICE_ALREADY_IN_THAT_STATE(108, "ImportProcessorDeviceAlreadyInThatState", "Can''t process line {0}: The device is already in {1} state", Level.WARNING),
    DEVICE_CAN_NOT_BE_MOVED_TO_STATE(109, "ImportProcessorDeviceCanNotBeMovedToState", "Can''t process line {0}: The device can''t be moved from state {2} to {1}", Level.WARNING),
    PRE_TRANSITION_CHECKS_FAILED(110, "ImportProcessorPreTransitionsChecksFailed", "Can''t process line {0}: Pre-transition check(s) failed: {1}", Level.WARNING),
    TRANSITION_ACTION_DATE_IS_INCORRECT(111, "ImportProcessorTransitionActionDateIsIncorrect", "Can''t process line {0}: The transition action date has incorrect value: {1}, {2}", Level.WARNING),
    DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER(112, "ImportProcessorDeviceCanNotBeMovedToStateByImporter", "Can''t process line {0}: The device can''t be moved from state {2} to {1}. Appropriate initial state(s) is(are) {3}", Level.WARNING),
    INCORRECT_MULTIPLIER_VALUE(113, "ImportProcessorInvalidMultiplierValue", "Can''t process line {0}: The value of the multiplier is not valid {1}", Level.WARNING),
    USELESS_MULTIPLIER_CONFIGURED(114, "ImportProcessorMultiplierConfiguredButNotUsed", "There is a multiplier configured on line {0} with value {1}, but the action 'set multiplier' is not defined on the lifecycle transition", Level.WARNING),

    READING_DATE_BEFORE_METER_ACTIVATION(201, "ReadingDateBeforeMeterActivation", "Note for line {0}: Reading date: {1} is before the first meter activation start date and will not be stored.", Level.WARNING),
    READING_DATE_AFTER_METER_ACTIVATION(202, "ReadingDateAfterMeterActivation", "Note for line {0}: Reading date: {1} is after the last meter activation end date and will not be stored.", Level.WARNING),
    NO_SUCH_READING_TYPE(203, "NoSuchReadingType", "Can''t process line {0}: Reading type {1} doesn''t exist", Level.WARNING),
    NOT_SUPPORTED_READING_TYPE(204, "NotSupportedReadingType", "Can''t process line {0}: Reading type {1} is not supported. Supported reading types: registers and channels with integration time >= day.", Level.WARNING),
    DEVICE_DOES_NOT_SUPPORT_READING_TYPE(205, "DeviceDoesNotSupportReadingType", "Can''t process line {0}: Reading type {1} is not available for device {2}", Level.WARNING),
    READING_VALUE_DOES_NOT_MATCH_REGISTER_CONFIG_OVERFLOW(206, "ReadingValueDoesNotMatchRegisterConfigOverflow", "Can''t process line {0}: Reading value for reading type {1} of device {2} doesn''t match with register configuration settings (overflow)", Level.WARNING),
    READING_VALUE_DOES_NOT_MATCH_CHANNEL_CONFIG_OVERFLOW(207, "ReadingValueDoesNotMatchChannelConfigOverflow", "Can''t process line {0}: Reading value for reading type {1} of device {2} doesn''t match with channel configuration settings (overflow)", Level.WARNING),
    READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG(208, "ReadingValueWasTruncatedToRegisterConfig", "Note for line {0}: Reading value was truncated to {1} according to register configuration.", Level.INFO),
    READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG(209, "ReadingValueWasTruncatedToChannelConfig", "Note for line {0}: Reading value was truncated to {1} according to channel configuration.", Level.INFO),
    READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE(210, "ReadingImportIsNotAllowedForDecommissionedDevices", "Can''t process line {0}: Import service doesn''t have privileges to import readings for device {1} since it is in Decommissioned state.", Level.WARNING),
    READING_IMPORT_NOT_ALLOWED_FOR_IN_STOCK_DEVICE(211, "ReadingImportIsNotAllowedForInStockDevices", "Note for line {0}: Reading can''t be imported for device {1} since this device is in In Stock state and was not installed or commissioned yet.", Level.WARNING),
    READING_DATE_INCORRECT_FOR_DAILY_CHANNEL(212, "ReadingDateIncorrectForDailyChannel", "Can''t process line {0}: Reading date is incorrect for reading type {1}. Time of reading date of daily reading must be midnight in device timezone ({2}).", Level.WARNING),
    READING_DATE_INCORRECT_FOR_MONTHLY_CHANNEL(213, "ReadingDateIncorrectForMonthlyChannel", "Can''t process line {0}: Reading date is incorrect for reading type {1}. Reading date of monthly reading must be the 1st day of the month and time of the day must be midnight in device timezone ({2}).", Level.WARNING),
    READING_DATE_INCORRECT_FOR_YEARLY_CHANNEL(214, "ReadingDateIncorrectForYearlyChannel", "Can''t process line {0}: Reading date is incorrect for reading type {1}. Reading date of yearly reading must be the 1st day of the year and time of the day must be midnight in device timezone ({2}).", Level.WARNING),
    READING_DATE_INCORRECT_FOR_MINUTES_CHANNEL(215, "ReadingDateIncorrectForMinutesChannel", "Can''t process line {0}: Reading date is incorrect for reading type {1}. Reading date of minutes reading must be divisible by {2} ({3}).", Level.WARNING),
    READING_TYPE_DUPLICATED_OBIS_CODE(216, "ReadingTypeDuplicatedObisCode", "Can''t process line {0}: Channel or register obis code {1} is duplicated on device {2}.", Level.WARNING),
    INVALID_DEVICE_LOADPROFILE_OBIS_CODE(217, "InvalidLoadProfileObisCode", "Can''t process line {0}: Invalid load profile obis code {1} on device {2}.", Level.WARNING),   //lori
    INVALID_DEVICE_LOGBOOK_OBIS_CODE(218, "InvalidLogBookObisCode", "Can''t process line {0}: Invalid logbook obis code {1} on device {2}.", Level.WARNING),
    READING_DATE_BEFORE_EVENT_DATE(219, "ReadingDateBeforeEventDate", "Note for line {0}: Reading date: {1}, is before the event creation date: {2}, on device with name: {3}, and imported line record will not be stored.", Level.WARNING),

    NO_CONNECTION_METHOD_ON_DEVICE(301, "NoSuchConnectionMethodOnDevice", "Can''t process line {0}: Connection method {1} is not supported on the device.", Level.WARNING),
    CONNECTION_METHOD_IS_NOT_UNIQUE_IN_FILE(302, "ConnectionMethodIsNotUniqueInFile", "Failure in line {0}: Connection method name is not unique in the file.", Level.SEVERE),
    REQUIRED_CONNECTION_ATTRIBUTES_MISSED(303, "RequiredConnectionAttributesMissed", "Note for line {0}: Next required attribute(s) is(are) missed: {1}. The state of the connection method is \"Incomplete\".", Level.INFO),
    CONNECTION_METHOD_NOT_CREATED(304, "ConnectionMethodNotCreated", "Can''t process line {0}: Connection method {1} couldn''t be created on the device {2}. Reasons: {3}", Level.WARNING),
    CONNECTION_ATTRIBUTE_INVALID_VALUE(305, "ConnectionAttributeInvalidValue", "Can''t process line {0}: Connection attribute value ''{1}'' is invalid for attribute ''{2}''", Level.WARNING),
    UNKNOWN_CONNECTION_ATTRIBUTE(306, "UnknownConnectionAttribute", "Note for file: Connection method {0} doesn''t have next connection attribute(s): {1}", Level.INFO),

    NO_SECURITY_SETTINGS_ON_DEVICE(401, "NoSuchSecuritySettingsOnDevice", "Can''t process line {0}: Security settings with name {1} is not available on the device.", Level.WARNING),
    SECURITY_ATTRIBUTES_NOT_SET(402, "SecurityAttributesNotCreated", "Can''t process line {0}: Security attributes are not set on the device {1}.", Level.WARNING),
    SECURITY_SETTINGS_NAME_IS_NOT_UNIQUE_IN_FILE(403, "SecuritySettingsNameIsNotUniqueInFile", "Failure in line {0}: Security settings name is not unique in the file.", Level.WARNING),
    REQUIRED_SECURITY_ATTRIBUTES_MISSED(404, "RequiredSecurityAttributesMissed", "Note for line {0}: Next required attribute(s) is(are) missed: {1}. The state of the security settings is \"Incomplete\".", Level.INFO),
    SECURITY_ATTRIBUTE_INVALID_VALUE(405, "SecurityAttributeInvalidValue", "Can''t process line {0}: Security attribute value ''{1}'' is invalid for attribute ''{2}''", Level.WARNING),
    INCORRECT_LOCATION_FORMAT(406, "IncorrectLocationFormat", "Incorrect location format. Expected : {0}", Level.SEVERE),
    LINE_MISSING_LOCATION_VALUE(407, "LineMissingLocationValue", "Can''t process line {0}: missing value for field ''{1}''.", Level.SEVERE),

    USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE(501, "UsagePointAlreadyLinkedToAnotherDeviceX", "Can''t process line {0}: Usage point {1} is already linked to device {2} starting from {3}", Level.WARNING),
    USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE_UNTIL(502, "UsagePointAlreadyLinkedToAnotherDeviceXUntil", "Can''t process line {0}: Usage point {1} is already linked to device {2} from {3} until {4}", Level.WARNING),
    UNSATISFIED_READING_TYPE_REQUIREMENTS_OF_USAGE_POINT(503, "DeviceDoesNotProvideRequiredReadingTypes", "Can''t process line {0}: Device {1} doesn''t have the following reading types that are specified in the metrology configurations of selected usage point {2}: {3}", Level.WARNING),
    PROCESS_SQL_EXCEPTION(504, "ProcessSqlException", "Can''t process line {0}.  There was a problem accessing the database", Level.SEVERE),
    NO_SUCH_KEY_ACCESSOR_TYPE(505, "NoSuchKeyAccessorType", "Can''t process line {0}.  The device type does not have a security accessor with name {1}", Level.SEVERE),
    UNKNOWN_KEY_WRAPPER(506, "UnknownKeyWrapperType", "Can''t process line {0}.  The importer doesn''t know how to handle values of this type, only plaintext keys are supported", Level.SEVERE),
    NO_VALUE_FOR_SECURITY_PROPERTY(507, "NoValueForSecurityProperty", "Can''t process line {0}.  No value was defined for property {1} so the importer doesn''t know where to store the value", Level.SEVERE),
    CAN_NOT_BE_SPACE_OR_EMPTY(508, "cannnotBeSpaceOrEmpty", "The delimiter cannot be empty or 'space'", Level.SEVERE),
    SCHEMA_FAILED(509, "SchemaFailedException", "XSD schema for secure device import couldn''t be read.", Level.SEVERE),
    VALIDATION_OF_FILE_FAILED(510, "XmlValidationfailed", "Validation failed, please check your file for invalid content.", Level.SEVERE),
    VALIDATION_OF_FILE_FAILED_WITH_DETAIL(511, "XmlValidationFailedDetailed", "Validation failed: \"{0}\"", Level.SEVERE),
    SECURE_DEVICE_IMPORT_FAILED(512, "SecureImportFailed", "Secure device importer failed: {0}", Level.SEVERE),
    FAILED_TO_CREATE_CERTIFICATE_FACTORY(513, "FailedToCreateCertificateFactory", "Failed to create the certificate factory: {0}", Level.SEVERE),
    NO_CERTIFICATE_FOUND_IN_SHIPMENT(514, "NoCertificateFoundInShipment", "Shipment file does not seem to contain certificate used to validate signature", Level.SEVERE),
    FAILED_TO_CREATE_CERTIFICATE(515, "FailedToCreateCertificate", "Failed to extract certificate from shipment file: {0}", Level.FINE),
    SHIPMENT_CERTIFICATE_UNTRUSTED(516, "ShipmentCertificateUntrusted", "Can''t process file: the certificate in the shipment file couldn''t be trusted: {0}", Level.SEVERE),
    FAILED_TO_VERIFY_CERTIFICATE(517, "FailedToValidateCertificate", "Failed to validate certificate", Level.SEVERE),
    FAILED_TO_CREATE_PUBLIC_KEY(518, "FailedToExtractPublicKeyFromShipment", "Can''t process file: failed to extract the public key from the shipment file", Level.SEVERE),
    INVALID_SIGNATURE(519, "InvalidSignatureOnShipment", "Can''t process file: the signature on the shipment file isn''t valid", Level.SEVERE),
    SIGNATURE_VALIDATION_FAILED(520, "FailedToValidateSignatureOnShipment", "Can''t process file: the signature on the shipment file couldn''t be validated", Level.SEVERE),
    SIGNATURE_OF_THE_SHIPMENT_FILE_VERIFIED_SUCCESSFULLY(521, "ShipmentSignatureValid", "Signature of the shipment file verified successfully", Level.INFO),
    NO_PUBLIC_KEY_FOUND_FOR_SIGNATURE_VALIDATION(522, "NoKeyInShipment", "Can''t process file: the signature on the shipment file couldn''t be validated: public key couldn''t be found in the shipment file", Level.SEVERE),
    NO_SUCH_DEVICE_TYPE(523, "DeviceTypeNotFound", "Can''t process file: the device type ''{0}'' required by the importer couldn''t be found", Level.SEVERE),
    NO_DEFAULT_DEVICE_CONFIG_FOUND(524, "NoDefaultDeviceConfigOnDeviceType", "Can''t process file: no default device configuration could be found for the device type", Level.SEVERE),
    DEFAULT_DEVICE_CONFIG_FOUND_NOT_ACTIVE(525, "DefaultConfigNotActive", "Can''t process file: the default device configuration is not active", Level.SEVERE),
    DEVICE_WITH_NAME_ALREADY_EXISTS(526, "NameAlreadyExists", "A device with name ''{0}'' already exists, skipped by importer", Level.WARNING),
    IMPORT_FAILED_FOR_DEVICE(527, "ImportFailedForDevice", "Failed to import device with name ''{0}'': {1}", Level.SEVERE),
    IMPORTING_DEVICE(528, "ImportingDevice", "Now importing device ''{0}''", Level.INFO),
    FAILED_TO_STORE_MAC_ADDRESS(529, "FailedToStoreMacAddress", "MAC Address value couldn''t be stored on device ''{0}''", Level.WARNING),
    WRAP_KEY_NOT_FOUND(530, "NoSuchWrapKey", "Can''t process device ''{1}'': Failed to import secret for security accessor ''{0}'': referenced wrap key ''{2}'' is not found in the shipment file", Level.SEVERE),
    NO_SUCH_KEY_ACCESSOR_TYPE_ON_DEVICE_TYPE(531, "NoSuchKeyAccessorTypeOnDeviceType", "Can''t process device ''{0}'': Security accessor ''{1}'' is not available on the device type.", Level.SEVERE),
    INITIALIZATION_VECTOR_ERROR(532, "NoIV", "IV not found in encrypted key", Level.SEVERE),
    IMPORTED_DEVICE(533, "ImportedDevice", "Device ''{0}'' imported successfully", Level.INFO),
    ACTUAL_VALUE_ALREADY_EXISTS(534, "ActualValueAlreadyExists", "Can''t process device ''{1}'': security accessor ''{0}'' already as an 'active' value.", Level.WARNING),
    IMPORT_COMPLETED(535, "ImportSuccesful", "The device import completed, a total of {0} devices have been created", Level.INFO),
    NO_SUCH_ALGORITHM(536, "NoSuchAlgorithm", "Algorithm not found: {0}", Level.SEVERE),
    ILLEGAL_KEY(537, "NoAValidPublicKey", "The key is not a valid public key: {0}", Level.SEVERE),
    DEVICE_WITH_NAME_DOES_NOT_EXIST(538, "NoDeviceWithName", "No device with name ''{0}'' could be found, skipped by importer", Level.WARNING),
    BOTH_VALUES_ALREADY_EXIST(539, "ActualAndPassiveAlreadyExist", "Can''t process device ''{1}'': security accessor ''{0}'' already has both 'active' and 'passive' values, skipped.", Level.WARNING),
    COULD_NOT_EXTRACT_SECURITY_ACCESSOR_TYPE(540, "CouldNotExtractSecurityAccessorType", "The security accessor type couldn''t be extracted from the certificate {0}", Level.SEVERE),
    COULD_NOT_EXTRACT_SERIAL_NUMBER(541, "CouldNotExtractSerialNumber", "The device serial number couldn''t be extracted from the filename {0}", Level.SEVERE),
    COULD_NOT_EXTRACT_CERTIFICATE_NAME(542, "CouldNotExtractCertificateName", "The certificate name couldn''t be extracted from the filename {0}", Level.SEVERE),
    CERTIFICATE_NO_SUCH_KEY_ACCESSOR_TYPE(543, "CertificateNoSuchKeyAccessorType", "Can''t process certificate {0}. The security accessor that starts with {1} is not available on the device.", Level.WARNING),
    NO_SERIAL_NUMBER(544, "ImportZipProcessorNoMatchingDevice", "Can''t process file {0}, no device found with serial number {1}.", Level.WARNING),
    NO_READINGTYPE_ON_DEVICE(545, "NoReadingtypeOnDeviceX", "Can''t process line {0}: reading type {1} is not found on device {2}", Level.WARNING),
    NO_CUSTOMATTRIBUTE_ON_DEVICE(546, "NoCustomAttributeOnDeviceX", "Can''t process line {0}: can''t find custom attribute set {1} on device {2}", Level.WARNING),
    NO_CUSTOMATTRIBUTE_ON_READINGTYPE_OF_DEVICE(547, "NoCustomAttributeOnReadingTypeOfDeviceX", "Can''t process line {0}: can''t find custom attribute set {1} on reading type {2} of device {3}", Level.WARNING),
    NO_CUSTOMATTRIBUTE_VERSION_ON_DEVICE(548, "NoCustomAttributeVersionOnDeviceX", "Can''t process line {0}:  custom attribute set version with start date {1} is not found on device {2}", Level.WARNING),
    UNRESOLVABLE_CUSTOMATTRIBUTE_CONFLICT(549, "UnresolvableCustomAttributesConflict", "Can''t process line {0}:  conflict of versions is found for custom attribute set {1} but conflicts resolution is switched off on device {2}", Level.WARNING),
    MISSING_REQUIRED_CUSTOMATTRIBUTE_VALUE_ON_DEVICE(550, "MissingRequiredCustomAttributeValueOnDeviceX", "Can''t process line {0}: attribute {1} is required on custom attribute set {2} on device {3}", Level.WARNING),
    INVALID_RANGE(551, "InvalidRange", "Can''t process line {0}: start time exceeds end time of custom attribute set {1} on device {2}", Level.WARNING),
    WRONG_QUANTITY_FORMAT(552, "WrongQuantitiyFormat", "Can''t process line {0}: wrong unit format for column {1}. Supported multipliers: {2}, supported units: {3}", Level.WARNING),
    WRONG_ENUM_FORMAT(553, "WrongEnumFormat", "Can''t process line {0}: wrong enumeration value for column {1}. Possible values: {2}", Level.WARNING),
    INVALID_CUSTIMATTRIBUTE_HEADER(554, "InvalidCustomAttributeFileHeader", "File should contain at least 3 columns separated by ''{0}''. Please check the delimiter.", Level.WARNING),
    NO_ENDTIME_SPECIFIED(555, "NoEndtimeSpecified", "Can''t process line {0}: end time is not specified for new version of custom attribute set {1} on device {2}", Level.WARNING),
    NO_STARTTIME_SPECIFIED(556, "NoStarttimeSpecified", "Can''t process line {0}: start time is not specified for new version of custom attribute set {1} on device {2}", Level.WARNING),
    IMPORT_FAILED_FOR_DEVICE_KEY(557, "ImportFailedForDeviceKey", "Failed to import device with name {0}, id {1}. Failed at key name {2}", Level.SEVERE),
    IMPORT_FAILED_NO_TRANSPORT_KEYS_DEFINED(558, "ImportFailedNoTransportKeysDefined", "Invalid file, no transport keys defined", Level.SEVERE),
    UNKNOWN_WRAPPER_KEY(559, "UnknownWrapperKey", "Could not find wrapper key define in file with name: {0}", Level.SEVERE),
    INVALID_WRAPPER_KEY(560, "InvalidWrapperKey", "Could not find wrapper key bytes define in file with name: {0}", Level.SEVERE),
    INVALID_TOPOLOGY_IMPORT_RECORD(561, "InvalidTopologyImportRecord", "Can''t process line {0}: at least one of the values should be specified: MasterDevice and/or SlaveDevice.", Level.WARNING),
    MASTER_DEVICE_NOT_FOUND(562, "TopologyImportMasterDeviceNotFound", "Can''t process line {0}: master device {1} hasn''t been found.", Level.WARNING),
    SLAVE_DEVICE_NOT_FOUND(563, "TopologyImportSlaveDeviceNotFound", "Can''t process line {0}: slave device {1} hasn''t been found.", Level.WARNING),
    MASTER_DEVICE_NOT_CONFIGURED(564, "TopologyImportMasterNotConfigured", "Can''t process line {0}: device {1} isn''t configured to act as master device.", Level.WARNING),
    SLAVE_DEVICE_NOT_CONFIGURED(565, "TopologyImportSlaveNotConfigured", "Can''t process line {0}: device {1} isn''t configured to act as slave device.", Level.WARNING),
    SAME_SERIAL_NUMBER(566, "TopologyImportSameSerialNumber", "Can''t process line {0}: multiple devices have the same serial number: {1}.", Level.WARNING),
    SLAVE_DEVICE_LINKED_TO_ANOTHER_MASTER(567, "TopologyImportSlaveLinkedToAnotherMaster", "Can''t process line {0}: reassigning is disabled on the import service and slave device {1} is already linked to master device {2} so it hasn''t been linked to master device {3}.", Level.WARNING),
    SLAVE_DEVICE_SUCCESSFULLY_REASSIGNED(568, "TopologyImportSlaveSuccessfullyReassigned", "Note for line {0}: slave device {1} has been reassigned - unlinked from master device {2} and linked to master device {3}.", Level.INFO),
    SLAVE_SUCCESSFULLY_LINKED(569, "TopologyImportSlaveSuccessfullyLinked", "Note for line {0}: slave device {1} has been successfully linked to a master device {2}.", Level.INFO),
    SLAVE_SUCCESSFULLY_UNLINKED(570, "TopologyImportSlaveSuccessfullyUnlinked", "Note for line {0}: slave device {1} has been unlinked from master device {2}.", Level.INFO),
    LINK_ALREADY_EXISTS(571, "TopologyImportLinkAlreadyExists", "Note for line {0}: no changes applied to slave device {1} since it is already linked to master device {2}.", Level.INFO),
    NO_LINK_EXISTS(572, "TopologyImportNoLinkExists", "Note for line {0}: no changes applied to slave device {1} since it is already unlinked.", Level.INFO),
    TOPOLOGY_SAME_DEVICE(573, "TopologyImportSlaveAndMasterAreSame", "Can''t process line {0}: device {1} can''t be its own gateway.", Level.WARNING),
    UNSUPPORTED_DEVICE_IDENTIFIER(574,"Unsupported device identifier","Unsupported device identifier provided: {0}.", Level.SEVERE),

    ATTRIBUTE_INVALID_VALUE(601, "AttributeInvalidValue", "Can''t process line {0}: Value ''{1}'' is invalid for attribute ''{2}''", Level.WARNING),
    NO_DEVICE_PROTOCOL_DIALECT_ON_DEVICE(602, "NoSuchProtocolDialectOnDevice", "Can''t process line {0}: Protocol dialect ''{1}'' is not supported on the device.", Level.WARNING),
    PROTOCOL_DIALECT_ATTRIBUTE_INVALID_VALUE(603, "ProtocolDialectAttributeInvalidValue", "Can''t process line {0}: Protocol dialect value ''{1}'' is invalid for attribute ''{2}''", Level.WARNING),
    UNKNOWN_PROTOCOL_DIALECT_ATTRIBUTE(604, "UnknownProtocolDialectAttribute", "Note for file: Protocol dialect ''{0}'' doesn''t have following attribute(s): {1}", Level.INFO),
    UNKNOWN_PROTOCOL_ATTRIBUTE(605, "UnknownProtocolAttribute", "Note for file: Device ''{0}'' doesn''t have following attribute(s): {1}", Level.INFO),
    INVALID_JSON(606, "InvalidJson", "Value is not a valid JSON", Level.SEVERE),
    CANNOT_EXTRACT_COMMON_NAME(607, "CannotExtractCommonName", "Cannot extract Common Name (CN) from certificate {0}: {1}", Level.WARNING),
    LINKING_CERTIFICATE(608, "LinkingCertificate", "Linking security accessor [{0}] of device [{1}] with certificate from file [{2}]", Level.FINE),
    CANNOT_FIND_DEVICE_WITH_COMMON_NAME(609, "CannotFindDeviceWithCommonName", "Cannot find any device with Common Name (CN) {0} in parameter {1}, for file {2}", Level.WARNING),
    SKIPPING_ENTRY(610, "SkippingEntry", "Skipping processing of file {0} - as configured", Level.INFO),
    DEVICE_WITH_SERIAL_NUMBER_ALREADY_EXISTS(611, "SerialNumberAlreadyExists", "A device with serial number ''{0}'' already exists, skipped by importer", Level.WARNING),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return DeviceDataImporterMessageHandler.COMPONENT;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }
}
