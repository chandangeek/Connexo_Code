package com.energyict.mdc.device.data.importers.impl.exceptions;

public class ProcessorException extends ImportException {
    public static enum Type {
        DEVICE_CONFIGURATION_NOT_FOUND("DVI.deviceConfigNotFound"),
        DEVICE_NOT_CREATED("DVI.deviceNotCreated"),
        DEVICE_NOT_FOUND("DVI.deviceNotFound"),
        DEVICE_SERIAL_NUMBER_MISMATCH("DVI.deviceSerialMismatch"),
        DEVICE_TYPE_NOT_FOUND("DVI.deviceTypeNotFound"),
        DEVICE_BATCH_NOT_FOUND("DVI.deviceBatchNotFound"),
        DEVICE_BATCH_MISMATCH("DVI.deviceBatchMismatch"),
        CONNECTION_ATTRIBUTES_NOT_CREATED("DVI.noConnectionAttributesAdded"),
        SECURITY_ATTRIBUTES_NOT_CREATED("DVI.noSecurityAttributesAdded"),
        PROPERTY_NOT_VALID("DVI.propertyNotValid"),
        INVALID_SECURITY_SETTING("DVI.securitySettingNotFoundForDevice"),
        CONNECTIONTYPE_NOT_FOUND("DVI.connectionTypeNotFoundOnDeviceConfiguration"),
        UNPARSEABLE_VALUE("DVI.CanNotParse"),
        SERVICE_CATEGORY_NOT_FOUND("DVI.serviceCategoryNotFound"),
        METER_NOT_FOUND("DVI.meterNotFound"),
        INVALID_SERVICE_CATEGORY("DVI.invalidServiceCategory"),
        ACTIVE_ACTIVATION_WITH_STARTDATE_BEFORE_NEW_STARTDATE("DVI.activeMeterActivationWithStartDateBeforeNewStartDate"),
        DEVICE_USED_BY_ISSUE("DVI.deviceUsedByIssue"),
        DEVICE_IS_IN_GROUP("DVI.deviceIsInGroup"),;

        private Type(String message){
            this.message = message;
        }

        private String message;
    }

    Type type;
    Exception source;

    public ProcessorException(Type type, String expected, String actual) {
        this.type = type;
        this.expected = expected;
        this.actual = actual;
    }

    public ProcessorException(Type type, Exception source, String expected, String actual) {
        this.type = type;
        this.source = source;
        this.expected = expected;
        this.actual = actual;
    }

    public String getMessage() {
        return this.type.message;
    }

    public boolean hasInnerException(){
        return (this.source != null);
    }

    public String getInnerExceptionMessage(){
        return this.source.getMessage();
    }
}