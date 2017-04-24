/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MetrologyContract;
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
class MetrologyContractValidationRuleSetStateUsageImpl implements MetrologyContractValidationRuleSetStateUsage {

    enum Fields {
        MC_VALRULESETUSAGE("metrologyContractValidationRuleSetUsage"),
        STATE("state");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @NotEmpty(message = MessageSeeds.Constants.REQUIRED)
    @IsPresent
    private Reference<MetrologyContractValidationRuleSetUsage> metrologyContractValidationRuleSetUsage = ValueReference.absent();
    @NotEmpty(message = MessageSeeds.Constants.REQUIRED)
    @IsPresent
    private Reference<State> state = ValueReference.absent();
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
    MetrologyContractValidationRuleSetStateUsageImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    MetrologyContractValidationRuleSetStateUsageImpl initAndSave(MetrologyContractValidationRuleSetUsage metrologyContractValidationRuleSetUsage, State state) {
        this.metrologyContractValidationRuleSetUsage.set(metrologyContractValidationRuleSetUsage);
        this.state.set(state);
        this.dataModel.persist(this);
        return this;
    }

    @Override
    public MetrologyContractValidationRuleSetUsage getMetrologyContractValidationRuleSetUsage() {
        return metrologyContractValidationRuleSetUsage.get();
    }

    @Override
    public State getState() {
        return state.get();
    }

}