package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;

import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link IEC1107SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (14:25)
 */
public class IEC1107SecuritySupportCustomPropertySet extends SecurityCustomPropertySet<IEC1107SecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public IEC1107SecuritySupportCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public IEC1107SecuritySupportPersistenceSupport getPersistenceSupport() {
        return new IEC1107SecuritySupportPersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.IEC1107_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.IEC1107_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService));
    }

}