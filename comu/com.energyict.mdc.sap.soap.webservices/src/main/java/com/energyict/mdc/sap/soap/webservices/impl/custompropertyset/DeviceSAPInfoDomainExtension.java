/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SapDeviceInfo;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Optional;

@UniqueDeviceIdentifier(groups = {Save.Create.class, Save.Update.class})
public class DeviceSAPInfoDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device>, SapDeviceInfo {

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
        CHARACTERISTICS_VALUE("characteristicsValue", "CHARACTERISTICS_VALUE"),
        PUSH_EVENTS_TO_SAP("pushEventsToSap", "PUSH_EVENTS_TO_SAP");


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
    private boolean pushEventsToSap;
    private CustomPropertySetService customPropertySetService;
    private Thesaurus thesaurus;

    @Inject
    public DeviceSAPInfoDomainExtension(CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this.customPropertySetService = customPropertySetService;
        this.thesaurus = thesaurus;
    }

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    void init(Device device, RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.device.set(device);
        super.setRegisteredCustomPropertySet(registeredCustomPropertySet);
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
        setPushEventsToSap((boolean) Optional.ofNullable(propertyValues.getProperty(FieldNames.PUSH_EVENTS_TO_SAP.javaName()))
                .orElse(true));
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
        propertySetValues.setProperty(FieldNames.PUSH_EVENTS_TO_SAP.javaName(), this.shouldPushEventsToSap());
    }

    @Override
    public void validateDelete() {
        // for future purposes
    }

    @Override
    public Optional<String> getDeviceIdentifier() {
        return Optional.ofNullable(deviceIdentifier);
    }

    @Override
    public Optional<String> getDeviceLocation() {
        return Optional.ofNullable(deviceLocation);
    }

    @Override
    public Optional<String> getInstallationNumber() {
        return Optional.ofNullable(installationNumber);
    }

    @Override
    public Optional<String> getPointOfDelivery() {
        return Optional.ofNullable(pointOfDelivery);
    }

    @Override
    public Optional<String> getDivisionCategoryCode() {
        return Optional.ofNullable(divisionCategoryCode);
    }

    @Override
    public Optional<String> getDeviceLocationInformation() {
        return Optional.ofNullable(deviceLocationInformation);
    }

    @Override
    public Optional<String> getModificationInformation() {
        return Optional.ofNullable(modificationInformation);
    }

    @Override
    public Optional<String> getActivationGroupAmiFunctions() {
        return Optional.ofNullable(activationGroupAmiFunctions);
    }

    @Override
    public Optional<String> getMeterFunctionGroup() {
        return Optional.ofNullable(meterFunctionGroup);
    }

    @Override
    public Optional<String> getAttributeMessage() {
        return Optional.ofNullable(attributeMessage);
    }

    @Override
    public Optional<String> getCharacteristicsValue() {
        return Optional.ofNullable(characteristicsValue);
    }

    @Override
    public Optional<String> getCharacteristicsId() {
        return Optional.ofNullable(characteristicsId);
    }

    @Override
    public void setDeviceIdentifier(String deviceIdentifier) {
        if (this.deviceIdentifier == null || this.deviceIdentifier.equals(deviceIdentifier)) {
            this.deviceIdentifier = deviceIdentifier;
        } else {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.DEVICE_ALREADY_HAS_SAP_IDENTIFIER, device.get().getName());
        }
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

    public boolean shouldPushEventsToSap() {
        return pushEventsToSap;
    }

    public void setPushEventsToSap(boolean pushEventsToSap) {
        this.pushEventsToSap = pushEventsToSap;
    }

    public Device getDevice() {
        return device.get();
    }

    @Override
    public void setActivationGroupAmiFunctions(String activationGroupAmiFunctions) {
        this.activationGroupAmiFunctions = activationGroupAmiFunctions;
    }

    @Override
    public void setMeterFunctionGroup(String meterFunctionGroup) {
        this.meterFunctionGroup = meterFunctionGroup;
    }

    @Override
    public void setAttributeMessage(String attributeMessage) {
        this.attributeMessage = attributeMessage;
    }

    @Override
    public void setCharacteristicsId(String characteristicsId) {
        this.characteristicsId = characteristicsId;
    }

    @Override
    public void setCharacteristicsValue(String characteristicsValue) {
        this.characteristicsValue = characteristicsValue;
    }

    @Override
    public void save() {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        this.copyTo(values);

        String cpsId = getRegisteredCustomPropertySet().getCustomPropertySet().getId();
        if (!getRegisteredCustomPropertySet().isEditableByCurrentUser()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, cpsId);
        }
        customPropertySetService.setValuesFor(getRegisteredCustomPropertySet().getCustomPropertySet(), device.get(), values);
        device.get().touchDevice();
    }
}
