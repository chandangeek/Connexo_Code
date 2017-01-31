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

import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link NoOrPasswordSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (14:25)
 */
public class NoOrPasswordCustomPropertySet extends SecurityCustomPropertySet<NoOrPasswordSecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public NoOrPasswordCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public NoOrPasswordPersistenceSupport getPersistenceSupport() {
        return new NoOrPasswordPersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.NO_OR_PASSWORD_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.NO_OR_PASSWORD_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService, this.thesaurus));
    }

}