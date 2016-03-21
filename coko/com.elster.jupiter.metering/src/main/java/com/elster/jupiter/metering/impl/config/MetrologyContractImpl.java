package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableFilter;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.List;

public class MetrologyContractImpl implements MetrologyContract {
    public enum Fields {
        METROLOGY_CONFIG("metrologyConfiguration"),
        METROLOGY_PURPOSE("metrologyPurpose"),
        MANDATORY("mandatory"),;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final MetrologyConfigurationService metrologyConfigurationService;

    private long id;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private final Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private final Reference<MetrologyPurpose> metrologyPurpose = ValueReference.absent();
    private boolean mandatory;

    private List<ReadingTypeDeliverable> readingTypeDeliverables;

    @Inject
    public MetrologyContractImpl(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public MetrologyContractImpl init(MetrologyConfiguration meterConfiguration, MetrologyPurpose metrologyPurpose) {
        this.metrologyConfiguration.set(meterConfiguration);
        this.metrologyPurpose.set(metrologyPurpose);
        return this;
    }


    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguration.orNull();
    }

    @Override
    public List<ReadingTypeDeliverable> getDeliverables() {
        return this.metrologyConfigurationService.findReadingTypeDeliverable(new ReadingTypeDeliverableFilter().withMetrologyContracts(this));
    }

    @Override
    public MetrologyPurpose getMetrologyPurpose() {
        return this.metrologyPurpose.orNull();
    }

    @Override
    public boolean isMandatory() {
        return this.mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MetrologyContractImpl that = (MetrologyContractImpl) o;
        return metrologyConfiguration.equals(that.metrologyConfiguration)
                && metrologyPurpose.equals(that.metrologyPurpose);

    }

    @Override
    public int hashCode() {
        int result = metrologyConfiguration.hashCode();
        result = 31 * result + metrologyPurpose.hashCode();
        return result;
    }
}
