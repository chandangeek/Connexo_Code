package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts a given UPL DeviceMessageSpec to the Connexo DeviceMessageSpec interface.
 * This is straight forward for all fields except for the PropertySpecs, they need adaptation.
 * <p>
 *
 * @author khe
 * @since 14/12/2016 - 11:40
 */
public class UPLDeviceMessageSpecAdapter implements DeviceMessageSpec {

    private final com.energyict.mdc.upl.messages.DeviceMessageSpec uplDeviceMessageSpec;

    public UPLDeviceMessageSpecAdapter(com.energyict.mdc.upl.messages.DeviceMessageSpec uplDeviceMessageSpec) {
        this.uplDeviceMessageSpec = uplDeviceMessageSpec;
    }

    public com.energyict.mdc.upl.messages.DeviceMessageSpec getUplDeviceMessageSpec() {
        return uplDeviceMessageSpec;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return new UPLDeviceMessageCategoryAdapter(uplDeviceMessageSpec.getCategory());
    }

    @Override
    public String getName() {
        return uplDeviceMessageSpec.getName();
    }

    @Override
    public DeviceMessageId getId() {
        return DeviceMessageId.from(uplDeviceMessageSpec.getId());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return uplDeviceMessageSpec.getPropertySpecs().stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UPLDeviceMessageSpecAdapter) {
            return uplDeviceMessageSpec.equals(((UPLDeviceMessageSpecAdapter) obj).uplDeviceMessageSpec);
        } else {
            return uplDeviceMessageSpec.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return uplDeviceMessageSpec.hashCode();
    }
}