/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageCategories;

import java.util.List;

class DeviceMessageCategoryImpl implements DeviceMessageCategory {
    private final DeviceMessageCategories category;
    private final int deviceMessageCategoryId;
    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    DeviceMessageCategoryImpl(DeviceMessageCategories category, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.category = category;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        deviceMessageCategoryId = this.category.ordinal();
    }

    @Override
    public String getName() {
        return thesaurus.getString(this.category.getNameResourceKey(), this.category.getDefaultFormat());
    }

    @Override
    public String getDescription() {
        return thesaurus.getString(this.category.getDescriptionResourceKey(), this.category.getDescriptionResourceKey());
    }

    @Override
    public int getId() {
        return deviceMessageCategoryId;
    }

    @Override
    public List<DeviceMessageSpec> getMessageSpecifications() {
        return this.category.getMessageSpecifications(this, propertySpecService, thesaurus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceMessageCategoryImpl)) {
            return false;
        }

        DeviceMessageCategoryImpl that = (DeviceMessageCategoryImpl) o;

        return deviceMessageCategoryId == that.deviceMessageCategoryId;
    }

    @Override
    public int hashCode() {
        int result = category.hashCode();
        result = 31 * result + deviceMessageCategoryId;
        return result;
    }
}
