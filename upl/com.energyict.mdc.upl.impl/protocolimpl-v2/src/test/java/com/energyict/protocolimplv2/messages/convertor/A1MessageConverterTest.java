package com.energyict.protocolimplv2.messages.convertor;

import com.elster.protocolimpl.dlms.A1;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 13/08/2015 - 17:01
 */
@RunWith(MockitoJUnitRunner.class)
public class A1MessageConverterTest extends AbstractMessageConverterTest {

    private static final String USER_FILE_XML = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><Calendar></Calendar>";
    private static final String TARIFF_CODE_EXPECTED_CONTENT = "<SetPassiveCalendar TARIFF_ACTIVATION_DATE=\"2015-08-01 00:00:00\" TARIFF_CALENDAR_FILE=\"<?xml version=''1.0'' encoding=''iso-8859-1''?><Calendar></Calendar>\"> </SetPassiveCalendar>";
    private static final String SPECIAL_DAYS_EXPECTED_CONTENT = "<SetSpecialDaysTable SPECIAL_DAYS_TABLE_FILE=\"<?xml version=''1.0'' encoding=''iso-8859-1''?><Calendar></Calendar>\"> </SetSpecialDaysTable>";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Mock
    private TariffCalendarFinder calendarFinder;
    @Mock
    private TariffCalendarExtractor calendarExtractor;
    @Mock
    private DeviceMessageFileExtractor messageFileExtractor;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;

