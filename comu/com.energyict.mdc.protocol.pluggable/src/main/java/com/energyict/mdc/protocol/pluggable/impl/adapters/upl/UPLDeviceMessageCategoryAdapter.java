package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts a given UPL DeviceMessageCategory to the Connexo DeviceMessageCategory interface.
 * This is straight forward for most fields, except the DeviceMessageSpec that needs adaptation.
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 14/12/2016 - 12:50
 */
public class UPLDeviceMessageCategoryAdapter implements DeviceMessageCategory {

    private final com.energyict.mdc.upl.messages.DeviceMessageCategory uplDeviceMessageCategory;

    public UPLDeviceMessageCategoryAdapter(com.energyict.mdc.upl.messages.DeviceMessageCategory uplDeviceMessageCategory) {
        this.uplDeviceMessageCategory = uplDeviceMessageCategory;
    }

    @Override
    public String getName() {
        return uplDeviceMessageCategory.getName();
    }

    @Override
    public String getDescription() {
        return uplDeviceMessageCategory.getDescription();
    }

    @Override
    public int getId() {
        return uplDeviceMessageCategory.getId();
    }

    @Override
    public List<DeviceMessageSpec> getMessageSpecifications() {
        return uplDeviceMessageCategory.getMessageSpecifications().stream()
                .map(UPLDeviceMessageSpecAdapter::new)
                .collect(Collectors.toList());
    }
}