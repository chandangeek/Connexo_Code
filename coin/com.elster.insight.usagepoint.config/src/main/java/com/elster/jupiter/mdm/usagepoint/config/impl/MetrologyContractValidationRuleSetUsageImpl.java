/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Provides an implementation for the {@link MetrologyContractValidationRuleSetUsage} interface.
 */
class MetrologyContractValidationRuleSetUsageImpl implements MetrologyContractValidationRuleSetUsage {

    enum Fields {
        METROLOGY_CONTRACT("metrologyContract"),
        VALIDATION_RULE_SET("validationRuleSet");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @NotEmpty(message = PrivateMessageSeeds.Constants.REQUIRED)
    @IsPresent
    private Reference<ValidationRuleSet> validationRuleSet = ValueReference.absent();
    @NotEmpty(message = PrivateMessageSeeds.Constants.REQUIRED)
    @IsPresent
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    private final DataModel dataModel;

    @Inject
    MetrologyContractValidationRuleSetUsageImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    MetrologyContractValidationRuleSetUsageImpl initAndSave(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet) {
        this.validationRuleSet.set(validationRuleSet);
        this.metrologyContract.set(metrologyContract);
        this.dataModel.persist(this);
        return this;
    }

    @Override
    public ValidationRuleSet getValidationRuleSet() {
        return validationRuleSet.get();
    }

    @Override
    public MetrologyContract getMetrologyContract() {
        return metrologyContract.get();
    }

}