package com.energyict.protocolimplv2.dlms.idis.hs3300;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300ConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

public class CryptoHS3300ConfigurationSupport extends HS3300ConfigurationSupport {

    public static final String EEK_STORAGE_LABEL = "EphemeralEncryptionKeyStorageLabel";

    public CryptoHS3300ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(eekStorageLabel());
        return propertySpecs;
    }

    private PropertySpec eekStorageLabel() {
        return UPLPropertySpecFactory.specBuilder(EEK_STORAGE_LABEL, false, PropertyTranslationKeys.EEK_STORAGE_LABEL, this.getPropertySpecService()::stringSpec).finish();
    }

}
