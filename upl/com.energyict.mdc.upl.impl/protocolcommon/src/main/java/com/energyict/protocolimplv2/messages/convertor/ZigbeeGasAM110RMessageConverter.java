package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cisac on 8/12/2015.
 */
public class ZigbeeGasAM110RMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {

        //Tariff
        registry.put(PricingInformationMessage.SEND_NEW_TARIFF, new MultipleAttributeMessageEntry(RtuMessageConstant.TOU_SEND_NEW_TARIFF, RtuMessageConstant.TOU_TARIFF_USER_FILE));

        // Pricing Information
        registry.put(PricingInformationMessage.SEND_NEW_PRICE_MATRIX, new MultipleAttributeMessageEntry(RtuMessageConstant.SEND_NEW_PRICE_MATRIX, RtuMessageConstant.PRICE_MATRIX_USER_FILE));
        registry.put(PricingInformationMessage.SetStandingCharge, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_STANDING_CHARGE, RtuMessageConstant.STANDING_CHARGE));
        registry.put(PricingInformationMessage.SetStandingChargeAndActivationDate, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_STANDING_CHARGE, RtuMessageConstant.STANDING_CHARGE, RtuMessageConstant.ACTIVATION_DATE));
        registry.put(PricingInformationMessage.SET_CURRENCY, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CURRENCY, RtuMessageConstant.CURRENCY));
        registry.put(PricingInformationMessage.SET_CURRENCY_AND_ACTIVATION_DATE, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CURRENCY, RtuMessageConstant.CURRENCY, RtuMessageConstant.ACTIVATION_DATE));

        // Change of Supplier
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED));
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_AND_ACTIVATION_DATE, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE));
        // Change of Tenancy
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_TENANT, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED));
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT_AND_ACTIVATION_DATE, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_TENANT, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE));

        // CV & CF information
        registry.put(ConfigurationChangeDeviceMessage.SetCalorificValue, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CALORIFIC_VALUE, RtuMessageConstant.CALORIFIC_VALUE));
        registry.put(ConfigurationChangeDeviceMessage.SetCalorificValueAndActivationDate, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CALORIFIC_VALUE, RtuMessageConstant.CALORIFIC_VALUE, RtuMessageConstant.ACTIVATION_DATE));
        registry.put(ConfigurationChangeDeviceMessage.SetConversionFactor, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CONVERSION_FACTOR, RtuMessageConstant.CONVERSION_FACTOR));
        registry.put(ConfigurationChangeDeviceMessage.SetConversionFactorAndActivationDate, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CONVERSION_FACTOR, RtuMessageConstant.CONVERSION_FACTOR, RtuMessageConstant.ACTIVATION_DATE));

        // Firmware
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE,
                new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE));
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE,
                new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE, RtuMessageConstant.ACTIVATE_DATE));

    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public ZigbeeGasAM110RMessageConverter() {
        super();
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
                return new String(((UserFile) messageAttribute).loadFileInByteArray(), Charset.forName("UTF-8"));   // We suppose the UserFile contains regular ASCII
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

}
