package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 18/03/14 - 16:18
 */
public class WatchTalkProperties extends Dsmr23Properties {

    private static final String PROPERTY_IGNORE_DST_STATUS_CODE = "IgnoreDstStatusCode";
    private static final String PROPERTY_FORCED_TO_READ_CACHE = "ForcedToReadCache";

    public static final String DEFAULT_VALIDATE_INVOKE_ID = "1";
    private static final String DEFAULT_IGNORE_DST_STATUS_CODE = "1";

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(UPLPropertySpecFactory.integer(PROPERTY_IGNORE_DST_STATUS_CODE, false));
        propertySpecs.add(UPLPropertySpecFactory.integer(PROPERTY_FORCED_TO_READ_CACHE, false));
        propertySpecs.add(UPLPropertySpecFactory.integer(VALIDATE_INVOKE_ID, false));
        return propertySpecs;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        NTASecurityProvider ntaSecurityProvider = new NTASecurityProvider(getProtocolProperties());
        ntaSecurityProvider.setClientToServerChallengeLength(NTASecurityProvider.ChallengeLength.LENGTH_8_BYTE);
        return ntaSecurityProvider;
    }

    @ProtocolProperty
    public boolean getForcedToReadCache() {
        return getBooleanProperty(PROPERTY_FORCED_TO_READ_CACHE, "0");
    }

    protected boolean validateInvokeId() {
        return getBooleanProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

    @Override
    public boolean isRequestTimeZone() {
        return false; // Do not request timezone, cause WatchTalk has custom implementation for timezone / deviation settings (always positive deviation indicating winter time offset))
    }

    public boolean ignoreDstStatusCode() {
        return getBooleanProperty(PROPERTY_IGNORE_DST_STATUS_CODE, DEFAULT_IGNORE_DST_STATUS_CODE);
    }

}