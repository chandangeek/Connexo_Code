/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.favorites.impl;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.LabelCategory;

import java.time.Instant;

public class DeviceLabelImpl implements DeviceLabel {

    @IsPresent(message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<User> user = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<Device> device = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<LabelCategory> labelCategory = ValueReference.absent();

    private String comment;
    private Instant creationDate;

    DeviceLabelImpl() {
        super();
    }

    DeviceLabelImpl(Device device, User user, LabelCategory category, String comment, Instant now) {
        this();
        setDevice(device);
        setUser(user);
        setLabelCategory(category);
        setComment(comment);
        setCreationDate(now);
    }

    @Override
    public Device getDevice() {
        return device.get();
    }

    public void setDevice(Device device) {
        this.device.set(device);
    }

    @Override
    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    @Override
    public LabelCategory getLabelCategory() {
        return labelCategory.get();
    }

    public void setLabelCategory(LabelCategory labelCategory) {
        this.labelCategory.set(labelCategory);
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }
}
