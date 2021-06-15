package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.EnableOrDisableDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetEndOfDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetStartOfDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.NumberMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CurrentRatioNumeratorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.VoltageRatioNumeratorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dayOfMonth;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dayOfWeek;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.hour;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.month;

/**
 * Represents a MessageConverter for the smart ZMD protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SmartZmdMessageConverter extends AbstractMessageConverter {
    private static final String BILLING_RESET = "BillingReset";

    private final TariffCalendarExtractor tariffCalendarExtractor;

    /**
     * Default constructor for at-runtime instantiation
     */
    public SmartZmdMessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor tariffCalendarExtractor) {
        super(propertySpecService, nlsService, converter);
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
        } else if (propertySpec.getName().equals(activityCalendarAttributeName)) {
            TariffCalendar calender = (TariffCalendar) messageAttribute;
            return this.tariffCalendarExtractor.id(calender) + TimeOfUseMessageEntry.SEPARATOR + convertCodeTableToXML(calender, this.tariffCalendarExtractor); //The ID and the XML representation of the code table, separated by a |
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // dst related
                .put(messageSpec(ClockDeviceMessage.EnableOrDisableDST), new EnableOrDisableDSTMessageEntry(enableDSTAttributeName))
                .put(messageSpec(ClockDeviceMessage.SetEndOfDST), new SetEndOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour))
                .put(messageSpec(ClockDeviceMessage.SetStartOfDST), new SetStartOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour))

                //Code table related
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME), new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarAttributeName))
                .put(messageSpec(ActivityCalendarDeviceMessage.SELECTION_OF_12_LINES_IN_TOU_TABLE), new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarAttributeName))

                .put(messageSpec(DisplayDeviceMessage.SET_DISPLAY_MESSAGE), new SetDisplayMessageEntry(DeviceMessageConstants.DisplayMessageAttributeName))
                // Voltage/Current parameters
                .put(messageSpec(PowerConfigurationDeviceMessage.SetVoltageAndCurrentParameters), new NumberMessageEntry(VoltageRatioNumeratorAttributeName))
                .put(messageSpec(PowerConfigurationDeviceMessage.SetVoltageAndCurrentParameters), new NumberMessageEntry(CurrentRatioNumeratorAttributeName))
                // reset messages
                .put(messageSpec(DeviceActionMessage.DEMAND_RESET), new SimpleTagMessageEntry(BILLING_RESET, false))
                .build();
    }
}