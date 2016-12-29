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
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PricingInformationMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.ConfigWithUserFileAndActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.ConfigWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;
import com.google.common.collect.ImmutableMap;

import javax.xml.parsers.ParserConfigurationException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;

/**
 * Represents a MessageConverter for the legacy IC AS300 protocol.
 *
 * @author sva
 * @since 28/10/13 - 14:22
 */

public class AS300MessageConverter extends AbstractMessageConverter {

    private static final String ActivationDate = "Activation date (dd/mm/yyyy hh:mm:ss) (optional)";

    public AS300MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.PricingInformationUserFileAttributeName:
                return this.getExtractor().contents((DeviceMessageFile) messageAttribute, Charset.forName("UTF-8"));   // We suppose the UserFile contains regular ASCII
            case DeviceMessageConstants.DisplayMessageActivationDate:
            case DeviceMessageConstants.ConfigurationChangeActivationDate:
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
            case DeviceMessageConstants.PricingInformationActivationDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                return this.getExtractor().id((DeviceMessageFile) messageAttribute);
            case DeviceMessageConstants.activityCalendarActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime()); //Millis since 1970
            case activityCalendarCodeTableAttributeName:
                TariffCalendar calender = (TariffCalendar) messageAttribute;
                return this.getExtractor().id(calender) + TimeOfUseMessageEntry.SEPARATOR + encode(calender); //The ID and the XML representation of the code table, separated by a |
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // Activity calendar
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDAR_READ), new SimpleTagMessageEntry("ReadActivityCalendar"))

                // Change of Supplier
                .put(messageSpec(ConfigurationChangeDeviceMessage.ChangeOfSupplier), new MultipleAttributeMessageEntry("Change_Of_Supplier", "Change_Of_Supplier_Name", "Change_Of_Supplier_ID", "Change_Of_Supplier_ActivationDate"))

                // Change of Tenancy
                .put(messageSpec(ConfigurationChangeDeviceMessage.ChangeOfTenancy), new MultipleAttributeMessageEntry("Change_Of_Tenant", "Change_Of_Tenant_ActivationDate"))

                // Connect/disconnect
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new SimpleTagMessageEntry("DisconnectControlReconnect"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new SimpleTagMessageEntry("DisconnectControlDisconnect"))

                // Display
                .put(messageSpec(DisplayDeviceMessage.SET_DISPLAY_MESSAGE_WITH_OPTIONS), new MultipleAttributeMessageEntry("TextToEmeterDisplay", "Message", "Duration of message", ActivationDate))
                .put(messageSpec(DisplayDeviceMessage.SET_DISPLAY_MESSAGE_ON_IHD_WITH_OPTIONS), new MultipleAttributeMessageEntry("TextToInHomeDisplay", "Message", "Duration of message", ActivationDate))

                // Firmware
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new WebRTUFirmwareUpgradeWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE), new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName, DeviceMessageConstants.firmwareUpdateActivationDateAttributeName))

                // Pricing Information
                .put(messageSpec(PricingInformationMessage.ReadPricingInformation), new SimpleTagMessageEntry("ReadPricePerUnit"))
                .put(messageSpec(PricingInformationMessage.SetPricingInformation), new ConfigWithUserFileAndActivationDateMessageEntry(DeviceMessageConstants.PricingInformationUserFileAttributeName, DeviceMessageConstants.PricingInformationActivationDateAttributeName ,"SetPricePerUnit"))
                .put(messageSpec(PricingInformationMessage.SetStandingChargeAndActivationDate), new MultipleAttributeMessageEntry("SetStandingCharge", "Standing charge", ActivationDate))
                .put(messageSpec(PricingInformationMessage.UpdatePricingInformation), new ConfigWithUserFileMessageEntry(DeviceMessageConstants.PricingInformationUserFileAttributeName, "Update_Pricing_Information"))

                // Time of Use
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME), new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName))
                .build();
    }

    /**
     * Return an XML representation of the code table.
     * The activation date and calendar name are set to 0, because they were stored in different message attributes.
     */
    protected String encode(TariffCalendar messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, this.getExtractor(), 0, "0");
        } catch (ParserConfigurationException e) {
            throw DataParseException.generalParseException(e);
        }
    }
}
