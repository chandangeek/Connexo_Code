/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.ConstraintValidatorContext;
import java.time.Instant;

@SelfValid
public class MetrologyContractReadingTypeDeliverableUsage implements SelfObjectValidator {

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

    @IsPresent(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();
    @IsPresent(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeDeliverable> deliverable = ValueReference.absent();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    MetrologyContractReadingTypeDeliverableUsage init(MetrologyContract metrologyContract, ReadingTypeDeliverable deliverable) {
        this.metrologyContract.set(metrologyContract);
        this.deliverable.set(deliverable);
        return this;
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        if (getMetrologyContract() != null && getDeliverable() != null
                && !getMetrologyContract().getMetrologyConfiguration().equals(getDeliverable().getMetrologyConfiguration())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + PrivateMessageSeeds.Constants.DELIVERABLE_MUST_HAVE_THE_SAME_CONFIGURATION + "}")
                    .addPropertyNode(Fields.DELIVERABLE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    public MetrologyContract getMetrologyContract() {
        return this.metrologyContract.orNull();
    }

    public ReadingTypeDeliverable getDeliverable() {
        return this.deliverable.orNull();
    }
}
