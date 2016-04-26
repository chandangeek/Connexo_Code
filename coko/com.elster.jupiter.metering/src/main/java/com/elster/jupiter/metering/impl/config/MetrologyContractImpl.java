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
import java.util.Iterator;
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
    private List<MetrologyContractReadingTypeDeliverableUsage> deliverables = new ArrayList<>();

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
        MetrologyContractReadingTypeDeliverableUsage deliverableMapping = this.metrologyConfigurationService.getDataModel()
                .getInstance(MetrologyContractReadingTypeDeliverableUsage.class)
                .init(this, deliverable);
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), deliverableMapping);
        this.deliverables.add(deliverableMapping);
        touch();
        return this;
    }

    @Override
    public void removeDeliverable(ReadingTypeDeliverable deliverable) {
        Iterator<MetrologyContractReadingTypeDeliverableUsage> iterator = this.deliverables.iterator();
        while (iterator.hasNext()) {
            MetrologyContractReadingTypeDeliverableUsage usage = iterator.next();
            if (usage.getDeliverable().equals(deliverable)) {
                iterator.remove();
                this.touch();
                return;
            }
        }
    }

    @Override
    public List<ReadingTypeDeliverable> getDeliverables() {
        return this.deliverables.stream()
                .map(MetrologyContractReadingTypeDeliverableUsage::getDeliverable)
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
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }
}
