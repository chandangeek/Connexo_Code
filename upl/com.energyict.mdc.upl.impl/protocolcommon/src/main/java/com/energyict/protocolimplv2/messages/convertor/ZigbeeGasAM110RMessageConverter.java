package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PricingInformationMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

/**
 * Created by cisac on 8/12/2015.
 */
public class ZigbeeGasAM110RMessageConverter extends AbstractMessageConverter {

    public ZigbeeGasAM110RMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
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
                return new String(((DeviceMessageFile) messageAttribute).loadFileInByteArray(), Charset.forName("UTF-8"));   // We suppose the UserFile contains regular ASCII
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                //Tariff
                .put(messageSpec(PricingInformationMessage.SEND_NEW_TARIFF), new MultipleAttributeMessageEntry(RtuMessageConstant.TOU_SEND_NEW_TARIFF, RtuMessageConstant.TOU_TARIFF_USER_FILE))

                // Pricing Information
                .put(messageSpec(PricingInformationMessage.SEND_NEW_PRICE_MATRIX), new MultipleAttributeMessageEntry(RtuMessageConstant.SEND_NEW_PRICE_MATRIX, RtuMessageConstant.PRICE_MATRIX_USER_FILE))
                .put(messageSpec(PricingInformationMessage.SetStandingCharge), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_STANDING_CHARGE, RtuMessageConstant.STANDING_CHARGE))
                .put(messageSpec(PricingInformationMessage.SetStandingChargeAndActivationDate), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_STANDING_CHARGE, RtuMessageConstant.STANDING_CHARGE, RtuMessageConstant.ACTIVATION_DATE))
                .put(messageSpec(PricingInformationMessage.SET_CURRENCY), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CURRENCY, RtuMessageConstant.CURRENCY))
                .put(messageSpec(PricingInformationMessage.SET_CURRENCY_AND_ACTIVATION_DATE), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CURRENCY, RtuMessageConstant.CURRENCY, RtuMessageConstant.ACTIVATION_DATE))

                // Change of Supplier
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED))
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_AND_ACTIVATION_DATE), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE))
                //Change of Tenancy
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_TENANT, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED))
                .put(messageSpec(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT_AND_ACTIVATION_DATE), new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_TENANT, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE))

                // CV & CF information
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetCalorificValue), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CALORIFIC_VALUE, RtuMessageConstant.CALORIFIC_VALUE))
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetCalorificValueAndActivationDate), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CALORIFIC_VALUE, RtuMessageConstant.CALORIFIC_VALUE, RtuMessageConstant.ACTIVATION_DATE))
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetConversionFactor), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CONVERSION_FACTOR, RtuMessageConstant.CONVERSION_FACTOR))
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetConversionFactorAndActivationDate), new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CONVERSION_FACTOR, RtuMessageConstant.CONVERSION_FACTOR, RtuMessageConstant.ACTIVATION_DATE))

                // Firmware
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE), new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE, RtuMessageConstant.ACTIVATE_DATE))
                .build();
    }

}
