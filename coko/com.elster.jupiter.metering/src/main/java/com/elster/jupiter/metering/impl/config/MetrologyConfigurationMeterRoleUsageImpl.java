package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UPMetrologyConfiguration;
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
    private Reference<UPMetrologyConfiguration> metrologyConfiguartion = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<MeterRole> meterRole = ValueReference.absent();

    public MetrologyConfigurationMeterRoleUsageImpl init(UPMetrologyConfiguration metrologyConfiguartion, MeterRole meterRole) {
        this.metrologyConfiguartion.set(metrologyConfiguartion);
        this.meterRole.set(meterRole);
        return this;
    }

    public UPMetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguartion.get();
    }

    public MeterRole getMeterRole() {
        return this.meterRole.get();
    }
}