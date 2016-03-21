package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

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
    private Reference<MetrologyConfiguration> metrologyConfiguartion = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<MeterRole> meterRole = ValueReference.absent();

    public MetrologyConfigurationMeterRoleUsageImpl init(MetrologyConfiguration metrologyConfiguartion, MeterRole meterRole) {
        this.metrologyConfiguartion.set(metrologyConfiguartion);
        this.meterRole.set(meterRole);
        return this;
    }

    public MetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguartion.get();
    }

    public MeterRole getMeterRole() {
        return this.meterRole.get();
    }
}