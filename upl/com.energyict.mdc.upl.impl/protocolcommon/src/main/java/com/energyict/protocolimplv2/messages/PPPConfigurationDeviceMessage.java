package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PPPConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    SetISP1Phone(0, "Set ISP1 phone", PropertyType.STRING, DeviceMessageConstants.SetISP1PhoneAttributeName, DeviceMessageConstants.SetISP1PhoneAttributeDefaultTranslation),
    SetISP1Username(1, "Set ISP1 username", PropertyType.STRING, DeviceMessageConstants.SetISP1UsernameAttributeName, DeviceMessageConstants.SetISP1UsernameAttributeDefaultTranslation),
    SetISP1Password(2, "Set ISP1 password", PropertyType.STRING, DeviceMessageConstants.SetISP1PasswordAttributeName, DeviceMessageConstants.SetISP1PasswordAttributeDefaultTranslation),
    SetISP1Tries(3, "Set ISP1 tries", PropertyType.STRING, DeviceMessageConstants.SetISP1TriesAttributeName, DeviceMessageConstants.SetISP1TriesAttributeDefaultTranslation),
    SetISP2Phone(4, "Set ISP2 phone", PropertyType.STRING, DeviceMessageConstants.SetISP2PhoneAttributeName, DeviceMessageConstants.SetISP2PhoneAttributeDefaultTranslation),
    SetISP2Username(5, "Set ISP2 username", PropertyType.STRING, DeviceMessageConstants.SetISP2UsernameAttributeName, DeviceMessageConstants.SetISP2UsernameAttributeDefaultTranslation),
    SetISP2Password(6, "Set ISP2 password", PropertyType.STRING, DeviceMessageConstants.SetISP2PasswordAttributeName, DeviceMessageConstants.SetISP2PasswordAttributeDefaultTranslation),
    SetISP2Tries(7, "Set ISP2 tries", PropertyType.STRING, DeviceMessageConstants.SetISP2TriesAttributeName, DeviceMessageConstants.SetISP2TriesAttributeDefaultTranslation),
    SetPPPIdleTimeout(8, "Set PPP idle timeout", PropertyType.STRING, DeviceMessageConstants.SetPPPIdleTimeoutAttributeName, DeviceMessageConstants.SetPPPIdleTimeoutAttributeDefaultTranslation),
    SetPPPRetryInterval(9, "Set PPP retry interval", PropertyType.STRING, DeviceMessageConstants.SetPPPRetryIntervalAttributeName, DeviceMessageConstants.SetPPPRetryIntervalAttributeDefaultTranslation),
    SetPPPOptions(10, "Set PPP options", PropertyType.STRING, DeviceMessageConstants.SetPPPOptionsAttributeName, DeviceMessageConstants.SetPPPOptionsAttributeDefaultTranslation),
    SetPPPIdleTime(11, "Set PPP idle time", PropertyType.BIGDECIMAL, DeviceMessageConstants.SetPPPIdleTime, DeviceMessageConstants.SetPPPIdleTimeoutAttributeDefaultTranslation),
    PPPSetOption(12, "PPP - Set an option", PropertyType.STRING, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    PPPClrOption(13, "PPP - Clear an option", PropertyType.STRING, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation);

    private enum PropertyType {
        STRING {
            @Override
            protected PropertySpec get(PropertySpecService service, String propertyName, String defaultTranslation) {
                TranslationKeyImpl translationKey = new TranslationKeyImpl(propertyName, defaultTranslation);
                return service
                        .stringSpec()
                        .named(propertyName, translationKey)
                        .describedAs(translationKey.description())
                        .finish();
            }
        },
        BIGDECIMAL {
            @Override
            protected PropertySpec get(PropertySpecService service, String propertyName, String defaultTranslation) {
                TranslationKeyImpl translationKey = new TranslationKeyImpl(propertyName, defaultTranslation);
                return service
                        .bigDecimalSpec()
                        .named(propertyName, translationKey)
                        .describedAs(translationKey.description())
                        .finish();
            }
        };

        protected abstract PropertySpec get(PropertySpecService service, String name, String defaultTranslation);
    }

    private final long id;
    private final String defaultNameTranslation;
    private final String propertyName;
    private final String propertyDefaultTranslation;
    private final PropertyType propertyType;

    PPPConfigurationDeviceMessage(int id, String defaultNameTranslation, PropertyType propertyType, String propertyName, String propertyDefaultTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.propertyType = propertyType;
        this.propertyName = propertyName;
        this.propertyDefaultTranslation = propertyDefaultTranslation;
    }

    private String getNameResourceKey() {
        return PPPConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    private List<PropertySpec> getPropertySpecs(PropertySpecService service) {
        return Collections.singletonList(this.propertyType.get(service, this.propertyName, this.propertyDefaultTranslation));
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.PPP_PARAMETERS,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}