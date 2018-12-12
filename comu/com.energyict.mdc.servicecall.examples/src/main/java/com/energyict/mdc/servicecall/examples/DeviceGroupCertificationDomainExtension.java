/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

/**
 * Created by bvn on 2/15/16.
 */
public class DeviceGroupCertificationDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        DEVICE_GROUP_ID("deviceGroupId", "device_group_id"),
        YEAR_OF_CERTIFICATION("yearOfCertification", "year");

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

    private long deviceGroupId;
    private long yearOfCertification;

    public DeviceGroupCertificationDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public long getDeviceGroupId() {
        return deviceGroupId;
    }

    public void setDeviceGroupId(long deviceGroupId) {
        this.deviceGroupId = deviceGroupId;
    }

    public long getYearOfCertification() {
        return yearOfCertification;
    }

    public void setYearOfCertification(long yearOfCertification) {
        this.yearOfCertification = yearOfCertification;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setDeviceGroupId((long) propertyValues.getProperty(FieldNames.DEVICE_GROUP_ID.javaName()));
        this.setYearOfCertification((long) propertyValues.getProperty(FieldNames.YEAR_OF_CERTIFICATION.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_GROUP_ID.javaName(), this.getDeviceGroupId());
        propertySetValues.setProperty(FieldNames.YEAR_OF_CERTIFICATION.javaName(), this.getYearOfCertification());
    }

    @Override
    public void validateDelete() {
    }
}