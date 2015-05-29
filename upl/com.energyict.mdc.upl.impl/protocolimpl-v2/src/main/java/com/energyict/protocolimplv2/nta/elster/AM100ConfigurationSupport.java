package com.energyict.protocolimplv2.nta.elster;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 29/05/2015 - 14:23
 */
public class AM100ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String READCACHE_PROPERTY = "ReadCache";

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.addAll(super.getOptionalProperties());
        optionalProperties.add(readCachePropertySpec());
        return optionalProperties;
    }

    protected PropertySpec readCachePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(READCACHE_PROPERTY, false);
    }

}
