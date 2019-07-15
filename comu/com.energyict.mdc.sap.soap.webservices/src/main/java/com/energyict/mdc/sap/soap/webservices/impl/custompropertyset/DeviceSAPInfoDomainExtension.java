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
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.Size;
import java.util.Optional;

public class DeviceSAPInfoDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum FieldNames {
        DOMAIN("device", "DEVICE"),
        DEVICE_IDENTIFIER("deviceIdentifier", "DEVICE_IDENTIFIER"),
        DEVICE_LOCATION("deviceLocation", "DEVICE_LOCATION"),
        POINT_OF_DELIVERY("pointOfDelivery", "POINT_OF_DELIVERY");

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
    private String pointOfDelivery;

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
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_IDENTIFIER.javaName(), deviceIdentifier);
        propertySetValues.setProperty(FieldNames.DEVICE_LOCATION.javaName(), deviceLocation);
        propertySetValues.setProperty(FieldNames.POINT_OF_DELIVERY.javaName(), pointOfDelivery);
    }

    @Override
    public void validateDelete() {
        // for future purposes
    }

    public Optional<String> getDeviceIdentifier() {
        return Optional.ofNullable(deviceIdentifier);
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public void setDeviceLocation(String deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    public void setPointOfDelivery(String pointOfDelivery) {
        this.pointOfDelivery = pointOfDelivery;
    }

    public Device getDevice() {
        return device.get();
    }
}
