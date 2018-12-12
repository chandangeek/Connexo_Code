package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 10:52
 */
public class CryptoBeacon3100ConfigurationSupport extends Beacon3100ConfigurationSupport {

    public static final String EEK_STORAGE_LABEL = "EphemeralEncryptionKeyStorageLabel";

    public CryptoBeacon3100ConfigurationSupport(PropertySpecService propertySpecService) {
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