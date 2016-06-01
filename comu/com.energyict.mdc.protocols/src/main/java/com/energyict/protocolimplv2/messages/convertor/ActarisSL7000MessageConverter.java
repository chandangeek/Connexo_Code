package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
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

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.dayOfMonth;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.dayOfWeek;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.enableDSTAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.hour;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.month;

/**
 * Represents a MessageConverter for the legacy Actaris Sl7000 protocol.
 *
 *  @author sva
  * @since 245/10/13 - 11:57
 */
public class ActarisSL7000MessageConverter extends AbstractMessageConverter {

    public ActarisSL7000MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // Battery
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_PROGRAM_BATTERY_EXPIRY_DATE, new MultipleAttributeMessageEntry("BatteryExpiry", "Date (dd/MM/yyyy)"));

        // Daylight saving
        registry.put(DeviceMessageId.CLOCK_ENABLE_OR_DISABLE_DST, new EnableOrDisableDSTMessageEntry(enableDSTAttributeName));
        registry.put(DeviceMessageId.CLOCK_SET_END_OF_DST, new SetEndOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));
        registry.put(DeviceMessageId.CLOCK_SET_START_OF_DST, new SetStartOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));

        // Demand reset
        registry.put(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET, new DemandResetMessageEntry());

        // Time of use
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarAttributeName));
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
            case activityCalendarAttributeName:
                Calendar codeTable = (Calendar) messageAttribute;
                return String.valueOf(codeTable.getId()) + TimeOfUseMessageEntry.SEPARATOR + encode(codeTable); //The ID and the XML representation of the code table, separated by a |
            default:
                return messageAttribute.toString();
        }
    }

    /**
     * Return an XML representation of the code table.
     * The activation date and calendar name are set to 0, because they were stored in different message attributes.
     */
    private String encode(Calendar messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
    }

}