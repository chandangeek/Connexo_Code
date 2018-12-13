package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;

/**
 * Test enum for DeviceMessageCategories.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/02/13
 * Time: 15:30
 */
public enum DeviceMessageTestCategories implements DeviceMessageCategorySupplier {

    CONNECTIVITY_SETUP,
    SECURITY,
    THIRD_TEST_CATEGORY;

    private String getNameResourceKey() {
        return DeviceMessageTestCategories.class.getSimpleName() + "." + this.toString();
    }

    private String getDescriptionResourceKey() {
        return this.getNameResourceKey() + ".description";
    }

    @Override
    public DeviceMessageCategory get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageCategoryImpl(
                this.ordinal(),
                new TranslationKeyImpl(this.getNameResourceKey(), "Translation not supported in unit testing"),
                new TranslationKeyImpl(this.getDescriptionResourceKey(), "Description for " + this.getNameResourceKey()),
                Arrays.asList(DeviceMessageTestSpec.values()),
                propertySpecService,
                nlsService, converter);
    }

}