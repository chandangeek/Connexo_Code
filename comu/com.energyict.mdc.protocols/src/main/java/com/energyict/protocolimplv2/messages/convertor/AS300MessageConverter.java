package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
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

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the legacy IC AS300 protocol.
 *
 * @author sva
 * @since 28/10/13 - 14:22
 */

public class AS300MessageConverter extends AbstractMessageConverter {

    private static final String ActivationDate = "Activation date (dd/mm/yyyy hh:mm:ss) (optional)";

    /**
     * Default constructor for at-runtime instantiation
     */
    public AS300MessageConverter() {
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
            case DeviceMessageConstants.firmwareUpdateFileAttributeName:
                FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
                return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
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
        initializeRegistry(registry);
        return registry;
    }

    protected void initializeRegistry(Map<DeviceMessageId, MessageEntryCreator> registry) {
        // Activity calendar
        registry.put(DeviceMessageId.ACTIVITY_CALENDAR_READ, new SimpleTagMessageEntry("ReadActivityCalendar"));

        // Time of Use
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));

        // Change of Supplier
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER, new MultipleAttributeMessageEntry("Change_Of_Supplier", "Change_Of_Supplier_Name", "Change_Of_Supplier_ID", "Change_Of_Supplier_ActivationDate"));

        // Change of Tenancy
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_OF_TENANCY, new MultipleAttributeMessageEntry("Change_Of_Tenant", "Change_Of_Tenant_ActivationDate"));

        // Connect/disconnect
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new SimpleTagMessageEntry("DisconnectControlReconnect"));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new SimpleTagMessageEntry("DisconnectControlDisconnect"));

        // Display
        registry.put(DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS, new MultipleAttributeMessageEntry("TextToEmeterDisplay", "Message", "Duration of message", ActivationDate));
        registry.put(DeviceMessageId.DISPLAY_SET_MESSAGE_ON_IHD_WITH_OPTIONS, new MultipleAttributeMessageEntry("TextToInHomeDisplay", "Message", "Duration of message", ActivationDate));

        // Firmware
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE, new WebRTUFirmwareUpgradeWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateFileAttributeName));
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE, new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(DeviceMessageConstants.firmwareUpdateFileAttributeName, DeviceMessageConstants.firmwareUpdateActivationDateAttributeName));

        // Pricing Information
        registry.put(DeviceMessageId.PRICING_GET_INFORMATION, new SimpleTagMessageEntry("ReadPricePerUnit"));
        registry.put(DeviceMessageId.PRICING_SET_INFORMATION, new ConfigWithUserFileAndActivationDateMessageEntry(DeviceMessageConstants.PricingInformationUserFileAttributeName, DeviceMessageConstants.PricingInformationActivationDateAttributeName, "SetPricePerUnit"));
        registry.put(DeviceMessageId.PRICING_SET_STANDING_CHARGE, new MultipleAttributeMessageEntry("SetStandingCharge", "Standing charge", ActivationDate));
        registry.put(DeviceMessageId.PRICING_UPDATE_INFORMATION, new ConfigWithUserFileMessageEntry(DeviceMessageConstants.PricingInformationUserFileAttributeName, "Update_Pricing_Information"));
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