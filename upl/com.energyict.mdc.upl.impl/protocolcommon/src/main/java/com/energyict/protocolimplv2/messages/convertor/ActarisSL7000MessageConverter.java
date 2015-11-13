package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DemandResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.EnableOrDisableDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetEndOfDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetStartOfDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the legacy Actaris Sl7000 protocol.
 *
 *  @author sva
  * @since 245/10/13 - 11:57
 */
public class ActarisSL7000MessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // Battery
        registry.put(ConfigurationChangeDeviceMessage.ProgramBatteryExpiryDate, new MultipleAttributeMessageEntry("BatteryExpiry", "Date (dd/MM/yyyy)"));

         // Daylight saving
        registry.put(ClockDeviceMessage.EnableOrDisableDST, new EnableOrDisableDSTMessageEntry(enableDSTAttributeName));
        registry.put(ClockDeviceMessage.SetEndOfDST, new SetEndOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));
        registry.put(ClockDeviceMessage.SetStartOfDST, new SetStartOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));

        // Demand reset
        registry.put(DeviceActionMessage.BILLING_RESET, new DemandResetMessageEntry());

        // Time of use
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
    }

    public ActarisSL7000MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.ConfigurationChangeDate:
                return dateFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.enableDSTAttributeName:
                return ((Boolean) messageAttribute).booleanValue() ? "1" : "0";
            case DeviceMessageConstants.activityCalendarActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime()); //Millis since 1970
            case activityCalendarCodeTableAttributeName:
                Code codeTable = (Code) messageAttribute;
                return String.valueOf(codeTable.getId()) + TimeOfUseMessageEntry.SEPARATOR + encode(codeTable); //The ID and the XML representation of the code table, separated by a |
            default:
                return messageAttribute.toString();
        }
    }

    /**
     * Return an XML representation of the code table.
     * The activation date and calendar name are set to 0, because they were stored in different message attributes.
     */
    protected String encode(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw DataParseException.generalParseException(e);
        }
    }
}
