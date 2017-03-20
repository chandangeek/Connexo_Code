package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.SetDisplayMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;

/**
 * Message converter for the ACE6000 protocol
 */
public class ACE6000MessageConverter extends AbstractMessageConverter{
    private static final String BILLING_RESET = "BillingReset";
    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
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
    public ACE6000MessageConverter() {
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
