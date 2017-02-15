/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link BasicAuthenticationSecurityProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (10:18)
 */
public class BasicAuthenticationCustomPropertySet extends SecurityCustomPropertySet<BasicAuthenticationSecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public BasicAuthenticationCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public BasicAuthenticationPersistenceSupport getPersistenceSupport() {
        return new BasicAuthenticationPersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.BASIC_AUTHENTICATION_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.BASIC_AUTHENTICATION_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                BasicAuthenticationSecurityProperties.ActualFields.USER_NAME.propertySpec(this.propertySpecService, this.thesaurus),
                BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.propertySpec(this.propertySpecService, this.thesaurus));
    }

}