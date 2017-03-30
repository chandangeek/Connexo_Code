/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import java.time.Instant;

public class MetrologyConfigurationMeterRoleUsageImpl {

    public enum Fields {
        METROLOGY_CONFIGURATION("metrologyConfiguartion"),
        METER_ROLE("meterRole");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<UsagePointMetrologyConfiguration> metrologyConfiguartion = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<MeterRole> meterRole = ValueReference.absent();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    public MetrologyConfigurationMeterRoleUsageImpl init(UsagePointMetrologyConfiguration metrologyConfiguartion, MeterRole meterRole) {
        this.metrologyConfiguartion.set(metrologyConfiguartion);
        this.meterRole.set(meterRole);
        return this;
    }

    public UsagePointMetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguartion.get();
    }

    public MeterRole getMeterRole() {
        return this.meterRole.get();
    }
}