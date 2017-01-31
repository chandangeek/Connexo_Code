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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link DlmsSecuritySupportPerClient}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (17:39)
 */
public class MTU155CustomPropertySet extends SecurityCustomPropertySet<MTU155SecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public MTU155CustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public MTU155PersistenceSupport getPersistenceSupport() {
        return new MTU155PersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.MTU155_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.MTU155_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Stream
                .of(MTU155SecurityProperties.ActualFields.values())
                .map(field -> field.propertySpec(this.propertySpecService, this.thesaurus))
                .collect(Collectors.toList());
    }

}