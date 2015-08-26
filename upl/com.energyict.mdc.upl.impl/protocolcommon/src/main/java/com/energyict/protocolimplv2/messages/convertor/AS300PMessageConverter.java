package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cisac on 8/13/2015.
 */
public class AS300PMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    protected static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(PricingInformationMessage.SEND_NEW_TARIFF, new MultipleAttributeMessageEntry(RtuMessageConstant.TOU_SEND_NEW_TARIFF, RtuMessageConstant.TOU_TARIFF_USER_FILE));

        // Pricing Information
        registry.put(PricingInformationMessage.SEND_NEW_PRICE_MATRIX, new MultipleAttributeMessageEntry(RtuMessageConstant.SEND_NEW_PRICE_MATRIX, RtuMessageConstant.PRICE_MATRIX_USER_FILE));
        registry.put(PricingInformationMessage.SetStandingCharge, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_STANDING_CHARGE, RtuMessageConstant.STANDING_CHARGE));
        registry.put(PricingInformationMessage.SetStandingChargeAndActivationDate, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_STANDING_CHARGE, RtuMessageConstant.STANDING_CHARGE, RtuMessageConstant.ACTIVATION_DATE));
        registry.put(PricingInformationMessage.SET_CURRENCY, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CURRENCY, RtuMessageConstant.CURRENCY));
        registry.put(PricingInformationMessage.SET_CURRENCY_AND_ACTIVATION_DATE, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_CURRENCY, RtuMessageConstant.CURRENCY, RtuMessageConstant.ACTIVATION_DATE));

        // Change of Tenancy
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_TENANT, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED));
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT_AND_ACTIVATION_DATE, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_TENANT, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE));
        // Change of Supplier A+ and A-
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_IMPORT_ENERGY, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER_IMPORT_ENERGY, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED));
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_IMPORT_ENERGY_AND_ACTIVATION_DATE, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER_IMPORT_ENERGY, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE));
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_EXPORT_ENERGY, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER_EXPORT_ENERGY, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED));
        registry.put(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_EXPORT_ENERGY_AND_ACTIVATION_DATE, new MultipleAttributeMessageEntry(RtuMessageConstant.CHANGE_OF_SUPPLIER_EXPORT_ENERGY, RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED, RtuMessageConstant.ACTIVATION_DATE));

        // Connect/disconnect
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new SimpleTagMessageEntry(RtuMessageConstant.DISCONNECT_CONTROL_RECONNECT));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new SimpleTagMessageEntry(RtuMessageConstant.DISCONNECT_CONTROL_DISCONNECT));
        registry.put(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_DISCONNECT_CONTROL_MODE, RtuMessageConstant.CONTROL_MODE));

        // Firmware
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE,
                new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE));
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE,
                new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE, RtuMessageConstant.ACTIVATE_DATE));

        //Security
        registry.put(ConfigurationChangeDeviceMessage.SET_ENGINEER_PIN, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_ENGINEER_PIN, RtuMessageConstant.ENGINEER_PIN, RtuMessageConstant.ENGINEER_PIN_TIMEOUT));
        registry.put(ConfigurationChangeDeviceMessage.SET_ENGINEER_PIN_AND_ACTIVATION_DATE, new MultipleAttributeMessageEntry(RtuMessageConstant.SET_ENGINEER_PIN, RtuMessageConstant.ENGINEER_PIN, RtuMessageConstant.ENGINEER_PIN_TIMEOUT, RtuMessageConstant.ACTIVATION_DATE));

    }


    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
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
            case DeviceMessageConstants.engineerPinTimeout:
                return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
            default:
                return messageAttribute.toString();
        }
    }
}
