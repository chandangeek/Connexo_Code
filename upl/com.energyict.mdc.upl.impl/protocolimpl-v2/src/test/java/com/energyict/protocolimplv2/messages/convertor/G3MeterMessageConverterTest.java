package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.protocolimpl.dlms.g3.AS330D;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.enums.ActivityCalendarType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.configUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.plcG3TimeoutAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.pskAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysAttributeName;
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
public class G3MeterMessageConverterTest extends AbstractV2MessageConverterTest {

    private static final String xmlSpecialDays = "<TimeOfUse><CalendarName/><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Central European Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2012</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>1</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>0</wkMonday><wkTuesday>0</wkTuesday><wkWednesday>0</wkWednesday><wkThursday>0</wkThursday><wkFriday>0</wkFriday><wkSaturday>0</wkSaturday><wkSunday>1</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>1</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";
    private static final String xmlEncodedCodeTableWithEmptyName = "<TimeOfUse><CalendarName>0</CalendarName><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Central European Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2012</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>0</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>0</wkMonday><wkTuesday>0</wkTuesday><wkWednesday>0</wkWednesday><wkThursday>0</wkThursday><wkFriday>0</wkFriday><wkSaturday>0</wkSaturday><wkSunday>1</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>1</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";
    private static final String xmlEncodedCodeTable = "<TimeOfUse><CalendarName>KHE</CalendarName><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Central European Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2012</CodeTableFromYear><CodeTableToYear>2020</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>1</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>0</wkMonday><wkTuesday>0</wkTuesday><wkWednesday>0</wkWednesday><wkThursday>0</wkThursday><wkFriday>0</wkFriday><wkSaturday>0</wkSaturday><wkSunday>1</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>1</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";

    private Date date;

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        String xml = xmlEncodedCodeTable.replace("<ActivationDate>1</ActivationDate>", "<ActivationDate>" + String.valueOf(getDateInFuture().getTime()) + "</ActivationDate>");
        assertEquals("<PublicNetworkActivity_Calendar><RawContent>" + ProtocolTools.compress(xml) + "</RawContent></PublicNetworkActivity_Calendar>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<PublicNetworkSpecial_Days><RawContent>" + ProtocolTools.compress(xmlSpecialDays) + "</RawContent></PublicNetworkSpecial_Days>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PLCConfigurationDeviceMessage.WritePlcG3Timeout.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WritePlcG3Timeout Timeout_in_minutes=\"2\"> \n\n</WritePlcG3Timeout>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.WRITE_PSK.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WritePlcPsk PSK=\"PSK\"> \n\n</WritePlcPsk>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpdate><IncludedFile>path</IncludedFile></FirmwareUpdate>", messageEntry.getContent());
        assertTrue(messageEntry.getTrackingId().toLowerCase().contains("noresume"));
        assertTrue(messageEntry.getTrackingId().toLowerCase().contains("plc"));
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AS330D(this.calendarFinder, calendarExtractor, propertySpecService);
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new TestG3MeterMessageConverter(propertySpecService, this.nlsService, this.converter, this.calendarExtractor, this.keyAccessorTypeExtractor);
    }

    /**
     * Gets the value to use for the given {@link com.energyict.mdc.upl.properties.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return getDateInFuture();
        } else if (propertySpec.getName().equals(specialDaysAttributeName) || propertySpec.getName().equals(activityCalendarAttributeName)) {
            return mock(TariffCalendar.class);
        } else if (propertySpec.getName().equals(activityCalendarNameAttributeName)) {
            return "KHE";
        } else if (propertySpec.getName().equals(configUserFileAttributeName)) {
            DeviceMessageFile deviceMessageFile = mock(DeviceMessageFile.class);
            when(deviceMessageFileExtractor.binaryContents(deviceMessageFile)).thenReturn("userFileBytes".getBytes());
            return deviceMessageFile;
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            return "path";
        } else if (propertySpec.getName().equals(resumeFirmwareUpdateAttributeName)) {
            return Boolean.FALSE;
        } else if (propertySpec.getName().equals(plcTypeFirmwareUpdateAttributeName)) {
            return Boolean.TRUE;
        } else if (propertySpec.getName().equals(pskAttributeName)) {
            KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
            when(keyAccessorTypeExtractor.passiveValueContent(keyAccessorType)).thenReturn("PSK");
            return keyAccessorType;
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

        public TestG3MeterMessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor tariffCalendarExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
            super(propertySpecService, nlsService, converter, tariffCalendarExtractor, keyAccessorTypeExtractor);
        }

        @Override
        protected String convertCodeTableToXML(TariffCalendar messageAttribute, TariffCalendarExtractor tariffCalendarExtractor) {
            return xmlEncodedCodeTableWithEmptyName;
        }

        @Override
        protected String convertSpecialDaysCodeTableToXML(TariffCalendar messageAttribute, TariffCalendarExtractor tariffCalendarExtractor) {
            return xmlSpecialDays;
        }
    }
}