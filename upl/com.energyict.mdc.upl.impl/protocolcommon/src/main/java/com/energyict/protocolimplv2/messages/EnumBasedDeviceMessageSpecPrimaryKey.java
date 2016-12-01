package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;

import static com.energyict.protocolimplv2.messages.EnumBasedPrimaryKeySupport.CARDINAL_REGEX;
import static com.energyict.protocolimplv2.messages.EnumBasedPrimaryKeySupport.cleanUpClassName;

/**
 * Provides an implementation for the {@link DeviceMessageSpecPrimaryKey} interface
 * that are being produced by {@link DeviceMessageSpecFactory DeviceMessageSpecFactories}
 * that are implemented by enum classes.
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 11:45
 */
public class EnumBasedDeviceMessageSpecPrimaryKey implements DeviceMessageSpecPrimaryKey {

    private final String enumClassName;
    private final String enumName;

    public EnumBasedDeviceMessageSpecPrimaryKey(DeviceMessageSpecFactory factory, String enumName) {
        this.enumClassName = factory.getClass().getName();
        this.enumName = enumName;
    }

    @Override
    public String getValue() {
        return cleanUpClassName(this.enumClassName + CARDINAL_REGEX + this.enumName);
    }

    public String getName() {
        return enumName;
    }

}