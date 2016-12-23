package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import java.util.Collections;
import java.util.List;

/**
 * If a protocol has this access level in its list of supported access levels,
 * it means that it's a slave device and it can inherit the security properties of its master device.
 * <p/>
 * Note that there's a copy of this class in comserver-core-impl, so it can be used by the adapters
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 16/05/14
 * Time: 15:44
 * Author: khe
 */
public class InheritedEncryptionDeviceAccessLevel implements EncryptionDeviceAccessLevel {

    public static final String TRANSLATION_KEY = "inheritedDeviceAccessLevel";

    @Override
    public int getId() {
        return DeviceAccessLevel.CAN_INHERIT_PROPERTIES_FROM_MASTER_ID;
    }

    @Override
    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Collections.emptyList();
    }

}