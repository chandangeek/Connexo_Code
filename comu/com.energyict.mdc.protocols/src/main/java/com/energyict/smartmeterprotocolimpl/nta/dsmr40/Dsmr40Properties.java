/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties;

import java.util.List;

public class Dsmr40Properties extends Dsmr23Properties {

    public static final String DSMR_40_HEX_PASSWORD = "HexPassword";
    public static final String PROPERTY_FORCED_TO_READ_CACHE = "ForcedToReadCache";
    public static final String CumulativeCaptureTimeChannel = "CumulativeCaptureTimeChannel";

    @Override
    public List<String> getOptionalKeys() {
        List<String> optionals = super.getOptionalKeys();
        optionals.add(DSMR_40_HEX_PASSWORD);
        optionals.add(PROPERTY_FORCED_TO_READ_CACHE);
        optionals.add(CumulativeCaptureTimeChannel);
        return optionals;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new Dsmr40SecurityProvider(getProtocolProperties());
    }

    @ProtocolProperty
    public String getHexPassword() {
        return getStringValue(DSMR_40_HEX_PASSWORD, "");
    }

    public boolean getCumulativeCaptureTimeChannel() {
        return getBooleanProperty(CumulativeCaptureTimeChannel, "0");
    }

    @ProtocolProperty
    public boolean isForcedToReadCache() {
        return getBooleanProperty(PROPERTY_FORCED_TO_READ_CACHE, "0");
    }

    public int getForcedToReadCache() {
        return getIntProperty(PROPERTY_FORCED_TO_READ_CACHE, "0");
    }
}
