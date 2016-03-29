package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class MetrologyContractReadingTypeDeliverableUsage {
    public enum Fields {
        METROLOGY_CONTRACT("metrologyContract"),
        DELIVERABLE("deliverable"),;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeDeliverable> deliverable = ValueReference.absent();

    MetrologyContractReadingTypeDeliverableUsage init(MetrologyContract metrologyContract, ReadingTypeDeliverable deliverable) {
        this.metrologyContract.set(metrologyContract);
        this.deliverable.set(deliverable);
        return this;
    }

    public MetrologyContract getMetrologyContract() {
        return this.metrologyContract.orNull();
    }

    public ReadingTypeDeliverable getDeliverable() {
        return this.deliverable.orNull();
    }
}
