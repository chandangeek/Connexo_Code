package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoDeviceMessageCategoryAdapter;
import com.energyict.protocolimplv2.messages.DeviceMessageCategoryImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts a given UPL DeviceMessageCategory to the Connexo DeviceMessageCategory interface.
 * This is straight forward for most fields, except the DeviceMessageSpec that needs adaptation.
 * <p>
 *
 * @author khe
 * @since 14/12/2016 - 12:50
 */
public class UPLDeviceMessageCategoryAdapter implements DeviceMessageCategory {

    private com.energyict.mdc.upl.messages.DeviceMessageCategory uplDeviceMessageCategory;

    public static DeviceMessageCategory adaptTo(com.energyict.mdc.upl.messages.DeviceMessageCategory uplDeviceMessageCategory) {
        if (uplDeviceMessageCategory instanceof ConnexoDeviceMessageCategoryAdapter) {
            return ((ConnexoDeviceMessageCategoryAdapter) uplDeviceMessageCategory).getConnexoDeviceMessageCategory();
        } else {
            return new UPLDeviceMessageCategoryAdapter(uplDeviceMessageCategory);
        }
    }

    public UPLDeviceMessageCategoryAdapter() {
    }

    private UPLDeviceMessageCategoryAdapter(com.energyict.mdc.upl.messages.DeviceMessageCategory uplDeviceMessageCategory) {
        this.uplDeviceMessageCategory = uplDeviceMessageCategory;
    }

    @XmlElements ({
            @XmlElement(type = DeviceMessageCategoryImpl.class),
            @XmlElement(type = ConnexoDeviceMessageCategoryAdapter.class),
    })
    public com.energyict.mdc.upl.messages.DeviceMessageCategory getUplDeviceMessageCategory() {
        return uplDeviceMessageCategory;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public String getName() {
        return uplDeviceMessageCategory.getName();
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public String getDescription() {
        return uplDeviceMessageCategory.getDescription();
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public int getId() {
        return uplDeviceMessageCategory.getId();
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public List<DeviceMessageSpec> getMessageSpecifications() {
        return uplDeviceMessageCategory.getMessageSpecifications().stream()
                .map(UPLDeviceMessageSpecAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UPLDeviceMessageCategoryAdapter) {
            return uplDeviceMessageCategory.equals(((UPLDeviceMessageCategoryAdapter) obj).uplDeviceMessageCategory);
        } else {
            return uplDeviceMessageCategory.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return uplDeviceMessageCategory != null ? uplDeviceMessageCategory.hashCode() : 0;
    }
}