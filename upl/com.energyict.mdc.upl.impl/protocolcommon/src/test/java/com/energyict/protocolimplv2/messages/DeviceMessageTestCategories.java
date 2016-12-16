package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.List;

/**
 * Test enum for DeviceMessageCategories.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/02/13
 * Time: 15:30
 */
public enum DeviceMessageTestCategories implements DeviceMessageCategorySupplier {

    CONNECTIVITY_SETUP {
        @Override
        protected List<DeviceMessageSpec> getMessageSpecifications(PropertySpecService service) {
            return null;
        }

        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.asList(DeviceMessageTestSpec.values());

        }
    },
    SECURITY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.asList(DeviceMessageTestSpec.values());

        }
    },
    THIRD_TEST_CATEGORY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.asList(DeviceMessageTestSpec.values());

        }
    };

    protected abstract List<DeviceMessageSpec> getMessageSpecifications(PropertySpecService service);

    private String getNameResourceKey() {
        return DeviceMessageTestCategories.class.getSimpleName() + "." + this.toString();
    }

    private String getDescriptionResourceKey() {
        return this.getNameResourceKey() + ".description";
    }

    @Override
    public DeviceMessageCategory get(PropertySpecService propertySpecService, NlsService nlsService) {
        return new DeviceMessageCategoryImpl(
                this.ordinal(),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                new TranslationKeyImpl(this.getDescriptionResourceKey(), this.defaultDescriptionTranslation),
                this.factories(),
                propertySpecService,
                nlsService, converter);
    }

}