/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

public class ServiceCategoryCustomPropertySetUsage {

    public enum Fields {
        SERVICECATEGORY("serviceCategory"),
        CUSTOMPROPERTYSET("registeredCustomPropertySet"),
        POSITION("position");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<ServiceCategory> serviceCategory = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = ValueReference.absent();

    private int position;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public ServiceCategoryCustomPropertySetUsage() {
    }

    ServiceCategoryCustomPropertySetUsage initialize(ServiceCategory serviceCategory, RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.serviceCategory.set(serviceCategory);
        this.registeredCustomPropertySet.set(registeredCustomPropertySet);
        return this;
    }

    public ServiceCategory getServiceCategory() {
        return serviceCategory.get();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
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
        ServiceCategoryCustomPropertySetUsage that = (ServiceCategoryCustomPropertySetUsage) o;
        return this.getServiceCategory().getId() == that.getServiceCategory().getId() &&
                this.getRegisteredCustomPropertySet().getId() == that.getRegisteredCustomPropertySet().getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceCategory, registeredCustomPropertySet);
    }
}