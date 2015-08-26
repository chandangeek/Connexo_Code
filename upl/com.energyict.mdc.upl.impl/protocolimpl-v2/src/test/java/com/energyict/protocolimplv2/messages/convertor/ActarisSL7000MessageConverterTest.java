package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.smartmeterprotocolimpl.actaris.sl7000.ActarisSl7000;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link ActarisSL7000MessageConverter} component.
 *
 * @author sva
 * @since 25/10/13 - 12:14
 */
@RunWith(MockitoJUnitRunner.class)
public class ActarisSL7000MessageConverterTest extends AbstractMessageConverterTest {

    private static final String XMLEncodedActivityCalendar = "<TimeOfUse><CalendarName>MyActivityCal</CalendarName><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Greenwich Mean Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2013</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>1382704200000</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>1</wkMonday><wkTuesday>1</wkTuesday><wkWednesday>1</wkWednesday><wkThursday>1</wkThursday><wkFriday>1</wkFriday><wkSaturday>0</wkSaturday><wkSunday>0</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>1</Month><Day>1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>0</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";
    private String ExpectedActivityCalendarMessageContent;
    private Date activityCalendarActivationDate;

    @Test
    public void testMessageConversion() {
        try {
            activityCalendarActivationDate = europeanDateTimeFormat.parse("25/10/2013 14:30:00");
            ExpectedActivityCalendarMessageContent = "<TimeOfUse name=\"MyActivityCal\" activationDate=\""+ activityCalendarActivationDate.getTime()+"\"><CodeId>8</CodeId><Activity_Calendar>H4sIAAAAAAAAAN1WS27bMBDtJkAvUcAXKCLLRdMFQaCwkyYLtwXsIEh2rDWGichkQFIJdKderXfokJT4kYS2XhQFqo3mvZl5HA7psb7/eHVmXr8hW36EL/tbDZQsWQ2iYuozOwJdtx93hj9z0yJNzjMfWcoKtuxbDTb7QQqgSxBGsXp22Sj5BEzMrAfTRoExdwXacMEMlyJ4PykA8cJ3h9l6QmQqI+rdCAPqmdV08b4okqzAx9ArJY/3wBQti/kiCQ18skPZBZapZsfGsA0wLcUGzE1Fy3kSmTqIa6mrf8UM0PniQ3lRvCsL+5DzgTeKo6PvPyVe8KuSe16DHuABdKeFymOyi9sYpgwlbjNvsWy/q7UU5kAReoOsWGuRffVaXaIHdwCP+VKBGaw9xFi/DZ1EGejlhxR5ecQiK19gsJHdNqAD3QPk76ASiSdCm3NoVJLUI/RcKR74zkZ2w0yjrF1YPiDraUTkvZ3VniPtWjwFUhsvkD+EiBPvlim+3+sxNWaGQoG1kR6447W/MUquZaPsRtybrLloDNaIRG/iJdhh27U/fW86+ZHUaM0/rLY8sdqLf1rtqb214+LvlDuidMb94qIV/91F+20vEmBd08M3jvUn2HFWu5EYbZ2C1L7Ef8fWT/WTZu2UwIhs/YFN8imdgWyPWUz8IvgJB2iK+SQIAAA=</Activity_Calendar></TimeOfUse>";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ProgramBatteryExpiryDate);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<BatteryExpiry Date (dd/MM/yyyy)=\"01/01/2050\"> </BatteryExpiry>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ClockDeviceMessage.EnableOrDisableDST);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<EnableDST>1</EnableDST>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ClockDeviceMessage.SetEndOfDST);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<EndOfDST Month=\"1\" Day of month=\"2\" Day of week=\"3\" Hour=\"4\"> </EndOfDST>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ClockDeviceMessage.SetStartOfDST);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<StartOfDST Month=\"1\" Day of month=\"2\" Day of week=\"3\" Hour=\"4\"> </StartOfDST>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.BILLING_RESET);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DemandReset/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals(ExpectedActivityCalendarMessageContent, messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new ActarisSl7000();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        ActarisSL7000MessageConverter messageConverter = spy(new ActarisSL7000MessageConverter());
        // We stub the encode method, cause CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable() is not subject of this test
        doReturn(XMLEncodedActivityCalendar).when(messageConverter).encode(any(Code.class));
        return messageConverter;
    }

    /**
     * Gets the value to use for the given {@link com.energyict.cpo.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.ConfigurationChangeDate:
                    return dateFormat.parse("01/01/2050");
                case DeviceMessageConstants.enableDSTAttributeName:
                    return new Boolean(true);
                case DeviceMessageConstants.month:
                    return 1;
                case DeviceMessageConstants.dayOfMonth:
                    return 2;
                case DeviceMessageConstants.dayOfWeek:
                    return 3;
                case DeviceMessageConstants.hour:
                    return 4;
                case DeviceMessageConstants.activityCalendarNameAttributeName:
                    return "MyActivityCal";
                case DeviceMessageConstants.activityCalendarCodeTableAttributeName:
                    Code code = mock(Code.class);
                    when(code.getId()).thenReturn(8);
                    return code;
                case DeviceMessageConstants.activityCalendarActivationDateAttributeName:
                    return activityCalendarActivationDate;
                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}