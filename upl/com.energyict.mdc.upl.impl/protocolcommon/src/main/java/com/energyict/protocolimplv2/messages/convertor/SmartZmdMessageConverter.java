package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;

import java.math.BigDecimal;
import java.util.*;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the smart ZMD protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SmartZmdMessageConverter extends AbstractMessageConverter {
    private static final String BILLING_RESET = "BillingReset";

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {

        // dst related
        registry.put(ClockDeviceMessage.EnableOrDisableDST, new EnableOrDisableDSTMessageEntry(enableDSTAttributeName));
        registry.put(ClockDeviceMessage.SetEndOfDST, new SetEndOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));
        registry.put(ClockDeviceMessage.SetStartOfDST, new SetStartOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));

        //Code table related
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(ActivityCalendarDeviceMessage.SELECTION_OF_12_LINES_IN_TOU_TABLE, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
        //Display configuration
        registry.put(DisplayDeviceMessage.SET_DISPLAY_MESSAGE,new SetDisplayMessageEntry(DisplayMessageAttributeName));
        //EOB reset messages
        registry.put(DeviceActionMessage.DEMAND_RESET, new SimpleTagMessageEntry(BILLING_RESET, false));
    }

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
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            Code codeTable = (Code) messageAttribute;
            return String.valueOf(codeTable.getId()) + TimeOfUseMessageEntry.SEPARATOR + convertCodeTableToXML(codeTable); //The ID and the XML representation of the code table, separated by a |
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}