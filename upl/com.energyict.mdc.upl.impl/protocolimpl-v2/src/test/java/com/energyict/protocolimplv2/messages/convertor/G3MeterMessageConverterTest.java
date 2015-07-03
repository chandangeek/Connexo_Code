package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.dlms.g3.AS330D;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.enums.ActivityCalendarType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
public class G3MeterMessageConverterTest extends AbstractMessageConverterTest {

    private static final String xmlSpecialDays = "<TimeOfUse><CalendarName/><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Central European Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2012</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>1</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>0</wkMonday><wkTuesday>0</wkTuesday><wkWednesday>0</wkWednesday><wkThursday>0</wkThursday><wkFriday>0</wkFriday><wkSaturday>0</wkSaturday><wkSunday>1</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>1</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";
    private static final String xmlEncodedCodeTableWithEmptyName = "<TimeOfUse><CalendarName>0</CalendarName><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Central European Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2012</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>0</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>0</wkMonday><wkTuesday>0</wkTuesday><wkWednesday>0</wkWednesday><wkThursday>0</wkThursday><wkFriday>0</wkFriday><wkSaturday>0</wkSaturday><wkSunday>1</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>1</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";
    private static final String xmlEncodedCodeTable = "<TimeOfUse><CalendarName>KHE</CalendarName><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Central European Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2012</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>1</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>0</wkMonday><wkTuesday>0</wkTuesday><wkWednesday>0</wkWednesday><wkThursday>0</wkThursday><wkFriday>0</wkFriday><wkSaturday>0</wkSaturday><wkSunday>1</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>1</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";
    private Date date;

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        String xml = xmlEncodedCodeTable.replace("<ActivationDate>1</ActivationDate>", "<ActivationDate>" + String.valueOf(getDateInFuture().getTime()) + "</ActivationDate>");
        assertEquals("<PublicNetworkActivity_Calendar><RawContent>" + ProtocolTools.compress(xml) + "</RawContent></PublicNetworkActivity_Calendar>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<PublicNetworkSpecial_Days><RawContent>" + ProtocolTools.compress(xmlSpecialDays) + "</RawContent></PublicNetworkSpecial_Days>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PLCConfigurationDeviceMessage.WritePlcG3Timeout);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WritePlcG3Timeout Timeout_in_minutes=\"2\"> \n\n</WritePlcG3Timeout>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.WRITE_PSK);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WritePlcPsk PSK=\"PSK\"> \n\n</WritePlcPsk>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpdate><IncludedFile>userFileBytes</IncludedFile></FirmwareUpdate>", messageEntry.getContent());
        assertTrue(messageEntry.getTrackingId().toLowerCase().contains("noresume"));
        assertTrue(messageEntry.getTrackingId().toLowerCase().contains("plc"));
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AS330D();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new TestG3MeterMessageConverter();
    }

    /**
     * Gets the value to use for the given {@link com.energyict.cpo.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return getDateInFuture();
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
        } else if (propertySpec.getName().equals(plcTypeFirmwareUpdateAttributeName)) {
            return Boolean.TRUE;
        } else if (propertySpec.getName().equals(pskAttributeName)) {
            return new Password("PSK");
        } else if (propertySpec.getName().equals(activityCalendarTypeAttributeName)) {
            return ActivityCalendarType.PublicNetwork.getDescription();
        } else if (propertySpec.getName().equals(plcG3TimeoutAttributeName)) {
            return new BigDecimal(2);
        }
        return "1";     //All other attribute values are "1"
    }

    private Date getDateInFuture() {
        if (date == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 1);   //Date in the future
            date = cal.getTime();
        }
        return date;
    }

    /**
     * Used to test to actual G3MeterMessageConverter.
     * This class overrides the convertCodeTableToXML method, this way we don't have to mock an entire codetable in order to get a decent XML description.
     */
    public class TestG3MeterMessageConverter extends G3MeterMessageConverter {

        @Override
        protected String convertCodeTableToXML(Code messageAttribute) {
            return xmlEncodedCodeTableWithEmptyName;
        }

        @Override
        protected String convertSpecialDaysCodeTableToXML(Code messageAttribute) {
            return xmlSpecialDays;
        }
    }
}