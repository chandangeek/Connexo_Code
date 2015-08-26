package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.ModbusMessageConverter} component.
 *
 * @author khe
 * @since 24/10/13 - 10:50
 */
@RunWith(MockitoJUnitRunner.class)
public class AS220DLMSMessageConverterTest extends AbstractMessageConverterTest {

    private static final String xmlEncodedCodeTable = "<TimeOfUse><CalendarName>TariffCodeTable</CalendarName><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Greenwich Mean Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2014</CodeTableFromYear><CodeTableToYear>2014</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>1</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>1</wkMonday><wkTuesday>1</wkTuesday><wkWednesday>1</wkWednesday><wkThursday>1</wkThursday><wkFriday>1</wkFriday><wkSaturday>0</wkSaturday><wkSunday>0</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>12</Month><Day>25</Day></SpecialDayEntryDate><SpecialDayEntryDayId>0</SpecialDayEntryDayId></SpecialDay><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>1</Month><Day>1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>0</SpecialDayEntryDayId></SpecialDay><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>0</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ActivatePassiveCalendar ActivationTime=\"1970/01/01 01:00:00\"> </ActivatePassiveCalendar>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<TimeOfUse>" + ProtocolTools.compress(xmlEncodedCodeTable)+"</TimeOfUse>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PLCConfigurationDeviceMessage.SetPlcChannelFreqSnrCredits);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetPlcChannelFreqSnrCredits CHANNEL1_FS=\"1\" CHANNEL1_FM=\"1\" CHANNEL1_SNR=\"1\" CHANNEL1_CREDITWEIGHT=\"1\" CHANNEL2_FS=\"1\" CHANNEL2_FM=\"1\" CHANNEL2_SNR=\"1\" CHANNEL2_CREDITWEIGHT=\"1\" CHANNEL3_FS=\"1\" CHANNEL3_FM=\"1\" CHANNEL3_SNR=\"1\" CHANNEL3_CREDITWEIGHT=\"1\" CHANNEL4_FS=\"1\" CHANNEL4_FM=\"1\" CHANNEL4_SNR=\"1\" CHANNEL4_CREDITWEIGHT=\"1\" CHANNEL5_FS=\"1\" CHANNEL5_FM=\"1\" CHANNEL5_SNR=\"1\" CHANNEL5_CREDITWEIGHT=\"1\" CHANNEL6_FS=\"1\" CHANNEL6_FM=\"1\" CHANNEL6_SNR=\"1\" CHANNEL6_CREDITWEIGHT=\"1\"> </SetPlcChannelFreqSnrCredits>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.DecommissionAll);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DecommissionAll> </DecommissionAll>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpdate><IncludedFile>userFileBytes</IncludedFile></FirmwareUpdate>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AS220();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new TestAS220DLMSMessageConverter();
    }

    /**
     * Gets the value to use for the given {@link com.energyict.cpo.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            try {
                return dateTimeFormat.parse("1970/01/01 01:00:00");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName) || propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return mock(Code.class);
        } else if (propertySpec.getName().equals(activityCalendarNameAttributeName)) {
            return "STIJNVA";
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = mock(UserFile.class);
            when(userFile.getId()).thenReturn(1121);
            when(userFile.loadFileInByteArray()).thenReturn("userFileBytes".getBytes());
            return userFile;
        }
        return "1";     //All other attribute values are "1"
    }

    /**
     * Used to test to actual G3MeterMessageConverter.
     * This class overrides the convertCodeTableToXML method, this way we don't have to mock an entire codetable in order to get a decent XML description.
     */
    public class TestAS220DLMSMessageConverter extends AS220DLMSMessageConverter {

        @Override
        protected String convertCodeTableToXML(Code messageAttribute) {
            return xmlEncodedCodeTable;
        }
    }
}