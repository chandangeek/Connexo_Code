package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetrologyContractImpl implements MetrologyContract {
    public enum Fields {
        METROLOGY_CONFIG("metrologyConfiguration"),
        METROLOGY_PURPOSE("metrologyPurpose"),
        MANDATORY("mandatory"),
        DELIVERABLES("deliverables"),;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    private long id;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private final Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private final Reference<MetrologyPurpose> metrologyPurpose = ValueReference.absent();
    private boolean mandatory;
    private List<MetrologyContractReadingTypeDeliverableMapping> deliverables = new ArrayList<>();

    @Inject
    public MetrologyContractImpl(ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public MetrologyContractImpl init(MetrologyConfiguration meterConfiguration, MetrologyPurpose metrologyPurpose) {
        this.metrologyConfiguration.set(meterConfiguration);
        this.metrologyPurpose.set(metrologyPurpose);
        return this;
    }

    private void touch() {
        this.metrologyConfigurationService.getDataModel().touch(this.getMetrologyConfiguration());
    }

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguration.orNull();
    }

    @Override
    public MetrologyContract addDeliverable(ReadingTypeDeliverable deliverable) {
        MetrologyContractReadingTypeDeliverableMapping deliverableMapping = this.metrologyConfigurationService.getDataModel().getInstance(MetrologyContractReadingTypeDeliverableMapping.class)
                .init(this, deliverable);
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), deliverableMapping);
        this.deliverables.add(deliverableMapping);
        touch();
        return this;
    }

    @Override
    public void removeDeliverable(ReadingTypeDeliverable deliverable) {
        if (this.deliverables.remove(deliverable)) {
            touch();
        }
    }

    @Override
    public List<ReadingTypeDeliverable> getDeliverables() {
        return this.deliverables.stream()
                .map(MetrologyContractReadingTypeDeliverableMapping::getDeliverable)
                .collect(Collectors.toList());
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
