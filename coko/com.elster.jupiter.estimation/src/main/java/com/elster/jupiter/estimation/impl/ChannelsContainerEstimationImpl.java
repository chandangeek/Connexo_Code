/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

public class ChannelsContainerEstimationImpl implements ChannelsContainerEstimation{

    private long id;
    private boolean active;
    private Reference<ChannelsContainer> channelsContainer = ValueReference.absent();
    private Reference<EstimationRuleSet> estimationRuleSet = ValueReference.absent();
    private final DataModel dataModel;

    @Inject
    ChannelsContainerEstimationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    ChannelsContainerEstimationImpl init(ChannelsContainer channelsContainer, EstimationRuleSet estimationRuleSet) {
        this.channelsContainer.set(channelsContainer);
        this.estimationRuleSet.set(estimationRuleSet);
        return this;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public EstimationRuleSet getEstimationRuleSet() {
        return estimationRuleSet.get();
    }

    @Override
    public ChannelsContainer getChannelsContainer() {
        return channelsContainer.get();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void save() {
        if (id == 0) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }
}
