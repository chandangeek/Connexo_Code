/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.inbound.ServletBasedInboundDeviceProtocol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the ServletBasedInboundDeviceProtocol interface
 * that will support inbound SMS communication for the CTR protocols (MTU155 and EK155).
 *
 * @author sva
 * @since 24/06/13 - 13:53
 */
public abstract class AbstractSMSServletBasedInboundDeviceProtocol implements ServletBasedInboundDeviceProtocol {

    public static final String SOURCE_PROPERTY_NAME = "API_source";
    public static final String AUTHENTICATION_PROPERTY_NAME = "API_authentication";

    public enum TranslationKeys implements TranslationKey {
        SOURCE_PROPERTY(SOURCE_PROPERTY_NAME, "API source"),
        AUTHENTICATION_PROPERTY(AUTHENTICATION_PROPERTY_NAME, "API authentication");

        private final String propertySpecName;
        private final String defaultFormat;

        TranslationKeys(String propertySpecName, String defaultFormat) {
            this.propertySpecName = propertySpecName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return "ServletBasedInboundSMS." + this.propertySpecName;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

    }
    protected TypedProperties properties;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected InboundDiscoveryContext context;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    // Subclasses should add @Inject
    protected AbstractSMSServletBasedInboundDeviceProtocol(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.properties.setAllProperties(properties);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(sourcePropertySpec(), authenticationPropertySpec());
    }

    @Override
    public void init(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-05-28 12:16:55 +0200 (di, 28 mei 2013) $";
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return this.context;
    }

    private PropertySpec sourcePropertySpec() {
        return this.propertySpecService
                .stringSpec()
                .named(TranslationKeys.SOURCE_PROPERTY)
                .fromThesaurus(this.thesaurus)
                .finish();
    }

    protected String sourcePropertyValue() {
        return (String) this.getProperty(SOURCE_PROPERTY_NAME);
    }

    private PropertySpec authenticationPropertySpec() {
        return this.propertySpecService
                .stringSpec()
                .named(TranslationKeys.AUTHENTICATION_PROPERTY)
                .fromThesaurus(this.thesaurus)
                .finish();
    }

    protected String authenticationPropertyValue() {
        return (String) this.getProperty(AUTHENTICATION_PROPERTY_NAME);
    }

    protected Object getProperty(String propertyName) {
        return this.properties.getProperty(propertyName);
    }

}