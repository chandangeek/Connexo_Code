/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UtilitiesDeviceLocationNotificationDomainExtension extends AbstractPersistentDomainExtension
        implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        // general
        DOMAIN("serviceCall", "SERVICE_CALL"),

        // provided
        DEVICE_ID("deviceId", "DEVICE_ID"),
        LOCATION_ID("locationId", "LOCATION_ID"),
        INSTALLATION_NUMBER("installationNumber", "INSTALLATION_NUMBER"),
        POINT_OF_DELIVERY("pointOfDelivery", "POINT_OF_DELIVERY"),
        DIVISION_CATEGORY_CODE("divisionCategoryCode", "DIVISION_CATEGORY_CODE"),
        LOCATION_INFORMATION("locationInformation", "LOCATION_INFORMATION"),
        MODIFICATION_INFORMATION("modificationInformation", "MODIFICATION_INFORMATION");

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

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceId;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String locationId;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String installationNumber;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String pointOfDelivery;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String divisionCategoryCode;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String locationInformation;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String modificationInformation;


    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getInstallationNumber() {
        return installationNumber;
    }

    public void setInstallationNumber(String installationNumber) {
        this.installationNumber = installationNumber;
    }

    public String getPod() {
        return pointOfDelivery;
    }

    public void setPod(String pointOfDelivery) {
        this.pointOfDelivery = pointOfDelivery;
    }

    public String getDivisionCategoryCode() {
        return divisionCategoryCode;
    }

    public void setDivisionCategoryCode(String divisionCategoryCode) {
        this.divisionCategoryCode = divisionCategoryCode;
    }

    public String getLocationInformation() {
        return locationInformation;
    }

    public void setLocationInformation(String locationInformation) {
        this.locationInformation = locationInformation;
    }

    public String getModificationInformation() {
        return modificationInformation;
    }

    public void setModificationInformationInformation(String modificationInformation) {
        this.modificationInformation = modificationInformation;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setDeviceId((String) propertyValues.getProperty(FieldNames.DEVICE_ID.javaName()));
        this.setLocationId((String) propertyValues.getProperty(FieldNames.LOCATION_ID.javaName()));
        this.setInstallationNumber((String) propertyValues.getProperty(FieldNames.INSTALLATION_NUMBER.javaName()));
        this.setPod((String) propertyValues.getProperty(FieldNames.POINT_OF_DELIVERY.javaName()));
        this.setDivisionCategoryCode((String) propertyValues.getProperty(FieldNames.DIVISION_CATEGORY_CODE.javaName()));
        this.setLocationInformation((String) propertyValues.getProperty(FieldNames.LOCATION_INFORMATION.javaName()));
        this.setModificationInformationInformation((String) propertyValues.getProperty(FieldNames.MODIFICATION_INFORMATION.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_ID.javaName(), this.getDeviceId());
        propertySetValues.setProperty(FieldNames.LOCATION_ID.javaName(), this.getLocationId());
        propertySetValues.setProperty(FieldNames.INSTALLATION_NUMBER.javaName(), this.getInstallationNumber());
        propertySetValues.setProperty(FieldNames.POINT_OF_DELIVERY.javaName(), this.getPod());
        propertySetValues.setProperty(FieldNames.DIVISION_CATEGORY_CODE.javaName(), this.getDivisionCategoryCode());
        propertySetValues.setProperty(FieldNames.LOCATION_INFORMATION.javaName(), this.getLocationInformation());
        propertySetValues.setProperty(FieldNames.MODIFICATION_INFORMATION.javaName(), this.getModificationInformation());
    }

    @Override
    public void validateDelete() {
    }
}
