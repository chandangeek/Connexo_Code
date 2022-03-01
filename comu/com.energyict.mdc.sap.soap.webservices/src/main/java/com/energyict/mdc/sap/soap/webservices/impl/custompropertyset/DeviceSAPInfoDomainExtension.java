/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.DeviceSAPInfo;

import javax.validation.constraints.Size;
import java.util.Optional;

@UniqueDeviceIdentifier(groups = {Save.Create.class, Save.Update.class})
public class DeviceSAPInfoDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device>, DeviceSAPInfo {

    public enum FieldNames {
        DOMAIN("device", "DEVICE"),
        DEVICE_IDENTIFIER("deviceIdentifier", "DEVICE_ID"),
        DEVICE_LOCATION("deviceLocation", "DEVICE_LOCATION"),
        INSTALLATION_NUMBER("installationNumber", "INSTALLATION_NUMBER"),
        POINT_OF_DELIVERY("pointOfDelivery", "POINT_OF_DELIVERY"),
        DIVISION_CATEGORY_CODE("divisionCategoryCode", "DIVISION_CATEGORY_CODE"),
        REGISTERED("registered", "REGISTERED"),
        DEVICE_LOCATION_INFORMATION("deviceLocationInformation", "DEVICE_LOCATION_INFORMATION"),
        MODIFICATION_INFORMATION("modificationInformation", "MODIFICATION_INFORMATION"),
        ACTIVATION_GROUP_AMI_FUNCTIONS("activationGroupAmiFunctions", "ACTIVATION_GROUP_AMI_FUNCTIONS"),
        METER_FUNCTION_GROUP("meterFunctionGroup", "METER_FUNCTION_GROUP"),
        ATTRIBUTE_MESSAGE("attributeMessage", "ATTRIBUTE_MESSAGE"),
        CHARACTERISTICS_ID("characteristicsId", "CHARACTERISTICS_ID"),
        CHARACTERISTICS_VALUE("characteristicsValue", "CHARACTERISTICS_VALUE");


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

    private Reference<Device> device = Reference.empty();
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceIdentifier;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceLocation;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String installationNumber;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String pointOfDelivery;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String divisionCategoryCode;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceLocationInformation;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String modificationInformation;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String activationGroupAmiFunctions;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterFunctionGroup;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String attributeMessage;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String characteristicsId;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String characteristicsValue;

    private boolean registered;

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        setDeviceIdentifier((String) propertyValues.getProperty(FieldNames.DEVICE_IDENTIFIER.javaName()));
        setDeviceLocation((String) propertyValues.getProperty(FieldNames.DEVICE_LOCATION.javaName()));
        setPointOfDelivery((String) propertyValues.getProperty(FieldNames.POINT_OF_DELIVERY.javaName()));
        setRegistered((boolean) Optional.ofNullable(propertyValues.getProperty(FieldNames.REGISTERED.javaName()))
                .orElse(false));
        setInstallationNumber((String) propertyValues.getProperty(FieldNames.INSTALLATION_NUMBER.javaName()));
        setDivisionCategoryCode((String) propertyValues.getProperty(FieldNames.DIVISION_CATEGORY_CODE.javaName()));
        setDeviceLocationInformation((String) propertyValues.getProperty(FieldNames.DEVICE_LOCATION_INFORMATION.javaName()));
        setModificationInformation((String) propertyValues.getProperty(FieldNames.MODIFICATION_INFORMATION.javaName()));
        setActivationGroupAmiFunctions((String) propertyValues.getProperty(FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.javaName()));
        setMeterFunctionGroup((String) propertyValues.getProperty(FieldNames.METER_FUNCTION_GROUP.javaName()));
        setAttributeMessage((String) propertyValues.getProperty(FieldNames.ATTRIBUTE_MESSAGE.javaName()));
        setCharacteristicsId((String) propertyValues.getProperty(FieldNames.CHARACTERISTICS_ID.javaName()));
        setCharacteristicsValue((String) propertyValues.getProperty(FieldNames.CHARACTERISTICS_VALUE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_IDENTIFIER.javaName(), deviceIdentifier);
        propertySetValues.setProperty(FieldNames.DEVICE_LOCATION.javaName(), deviceLocation);
        propertySetValues.setProperty(FieldNames.POINT_OF_DELIVERY.javaName(), pointOfDelivery);
        propertySetValues.setProperty(FieldNames.REGISTERED.javaName(), this.isRegistered());
        propertySetValues.setProperty(FieldNames.INSTALLATION_NUMBER.javaName(), installationNumber);
        propertySetValues.setProperty(FieldNames.DIVISION_CATEGORY_CODE.javaName(), divisionCategoryCode);
        propertySetValues.setProperty(FieldNames.DEVICE_LOCATION_INFORMATION.javaName(), deviceLocationInformation);
        propertySetValues.setProperty(FieldNames.MODIFICATION_INFORMATION.javaName(), modificationInformation);
        propertySetValues.setProperty(FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.javaName(), activationGroupAmiFunctions);
        propertySetValues.setProperty(FieldNames.METER_FUNCTION_GROUP.javaName(), meterFunctionGroup);
        propertySetValues.setProperty(FieldNames.ATTRIBUTE_MESSAGE.javaName(), attributeMessage);
        propertySetValues.setProperty(FieldNames.CHARACTERISTICS_ID.javaName(), characteristicsId);
        propertySetValues.setProperty(FieldNames.CHARACTERISTICS_VALUE.javaName(), characteristicsValue);
    }

