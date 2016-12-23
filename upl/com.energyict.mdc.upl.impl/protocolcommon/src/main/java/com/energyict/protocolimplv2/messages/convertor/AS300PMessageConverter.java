package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PricingInformationMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.Map;

/**
 * Created by cisac on 8/13/2015.
 */
public class AS300PMessageConverter extends AbstractMessageConverter {

    private final Extractor extractor;

    public AS300PMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.extractor = extractor;
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                .put(messageSpec(PricingInformationMessage.SEND_NEW_TARIFF), new MultipleAttributeMessageEntry(RtuMessageConstant.TOU_SEND_NEW_TARIFF, RtuMessageConstant.TOU_TARIFF_USER_FILE))

                // Pricing Information
                .put(messageSpec(PricingInformationMessage.SEND_NEW_PRICE_MATRIX), new MultipleAttributeMessageEntry(RtuMessageConstant.SEND_NEW_PRICE_MATRIX, RtuMessageConstant.PRICE_MATRIX_USER_FILE))
                .put(messageSpec(PricingInformationMessage.SetStandingCharge), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_STANDING_CHARGE, RtuMessageConstant.STANDING_CHARGE))
                .put(messageSpec(PricingInformationMessage.SetStandingChargeAndActivationDate), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_STANDING_CHARGE, RtuMessageConstant.STANDING_CHARGE, RtuMessageConstant.ACTIVATION_DATE))
                .put(messageSpec(PricingInformationMessage.SET_CURRENCY), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CURRENCY, RtuMessageConstant.CURRENCY))
                .put(messageSpec(PricingInformationMessage.SET_CURRENCY_AND_ACTIVATION_DATE), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CURRENCY, RtuMessageConstant.CURRENCY, RtuMessageConstant.ACTIVATION_DATE))

                // Change of Tenancy
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_TENANT, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED))
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT_AND_ACTIVATION_DATE), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_TENANT, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE))
                // Change of Supplier A+ and A-
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_IMPORT_ENERGY), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER_IMPORT_ENERGY, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED))
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_IMPORT_ENERGY_AND_ACTIVATION_DATE), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER_IMPORT_ENERGY, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE))
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_EXPORT_ENERGY), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER_EXPORT_ENERGY, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED))
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_EXPORT_ENERGY_AND_ACTIVATION_DATE), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER_EXPORT_ENERGY, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE))

                // Connect/disconnect
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new SimpleTagMessageEntry(RtuMessageConstant.DISCONNECT_CONTROL_RECONNECT))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new SimpleTagMessageEntry(RtuMessageConstant.DISCONNECT_CONTROL_DISCONNECT))
                .put(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_DISCONNECT_CONTROL_MODE, RtuMessageConstant.CONTROL_MODE))

                // Firmware
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE), new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE, RtuMessageConstant.ACTIVATE_DATE))

                //Security
                .put(messageSpec(ConfigurationChangeDeviceMessage.SET_ENGINEER_PIN), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_ENGINEER_PIN, RtuMessageConstant.ENGINEER_PIN, RtuMessageConstant.ENGINEER_PIN_TIMEOUT))
                .put(messageSpec(ConfigurationChangeDeviceMessage.SET_ENGINEER_PIN_AND_ACTIVATION_DATE), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_ENGINEER_PIN, RtuMessageConstant.ENGINEER_PIN, RtuMessageConstant.ENGINEER_PIN_TIMEOUT, RtuMessageConstant.ACTIVATION_DATE))
                .build();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.ConfigurationChangeActivationDate:
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
            case DeviceMessageConstants.PricingInformationActivationDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
            case DeviceMessageConstants.contractsXmlUserFileAttributeName:
                return this.extractor.contents((DeviceMessageFile) messageAttribute, Charset.forName("UTF-8"));   // We assume the UserFile contains regular ASCII
            case DeviceMessageConstants.engineerPinTimeout:
                return String.valueOf(Temporals.toSeconds((TemporalAmount) messageAttribute));
            default:
                return messageAttribute.toString();
        }
    }
}
