/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Provides an implementation for the {@link MetrologyContractEstimationRuleSetUsage} interface.
 */
@UniqueRuleSetUsage(groups = {Save.Create.class, Save.Update.class}, message = "{" + com.elster.jupiter.mdm.usagepoint.config.impl.MessageSeeds.Keys.DUPLICATE_VALIDATION_RULE_USAGE + "}")
class MetrologyContractEstimationRuleSetUsageImpl implements MetrologyContractEstimationRuleSetUsage {

    enum Fields {
        METROLOGY_CONTRACT("metrologyContract"),
        ESTIMATION_RULE_SET("estimationRuleSet"),
        POSITION("position");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @NotEmpty(message = MessageSeeds.Keys.REQUIRED)
    @IsPresent
    private Reference<EstimationRuleSet> estimationRuleSet = ValueReference.absent();
    @NotEmpty(message = MessageSeeds.Keys.REQUIRED)
    @IsPresent
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();
    private long position;
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
    MetrologyContractEstimationRuleSetUsageImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    MetrologyContractEstimationRuleSetUsageImpl initAndSave(MetrologyContract metrologyContract, EstimationRuleSet estimationRuleSet, long position) {
        this.estimationRuleSet.set(estimationRuleSet);
        this.metrologyContract.set(metrologyContract);
        this.position = position;
        this.dataModel.persist(this);
        return this;
    }

    @Override
    public EstimationRuleSet getEstimationRuleSet() {
        return estimationRuleSet.get();
    }

    @Override
    public MetrologyContract getMetrologyContract() {
        return metrologyContract.get();
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public void setPosition(long position) {
        this.position = position;
        this.dataModel.update(this);
    }
}