/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.DlmsTranslationKeys;
import com.energyict.protocolimplv2.edp.EDPProperties;
import com.energyict.protocolimplv2.g3.common.G3Properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class G3GatewayProperties extends G3Properties {

    public static final String AARQ_TIMEOUT_PROP_NAME = "AARQ_Timeout";
    public static final TimeDuration AARQ_TIMEOUT_DEFAULT = TimeDuration.NONE;

    public G3GatewayProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    public long getAarqTimeout() {
        return getProperties().getTypedProperty(AARQ_TIMEOUT_PROP_NAME, AARQ_TIMEOUT_DEFAULT).getMilliSeconds();
    }

    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(EDPProperties.READCACHE_PROPERTY, false);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        Collections.addAll(
                propertySpecs,
                validateInvokeIdPropertySpec(),
                aarqTimeoutPropertySpec(),
                readCachePropertySpec(),
                forcedDelayPropertySpec(),
                maxRecPduSizePropertySpec());
        return propertySpecs;
    }

    private PropertySpec validateInvokeIdPropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(DlmsProtocolProperties.VALIDATE_INVOKE_ID, DlmsTranslationKeys.VALIDATE_INVOKE_ID)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(true)
                .finish();
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return getPropertySpecService()
                .timeDurationSpec()
                .named(G3GatewayProperties.AARQ_TIMEOUT_PROP_NAME, DlmsTranslationKeys.AARQ_TIMEOUT_PROP_NAME)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(G3GatewayProperties.AARQ_TIMEOUT_DEFAULT)
                .finish();
    }

    private PropertySpec readCachePropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(EDPProperties.READCACHE_PROPERTY, DlmsTranslationKeys.READCACHE_PROPERTY)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec maxRecPduSizePropertySpec() {
        return getPropertySpecService()
                .bigDecimalSpec()
                .named(DlmsProtocolProperties.MAX_REC_PDU_SIZE, DlmsTranslationKeys.MAX_REC_PDU_SIZE)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(DEFAULT_MAX_REC_PDU_SIZE)
                .finish();
    }

}