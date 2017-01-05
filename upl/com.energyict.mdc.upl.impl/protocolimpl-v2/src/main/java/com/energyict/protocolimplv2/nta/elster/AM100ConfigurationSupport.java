package com.energyict.protocolimplv2.nta.elster;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.List;

/**
 * @author sva
 * @since 29/05/2015 - 14:23
 */
public class AM100ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String READCACHE_PROPERTY = "ReadCache";

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(readCachePropertySpec());
        return propertySpecs;
    }

    protected PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory.booleanValue(READCACHE_PROPERTY, false);
    }

}
