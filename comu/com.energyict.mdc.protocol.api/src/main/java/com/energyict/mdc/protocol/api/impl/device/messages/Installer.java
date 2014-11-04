package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Executes the required installation steps on behalf of the {@link DeviceMessageSpecificationServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-11 (14:40)
 */
public class Installer {

    private final Thesaurus thesaurus;

    public Installer(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    public void install() {
        this.createTranslations();
    }

    private void createTranslations() {
        this.thesaurus.addTranslations(
            Arrays.stream(DeviceMessageCategories.values()).
                map(this::toTranslation).
                collect(Collectors.toList()));
        this.addDeviceMessageSpecEnumTranslations(ActivityCalendarDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(AdvancedTestMessage.values());
        this.addDeviceMessageSpecEnumTranslations(AlarmConfigurationMessage.values());
        this.addDeviceMessageSpecEnumTranslations(ChannelConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(ClockDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(ConfigurationChangeDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(ContactorDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(DeviceActionMessage.values());
        this.addDeviceMessageSpecEnumTranslations(DisplayDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(DLMSConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(EIWebConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(FirmwareDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(GeneralDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(LoadBalanceDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(LoadProfileMessage.values());
        this.addDeviceMessageSpecEnumTranslations(LogBookDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(MailConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(MBusConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(MBusSetupDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(ModbusConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(ModemConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(NetworkConnectivityMessage.values());
        this.addDeviceMessageSpecEnumTranslations(OpusConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(PeakShaverConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(PLCConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(PowerConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(PPPConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(PrepaidConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(PricingInformationMessage.values());
        this.addDeviceMessageSpecEnumTranslations(SecurityMessage.values());
        this.addDeviceMessageSpecEnumTranslations(SMSConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(TotalizersConfigurationDeviceMessage.values());
        this.addDeviceMessageSpecEnumTranslations(ZigBeeConfigurationDeviceMessage.values());
    }

    private Translation toTranslation(DeviceMessageCategories deviceMessageCategory) {
        return this.toTranslation(
                SimpleNlsKey.key(DeviceMessageSpecificationService.COMPONENT_NAME, Layer.DOMAIN, deviceMessageCategory.getNameResourceKey()),
                Locale.ENGLISH,
                deviceMessageCategory.defaultTranslation());
    }

    private void addDeviceMessageSpecEnumTranslations(DeviceMessageSpecEnum... values) {
        this.thesaurus.addTranslations(
                Arrays.stream(values).
                        map(this::toTranslation).
                        collect(Collectors.toList()));
    }

    private Translation toTranslation(DeviceMessageSpecEnum deviceMessageSpecEnum) {
        return this.toTranslation(
                SimpleNlsKey.key(DeviceMessageSpecificationService.COMPONENT_NAME, Layer.DOMAIN, deviceMessageSpecEnum.getNameResourceKey()),
                Locale.ENGLISH,
                deviceMessageSpecEnum.defaultTranslation());
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return SimpleTranslation.translation(nlsKey, locale, translation);
    }

}