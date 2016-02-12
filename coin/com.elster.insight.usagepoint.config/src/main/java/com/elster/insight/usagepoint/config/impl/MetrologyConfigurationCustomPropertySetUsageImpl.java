package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.Objects;

public class MetrologyConfigurationCustomPropertySetUsageImpl implements MetrologyConfigurationCustomPropertySetUsage {

    public enum Fields {
        METROLOGY_CONFIG("metrologyConfiguration"),
        CUSTOM_PROPERTY_SET("registeredCustomPropertySet"),
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
    private Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = ValueReference.absent();
    private int position;

    @Inject
    public MetrologyConfigurationCustomPropertySetUsageImpl() {
    }

    MetrologyConfigurationCustomPropertySetUsageImpl init(MetrologyConfiguration metrologyConfiguration, RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.registeredCustomPropertySet.set(registeredCustomPropertySet);
        return this;
    }

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguration.get();
    }

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return this.registeredCustomPropertySet.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetrologyConfigurationCustomPropertySetUsageImpl that = (MetrologyConfigurationCustomPropertySetUsageImpl) o;
        return this.getMetrologyConfiguration().getId() == that.getMetrologyConfiguration().getId() &&
                this.getRegisteredCustomPropertySet().getId() == that.getRegisteredCustomPropertySet().getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.metrologyConfiguration, this.registeredCustomPropertySet);
    }
}
