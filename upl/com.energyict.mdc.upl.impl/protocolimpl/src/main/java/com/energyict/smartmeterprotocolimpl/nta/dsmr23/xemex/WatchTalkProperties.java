package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
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

    public static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    private static final boolean DEFAULT_IGNORE_DST_STATUS_CODE = true;

    public WatchTalkProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(PROPERTY_IGNORE_DST_STATUS_CODE, false, PropertyTranslationKeys.NTA_IGNORE_DST_STATUS_CODE, this.getPropertySpecService()::integerSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(PROPERTY_FORCED_TO_READ_CACHE, false, PropertyTranslationKeys.NTA_FORCED_TO_READ_CACHE, this.getPropertySpecService()::integerSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(VALIDATE_INVOKE_ID, false, PropertyTranslationKeys.NTA_VALIDATE_INVOKE_ID, this.getPropertySpecService()::integerSpec).finish());
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
        return getBooleanProperty(PROPERTY_FORCED_TO_READ_CACHE, false);
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