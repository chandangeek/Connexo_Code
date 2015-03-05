package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.ConfigWithUserFileAndActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.ConfigWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import javax.xml.parsers.ParserConfigurationException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarNameAttributeName;

/**
 * Represents a MessageConverter for the legacy IC ZigbeeGas protocol.
 *
 * @author sva
 * @since 25/10/13 - 9:35
 */

public class ZigbeeGasMessageConverter extends AbstractMessageConverter {

    private static final String ActivationDate = "Activation date (dd/mm/yyyy hh:mm:ss) (optional)";

    /**
     * Default constructor for at-runtime instantiation
     */
    public ZigbeeGasMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.PricingInformationUserFileAttributeName:
                return new String(((UserFile) messageAttribute).loadFileInByteArray(), Charset.forName("UTF-8"));   // We suppose the UserFile contains regular ASCII
            case DeviceMessageConstants.DisplayMessageActivationDate:
            case DeviceMessageConstants.ConfigurationChangeActivationDate:
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
            case DeviceMessageConstants.PricingInformationActivationDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.UserFileConfigAttributeName:
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                return Integer.toString(((UserFile) messageAttribute).getId());
            case DeviceMessageConstants.activityCalendarActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime()); //Millis since 1970
            case activityCalendarCodeTableAttributeName:
                Code codeTable = (Code) messageAttribute;
                return String.valueOf(codeTable.getId()) + TimeOfUseMessageEntry.SEPARATOR + encode(codeTable); //The ID and the XML representation of the code table, separated by a |
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // Change of Supplier
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER, new MultipleAttributeMessageEntry("Change_Of_Supplier", "Change_Of_Supplier_Name", "Change_Of_Supplier_ID", "Change_Of_Supplier_ActivationDate"));

        // Change of Tenancy
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_OF_TENANCY, new MultipleAttributeMessageEntry("Change_Of_Tenant", "Change_Of_Tenant_ActivationDate"));

        // Connect/disconnect
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new SimpleTagMessageEntry("RemoteConnect"));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new SimpleTagMessageEntry("RemoteDisconnect"));

        // CV & CF information
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_CALORIFIC_VALUE, new MultipleAttributeMessageEntry("SetCalorificValue", "Calorific value", ActivationDate));
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_CONVERSION_FACTOR, new MultipleAttributeMessageEntry("SetConversionFactor", "Conversion factor", ActivationDate));

        // Display
        registry.put(DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS, new MultipleAttributeMessageEntry("TextToDisplay", "Message", "Duration of message", ActivationDate));

        // Firmware
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
                new WebRTUFirmwareUpgradeWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName));
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE,
                new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName, DeviceMessageConstants.firmwareUpdateActivationDateAttributeName));

        // Pricing Information
        registry.put(DeviceMessageId.PRICING_GET_INFORMATION, new SimpleTagMessageEntry("ReadPricePerUnit"));
        registry.put(DeviceMessageId.PRICING_SET_INFORMATION, new ConfigWithUserFileAndActivationDateMessageEntry(DeviceMessageConstants.PricingInformationUserFileAttributeName, DeviceMessageConstants.PricingInformationActivationDateAttributeName ,"SetPricePerUnit"));
        registry.put(DeviceMessageId.PRICING_SET_STANDING_CHARGE, new MultipleAttributeMessageEntry("SetStandingCharge", "Standing charge", ActivationDate));
        registry.put(DeviceMessageId.PRICING_UPDATE_INFORMATION, new ConfigWithUserFileMessageEntry(DeviceMessageConstants.PricingInformationUserFileAttributeName, "Update_Pricing_Information"));

        // TestMessage
        registry.put(DeviceMessageId.ADVANCED_TEST_USERFILE_CONFIG, new MultipleAttributeMessageEntry("Test_Message", "Test_File"));

        // Time of Use
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
        return registry;
    }

    /**
     * Return an XML representation of the code table.
     * The activation date and calendar name are set to 0, because they were stored in different message attributes.
     */
    protected String encode(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
    }

}