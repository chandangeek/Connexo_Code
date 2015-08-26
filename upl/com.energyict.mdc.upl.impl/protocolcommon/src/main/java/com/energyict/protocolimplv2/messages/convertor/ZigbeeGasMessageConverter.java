package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
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

import javax.xml.parsers.ParserConfigurationException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the legacy IC ZigbeeGas protocol.
 *
 * @author sva
 * @since 25/10/13 - 9:35
 */

public class ZigbeeGasMessageConverter extends AbstractMessageConverter {

    private static final String ActivationDate = "Activation date (dd/mm/yyyy hh:mm:ss) (optional)";

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // Change of Supplier
        registry.put(ConfigurationChangeDeviceMessage.ChangeOfSupplier, new MultipleAttributeMessageEntry("Change_Of_Supplier", "Change_Of_Supplier_Name", "Change_Of_Supplier_ID", "Change_Of_Supplier_ActivationDate"));

        // Change of Tenancy
        registry.put(ConfigurationChangeDeviceMessage.ChangeOfTenancy, new MultipleAttributeMessageEntry("Change_Of_Tenant", "Change_Of_Tenant_ActivationDate"));

        // Connect/disconnect
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new SimpleTagMessageEntry("RemoteConnect"));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new SimpleTagMessageEntry("RemoteDisconnect"));

        // CV & CF information
        registry.put(ConfigurationChangeDeviceMessage.SetCalorificValueAndActivationDate, new MultipleAttributeMessageEntry("SetCalorificValueAndActivationDate", "Calorific value", ActivationDate));
        registry.put(ConfigurationChangeDeviceMessage.SetConversionFactorAndActivationDate, new MultipleAttributeMessageEntry("SetConversionFactorAndActivationDate", "Conversion factor", ActivationDate));

        // Display
        registry.put(DisplayDeviceMessage.SET_DISPLAY_MESSAGE_WITH_OPTIONS, new MultipleAttributeMessageEntry("TextToDisplay", "Message", "Duration of message", ActivationDate));

        // Firmware
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE,
                new WebRTUFirmwareUpgradeWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName));
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE,
                new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName, DeviceMessageConstants.firmwareUpdateActivationDateAttributeName));

        // Pricing Information
        registry.put(PricingInformationMessage.ReadPricingInformation, new SimpleTagMessageEntry("ReadPricePerUnit"));
        registry.put(PricingInformationMessage.SetPricingInformation, new ConfigWithUserFileAndActivationDateMessageEntry(DeviceMessageConstants.PricingInformationUserFileAttributeName, DeviceMessageConstants.PricingInformationActivationDateAttributeName ,"SetPricePerUnit"));
        registry.put(PricingInformationMessage.SetStandingChargeAndActivationDate, new MultipleAttributeMessageEntry("SetStandingChargeAndActivationDate", "Standing charge", ActivationDate));
        registry.put(PricingInformationMessage.UpdatePricingInformation, new ConfigWithUserFileMessageEntry(DeviceMessageConstants.PricingInformationUserFileAttributeName, "Update_Pricing_Information"));

        // TestMessage
        registry.put(AdvancedTestMessage.USERFILE_CONFIG, new MultipleAttributeMessageEntry("Test_Message", "Test_File"));

        // Time of Use
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
    }

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

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
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
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
    }
}
