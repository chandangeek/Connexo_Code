/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

/**
 * Maps the Validation Configuration Status on a purpose on a usage point
 */
public class PurposeValidationImpl {

    private boolean isActive = false;
    private Reference<ChannelsContainer> channelsContainer = ValueReference.absent();

    private transient boolean saved = true;
    private final DataModel dataModel;

    @Inject
    PurposeValidationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    PurposeValidationImpl init(ChannelsContainer channelsContainer) {
        this.channelsContainer.set(channelsContainer);
        saved = false;
        return this;
    }

    boolean getActivationStatus() {
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
