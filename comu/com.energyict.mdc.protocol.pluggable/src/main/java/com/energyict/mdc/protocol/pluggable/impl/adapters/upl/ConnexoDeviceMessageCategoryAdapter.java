package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLDeviceMessageCategoryAdapter;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts a given Connexo DeviceMessageCategory to the UPL DeviceMessageCategory interface.
 * This is straight forward for most fields, except the DeviceMessageSpec that needs adaptation.
 * <p>
 *
 * @author khe
 * @since 14/12/2016 - 12:50
 */
public class ConnexoDeviceMessageCategoryAdapter implements com.energyict.mdc.upl.messages.DeviceMessageCategory {

    private final DeviceMessageCategory cxoDeviceMessageCategory;

    public static com.energyict.mdc.upl.messages.DeviceMessageCategory adaptTo(DeviceMessageCategory cxoDeviceMessageCategory) {
        if (cxoDeviceMessageCategory instanceof UPLDeviceMessageCategoryAdapter) {
            return ((UPLDeviceMessageCategoryAdapter) cxoDeviceMessageCategory).getUplDeviceMessageCategory();
        } else {
            return new ConnexoDeviceMessageCategoryAdapter(cxoDeviceMessageCategory);
        }
    }

    private ConnexoDeviceMessageCategoryAdapter(DeviceMessageCategory cxoDeviceMessageCategory) {
        this.cxoDeviceMessageCategory = cxoDeviceMessageCategory;
    }

    public DeviceMessageCategory getConnexoDeviceMessageCategory() {
        return cxoDeviceMessageCategory;
    }

    @Override
    public String getName() {
        return cxoDeviceMessageCategory.getName();
    }

    @Override
    public String getDescription() {
        return cxoDeviceMessageCategory.getDescription();
    }

    @Override
    public int getId() {
        return cxoDeviceMessageCategory.getId();
    }

    @Override
    public String getNameResourceKey() {
        return cxoDeviceMessageCategory.getName();
    }

    @Override
    public List<DeviceMessageSpec> getMessageSpecifications() {
        return cxoDeviceMessageCategory.getMessageSpecifications().stream()
                .map(ConnexoDeviceMessageSpecAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnexoDeviceMessageCategoryAdapter) {
            return cxoDeviceMessageCategory.equals(((ConnexoDeviceMessageCategoryAdapter) obj).cxoDeviceMessageCategory);
        } else {
            return cxoDeviceMessageCategory.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return cxoDeviceMessageCategory != null ? cxoDeviceMessageCategory.hashCode() : 0;
    }
}