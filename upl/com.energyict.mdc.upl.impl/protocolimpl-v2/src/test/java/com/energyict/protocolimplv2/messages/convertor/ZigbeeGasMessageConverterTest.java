package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PricingInformationMessage;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link ZigbeeGasMessageConverter} component.
 *
 * @author sva
 * @since 28/10/13 - 11:37
 */
@RunWith(MockitoJUnitRunner.class)
public class ZigbeeGasMessageConverterTest extends AbstractMessageConverterTest {

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

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ChangeOfSupplier);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier Change_Of_Supplier_Name=\"Name\" Change_Of_Supplier_ID=\"1\" Change_Of_Supplier_ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Supplier>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ChangeOfTenancy);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Tenant Change_Of_Tenant_ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Tenant>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<RemoteConnect> </RemoteConnect>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_CLOSE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<RemoteDisconnect> </RemoteDisconnect>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SetCalorificValueAndActivationDate);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetCalorificValueAndActivationDate Calorific value=\"1\" Activation date (dd/mm/yyyy hh:mm:ss) (optional)=\"28/10/2013 10:00:00\"> </SetCalorificValueAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SetConversionFactorAndActivationDate);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetConversionFactorAndActivationDate Conversion factor=\"1\" Activation date (dd/mm/yyyy hh:mm:ss) (optional)=\"28/10/2013 10:00:00\"> </SetConversionFactorAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DisplayDeviceMessage.SET_DISPLAY_MESSAGE_WITH_OPTIONS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<TextToDisplay Message=\"Message\" Duration of message=\"1\" Activation date (dd/mm/yyyy hh:mm:ss) (optional)=\"28/10/2013 10:00:00\"> </TextToDisplay>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade UserFileID=\"1\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade UserFileID=\"1\" Activation_date=\"28/10/2013 10:00:00\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.ReadPricingInformation);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ReadPricePerUnit> </ReadPricePerUnit>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SetPricingInformation);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetPricePerUnit><IncludedFile>Content</IncludedFile><ActivationDate>28/10/2013 10:00:00</ActivationDate></SetPricePerUnit>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SetStandingChargeAndActivationDate);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetStandingChargeAndActivationDate Standing charge=\"1\" Activation date (dd/mm/yyyy hh:mm:ss) (optional)=\"28/10/2013 10:00:00\"> </SetStandingChargeAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.UpdatePricingInformation);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Update_Pricing_Information><IncludedFile>Content</IncludedFile></Update_Pricing_Information>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(AdvancedTestMessage.USERFILE_CONFIG);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Test_Message Test_File=\"10\"> </Test_Message>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        Assert.assertEquals(ExpectedActivityCalendarMessageContent, messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new ZigbeeGas();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        ZigbeeGasMessageConverter messageConverter = spy(new ZigbeeGasMessageConverter());
        // We stub the encode method, cause CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable() is not subject of this test
        doReturn(XMLEncodedActivityCalendar).when(messageConverter).encode(any(Code.class));
        return messageConverter;
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.ChangeOfSupplierName:
                    return "Name";
                case DeviceMessageConstants.DisplayMessageAttributeName:
                    return "Message";
                case DeviceMessageConstants.CalorificValue:
                case DeviceMessageConstants.ConversionFactor:
                case DeviceMessageConstants.StandingChargeAttributeName:
                case DeviceMessageConstants.ChangeOfSupplierID:
                case DeviceMessageConstants.DisplayMessageTimeDurationAttributeName:
                    return "1";
                case DeviceMessageConstants.PricingInformationUserFileAttributeName:
                    UserFile mockedUserFile = mock(UserFile.class);
                    when(mockedUserFile.loadFileInByteArray()).thenReturn("Content".getBytes(Charset.forName("UTF-8")));
                    return mockedUserFile;
                case DeviceMessageConstants.UserFileConfigAttributeName:
                    mockedUserFile = mock(UserFile.class);
                    when(mockedUserFile.getId()).thenReturn(10);
                    return mockedUserFile;
                case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                    mockedUserFile = mock(UserFile.class);
                    when(mockedUserFile.getId()).thenReturn(1);
                    return mockedUserFile;
                case DeviceMessageConstants.DisplayMessageActivationDate:
                case DeviceMessageConstants.ConfigurationChangeActivationDate:
                case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                case DeviceMessageConstants.PricingInformationActivationDateAttributeName:
                    return europeanDateTimeFormat.parse("28/10/2013 10:00:00");
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