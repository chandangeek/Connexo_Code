package com.energyict.protocolimplv2.security;

import com.energyict.mdc.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

import java.util.Collections;
import java.util.List;

/**
 * If a protocol has this access level in its list of supported access levels,
 * it means that it's a slave device and it can inherit the security properties of its master device.
 * <p/>
 * Copyrights EnergyICT
 * Author: khe
 */
public class InheritedRequestSecurityLevel implements RequestSecurityLevel {

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