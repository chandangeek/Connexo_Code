/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.servicecall.ServiceCallType;

import java.time.Instant;
import java.util.Objects;

class ServiceCallTypeCustomPropertySetUsageImpl implements ServiceCallTypeCustomPropertySetUsage {

    public enum Fields {
        ServciceCallType("serviceCallType"),
        CustomPropertySet("registeredCustomPropertySet");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<ServiceCallType> serviceCallType = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = ValueReference.absent();
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;

    ServiceCallTypeCustomPropertySetUsageImpl initialize(ServiceCallType serviceCallType, RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.serviceCallType.set(serviceCallType);
        this.registeredCustomPropertySet.set(registeredCustomPropertySet);
        return this;
    }

    public ServiceCallType getServiceCallType() {
        return serviceCallType.get();
    }

    @Override
    public RegisteredCustomPropertySet getCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceCallTypeCustomPropertySetUsageImpl that = (ServiceCallTypeCustomPropertySetUsageImpl) o;
        return this.getServiceCallType().getId() == that.getServiceCallType().getId() &&
                this.getCustomPropertySet().getId() == that.getCustomPropertySet().getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceCallType, registeredCustomPropertySet);
    }
}