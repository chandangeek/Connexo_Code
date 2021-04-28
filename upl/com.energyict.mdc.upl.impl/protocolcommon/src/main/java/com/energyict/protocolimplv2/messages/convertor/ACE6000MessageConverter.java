package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleInnerTagsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.SetDisplayMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.TimeOfUseMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Message converter for the ACE6000 protocol
 */
public class ACE6000MessageConverter extends AbstractMessageConverter {

    private static final String BILLING_RESET = "BillingReset";
    public static final String VOLTAGE_AND_CURRENT_PARAMS = "VoltageAndCurrentParams";

    private TariffCalendarExtractor tariffCalendarExtractor;
    /**
     * Represents a mapping between {@link com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpecSupplier, MessageEntryCreator> registry = new HashMap<>();

    static {
        //Code table related
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(ActivityCalendarDeviceMessage.SELECTION_OF_12_LINES_IN_TOU_TABLE, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
        //Display configuration
        registry.put(DisplayDeviceParametersMessage.DISPLAY_GENERAL_PARAMETERS,
                new MultipleInnerTagsMessageEntry(DisplayDeviceParametersMessage.DISPLAY_GENERAL_PARAMETERS.toString(),
                        DeviceMessageConstants.DisplayLeadingZero, DeviceMessageConstants.DisplayBacklight,
                        DeviceMessageConstants.DisplayEOB_Confirm, DeviceMessageConstants.DisplayString_EOB_Confirm,
                        DeviceMessageConstants.DisplaySeparators_Display, DeviceMessageConstants.DisplayTime_Format,
                        DeviceMessageConstants.DisplayDate_Format, DeviceMessageConstants.DisplayTimeOut_For_Set_Mode,
                        DeviceMessageConstants.Display_On_TimeOut, DeviceMessageConstants.Display_Off_TimeOut,
                        DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Normal_Mode, DeviceMessageConstants.DisplayExistence_Of_EndOfText,
                        DeviceMessageConstants.DisplayEndOfTextString, DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode1,
                        DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode2, DeviceMessageConstants.DisplayTimeout_For_AltMode,
                        DeviceMessageConstants.DisplayAutorized_EOB, DeviceMessageConstants.DisplayTimeout_Load_Profile,
                        DeviceMessageConstants.Displaying_Of_LPMenus,
                        DeviceMessageConstants.Display_ButtonEmulation_By_Optical_Head));
        registry.put(DisplayDeviceParametersMessage.DISPLAY_READOUT_TABLE_PARAMETERS,
                new MultipleInnerTagsMessageEntry(DisplayDeviceParametersMessage.DISPLAY_READOUT_TABLE_PARAMETERS.toString(),
                        DeviceMessageConstants.DisplayInternal_Identifier, DeviceMessageConstants.DisplaySequence_Indicator,
                        DeviceMessageConstants.DisplayIdentification_Code, DeviceMessageConstants.DisplayScaler,
                        DeviceMessageConstants.DisplayNumber_Of_Decimal, DeviceMessageConstants.DisplayNumber_Of_Display_Historical_Data,
                        DeviceMessageConstants.DisplayNumber_Of_Displayable_Digit));
        //EOB reset messages
        registry.put(DeviceActionMessage.DEMAND_RESET, new SimpleTagMessageEntry(BILLING_RESET, false));
        // Voltage and Current parameters
        registry.put(PowerConfigurationDeviceMessage.SetVoltageAndCurrentParameters,
                new MultipleInnerTagsMessageEntry(VOLTAGE_AND_CURRENT_PARAMS,
                        VoltageRatioDenominatorAttributeName, VoltageRatioNumeratorAttributeName,
                        CurrentRatioDenominatorAttributeName, CurrentRatioNumeratorAttributeName)
        );
    }

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
