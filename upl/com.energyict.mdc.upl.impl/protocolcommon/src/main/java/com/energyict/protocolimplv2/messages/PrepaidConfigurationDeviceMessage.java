package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimplv2.messages.nls.Thesaurus;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.prepaidCreditAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.prepaidCreditAttributeNameDefaultTranslation;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PrepaidConfigurationDeviceMessage implements DeviceMessageSpec {

    AddPrepaidCredit(0) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            TranslationKeyImpl translationKey = new TranslationKeyImpl(prepaidCreditAttributeName, prepaidCreditAttributeNameDefaultTranslation);
            return Collections.singletonList(
                Services
                    .propertySpecService()
                    .bigDecimalSpec()
                    .named(prepaidCreditAttributeName, translationKey)
                    .describedAs(translationKey.description())
                    .finish());
        }
    },
    DisablePrepaid(1) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    EnablePrepaid(2) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    };

    private final long id;

    PrepaidConfigurationDeviceMessage(long id) {
        this.id = id;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.PREPAID_CONFIGURATION;
    }

    @Override
    public String getName() {
        return Services
                .nlsService()
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    @Override
    public String getNameResourceKey() {
        return PrepaidConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    private TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), "MR" + this.getNameResourceKey());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public long getMessageId() {
        return id;
    }

}