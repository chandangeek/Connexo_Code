/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
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
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link DlmsSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (14:23)
 */
public class DlmsSecurityCryptographicCustomPropertySet extends SecurityCustomPropertySet<DlmsSecurityCryptogaphicProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public DlmsSecurityCryptographicCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public DlmsSecurityCryptographyPersistenceSupport getPersistenceSupport() {
        return new DlmsSecurityCryptographyPersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.DLMS_SECURITY_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.DLMS_SECURITY_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService, this.thesaurus),
                DeviceSecurityProperty.ENCRYPTION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(this.propertySpecService, this.thesaurus),
                DeviceSecurityProperty.AUTHENTICATION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(this.propertySpecService, this.thesaurus),
                DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(this.propertySpecService, this.thesaurus));
    }

}