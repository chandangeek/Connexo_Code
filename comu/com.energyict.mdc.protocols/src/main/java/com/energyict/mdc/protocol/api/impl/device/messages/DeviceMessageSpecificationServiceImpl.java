package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.adapters.ConnexoTranslationKeyAdapter;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLDeviceMessageCategoryAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLDeviceMessageSpecAdapter;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.upl.properties.Converter;

import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageCategories;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;
import com.energyict.protocolimplv2.messages.UmiwanDeviceMessage;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-11 (13:33)
 */
@Component(name = "com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService", service = {DeviceMessageSpecificationService.class, TranslationKeyProvider.class, MessageSeedProvider.class}, property = "name=" + DeviceMessageSpecificationService.COMPONENT_NAME, immediate = true)
public class DeviceMessageSpecificationServiceImpl implements DeviceMessageSpecificationService, TranslationKeyProvider, MessageSeedProvider {

    private volatile Converter converter;
    private volatile com.energyict.mdc.upl.nls.NlsService uplNlsService;
    private volatile com.energyict.mdc.upl.properties.PropertySpecService uplPropertySpecService;

    // For OSGi
    @SuppressWarnings("unused")
    public DeviceMessageSpecificationServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public DeviceMessageSpecificationServiceImpl(Converter converter, com.energyict.mdc.upl.nls.NlsService uplNlsService, com.energyict.mdc.upl.properties.PropertySpecService uplPropertySpecService) {
        super();
        this.setConverter(converter);
        this.setUplNlsService(uplNlsService);
        this.setUplPropertySpecService(uplPropertySpecService);
    }

    @Activate
    public void activate() {
        //
    }

    @Reference
    public void setUplNlsService(com.energyict.mdc.upl.nls.NlsService uplNlsService) {
        this.uplNlsService = uplNlsService;
    }

    @Reference
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Reference
    public void setUplPropertySpecService(com.energyict.mdc.upl.properties.PropertySpecService uplPropertySpecService) {
        this.uplPropertySpecService = uplPropertySpecService;
    }

    private EnumSet<DeviceMessageCategories> filteredCategories() {
        return EnumSet.of(
                DeviceMessageCategories.ACTIVITY_CALENDAR,
                DeviceMessageCategories.FIRMWARE,
                DeviceMessageCategories.ADVANCED_TEST,
                DeviceMessageCategories.CHANNEL_CONFIGURATION,
                DeviceMessageCategories.EIWEB_PARAMETERS);
    }

    @Override
    public List<DeviceMessageCategory> filteredCategoriesForUserSelection() {
        EnumSet<DeviceMessageCategories> excluded = this.filteredCategories();
        EnumSet<DeviceMessageCategories> included = EnumSet.complementOf(excluded);

        return included.stream()
                .map(supplier -> supplier.get(uplPropertySpecService, uplNlsService, converter))
                .map(UPLDeviceMessageCategoryAdapter::adaptTo).collect(Collectors.toList());
    }

    @Override
    public List<DeviceMessageCategory> filteredCategoriesForComTaskDefinition() {
        EnumSet<DeviceMessageCategories> excluded = this.filteredCategories();
        EnumSet<DeviceMessageCategories> included = EnumSet.complementOf(excluded);
        included.add(DeviceMessageCategories.ACTIVITY_CALENDAR);

        return included.stream()
                .map(supplier -> supplier.get(uplPropertySpecService, uplNlsService, converter))
                .map(UPLDeviceMessageCategoryAdapter::adaptTo).collect(Collectors.toList());
    }

    @Override
    public List<DeviceMessageCategory> allCategories() {
        return Stream.of(DeviceMessageCategories.values())
                .map(supplier -> supplier.get(uplPropertySpecService, uplNlsService, converter))
                .map(UPLDeviceMessageCategoryAdapter::adaptTo).collect(Collectors.toList());
    }

    @Override
    public Optional<DeviceMessageSpec> findMessageSpecById(long messageSpecIdDbValue) {
        return this.allMessageSpecs().stream()
                .filter(messageSpec -> messageSpecIdDbValue == messageSpec.getId())
                .findFirst()
                .map(UPLDeviceMessageSpecAdapter::adaptTo);
    }

