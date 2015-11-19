package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link ExtendedAnsiC12SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (13:50)
 */
public class ExtendedAnsiC12SecuritySupportCustomPropertySet extends SecurityCustomPropertySet<AnsiC12SecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public ExtendedAnsiC12SecuritySupportCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public AnsiC12SecuritySupportPersistenceSupport getPersistenceSupport() {
        return new AnsiC12SecuritySupportPersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.EXTENDED_ANSI_C12_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.EXTENDED_ANSI_C12_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(this.propertySpecService));
    }

}