    @Test
    public void testMessageConversion_ChangeCredentials() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_modem_setup APN=\"MyTestAPN\" Username=\"MyTestUserName\" Password=\"MyTestPassword\"> </GPRS_modem_setup>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_WriteNewPdrNumber() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.WriteNewPDRNumber.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WritePDR PdrToWrite=\"PDR\"> </WritePDR>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ClearPassiveTariff() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ClearPassiveTariff/>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ChangeSecurityKeys() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_SECURITY_KEYS.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ChangeKeys ClientId=\"1\" WrapperKey=\"MASTER_Key\" NewAuthenticationKey=\"AUTH_Key\" NewEncryptionKey=\"ENCR_Key\"> </ChangeKeys>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ChangeSessionTimeout() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ChangeSessionTimeout.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SessionTimeout SessionTimeout[ms]=\"10\"> </SessionTimeout>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_SetCyclicMode() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.SetCyclicMode.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<CyclicMode CallDistance=\"1 02:30:00\"> </CyclicMode>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_SetPreferredDateMode() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.SetPreferredDateMode.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<PreferredDateMode PreferredDate=\"1 02:30:00\"> </PreferredDateMode>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_SetWANConfiguration() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.SetWANConfiguration.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WanConfiguration Destination1=\"10.0.1.50:4059\" Destination2=\"11.1.2.20\"> </WanConfiguration>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ConfigureBillingPeriodStartDate() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ConfigureBillingPeriodStartDate.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<BillingPeriodStart BILLING_PERIOD_START_DATE=\"2015-08-01 SU\"> </BillingPeriodStart>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ConfigureBillingPeriodLength() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ConfigureBillingPeriodLength.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<BillingPeriod BILLING_PERIOD_LENGTH=\"30\"> </BillingPeriod>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_SetOnDemandBillingDate() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.WriteNewOnDemandBillingDate.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<OnDemandSnapshotTime DATE=\"2015-08-01 00:00:00\" REASON=\"1\"> </OnDemandSnapshotTime>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ChangeUnitStatus() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ChangeUnitStatus.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<UnitsStatus UNITS_Status=\"Maintenance\"> </UnitsStatus>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_AlarmRegisterReset() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(DeviceActionMessage.ALARM_REGISTER_RESET.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ResetAlarms/>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ResetEventLog() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(DeviceActionMessage.EVENT_LOG_RESET.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ResetUNITSLog/>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ConfigureClock() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ClockDeviceMessage.ConfigureClock.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetClockConfiguration TIMEZONE_OFFSET=\"120\" DST_ENABLED=\"1\" DST_DEVIATION=\"60\"> </SetClockConfiguration>", messageEntry.getContent());
    }
    @Test
    public void testMessageConversion_ConfigureStartOfGasDayDSTSettings() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ConfigureStartOfGasDaySettings.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GasDayConfiguration GDC_FLAG=\"0\"> </GasDayConfiguration>", messageEntry.getContent());
    }
    @Test
    public void testMessageConversion_ConfigureStartOfGasDay() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ConfigureStartOfGasDay.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<StartOfGasDay SGD_TIME=\"06:30:00\"> </StartOfGasDay>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ConfigureRSSIMultipleSampling() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ConfigureRSSIMultipleSampling.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<RSSIMultipleSampling RSSIMS_ACTION=\"1\"> </RSSIMultipleSampling>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_SpecialDayCalendarSendFromXMLUserFile() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_FROM_XML_USER_FILE.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals(SPECIAL_DAYS_EXPECTED_CONTENT, messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ActivityCalendarSendFromXMLUserFile() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDAR_SEND_WITH_DATETIME_FROM_XML_USER_FILE.get(this.propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals(TARIFF_CODE_EXPECTED_CONTENT, messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new A1(this.calendarFinder, this.calendarExtractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new A1MessageConverter(null, this.propertySpecService, this.nlsService, this.converter,  this.messageFileExtractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.apnAttributeName:
                    return "MyTestAPN";
                case DeviceMessageConstants.usernameAttributeName:
                    return "MyTestUserName";
                case DeviceMessageConstants.passwordAttributeName:
                    return new Password("MyTestPassword");
                case DeviceMessageConstants.clientMacAddress:
                    return BigDecimal.ONE;
                case DeviceMessageConstants.masterKey:
                    return new Password("MASTER_Key");
                case DeviceMessageConstants.newAuthenticationKeyAttributeName:
                    return new Password("AUTH_Key");
                case DeviceMessageConstants.newEncryptionKeyAttributeName:
                    return new Password("ENCR_Key");
                case DeviceMessageConstants.newPDRAttributeName:
                    return "PDR";
                case DeviceMessageConstants.sessionTimeoutAttributeName:
                    return BigDecimal.TEN;
                case DeviceMessageConstants.day:
                    return BigDecimal.ONE;
                case DeviceMessageConstants.hour:
                    return BigDecimal.valueOf(2);
                case DeviceMessageConstants.minute:
                    return BigDecimal.valueOf(30);
                case DeviceMessageConstants.second:
                    return BigDecimal.ZERO;
                case DeviceMessageConstants.Destination1IPAddressAttributeName:
                    return "10.0.1.50:4059";
                case DeviceMessageConstants.Destination2IPAddressAttributeName:
                    return "11.1.2.20";
                case DeviceMessageConstants.billingPeriodLengthAttributeName:
                    return BigDecimal.valueOf(30);
                case DeviceMessageConstants.year:
                    return BigDecimal.valueOf(2015);
                case DeviceMessageConstants.month:
                    return BigDecimal.valueOf(8);
                case DeviceMessageConstants.dayOfWeek:
                    return "SU";
                case DeviceMessageConstants.setOnDemandBillingDateAttributeName:
                    return dateFormat.parse("2015-08-01 00:00:00");
                case DeviceMessageConstants.OnDemandBillingReasonAttributeName:
                    return BigDecimal.ONE;
                case DeviceMessageConstants.UnitStatusAttributeName:
                    return "Maintenance";
                case DeviceMessageConstants.TimeZoneOffsetInHoursAttributeName:
                    return BigDecimal.valueOf(2);
                case DeviceMessageConstants.enableDSTAttributeName:
                    return true;
                case DeviceMessageConstants.DSTDeviationAttributeName:
                    return BigDecimal.valueOf(60);
                case DeviceMessageConstants.IgnoreDSTAttributeName:
                    return false;
                case DeviceMessageConstants.StartOfGasDayAttributeName:
                    return LocalTime.ofSecondOfDay(23400);
                case DeviceMessageConstants.enableRSSIMultipleSampling:
                    return true;
                case DeviceMessageConstants.XmlUserFileAttributeName:
                    UserFile userFile = mock(UserFile.class);
                    when(userFile.loadFileInByteArray()).thenReturn(USER_FILE_XML.getBytes(Charset.forName("US-ASCII")));
                    return userFile;
                case DeviceMessageConstants.activityCalendarActivationDateAttributeName:
                    return dateFormat.parse("2015-08-01 00:00:00");
                default:
                    return "";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}