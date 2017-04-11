package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.SetDisplayMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayMessageAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dayOfMonth;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dayOfWeek;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.hour;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.month;

/**
 * Message converter for the ACE6000 protocol
 */
public class ACE6000MessageConverter extends AbstractMessageConverter {
    private static final String BILLING_RESET = "BillingReset";

    private TariffCalendarExtractor tariffCalendarExtractor;

    /**
     * Default constructor for at-runtime instantiation
     */
    public ACE6000MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor tariffCalendarExtractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.tariffCalendarExtractor = tariffCalendarExtractor;
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
            TariffCalendar calender = (TariffCalendar) messageAttribute;
            return String.valueOf(tariffCalendarExtractor.id(calender)) + TimeOfUseMessageEntry.SEPARATOR + convertCodeTableToXML(calender, tariffCalendarExtractor); //The ID and the XML representation of the code table, separated by a |
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap.of(
                messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND), new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName),
                messageSpec(ActivityCalendarDeviceMessage.SELECTION_OF_12_LINES_IN_TOU_TABLE), new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName),
                //Display configuration
                messageSpec(DisplayDeviceMessage.SET_DISPLAY_MESSAGE), new SetDisplayMessageEntry(DisplayMessageAttributeName),
                //EOB reset messages
                messageSpec(DeviceActionMessage.DEMAND_RESET), new SimpleTagMessageEntry(BILLING_RESET, false)

        );
    }
}
