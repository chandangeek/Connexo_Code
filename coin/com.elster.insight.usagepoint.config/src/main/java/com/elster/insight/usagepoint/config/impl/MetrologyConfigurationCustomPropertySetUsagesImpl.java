package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.MetrologyConfigurationCustomPropertySetUsages;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.Objects;

public class MetrologyConfigurationCustomPropertySetUsagesImpl implements MetrologyConfigurationCustomPropertySetUsages {

    public enum Fields {
        METROLOGY_CONFIG("metrologyConfiguration"),
        CUSTOM_PROPERTY_SET("registeredCustomPropertySet");

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
    @Inject
    public MetrologyConfigurationCustomPropertySetUsagesImpl() {
    }

    MetrologyConfigurationCustomPropertySetUsagesImpl init(MetrologyConfiguration metrologyConfiguration, RegisteredCustomPropertySet registeredCustomPropertySet) {
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
        MetrologyConfigurationCustomPropertySetUsagesImpl that = (MetrologyConfigurationCustomPropertySetUsagesImpl) o;
        return this.getMetrologyConfiguration().getId() == that.getMetrologyConfiguration().getId() &&
                this.getRegisteredCustomPropertySet().getId() == that.getRegisteredCustomPropertySet().getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.metrologyConfiguration, this.registeredCustomPropertySet);
    }
}
