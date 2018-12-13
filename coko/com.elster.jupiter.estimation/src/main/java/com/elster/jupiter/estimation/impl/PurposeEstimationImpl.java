/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

/**
 * Maps the Estimation Configuration Status on a purpose on a usage point
 */
public class PurposeEstimationImpl {

    private boolean isActive = false;
    private Reference<ChannelsContainer> channelsContainer = ValueReference.absent();

    private transient boolean saved = true;
    private final DataModel dataModel;

    @Inject
    PurposeEstimationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    PurposeEstimationImpl init(ChannelsContainer channelsContainer) {
        this.channelsContainer.set(channelsContainer);
        saved = false;
        return this;
    }

    boolean isActive() {
        return isActive;
    }

    public ChannelsContainer getChannelsContainer() {
        return channelsContainer.get();
    }

    void setActivationStatus(boolean status) {
        this.isActive = status;
    }

    public void save() {
        if (!saved) {
            Save.CREATE.save(dataModel, this);
            saved = true;
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }
}