/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;

public class ServiceCategoryMeterRoleUsage {

    public enum Fields {
        SERVICECATEGORY("serviceCategory"),
        METERROLE("meterRole");

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
    private Reference<MeterRole> meterRole = ValueReference.absent();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public ServiceCategoryMeterRoleUsage() {
    }

    public ServiceCategoryMeterRoleUsage init(ServiceCategory serviceCategory, MeterRole meterRole) {
        this.serviceCategory.set(serviceCategory);
        this.meterRole.set(meterRole);
        return this;
    }

    public ServiceCategory getServiceCategory() {
        return serviceCategory.get();
    }

    public MeterRole getMeterRole() {
        return meterRole.get();
    }
}