    private List<com.energyict.mdc.upl.messages.DeviceMessageSpec> allMessageSpecs() {
        return Stream
                .of(DeviceMessageCategories.values())
                .map(supplier -> supplier.get(uplPropertySpecService, uplNlsService, converter))
                .map(com.energyict.mdc.upl.messages.DeviceMessageCategory::getMessageSpecifications)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOptionFor(DeviceMessageId deviceMessageId) {
        for (com.energyict.protocolimplv2.messages.FirmwareDeviceMessage deviceMessageProvider : com.energyict.protocolimplv2.messages.FirmwareDeviceMessage.values()) {
            if (deviceMessageProvider.id() == deviceMessageId.dbValue()) {
                return deviceMessageProvider.getProtocolSupportedFirmwareOption();
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean needsImageIdentifierAtFirmwareUpload(DeviceMessageId deviceMessageId) {
        return Stream.of(com.energyict.protocolimplv2.messages.FirmwareDeviceMessage.values())
                .filter(fdm -> fdm.id() == deviceMessageId.dbValue())
                .anyMatch(fdm -> fdm.getFirmwareIdentifierPropertySpec(uplPropertySpecService).isPresent());
    }

    @Override
    public boolean canResumeFirmwareUpload(DeviceMessageId deviceMessageId) {
        return Stream.of(com.energyict.protocolimplv2.messages.FirmwareDeviceMessage.values())
                .filter(fdm -> fdm.id() == deviceMessageId.dbValue())
                .anyMatch(fdm -> fdm.getResumeFirmwareUploadPropertySpec(uplPropertySpecService).isPresent());
    }

    @Override
    public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOptionsFor(DeviceMessageId deviceMessageId) {
        for (ActivityCalendarDeviceMessage deviceMessageProvider : ActivityCalendarDeviceMessage.values()) {
            com.energyict.mdc.upl.messages.DeviceMessageSpec spec = deviceMessageProvider.get(uplPropertySpecService, uplNlsService, converter);
            if (spec.getId() == deviceMessageId.dbValue()) {
                return deviceMessageProvider.getProtocolSupportedCalendarOption();
            }
        }
        return Optional.empty();
    }

    @Override
    public DeviceMessageCategory getFirmwareCategory() {
        return UPLDeviceMessageCategoryAdapter.adaptTo(DeviceMessageCategories.FIRMWARE.get(uplPropertySpecService, uplNlsService, converter));
    }

    @Override
    public DeviceMessageCategory getSecurityCategory() {
        return UPLDeviceMessageCategoryAdapter.adaptTo(DeviceMessageCategories.SECURITY.get(uplPropertySpecService, uplNlsService, converter));
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        //TODO all possible issues and exceptions from the protocol impls used to be here. Should they still be here? (they are in fact not related to DeviceMessageSpecs).
        //TODO or should these be somewhere else?
        //TODO anyway, let's wait for the refactoring of exception messages in 9.1
        return Collections.emptyList();
    }

    /**
     * Get the translation keys for all categories, all messages and all their message attributes.
     */
    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();

        //All categories
        Stream.of(DeviceMessageCategories.values())
                .map(supplier -> supplier.get(uplPropertySpecService, uplNlsService, converter))
                .map(category -> new TranslationKeyImpl(category.getNameResourceKey(), category.getName()))
                .forEach(keys::add);

        //All messages (and their attributes), of all categories
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.AdvancedTestMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.AlarmConfigurationMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.ChannelConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.ClockDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.ContactorDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.DeviceActionMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.DisplayDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.DLMSConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.EIWebConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.FirewallConfigurationMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.FirmwareDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.GeneralDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.LoadProfileMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.LogBookDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.LoggingConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.MailConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.ModbusConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.ModemConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.NetworkConnectivityMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.OpusConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.OutputConfigurationMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.PeakShaverConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.PPPConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.PrepaidConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.PricingInformationMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.PublicLightingDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.SecurityMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.SMSConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.TotalizersConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.UplinkConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.WavenisDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.ZigBeeConfigurationDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.CreditDeviceMessage.values());
        addAllTranslationKeys(keys, com.energyict.protocolimplv2.messages.ChargeDeviceMessage.values());
        addAllTranslationKeys(keys, UmiwanDeviceMessage.values());

        return keys;
    }

    /**
     * Add the translation keys of the given {@link com.energyict.mdc.upl.messages.DeviceMessageSpec}s and their {@link com.energyict.mdc.upl.properties.PropertySpec}s
     */
    private void addAllTranslationKeys(List<TranslationKey> keys, DeviceMessageSpecSupplier[] values) {
        Stream.of(values)
                .map(deviceMessageSpecSupplier -> deviceMessageSpecSupplier.get(uplPropertySpecService, uplNlsService, converter))
                .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getNameTranslationKey)
                .map(ConnexoTranslationKeyAdapter::adaptTo)
                .forEach(keys::add);

        Stream.of(values)
                .map(deviceMessageSpecSupplier -> deviceMessageSpecSupplier.get(uplPropertySpecService, uplNlsService, converter))
                .flatMap(deviceMessageSpec -> deviceMessageSpec.getPropertySpecs().stream())
                .map(propertySpec -> new TranslationKeyImpl(propertySpec.getName(), propertySpec.getDisplayName()))
                .forEach(keys::add);
    }

    private class TranslationKeyImpl implements TranslationKey {
        private final String key;
        private final String defaultFormat;

        public TranslationKeyImpl(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }
}
