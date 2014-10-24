package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by bvn on 10/24/14.
 */
public class DeviceMessageCategoryInfoFactory {

    private final DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory;

    @Inject
    public DeviceMessageCategoryInfoFactory(Thesaurus thesaurus, DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory) {
        this.deviceMessageSpecInfoFactory = deviceMessageSpecInfoFactory;
    }

    public DeviceMessageCategoryInfo asInfo(DeviceMessageCategory category, List<DeviceMessageSpec> deviceMessageSpecs) {
        DeviceMessageCategoryInfo info = new DeviceMessageCategoryInfo();
        info.id = category.getId();
        info.name = category.getName();
        info.deviceMessageSpecs = new ArrayList<>();

        for (DeviceMessageSpec deviceMessageSpec : deviceMessageSpecs) {
            info.deviceMessageSpecs.add(deviceMessageSpecInfoFactory.asInfo(deviceMessageSpec));
        }

        return info;
    }

}
