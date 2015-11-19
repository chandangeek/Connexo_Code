package com.energyict.protocolimplv2.elster.garnet;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;

import com.energyict.protocolimplv2.security.CustomPropertySetTranslationKeys;
import com.energyict.protocolimplv2.security.DeviceSecurityProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (14:23)
 */
public class GarnetSecuritySupportCustomPropertySet extends SecurityCustomPropertySet<GarnetSecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public GarnetSecuritySupportCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public GarnetSecuritySupportPersistenceSupport getPersistenceSupport() {
        return new GarnetSecuritySupportPersistenceSupport();
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
                DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.getPropertySpec(propertySpecService),
                DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.getPropertySpec(propertySpecService));
    }

}