package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleInnerTagsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.SetDisplayMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CurrentRatioDenominatorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CurrentRatioNumeratorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayAutorized_EOB;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayBacklight;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayDate_Format;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayEOB_Confirm;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayEndOfTextString;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayExistence_Of_EndOfText;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayIdentification_Code;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayInternal_Identifier;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayLeadingZero;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayMessageAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode1;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode2;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Normal_Mode;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayNumber_Of_Decimal;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayNumber_Of_Display_Historical_Data;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayNumber_Of_Displayable_Digit;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayScaler;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplaySeparators_Display;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplaySequence_Indicator;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayString_EOB_Confirm;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayTimeOut_For_Set_Mode;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayTime_Format;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayTimeout_For_AltMode;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayTimeout_Load_Profile;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.Display_ButtonEmulation_By_Optical_Head;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.Display_Off_TimeOut;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.Display_On_TimeOut;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.Displaying_Of_LPMenus;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.VoltageRatioDenominatorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.VoltageRatioNumeratorAttributeName;
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

    public static final String VOLTAGE_AND_CURRENT_PARAMS = "VoltageAndCurrentParams";

    private TariffCalendarExtractor tariffCalendarExtractor;

    /**
     * Default constructor for at-runtime instantiation
     */
    public ACE6000MessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor tariffCalendarExtractor) {
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
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            TariffCalendar calender = (TariffCalendar) messageAttribute;
            return tariffCalendarExtractor.id(calender) + TimeOfUseMessageEntry.SEPARATOR + convertCodeTableToXML(calender, tariffCalendarExtractor); //The ID and the XML representation of the code table, separated by a |
        }
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME), new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName))
                .put(messageSpec(ActivityCalendarDeviceMessage.SELECTION_OF_12_LINES_IN_TOU_TABLE), new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName))
                //Display configuration
                .put(messageSpec(DisplayDeviceMessage.SET_DISPLAY_MESSAGE), new SetDisplayMessageEntry(DisplayMessageAttributeName))
                //EOB reset messages
                .put(messageSpec(DeviceActionMessage.DEMAND_RESET), new SimpleTagMessageEntry(RtuMessageConstant.DEMAND_RESET, false))
                .put(messageSpec(DisplayDeviceMessage.DISPLAY_GENERAL_PARAMETERS),
                    new MultipleInnerTagsMessageEntry(DisplayDeviceMessage.DISPLAY_GENERAL_PARAMETERS.toString(),
                        DisplayLeadingZero, DisplayBacklight,
                        DisplayEOB_Confirm, DisplayString_EOB_Confirm,
                        DisplaySeparators_Display, DisplayTime_Format,
                        DisplayDate_Format, DisplayTimeOut_For_Set_Mode,
                        Display_On_TimeOut, Display_Off_TimeOut,
                        DisplayNb_DisplayedHisto_Sets_Normal_Mode, DisplayExistence_Of_EndOfText,
                        DisplayEndOfTextString, DisplayNb_DisplayedHisto_Sets_Altmode1,
                        DisplayNb_DisplayedHisto_Sets_Altmode2, DisplayTimeout_For_AltMode,
                        DisplayAutorized_EOB, DisplayTimeout_Load_Profile,
                        Displaying_Of_LPMenus, Display_ButtonEmulation_By_Optical_Head))
                .put(messageSpec(DisplayDeviceMessage.DISPLAY_READOUT_TABLE_PARAMETERS),
                    new MultipleInnerTagsMessageEntry(DisplayDeviceMessage.DISPLAY_READOUT_TABLE_PARAMETERS.toString(),
                        DisplayInternal_Identifier, DisplaySequence_Indicator,
                        DisplayIdentification_Code, DisplayScaler,
                        DisplayNumber_Of_Decimal, DisplayNumber_Of_Display_Historical_Data,
                        DisplayNumber_Of_Displayable_Digit))

                .put(messageSpec(PowerConfigurationDeviceMessage.SetVoltageAndCurrentParameters),
                    new MultipleInnerTagsMessageEntry(VOLTAGE_AND_CURRENT_PARAMS,
                        VoltageRatioDenominatorAttributeName, VoltageRatioNumeratorAttributeName,
                        CurrentRatioDenominatorAttributeName, CurrentRatioNumeratorAttributeName))
                .build();
    }
}