    @Override
    public void validateDelete() {
        // for future purposes
    }

    public Optional<String> getDeviceIdentifier() {
        return Optional.ofNullable(deviceIdentifier);
    }

    public String getDeviceLocation() {
        return deviceLocation;
    }

    public String getInstallationNumber() {
        return installationNumber;
    }

    public String getPointOfDelivery() {
        return pointOfDelivery;
    }

    public String getDivisionCategoryCode() {
        return divisionCategoryCode;
    }

    public String getDeviceLocationInformation() {
        return deviceLocationInformation;
    }

    public String getModificationInformation() {
        return modificationInformation;
    }

    @Override
    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public void setDeviceLocation(String deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    @Override
    public void setInstallationNumber(String installationNumber) {
        this.installationNumber = installationNumber;
    }

    @Override
    public void setPointOfDelivery(String pointOfDelivery) {
        this.pointOfDelivery = pointOfDelivery;
    }

    @Override
    public void setDivisionCategoryCode(String divisionCategoryCode) {
        this.divisionCategoryCode = divisionCategoryCode;
    }

    @Override
    public void setDeviceLocationInformation(String deviceLocationInformation) {
        this.deviceLocationInformation = deviceLocationInformation;
    }

    @Override
    public void setModificationInformation(String modificationInformation) {
        this.modificationInformation = modificationInformation;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public Device getDevice() {
        return device.get();
    }

    public String getActivationGroupAmiFunctions() {
        return activationGroupAmiFunctions;
    }

    @Override
    public void setActivationGroupAmiFunctions(String activationGroupAmiFunctions) {
        this.activationGroupAmiFunctions = activationGroupAmiFunctions;
    }

    public String getMeterFunctionGroup() {
        return meterFunctionGroup;
    }

    @Override
    public void setMeterFunctionGroup(String meterFunctionGroup) {
        this.meterFunctionGroup = meterFunctionGroup;
    }

    public String getAttributeMessage() {
        return attributeMessage;
    }

    @Override
    public void setAttributeMessage(String attributeMessage) {
        this.attributeMessage = attributeMessage;
    }

    public String getCharacteristicsId() {
        return characteristicsId;
    }

    @Override
    public void setCharacteristicsId(String characteristicsId) {
        this.characteristicsId = characteristicsId;
    }

    public String getCharacteristicsValue() {
        return characteristicsValue;
    }

    @Override
    public void setCharacteristicsValue(String characteristicsValue) {
        this.characteristicsValue = characteristicsValue;
    }

    @Override
    public CustomPropertySetValues getCustomPropertySetValue() {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        this.copyTo(values);
        return values;
    }
}
