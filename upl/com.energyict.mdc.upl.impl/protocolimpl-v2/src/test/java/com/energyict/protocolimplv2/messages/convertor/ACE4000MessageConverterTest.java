package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.messages.ACE4000ConfigurationMessages;
import com.energyict.protocolimplv2.ace4000.messages.ACE4000GeneralMessages;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.ace4000.ACE4000MessageConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with "1" values) and converts them to the legacy XML message, using the ACE4000MessageConverter.
 * <p/>
 */

@RunWith(MockitoJUnitRunner.class)
public class ACE4000MessageConverterTest extends AbstractMessageConverterTest {
    private String expectedSpecialDateModeDurationDaysMessageContent;
    Date date;
    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;

        OfflineDeviceMessage offlineMessage = createMessage(ACE4000ConfigurationMessages.SendShortDisplayMessage);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.SHORT_DISPLAY_MESSAGE> </ACE4000ConfigurationMessages.SHORT_DISPLAY_MESSAGE>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000ConfigurationMessages.SendLongDisplayMessage);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.LONG_DISPLAY_MESSAGE> </ACE4000ConfigurationMessages.LONG_DISPLAY_MESSAGE>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000ConfigurationMessages.DisplayMessage);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<Display_Message> </Display_Message>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000ConfigurationMessages.ConfigureLCDDisplay);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.NUMBER_OF_DIGITS_BEFORE_COMMA ACE4000ConfigurationMessages.NUMBER_OF_DIGITS_AFTER_COMMA=\"2\" ACE4000ConfigurationMessages.DISPLAY_SEQUENCE=\"1\" ACE4000ConfigurationMessages.DISPLAY_CYCLE_TIME=\"1\"> </ACE4000ConfigurationMessages.NUMBER_OF_DIGITS_BEFORE_COMMA>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000ConfigurationMessages.ConfigureLoadProfileDataRecording);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.ENABLE_DISABLE ACE4000ConfigurationMessages.CONFIG_LOAD_PROFILE_INTERVAL=\"1\" ACE4000ConfigurationMessages.MAX_NUMBER_RECORDS=\"1\"> </ACE4000ConfigurationMessages.ENABLE_DISABLE>", messageEntry.getContent());

        try {
            date = europeanDateTimeFormat.parse("25/10/2013 14:30:00");
            expectedSpecialDateModeDurationDaysMessageContent = "<ACE4000ConfigurationMessages.SPECIAL_DATE_MODE_DURATION_DAYS ACE4000ConfigurationMessages.SPECIAL_BILLING_REGISTER_RECORDING=\""+ date.getTime() + "\" ACE4000ConfigurationMessages.SPECIAL_BILLING_REGISTER_RECORDING_INTERVAL=\"1\" ACE4000ConfigurationMessages.SPECIAL_BILLING_REGISTER_RECORDING_MAX_NUMBER_RECORDS=\"1\" ACE4000ConfigurationMessages.SPECIAL_LOAD_PROFILE=\"100\" ACE4000ConfigurationMessages.SPECIAL_LOAD_PROFILE_INTERVAL=\"1\"> </ACE4000ConfigurationMessages.SPECIAL_DATE_MODE_DURATION_DAYS>";
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        offlineMessage = createMessage(ACE4000ConfigurationMessages.ConfigureSpecialDataMode);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals(expectedSpecialDateModeDurationDaysMessageContent, messageEntry.getContent());

        offlineMessage = createMessage(ACE4000ConfigurationMessages.ConfigureMaxDemandSettings);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1 ACE4000ConfigurationMessages.NUMBER_OF_SUBINTERVALS=\"1\" ACE4000ConfigurationMessages.SUB_INTERVAL_DURATION=\"1\" ACE4000ConfigurationMessages.SPECIAL_LOAD_PROFILE_MAX_NO=\"1\"> </ACE4000ConfigurationMessages.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000ConfigurationMessages.ConfigureConsumptionLimitationsSettings);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.NUMBER_OF_SUBINTERVALS ACE4000ConfigurationMessages.SUB_INTERVAL_DURATION=\"1\" ACE4000ConfigurationMessages.OVERRIDE_RATE=\"1\" ACE4000ConfigurationMessages.ALLOWED_EXCESS_TOLERANCE=\"1\" ACE4000ConfigurationMessages.THRESHOLD_SELECTION=\"1\" ACE4000ConfigurationMessages.SWITCHING_MOMENTS_DAILY_PROFILE0=\"1\" ACE4000ConfigurationMessages.THRESHOLDS_MOMENTS_DAILY_PROFILE0=\"1\" ACE4000ConfigurationMessages.THRESHOLDS_MOMENTS=\"1\" ACE4000ConfigurationMessages.ACTIONS_IN_HEX_DAILY_PROFILE0=\"1\" ACE4000ConfigurationMessages.SWITCHING_MOMENTS_DAILY_PROFILE1=\"1\" zorro=\"1\" ACE4000ConfigurationMessages.THRESHOLDS_MOMENTS=\"1\" ACE4000ConfigurationMessages.ACTIONS_IN_HEX_DAILY_PROFILE1=\"1\" ACE4000ConfigurationMessages.DAY_PROFILES=\"1\" ACE4000ConfigurationMessages.ACTIVATION_DATE=\"1\"> </ACE4000ConfigurationMessages.NUMBER_OF_SUBINTERVALS>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000ConfigurationMessages.ConfigureEmergencyConsumptionLimitation);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.DURATION_MINUTES ACE4000ConfigurationMessages.TRESHOLD_VALUE=\"1\" ACE4000ConfigurationMessages.TRESHOLD_UNIT=\"1\" ACE4000ConfigurationMessages.OVERRIDE_RATE=\"1\"> </ACE4000ConfigurationMessages.DURATION_MINUTES>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000ConfigurationMessages.ConfigureTariffSettings);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.UNIQUE_TARIFF_ID_NO ACE4000ConfigurationMessages.NUMBER_OF_TARIFF_RATES=\"1\" ACE4000ConfigurationMessages.CODE_TABLE_ID=\"1\"> </ACE4000ConfigurationMessages.UNIQUE_TARIFF_ID_NO>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000GeneralMessages.FirmwareUpgrade);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.URL_PATH> </ACE4000ConfigurationMessages.URL_PATH>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000GeneralMessages.Connect);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.OPTIONAL_DATE> </ACE4000ConfigurationMessages.OPTIONAL_DATE>", messageEntry.getContent());

        offlineMessage = createMessage(ACE4000GeneralMessages.Disconnect);
        messageEntry = getMessageConverter().toMessageEntry(offlineMessage);
        assertEquals("<ACE4000ConfigurationMessages.OPTIONAL_DATE> </ACE4000ConfigurationMessages.OPTIONAL_DATE>", messageEntry.getContent());

    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new ACE4000Outbound();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new ACE4000MessageConverter();
    }


    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.SHORT_DISPLAY_MESSAGE:
                return "Message";
            case DeviceMessageConstants.LONG_DISPLAY_MESSAGE:
                return "Message";
            case DeviceMessageConstants.NUMBER_OF_DIGITS_AFTER_COMMA:
                return 1;
            case DeviceMessageConstants.NUMBER_OF_DIGITS_BEFORE_COMMA:
                return 2;
            case DeviceMessageConstants.DISPLAY_SEQUENCE:
                return 1;
            case DeviceMessageConstants.DISPLAY_CYCLE_TIME:
                return 1;
            case DeviceMessageConstants.ENABLE_DISABLE:
                return 1;
            case DeviceMessageConstants.CONFIG_LOAD_PROFILE_INTERVAL:
                return 1;
            case DeviceMessageConstants.MAX_NUMBER_RECORDS:
                return 1;
            case DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE:
                return date.getTime();
            case DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING:
                return 1;
            case DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_INTERVAL:
                return 1;
            case DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_MAX_NUMBER_RECORDS:
                return 100;
            case DeviceMessageConstants.SPECIAL_LOAD_PROFILE:
                return 1;
            case DeviceMessageConstants.NUMBER_OF_SUBINTERVALS:
                return 1;
            case DeviceMessageConstants.SUB_INTERVAL_DURATION:
                return 1;
            case DeviceMessageConstants.SPECIAL_LOAD_PROFILE_MAX_NO:
                return 1;
            case DeviceMessageConstants.OVERRIDE_RATE:
                return 1;
            case DeviceMessageConstants.ALLOWED_EXCESS_TOLERANCE:
                return 1;
            case DeviceMessageConstants.THRESHOLD_SELECTION:
                return 1;
            case DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE0:
                return 1;
            case DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE0:
                return 1;
            case DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE1:
                return 1;
            case DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE1:
                return 1;
            case DeviceMessageConstants.THRESHOLDS_MOMENTS:
                return 1;
            case DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE0:
                return 1;
            case DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE1:
                return 1;
            case DeviceMessageConstants.DAY_PROFILES:
                return 1;
            case DeviceMessageConstants.ACTIVATION_DATE:
                return date.getTime();
            case DeviceMessageConstants.NUMBER_OF_TARIFF_RATES:
                return 1;
            case DeviceMessageConstants.TRESHOLD_VALUE:
                return 1;
            case DeviceMessageConstants.TRESHOLD_UNIT:
                return 1;
            case DeviceMessageConstants.UNIQUE_TARIFF_ID_NO:
                return 1;
            case DeviceMessageConstants.CODE_TABLE_ID:
                return 1;
            case DeviceMessageConstants.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1:
                return 1;
            case DeviceMessageConstants.DURATION_MINUTES:
                return 1;
            default:
                return "";
        }
    }
}