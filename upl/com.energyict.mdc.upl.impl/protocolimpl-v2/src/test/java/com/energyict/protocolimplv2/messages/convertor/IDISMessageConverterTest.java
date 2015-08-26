package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link ModbusMessageConverter} component.
 *
 * @author khe
 * @since 24/10/13 - 10:50
 */
@RunWith(MockitoJUnitRunner.class)
public class IDISMessageConverterTest extends AbstractMessageConverterTest {

    private static final String xmlSpecialDays = "<TimeOfUse><CalendarName/><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Central European Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2012</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>1</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>0</wkMonday><wkTuesday>0</wkTuesday><wkWednesday>0</wkWednesday><wkThursday>0</wkThursday><wkFriday>0</wkFriday><wkSaturday>0</wkSaturday><wkSunday>1</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>1</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";
    private static final String xmlEncodedCodeTableWithEmptyName = "<TimeOfUse><CalendarName>0</CalendarName><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Central European Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2012</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>0</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>0</wkMonday><wkTuesday>0</wkTuesday><wkWednesday>0</wkWednesday><wkThursday>0</wkThursday><wkFriday>0</wkFriday><wkSaturday>0</wkSaturday><wkSunday>1</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>1</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";
    private static final String xmlEncodedCodeTable = "<TimeOfUse><CalendarName>KHE</CalendarName><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Central European Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2012</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>1</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>0</wkMonday><wkTuesday>0</wkTuesday><wkWednesday>0</wkWednesday><wkThursday>0</wkThursday><wkFriday>0</wkFriday><wkSaturday>0</wkSaturday><wkSunday>1</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>1</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";
    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm");

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Activity_Calendar><RawContent>" + ProtocolTools.compress(xmlEncodedCodeTable) + "</RawContent></Activity_Calendar>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Special_Days><RawContent>" + ProtocolTools.compress(xmlSpecialDays) + "</RawContent></Special_Days>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Configuration download><IncludedFile>userFileBytes</IncludedFile></Configuration download>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<TimedDisconnect Date (dd/mm/yyyy hh:mm)=\"17/05/1970 01:05\" TimeZone=\"" + TimeZone.getDefault().getID() + "\"> </TimedDisconnect>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpdate><IncludedFile>userFileBytes</IncludedFile></FirmwareUpdate>", messageEntry.getContent());
        assertTrue(messageEntry.getTrackingId().toLowerCase().contains("noresume"));
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new IDIS();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new TestIDISMessageConverter();
    }

    /**
     * Used to test to actual IDISMessageConverter.
     * This class overrides the convertCodeTableToXML method, this way we don't have to mock an entire codetable in order to get a decent XML description.
     */
    public class TestIDISMessageConverter extends IDISMessageConverter {

        @Override
        protected String convertCodeTableToXML(Code messageAttribute) {
            return xmlEncodedCodeTableWithEmptyName;
        }

        @Override
        protected String convertSpecialDaysCodeTableToXML(Code messageAttribute) {
            return xmlSpecialDays;
        }
    }

    /**
     * Gets the value to use for the given {@link com.energyict.cpo.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName) || propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            try {
                return dateFormat.parse("17/05/1970 01:05");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName) || propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return mock(Code.class);
        } else if (propertySpec.getName().equals(activityCalendarNameAttributeName)) {
            return "KHE";
        } else if (propertySpec.getName().equals(configUserFileAttributeName) || propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = mock(UserFile.class);
            when(userFile.loadFileInByteArray()).thenReturn("userFileBytes".getBytes());
            return userFile;
        } else if (propertySpec.getName().equals(resumeFirmwareUpdateAttributeName)) {
            return Boolean.FALSE;
        }
        return "1";     //All other attribute values are "1"
    }
}