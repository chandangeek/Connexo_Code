package com.energyict.protocolimplv2.edp;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.dlms.DlmsProperties;
import com.energyict.protocolimplv2.dlms.DlmsTranslationKeys;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class that holds the EDP DLMS protocol properties, parses them and returns the proper values.
 * This reuses the general DlmsProperties functionality and adds a few things.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 16:11
 * Author: khe
 */
public class EDPProperties extends DlmsProperties {

    public static final String READCACHE_PROPERTY = "ReadCache";

    public EDPProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, BigDecimal.valueOf(16));
    }

    private PropertySpec readCachePropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(EDPProperties.READCACHE_PROPERTY, DlmsTranslationKeys.READCACHE_PROPERTY)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(false)
                .finish();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(readCachePropertySpec());
        return propertySpecs;
    }
}