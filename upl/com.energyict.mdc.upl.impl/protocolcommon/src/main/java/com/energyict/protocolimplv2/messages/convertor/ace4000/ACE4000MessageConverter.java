package com.energyict.protocolimplv2.messages.convertor.ace4000;


import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.ace4000.messages.ACE4000ConfigurationMessages;
import com.energyict.protocolimplv2.ace4000.messages.ACE4000GeneralMessages;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.AbstractMessageConverter;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

  /**
    *   Represents the message converter for legacy protocol ACE4000Outbound
  */

public class ACE4000MessageConverter extends AbstractMessageConverter {
   /**
   * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
   * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
   */
   protected static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

   static {
       //Configuration messages
       registry.put(ACE4000ConfigurationMessages.SendShortDisplayMessage, new MultipleAttributeMessageEntry(DeviceMessageConstants.SHORT_DISPLAY_MESSAGE));
       registry.put(ACE4000ConfigurationMessages.SendLongDisplayMessage, new MultipleAttributeMessageEntry(DeviceMessageConstants.LONG_DISPLAY_MESSAGE));
       registry.put(ACE4000ConfigurationMessages.DisplayMessage, new SimpleTagMessageEntry(DeviceMessageConstants.DISPLAY_MESSAGE));
       registry.put(ACE4000ConfigurationMessages.ConfigureLCDDisplay, new MultipleAttributeMessageEntry(DeviceMessageConstants.NUMBER_OF_DIGITS_BEFORE_COMMA,DeviceMessageConstants.NUMBER_OF_DIGITS_AFTER_COMMA, DeviceMessageConstants.DISPLAY_SEQUENCE,  DeviceMessageConstants.DISPLAY_CYCLE_TIME));
       registry.put(ACE4000ConfigurationMessages.ConfigureLoadProfileDataRecording, new MultipleAttributeMessageEntry(DeviceMessageConstants.ENABLE_DISABLE,DeviceMessageConstants.CONFIG_LOAD_PROFILE_INTERVAL, DeviceMessageConstants.MAX_NUMBER_RECORDS));
       registry.put(ACE4000ConfigurationMessages.ConfigureSpecialDataMode, new MultipleAttributeMessageEntry(DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DAYS,DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_INTERVAL, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_MAX_NUMBER_RECORDS,DeviceMessageConstants.SPECIAL_LOAD_PROFILE,DeviceMessageConstants.SPECIAL_LOAD_PROFILE_INTERVAL));
       registry.put(ACE4000ConfigurationMessages.ConfigureMaxDemandSettings, new MultipleAttributeMessageEntry(DeviceMessageConstants.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1, DeviceMessageConstants.NUMBER_OF_SUBINTERVALS, DeviceMessageConstants.SUB_INTERVAL_DURATION, DeviceMessageConstants.SPECIAL_LOAD_PROFILE_MAX_NO));
       registry.put(ACE4000ConfigurationMessages.ConfigureConsumptionLimitationsSettings, new MultipleAttributeMessageEntry(DeviceMessageConstants.NUMBER_OF_SUBINTERVALS,DeviceMessageConstants.SUB_INTERVAL_DURATION, DeviceMessageConstants.OVERRIDE_RATE,  DeviceMessageConstants.ALLOWED_EXCESS_TOLERANCE, DeviceMessageConstants.THRESHOLD_SELECTION, DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE0, DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE0, DeviceMessageConstants.THRESHOLDS_MOMENTS, DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE0, DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE1, DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE1,DeviceMessageConstants.THRESHOLDS_MOMENTS,DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE1,DeviceMessageConstants.DAY_PROFILES,DeviceMessageConstants.ACTIVATION_DATE ));
       registry.put(ACE4000ConfigurationMessages.ConfigureEmergencyConsumptionLimitation, new MultipleAttributeMessageEntry(DeviceMessageConstants.DURATION_MINUTES,DeviceMessageConstants.TRESHOLD_VALUE, DeviceMessageConstants.TRESHOLD_UNIT,  DeviceMessageConstants.OVERRIDE_RATE));
       registry.put(ACE4000ConfigurationMessages.ConfigureTariffSettings, new MultipleAttributeMessageEntry(DeviceMessageConstants.UNIQUE_TARIFF_ID_NO, DeviceMessageConstants.NUMBER_OF_TARIFF_RATES, DeviceMessageConstants.CODE_TABLE_ID));

       //General messages
       registry.put(ACE4000GeneralMessages.FirmwareUpgrade, new MultipleAttributeMessageEntry(DeviceMessageConstants.URL_PATH, DeviceMessageConstants.JAR_FILE_SIZE, DeviceMessageConstants.JAD_FILE_SIZE));
       registry.put(ACE4000GeneralMessages.Connect, new MultipleAttributeMessageEntry(DeviceMessageConstants.OPTIONAL_DATE));
       registry.put(ACE4000GeneralMessages.Disconnect, new MultipleAttributeMessageEntry(DeviceMessageConstants.OPTIONAL_DATE));
   }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {

        return messageAttribute.toString();
    }
}
