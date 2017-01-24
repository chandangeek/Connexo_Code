package com.energyict.protocolimplv2.dlms.g3.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.g3.common.G3Properties;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 10:52
 */
public class AS330DConfigurationSupport extends G3Properties {

    public static final String MIRROR_LOGICAL_DEVICE_ID = "MirrorLogicalDeviceId";
    public static final String GATEWAY_LOGICAL_DEVICE_ID = "GatewayLogicalDeviceId";

    public AS330DConfigurationSupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

}