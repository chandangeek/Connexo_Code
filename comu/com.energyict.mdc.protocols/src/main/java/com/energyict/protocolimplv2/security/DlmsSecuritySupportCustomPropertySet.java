package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link DlmsSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (14:23)
 */
public class DlmsSecuritySupportCustomPropertySet extends SecurityCustomPropertySet<DlmsSecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public DlmsSecuritySupportCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public DlmsSecuritySupportPersistenceSupport getPersistenceSupport() {
        return new DlmsSecuritySupportPersistenceSupport();
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
                DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(this.propertySpecService));
    }

}