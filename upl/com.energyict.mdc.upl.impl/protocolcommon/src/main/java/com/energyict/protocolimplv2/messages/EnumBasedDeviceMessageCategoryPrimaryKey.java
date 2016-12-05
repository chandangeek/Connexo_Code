package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategoryPrimaryKey;

import static com.energyict.protocolimplv2.messages.EnumBasedPrimaryKeySupport.CARDINAL_REGEX;
import static com.energyict.protocolimplv2.messages.EnumBasedPrimaryKeySupport.cleanUpClassName;

/**
 * Provides an implementation for the {@link DeviceMessageCategoryPrimaryKey} interface
 * that are being produced by {@link DeviceMessageCategorySupplier DeviceMessageCategoryFactories}
 * that are implemented by enum classes.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/02/13
 * Time: 16:25
 */
public class EnumBasedDeviceMessageCategoryPrimaryKey implements DeviceMessageCategoryPrimaryKey {

    private final String factoryClassName;
    private final String enumName;

    public EnumBasedDeviceMessageCategoryPrimaryKey(DeviceMessageCategorySupplier factory, String enumName) {
        this.factoryClassName = factory.getClass().getName();
        this.enumName = enumName;
    }

    @Override
    public String getValue() {
        return cleanUpClassName(this.factoryClassName) + CARDINAL_REGEX + this.enumName;
    }

}