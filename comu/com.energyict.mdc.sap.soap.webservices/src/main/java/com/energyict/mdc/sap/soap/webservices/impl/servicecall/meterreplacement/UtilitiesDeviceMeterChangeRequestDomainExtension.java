/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.text.MessageFormat;
import java.time.Instant;

public class UtilitiesDeviceMeterChangeRequestDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        // general
        DOMAIN("serviceCall", "SERVICE_CALL"),

        // provided
        REQUEST_ID("requestId", "REQUEST_ID"),
        UUID("uuid", "UUID"),
        SHIPMENT_DATE("shipmentDate", "SHIPMENT_DATE"),
        DEACTIVATION_DATE("deactivationDate", "DEACTIVATION_DATE"),
        DEVICE_ID("deviceId", "DEVICE_ID"),
        SERIAL_ID("serialId", "SERIAL_ID"),
        DEVICE_TYPE("deviceType", "DEVICE_TYPE"),
        MATERIAL_ID("materialId", "MATERIAL_ID"),
        MANUFACTURER("manufacturer", "MANUFACTURER"),
        MANUFACTURER_SERIAL_ID("manufacturerSerialId", "MODEL_NUMBER"),
        ACTIVATION_GROUP_AMI_FUNCTIONS("activationGroupAmiFunctions", "ACTIVATION_GROUP_AMI_FUNCTIONS"),
        METER_FUNCTION_GROUP("meterFunctionGroup", "METER_FUNCTION_GROUP"),
        ATTRIBUTE_MESSAGE("attributeMessage", "ATTRIBUTE_MESSAGE"),
        CHARACTERISTICS_ID("characteristicsId", "CHARACTERISTICS_ID"),
        CHARACTERISTICS_VALUE("characteristicsValue", "CHARACTERISTICS_VALUE"),

        //returned
        ERROR_CODE("errorCode", "ERROR_CODE"),
        ERROR_MESSAGE("errorMessage", "ERROR_MESSAGE"),
        ;

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private Reference<ServiceCall> serviceCall = Reference.empty();

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String requestId;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String uuid;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceId;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String serialId;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceType;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String materialId;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String manufacturer;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String manufacturerSerialId;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String activationGroupAmiFunctions;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterFunctionGroup;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String attributeMessage;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String characteristicsId;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String characteristicsValue;

    private Instant shipmentDate;

    private Instant deactivationDate;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorCode;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorMessage;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSerialId() {
        return serialId;
    }

    public void setSerialId(String serialId) {
        this.serialId = serialId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturerSerialId() {
        return manufacturerSerialId;
    }

    public void setManufacturerSerialId(String manufacturerSerialId) {
        this.manufacturerSerialId = manufacturerSerialId;
    }

    public String getActivationGroupAmiFunctions() {
        return activationGroupAmiFunctions;
    }

    public void setActivationGroupAmiFunctions(String activationGroupAmiFunctions) {
        this.activationGroupAmiFunctions = activationGroupAmiFunctions;
    }

    public String getMeterFunctionGroup() {
        return meterFunctionGroup;
    }

    public void setMeterFunctionGroup(String meterFunctionGroup) {
        this.meterFunctionGroup = meterFunctionGroup;
    }

    public String getAttributeMessage() {
        return attributeMessage;
    }

    public void setAttributeMessage(String attributeMessage) {
        this.attributeMessage = attributeMessage;
    }

    public String getCharacteristicsId() {
        return characteristicsId;
    }

    public void setCharacteristicsId(String characteristicsId) {
        this.characteristicsId = characteristicsId;
    }

    public String getCharacteristicsValue() {
        return characteristicsValue;
    }

    public void setCharacteristicsValue(String characteristicsValue) {
        this.characteristicsValue = characteristicsValue;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setError(MessageSeed messageSeed, Object... args) {
        setErrorCode(String.valueOf(messageSeed.getNumber()));
        setErrorMessage(MessageFormat.format(messageSeed.getDefaultFormat(), args));
    }

    public Instant getShipmentDate() {
        return shipmentDate;
    }

    public void setShipmentDate(Instant shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    public Instant getDeactivationDate() {
        return deactivationDate;
    }

    public void setDeactivationDate(Instant deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setRequestId((String) propertyValues.getProperty(FieldNames.REQUEST_ID.javaName()));
        this.setUuid((String) propertyValues.getProperty(FieldNames.UUID.javaName()));
        this.setDeviceId((String) propertyValues.getProperty(FieldNames.DEVICE_ID.javaName()));
        this.setSerialId((String) propertyValues.getProperty(FieldNames.SERIAL_ID.javaName()));
        this.setDeviceType((String) propertyValues.getProperty(FieldNames.DEVICE_TYPE.javaName()));
        this.setMaterialId((String) propertyValues.getProperty(FieldNames.MATERIAL_ID.javaName()));
        this.setManufacturer((String) propertyValues.getProperty(FieldNames.MANUFACTURER.javaName()));
        this.setManufacturerSerialId((String) propertyValues.getProperty(FieldNames.MANUFACTURER_SERIAL_ID.javaName()));
        this.setActivationGroupAmiFunctions((String) propertyValues.getProperty(FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.javaName()));
        this.setMeterFunctionGroup((String) propertyValues.getProperty(FieldNames.METER_FUNCTION_GROUP.javaName()));
        this.setAttributeMessage((String) propertyValues.getProperty(FieldNames.ATTRIBUTE_MESSAGE.javaName()));
        this.setCharacteristicsId((String) propertyValues.getProperty(FieldNames.CHARACTERISTICS_ID.javaName()));
        this.setCharacteristicsValue((String) propertyValues.getProperty(FieldNames.CHARACTERISTICS_VALUE.javaName()));
        this.setErrorCode((String) propertyValues.getProperty(FieldNames.ERROR_CODE.javaName()));
        this.setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
        this.setShipmentDate((Instant) propertyValues.getProperty(FieldNames.SHIPMENT_DATE.javaName()));
        this.setDeactivationDate((Instant) propertyValues.getProperty(FieldNames.DEACTIVATION_DATE.javaName()));


    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.REQUEST_ID.javaName(), this.getRequestId());
        propertySetValues.setProperty(FieldNames.UUID.javaName(), this.getUuid());
        propertySetValues.setProperty(FieldNames.DEVICE_ID.javaName(), this.getDeviceId());
        propertySetValues.setProperty(FieldNames.SERIAL_ID.javaName(), this.getSerialId());
        propertySetValues.setProperty(FieldNames.DEVICE_TYPE.javaName(), this.getDeviceType());
        propertySetValues.setProperty(FieldNames.MATERIAL_ID.javaName(), this.getMaterialId());
        propertySetValues.setProperty(FieldNames.MANUFACTURER.javaName(), this.getManufacturer());
        propertySetValues.setProperty(FieldNames.MANUFACTURER_SERIAL_ID.javaName(), this.getManufacturerSerialId());
        propertySetValues.setProperty(FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.javaName(), activationGroupAmiFunctions);
        propertySetValues.setProperty(FieldNames.METER_FUNCTION_GROUP.javaName(), meterFunctionGroup);
        propertySetValues.setProperty(FieldNames.ATTRIBUTE_MESSAGE.javaName(), attributeMessage);
        propertySetValues.setProperty(FieldNames.CHARACTERISTICS_ID.javaName(), characteristicsId);
        propertySetValues.setProperty(FieldNames.CHARACTERISTICS_VALUE.javaName(), characteristicsValue);
        propertySetValues.setProperty(FieldNames.ERROR_CODE.javaName(), this.getErrorCode());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), this.getErrorMessage());
        propertySetValues.setProperty(FieldNames.SHIPMENT_DATE.javaName(), this.getShipmentDate());
        propertySetValues.setProperty(FieldNames.DEACTIVATION_DATE.javaName(), this.getDeactivationDate());
    }

    @Override
    public void validateDelete() {

    }
}
