package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DemandResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.EnableOrDisableDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetEndOfDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetStartOfDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;

import java.math.BigDecimal;
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
 * Represents a MessageConverter for the smart ZMD protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SmartZmdMessageConverter extends AbstractMessageConverter {

    /**
     * Default constructor for at-runtime instantiation
     */
    public SmartZmdMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(enableDSTAttributeName)) {
            return ((Boolean) messageAttribute) ? "1" : "0";     //1: true, 0: false
        } else if (propertySpec.getName().equals(month)
                || propertySpec.getName().equals(dayOfMonth)
                || propertySpec.getName().equals(dayOfWeek)
                || propertySpec.getName().equals(hour)) {
            return String.valueOf(((BigDecimal) messageAttribute).intValue());
        } else if (propertySpec.getName().equals(activityCalendarNameAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime()); //Millis since 1970
        } else if (propertySpec.getName().equals(activityCalendarAttributeName)) {
            Calendar codeTable = (Calendar) messageAttribute;
            return String.valueOf(codeTable.getId()) + TimeOfUseMessageEntry.SEPARATOR + convertCodeTableToXML(codeTable); //The ID and the XML representation of the code table, separated by a |
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // dst related
        registry.put(DeviceMessageId.CLOCK_ENABLE_OR_DISABLE_DST, new EnableOrDisableDSTMessageEntry(enableDSTAttributeName));
        registry.put(DeviceMessageId.CLOCK_SET_END_OF_DST, new SetEndOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));
        registry.put(DeviceMessageId.CLOCK_SET_START_OF_DST, new SetStartOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));

        //Code table related
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarAttributeName));

        // reset messages
        registry.put(DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, new DemandResetMessageEntry());
        return registry;
    }
}