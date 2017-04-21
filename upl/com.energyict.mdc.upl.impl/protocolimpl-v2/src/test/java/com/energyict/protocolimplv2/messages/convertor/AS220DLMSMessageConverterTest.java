package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
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

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysAttributeName;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.ModbusMessageConverter} component.
 *
 * @author khe
 * @since 24/10/13 - 10:50
 */
@RunWith(MockitoJUnitRunner.class)
public class AS220DLMSMessageConverterTest extends AbstractV2MessageConverterTest {

    private static final String xmlEncodedCodeTable = "<TimeOfUse><CalendarName>TariffCodeTable</CalendarName><CodeTableTimeZone>Central European Time</CodeTableTimeZone><CodeTableDestinationTimeZone>Greenwich Mean Time</CodeTableDestinationTimeZone><CodeTableInterval>3600</CodeTableInterval><CodeTableFromYear>2014</CodeTableFromYear><CodeTableToYear>2014</CodeTableToYear><CodeTableSeasonSetId>21</CodeTableSeasonSetId><ActivationDate>1</ActivationDate><CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>0</SeasonProfileName><SeasonStart><Year>-1</Year><Month>1</Month><Day>1</Day></SeasonStart><SeasonWeekName>0</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>0</WeekProfileName><wkMonday>1</wkMonday><wkTuesday>1</wkTuesday><wkWednesday>1</wkWednesday><wkThursday>1</wkThursday><wkFriday>1</wkFriday><wkSaturday>0</wkSaturday><wkSunday>0</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>1</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>2</DayProfileTariffId><DayTariffStartTime><Hour>7</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>21</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile><DayProfile><DayProfileId>0</DayProfileId><DayProfileTariffs><DayProfileTariff><DayProfileTariffId>1</DayProfileTariffId><DayTariffStartTime><Hour>0</Hour><Minutes>0</Minutes><Seconds>0</Seconds></DayTariffStartTime></DayProfileTariff></DayProfileTariffs></DayProfile></DayProfiles></CodeTableActCalendar><CodeTableSpecialDay><SpecialDays><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>12</Month><Day>25</Day></SpecialDayEntryDate><SpecialDayEntryDayId>0</SpecialDayEntryDayId></SpecialDay><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>1</Month><Day>1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>0</SpecialDayEntryDayId></SpecialDay><SpecialDay><SpecialDayEntryDate><Year>-1</Year><Month>-1</Month><Day>-1</Day></SpecialDayEntryDate><SpecialDayEntryDayId>0</SpecialDayEntryDayId></SpecialDay></SpecialDays></CodeTableSpecialDay></TimeOfUse>";

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ActivatePassiveCalendar ActivationTime=\"1970/01/01 01:00:00\"> </ActivatePassiveCalendar>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<TimeOfUse>" + ProtocolTools.compress(xmlEncodedCodeTable) + "</TimeOfUse>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PLCConfigurationDeviceMessage.SetPlcChannelFreqSnrCredits.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetPlcChannelFreqSnrCredits CHANNEL1_FS=\"1\" CHANNEL1_FM=\"1\" CHANNEL1_SNR=\"1\" CHANNEL1_CREDITWEIGHT=\"1\" CHANNEL2_FS=\"1\" CHANNEL2_FM=\"1\" CHANNEL2_SNR=\"1\" CHANNEL2_CREDITWEIGHT=\"1\" CHANNEL3_FS=\"1\" CHANNEL3_FM=\"1\" CHANNEL3_SNR=\"1\" CHANNEL3_CREDITWEIGHT=\"1\" CHANNEL4_FS=\"1\" CHANNEL4_FM=\"1\" CHANNEL4_SNR=\"1\" CHANNEL4_CREDITWEIGHT=\"1\" CHANNEL5_FS=\"1\" CHANNEL5_FM=\"1\" CHANNEL5_SNR=\"1\" CHANNEL5_CREDITWEIGHT=\"1\" CHANNEL6_FS=\"1\" CHANNEL6_FM=\"1\" CHANNEL6_SNR=\"1\" CHANNEL6_CREDITWEIGHT=\"1\"> </SetPlcChannelFreqSnrCredits>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.DecommissionAll.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DecommissionAll> </DecommissionAll>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpdate><IncludedFile>path</IncludedFile></FirmwareUpdate>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AS220(propertySpecService, this.calendarFinder, this.calendarExtractor);
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new TestAS220DLMSMessageConverter(propertySpecService, this.nlsService, this.converter, this.calendarExtractor, this.deviceMessageFileExtractor);
    }

    /**
     * Gets the value to use for the given {@link com.energyict.mdc.upl.properties.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            try {
                return dateTimeFormat.parse("1970/01/01 01:00:00");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (propertySpec.getName().equals(specialDaysAttributeName) || propertySpec.getName().equals(activityCalendarAttributeName)) {
            return mock(TariffCalendar.class);
        } else if (propertySpec.getName().equals(activityCalendarNameAttributeName)) {
            return "STIJNVA";
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            return "path";
        }
        return "1";     //All other attribute values are "1"
    }

    /**
     * Used to test to actual G3MeterMessageConverter.
     * This class overrides the convertCodeTableToXML method, this way we don't have to mock an entire codetable in order to get a decent XML description.
     */
    public class TestAS220DLMSMessageConverter extends AS220DLMSMessageConverter {
        public TestAS220DLMSMessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor tariffCalendarExtractor, DeviceMessageFileExtractor deviceMessageFileExtractor) {
            super(propertySpecService, nlsService, converter, tariffCalendarExtractor);
        }

        @Override
        protected String convertCodeTableToXML(TariffCalendar messageAttribute, TariffCalendarExtractor extractor) {
            return xmlEncodedCodeTable;
        }
    }
}