package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.inject.Inject;

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

    @NotEmpty(message = MessageSeeds.Constants.REQUIRED)
    private Reference<ValidationRuleSet> validationRuleSet = ValueReference.absent();
    @NotEmpty(message = MessageSeeds.Constants.REQUIRED)
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();

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