package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.p1InformationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.xmlConfigAttributeName;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link Dsmr23MessageConverter} component
 * <p>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 15:19
 */
@RunWith(MockitoJUnitRunner.class)
public class Dsmr23MessageConverterTest extends AbstractMessageConverterTest {

    private static final String METER_SERIAL_NUMBER = "SomeSerialNumber";
    private static final ObisCode LOAD_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode OBISCODE1 = ObisCode.fromString("1.0.1.8.0.255");
    private static final ObisCode OBISCODE2 = ObisCode.fromString("1.0.2.8.0.255");
    private static final Unit UNIT = Unit.get("kWh");
    private static final String MRID1 = "0.0.1.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private static final String MRID2 = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";

    @Mock
    private TariffCalendarFinder tariffCalendarFinder;
    @Mock
    private DeviceMessageFileFinder deviceMessageFileFinder;
    @Mock
    private TariffCalendarExtractor calendarExtractor;
    @Mock
    private KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    @Mock
    private DeviceMessageFileExtractor messageFileExtractor;
    @Mock
    private LoadProfileExtractor loadProfileExtractor;
    @Mock
    private NumberLookupExtractor numberLookupExtractor;
    @Mock
    private NumberLookupFinder numberLookupFinder;

    private Dsmr23MessageConverter dsmr23MessageConverter;

    @Before
    public void beforeEachTest() {
        Messaging smartMeterProtocol = new WebRTUKP(propertySpecService, tariffCalendarFinder, calendarExtractor, messageFileExtractor, deviceMessageFileFinder, numberLookupExtractor, numberLookupFinder);
        dsmr23MessageConverter = new Dsmr23MessageConverter(propertySpecService, this.nlsService, this.converter, this.loadProfileExtractor, this.numberLookupExtractor, this.calendarExtractor, this.keyAccessorTypeExtractor);
        dsmr23MessageConverter.setMessagingProtocol(smartMeterProtocol);
    }

    @Test
    public void formatContactorModeTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(contactorModeAttributeName);
        final BigDecimal modeAttribute = new BigDecimal(3);

        // business method
        final String modeAttributeFormatting = dsmr23MessageConverter.format(propertySpec, modeAttribute);

        // asserts
        assertThat(modeAttributeFormatting).isEqualTo(modeAttribute.toString());
    }

    @Test
    public void formatActivationDateTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(contactorActivationDateAttributeName);
        long millis = 1363101865123L;
        Date currentDate = new Date(millis);

        // business method
        final String currentDateFormatting = dsmr23MessageConverter.format(propertySpec, currentDate);

        // asserts
        assertThat(currentDateFormatting).isEqualTo(String.valueOf(millis / 1000));
    }

    @Test
    public void formatFirmwareUpgradeActionDateTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(firmwareUpdateActivationDateAttributeName);
        long millis = 1363735265123L;
        Date currentDate = new Date(millis);

        // business method
        final String currentDateFormatting = dsmr23MessageConverter.format(propertySpec, currentDate);

        // asserts
        assertThat(currentDateFormatting).isEqualTo(String.valueOf(millis / 1000));
    }

    @Test
    public void formatFirmwareUpgradeUserFileTest() {
        String firmwareFilePath = "path";
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(firmwareUpdateFileAttributeName);

        // business method
        final String userFileFormatting = dsmr23MessageConverter.format(propertySpec, firmwareFilePath);

        // asserts
        assertThat(userFileFormatting).isEqualTo(firmwareFilePath);
    }

    @Test
    public void formatActivityCalendarNameTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(activityCalendarNameAttributeName);
        final String calendarNameAttribute = "ThisIsMyTestCalendarName";

        // business method
        final String calendarNameFormatting = dsmr23MessageConverter.format(propertySpec, calendarNameAttribute);

        // asserts
        assertThat(calendarNameFormatting).isEqualTo(calendarNameAttribute);
    }

    @Test
    public void formatActivityCalendarCodeTableTest() {
        final int codeTableId = 324532;
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(activityCalendarAttributeName);
        TariffCalendar codeAttribute = mock(TariffCalendar.class);
        when(calendarExtractor.id(codeAttribute)).thenReturn(String.valueOf(codeTableId));

        // business method
        final String codeTableFormatting = dsmr23MessageConverter.format(propertySpec, codeAttribute);

        // asserts
        assertThat(codeTableFormatting).isEqualTo(String.valueOf(codeTableId));
    }

    @Test
    public void formatActivityCalendarActivationDateTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(activityCalendarActivationDateAttributeName);
        long millis = 1363735265123L;
        Date currentDate = new Date(millis);

        // business method
        final String currentDateFormatting = dsmr23MessageConverter.format(propertySpec, currentDate);

        // asserts
        assertThat(currentDateFormatting).isEqualTo(String.valueOf(millis / 1000));
    }

    @Test
    public void formatEncryptionLevelTest() {
        PropertySpec encryptionAttribute = mock(PropertySpec.class);
        when(encryptionAttribute.getName()).thenReturn(encryptionLevelAttributeName);

        // business method
        final String noEncryptionFormatting = dsmr23MessageConverter.format(encryptionAttribute, "No encryption");
        final String authenticationFormatting = dsmr23MessageConverter.format(encryptionAttribute, "Data authentication");
        final String encryptionFormatting = dsmr23MessageConverter.format(encryptionAttribute, "Data encryption");
        final String authenticationAndEncryptionFormatting = dsmr23MessageConverter.format(encryptionAttribute, "Data authentication and encryption");

        // asserts
        assertThat(noEncryptionFormatting).isEqualTo("0");
        assertThat(authenticationFormatting).isEqualTo("1");
        assertThat(encryptionFormatting).isEqualTo("2");
        assertThat(authenticationAndEncryptionFormatting).isEqualTo("3");
    }

    @Test
    public void formatAuthenticationLevelTest() {
        PropertySpec authenticationAttribute = mock(PropertySpec.class);
        when(authenticationAttribute.getName()).thenReturn(authenticationLevelAttributeName);

        // business method
        final String noAuthentication = dsmr23MessageConverter.format(authenticationAttribute, "No authentication");
        final String lowLevelAuthentication = dsmr23MessageConverter.format(authenticationAttribute, "Low level authentication");
        final String manufacturerAuthentication = dsmr23MessageConverter.format(authenticationAttribute, "Manufacturer specific");
        final String highLevelMd5 = dsmr23MessageConverter.format(authenticationAttribute, "High level authentication - MD5");
        final String highLevelSha1 = dsmr23MessageConverter.format(authenticationAttribute, "High level authentication - SHA-1");
        final String highLevelGmac = dsmr23MessageConverter.format(authenticationAttribute, "High level authentication - GMAC");

        // asserts
        assertThat(noAuthentication).isEqualTo("0");
        assertThat(lowLevelAuthentication).isEqualTo("1");
        assertThat(manufacturerAuthentication).isEqualTo("2");
        assertThat(highLevelMd5).isEqualTo("3");
        assertThat(highLevelSha1).isEqualTo("4");
        assertThat(highLevelGmac).isEqualTo("5");
    }

    @Test
    public void formatApnTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(apnAttributeName);
        final String myApn = "com.test.energyict.apn";

        // business method
        final String formattedApn = dsmr23MessageConverter.format(propertySpec, myApn);

        // asserts
        assertThat(formattedApn).isEqualTo(myApn);
    }

    @Test
    public void formatUserNameTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(usernameAttributeName);
        final String myUserName = "MyUser_N@me";

        // business method
        final String formattedUserName = dsmr23MessageConverter.format(propertySpec, myUserName);

        // asserts
        assertThat(formattedUserName).isEqualTo(myUserName);
    }

    @Test
    public void formatPasswordTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(passwordAttributeName);
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorTypeExtractor.passiveValueContent(keyAccessorType)).thenReturn("MyPr1v@t€P@55wd");

        // business method
        final String formattedPassword = dsmr23MessageConverter.format(propertySpec, keyAccessorType);

        // asserts
        assertThat(formattedPassword).isEqualTo("MyPr1v@t€P@55wd");
    }

    @Test
    public void formatWhiteListPhoneNumbers() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(whiteListPhoneNumbersAttributeName);
        final String phoneNumbersOfWhiteList = "0477993322;+32485124578;00352478123";

        // business method
        final String formattedPhoneNumbersOfWhiteList = dsmr23MessageConverter.format(propertySpec, phoneNumbersOfWhiteList);

        // asserts
        assertThat(formattedPhoneNumbersOfWhiteList).isEqualTo(phoneNumbersOfWhiteList);
    }

    @Test
    public void formatP1Information() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(p1InformationAttributeName);
        final String p1Information = "SomeTextOrCodeToSendToTheP1Port";

        // business method
        final String formattedP1Information = dsmr23MessageConverter.format(propertySpec, p1Information);

        // asserts
        assertThat(formattedP1Information).isEqualTo(p1Information);
    }

    @Test
    public void formatNormalThresholdTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(normalThresholdAttributeName);
        final String normalThresholdValue = "6";
        final BigDecimal normalThreshold = new BigDecimal(normalThresholdValue);

        // business method
        final String formattedNormalThreshold = dsmr23MessageConverter.format(propertySpec, normalThreshold);

        // asserts
        assertThat(formattedNormalThreshold).isEqualTo(normalThresholdValue);
    }

    @Test
    public void formatEmergencyThresholdTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyThresholdAttributeName);
        final String emergencyThresholdValue = "2";
        final BigDecimal emergencyThreshold = new BigDecimal(emergencyThresholdValue);

        // business method
        final String formattedEmergencyThreshold = dsmr23MessageConverter.format(propertySpec, emergencyThreshold);

        // asserts
        assertThat(formattedEmergencyThreshold).isEqualTo(emergencyThresholdValue);
    }

    @Test
    public void formatOverThresholdDurationTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(overThresholdDurationAttributeName);
        final String overThresholdDurationValue = "3600";
        final Duration overThresholdDuration = Duration.ofHours(1);

        // business method
        final String formattedOverThresholdDuration = dsmr23MessageConverter.format(propertySpec, overThresholdDuration);

        // asserts
        assertThat(formattedOverThresholdDuration).isEqualTo(overThresholdDurationValue);
    }

    @Test
    public void formatEmergencyProfileIdTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyProfileIdAttributeName);
        final String emergencyProfileIdValue = "6543";
        final BigDecimal emergencyProfileId = new BigDecimal(emergencyProfileIdValue);

        // business method
        final String formattedEmergencyProfileId = dsmr23MessageConverter.format(propertySpec, emergencyProfileId);

        // asserts
        assertThat(formattedEmergencyProfileId).isEqualTo(emergencyProfileIdValue);
    }

    @Test
    public void formatEmergencyProfileActivationDateTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyProfileActivationDateAttributeName);
        final long timeInMilliSeconds = 1364983938654L;
        final Date activationDate = new Date(timeInMilliSeconds);

        // business method
        final String formattedActivationDate = dsmr23MessageConverter.format(propertySpec, activationDate);

        // asserts
        assertThat(formattedActivationDate).isEqualTo(String.valueOf(timeInMilliSeconds / 1000));
    }

    @Test
    public void formatEmergencyProfileDurationTest() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyProfileDurationAttributeName);
        final String emergencyProfileDurationValue = "259200";
        final Period emergencyProfileDuration = Period.ofDays(3);

        // business method
        final String formattedEmergencyProfileDuration = dsmr23MessageConverter.format(propertySpec, emergencyProfileDuration);

        // asserts
        assertThat(formattedEmergencyProfileDuration).isEqualTo(emergencyProfileDurationValue);
    }

    @Test
    public void formatEmergencyProfileLookupIdTest() {
        final int lookupId = 324532;
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyProfileGroupIdListAttributeName);
        NumberLookup lookupAttribute = mock(NumberLookup.class);
        when(numberLookupExtractor.id(lookupAttribute)).thenReturn(String.valueOf(lookupId));

        // business method
        final String formattedLookupId = dsmr23MessageConverter.format(propertySpec, lookupAttribute);

        // asserts
        assertThat(formattedLookupId).isEqualTo(String.valueOf(lookupId));
    }

    @Test
    public void formatXmlConfigTest() {
        final String xmlString = "<xml>someXml<t>blabla</t></xml>";
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(xmlConfigAttributeName);

        // business method
        final String formattedXml = dsmr23MessageConverter.format(propertySpec, xmlString);

        // asserts
        assertThat(formattedXml).isEqualTo(xmlString);
    }

    @Ignore("Hardcoded CET date. Needs refactoring")
    @Test
    public void formatFromDateTest() {
        Date fromDate = new Date(1367581336000L);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(fromDateAttributeName);

        // business method
        final String formattedFromDate = dsmr23MessageConverter.format(propertySpec, fromDate);

        // asserts
        assertThat(formattedFromDate).isEqualTo("2013/05/03 13:42:16 CEST");
    }

    @Ignore("Hardcoded CET date. Needs refactoring")
    @Test
    public void formatToDateTest() {
        Date fromDate = new Date(1367581336000L);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(toDateAttributeName);

        // business method
        final String formattedFromDate = dsmr23MessageConverter.format(propertySpec, fromDate);

        // asserts
        assertThat(formattedFromDate).isEqualTo("2013/05/03 13:42:16 CEST");
    }

    @Test
    public void formatLoadProfileAttributeTest() {
        final String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><LoadProfile LPId=\"0\" LPObisCode=\"1.0.99.1.0.255\" MSerial=\"SomeSerialNumber\"><Channels><Ch ID=\"SomeSerialNumber\" Id=\"0\" MRID=\"0.0.1.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.1.8.0.255\" Unit=\"kWh\"/><Ch ID=\"SomeSerialNumber\" Id=\"1\" MRID=\"0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.2.8.0.255\" Unit=\"kWh\"/></Channels><RtuRegs><Reg ID=\"SomeSerialNumber\" OC=\"1.0.1.8.0.255\" RegID=\"1\"/><Reg ID=\"SomeSerialNumber\" OC=\"1.0.2.8.0.255\" RegID=\"2\"/></RtuRegs></LoadProfile>";

        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfileExtractor.specDeviceObisCode(loadProfile)).thenReturn(LOAD_PROFILE_OBISCODE.toString());
        when(loadProfileExtractor.id(loadProfile)).thenReturn("0");
        when(loadProfileExtractor.deviceSerialNumber(loadProfile)).thenReturn(METER_SERIAL_NUMBER);
        LoadProfileExtractor.Channel channel1 = mock(LoadProfileExtractor.Channel.class);
        when(channel1.obisCode()).thenReturn(OBISCODE1.toString());
        when(channel1.MRID()).thenReturn(MRID1);
        when(channel1.deviceSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(channel1.unit()).thenReturn(UNIT.toString());
        LoadProfileExtractor.Channel channel2 = mock(LoadProfileExtractor.Channel.class);
        when(channel2.obisCode()).thenReturn(OBISCODE2.toString());
        when(channel2.MRID()).thenReturn(MRID2);
        when(channel2.deviceSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(channel2.unit()).thenReturn(UNIT.toString());
        when(loadProfileExtractor.channels(loadProfile)).thenReturn(Arrays.asList(channel1, channel2));

        LoadProfileExtractor.Register register1 = mock(LoadProfileExtractor.Register.class);
        when(register1.obisCode()).thenReturn(OBISCODE1.toString());
        when(register1.deviceSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(register1.getRegisterId()).thenReturn(1);
        LoadProfileExtractor.Register register2 = mock(LoadProfileExtractor.Register.class);
        when(register2.obisCode()).thenReturn(OBISCODE2.toString());
        when(register2.deviceSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(register2.getRegisterId()).thenReturn(2);
        when(loadProfileExtractor.registers(loadProfile)).thenReturn(Arrays.asList(register1, register2));

        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(loadProfileAttributeName);

        // business method
        final String formattedLoadProfile = dsmr23MessageConverter.format(propertySpec, loadProfile);

        // assert
        assertThat(formattedLoadProfile).isEqualTo(expectedXml);
    }

    @Test
    public void contactorOpenTest() {
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        when(contactorOpen.getDeviceMessageId()).thenReturn(ContactorDeviceMessage.CONTACTOR_OPEN.id());
        when(contactorOpen.getSpecification()).thenReturn(ContactorDeviceMessage.CONTACTOR_OPEN.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<disconnectLoad> </disconnectLoad>");
    }

    @Test
    public void installMBusMessageTest() {
        OfflineDeviceMessage installMBusMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(DeviceMessageConstants.mbusChannel);
        when(offlineDeviceMessageAttribute.getValue()).thenReturn("1");
        doReturn(Collections.singletonList(offlineDeviceMessageAttribute)).when(installMBusMessage).getDeviceMessageAttributes();
        when(installMBusMessage.getDeviceMessageId()).thenReturn(MBusSetupDeviceMessage.Commission_With_Channel.id());
        when(installMBusMessage.getSpecification()).thenReturn(MBusSetupDeviceMessage.Commission_With_Channel.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(installMBusMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Mbus_Install Mbus_channel=\"1\"> </Mbus_Install>");
    }

    @Test
    public void contactorOpenWithActivationDateTest() {
        final long millis = 1234567890321L;
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(contactorActivationDateAttributeName);
        when(offlineDeviceMessageAttribute.getValue()).thenReturn(String.valueOf(millis));
        doReturn(Collections.singletonList(offlineDeviceMessageAttribute)).when(contactorOpen).getDeviceMessageAttributes();
        when(contactorOpen.getDeviceMessageId()).thenReturn(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.id());
        when(contactorOpen.getSpecification()).thenReturn(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<disconnectLoad Activation_date=\"1234567890321\"> </disconnectLoad>");
    }

    @Test
    public void contactorCloseTest() {
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        when(contactorOpen.getDeviceMessageId()).thenReturn(ContactorDeviceMessage.CONTACTOR_CLOSE.id());
        when(contactorOpen.getSpecification()).thenReturn(ContactorDeviceMessage.CONTACTOR_CLOSE.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<connectLoad> </connectLoad>");
    }

    @Test
    public void contactorCloseWithActivationDateTest() {
        final long millis = 1234567890321L;
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(contactorActivationDateAttributeName);
        when(offlineDeviceMessageAttribute.getValue()).thenReturn(String.valueOf(millis));
        doReturn(Collections.singletonList(offlineDeviceMessageAttribute)).when(contactorOpen).getDeviceMessageAttributes();
        when(contactorOpen.getDeviceMessageId()).thenReturn(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.id());
        when(contactorOpen.getSpecification()).thenReturn(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<connectLoad Activation_date=\"1234567890321\"> </connectLoad>");
    }

    @Test
    public void changeContactorModeTest() {
        final BigDecimal mode = new BigDecimal("3");
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(contactorModeAttributeName);
        when(offlineDeviceMessageAttribute.getValue()).thenReturn(mode.toString());
        doReturn(Collections.singletonList(offlineDeviceMessageAttribute)).when(contactorOpen).getDeviceMessageAttributes();
        when(contactorOpen.getDeviceMessageId()).thenReturn(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.id());
        when(contactorOpen.getSpecification()).thenReturn(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Connect_control_mode Mode=\"3\"> </Connect_control_mode>");
    }

    @Test
    public void upgradeFirmwareWithUserFileTest() {
        OfflineDeviceMessage firmwareUpgrade = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(firmwareUpdateFileAttributeName);
        when(offlineDeviceMessageAttribute.getValue()).thenReturn("path");
        doReturn(Collections.singletonList(offlineDeviceMessageAttribute)).when(firmwareUpgrade).getDeviceMessageAttributes();
        when(firmwareUpgrade.getDeviceMessageId()).thenReturn(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.id());
        when(firmwareUpgrade.getSpecification()).thenReturn(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(firmwareUpgrade);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<FirmwareUpgrade FirmwareFilePath=\"path\"> </FirmwareUpgrade>");
    }

    @Test
    public void upgradeFirmwareWithUserFileAndActivationDateTest() {
        final long millis = 1234567890321L;
        OfflineDeviceMessage firmwareUpgrade = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute userFileMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(userFileMessageAttribute.getName()).thenReturn(firmwareUpdateFileAttributeName);
        when(userFileMessageAttribute.getValue()).thenReturn("path");
        OfflineDeviceMessageAttribute activationDateMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(activationDateMessageAttribute.getName()).thenReturn(firmwareUpdateActivationDateAttributeName);
        when(activationDateMessageAttribute.getValue()).thenReturn(String.valueOf(millis));
        doReturn(Arrays.asList(userFileMessageAttribute, activationDateMessageAttribute)).when(firmwareUpgrade).getDeviceMessageAttributes();
        when(firmwareUpgrade.getDeviceMessageId()).thenReturn(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE.id());
        when(firmwareUpgrade.getSpecification()).thenReturn(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(firmwareUpgrade);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<FirmwareUpgrade FirmwareFilePath=\"path\" Activation_date=\"1234567890321\"> </FirmwareUpgrade>");
    }

    @Test
    public void activityCalendarConfigurationTest() {
        final String calendarName = "ActivityCalendarTestName";
        final int codeTableId = 324532;
        OfflineDeviceMessage activityCalendarConfiguration = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute codeTableMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(codeTableMessageAttribute.getName()).thenReturn(activityCalendarAttributeName);
        when(codeTableMessageAttribute.getValue()).thenReturn(String.valueOf(codeTableId));
        OfflineDeviceMessageAttribute activityCalendarNameMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(activityCalendarNameMessageAttribute.getName()).thenReturn(activityCalendarNameAttributeName);
        when(activityCalendarNameMessageAttribute.getValue()).thenReturn(calendarName);
        doReturn(Arrays.asList(activityCalendarNameMessageAttribute, codeTableMessageAttribute)).when(activityCalendarConfiguration).getDeviceMessageAttributes();
        when(activityCalendarConfiguration.getDeviceMessageId()).thenReturn(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND.id());
        when(activityCalendarConfiguration.getSpecification()).thenReturn(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(activityCalendarConfiguration);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Activity_Calendar Calendar_Name=\"" + calendarName + "\" Code_Table=\"" + String.valueOf(codeTableId) + "\"> </Activity_Calendar>");
    }

    @Test
    public void activityCalendarConfigurationWithActivationDateTest() {
        final String calendarName = "ActivityCalendarTestName";
        final int codeTableId = 324532;
        final long millis = 1234567890321L;
        OfflineDeviceMessage activityCalendarConfiguration = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute codeTableMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(codeTableMessageAttribute.getName()).thenReturn(activityCalendarAttributeName);
        when(codeTableMessageAttribute.getValue()).thenReturn(String.valueOf(codeTableId));
        OfflineDeviceMessageAttribute activityCalendarNameMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(activityCalendarNameMessageAttribute.getName()).thenReturn(activityCalendarNameAttributeName);
        when(activityCalendarNameMessageAttribute.getValue()).thenReturn(calendarName);
        OfflineDeviceMessageAttribute activationDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(activationDateAttribute.getName()).thenReturn(activityCalendarActivationDateAttributeName);
        when(activationDateAttribute.getValue()).thenReturn(String.valueOf(millis));
        doReturn(Arrays.asList(activityCalendarNameMessageAttribute, codeTableMessageAttribute, activationDateAttribute)).when(activityCalendarConfiguration).getDeviceMessageAttributes();
        when(activityCalendarConfiguration.getDeviceMessageId()).thenReturn(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME.id());
        when(activityCalendarConfiguration.getSpecification()).thenReturn(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(activityCalendarConfiguration);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Activity_Calendar Calendar_Name=\"" + calendarName + "\" Activation_Date=\""
                + String.valueOf(millis) + "\" Code_Table=\"" + String.valueOf(codeTableId) + "\"> </Activity_Calendar>");
    }

    @Test
    public void specialDayTableTest() {
        final int codeTableId = 324532;
        OfflineDeviceMessage activityCalendarConfiguration = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute codeTableMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(codeTableMessageAttribute.getName()).thenReturn(specialDaysAttributeName);
        when(codeTableMessageAttribute.getValue()).thenReturn(String.valueOf(codeTableId));
        doReturn(Collections.singletonList(codeTableMessageAttribute)).when(activityCalendarConfiguration).getDeviceMessageAttributes();
        when(activityCalendarConfiguration.getDeviceMessageId()).thenReturn(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND.id());
        when(activityCalendarConfiguration.getSpecification()).thenReturn(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(activityCalendarConfiguration);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Special_Days Code_Table=\"" + String.valueOf(codeTableId) + "\"> </Special_Days>");
    }

    @Test
    public void activateEncryptionTest() {
        String encryptionLevel = "3";
        OfflineDeviceMessage activateEncryptionLevelMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute encryptionLevelAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(encryptionLevelAttribute.getName()).thenReturn(encryptionLevelAttributeName);
        when(encryptionLevelAttribute.getValue()).thenReturn(encryptionLevel);
        doReturn(Collections.singletonList(encryptionLevelAttribute)).when(activateEncryptionLevelMessage).getDeviceMessageAttributes();
        when(activateEncryptionLevelMessage.getDeviceMessageId()).thenReturn(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION.id());
        when(activateEncryptionLevelMessage.getSpecification()).thenReturn(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(activateEncryptionLevelMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Activate_dataTransport_Security SecurityLevel=\"" + encryptionLevel + "\"> </Activate_dataTransport_Security>");
    }

    @Test
    public void changeAuthenticationLevelTest() {
        String authenticationLevel = "3";
        OfflineDeviceMessage changeAuthenticationLevel = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute authenticationLevelAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(authenticationLevelAttribute.getName()).thenReturn(authenticationLevelAttributeName);
        when(authenticationLevelAttribute.getValue()).thenReturn(authenticationLevel);
        doReturn(Collections.singletonList(authenticationLevelAttribute)).when(changeAuthenticationLevel).getDeviceMessageAttributes();
        when(changeAuthenticationLevel.getDeviceMessageId()).thenReturn(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL.id());
        when(changeAuthenticationLevel.getSpecification()).thenReturn(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(changeAuthenticationLevel);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Change_authentication_level AuthenticationLevel=\"" + authenticationLevel + "\"> </Change_authentication_level>");
    }

    @Test
    public void activateSmsWakeUpMechanismTest() {
        OfflineDeviceMessage activateSmsWakeUp = mock(OfflineDeviceMessage.class);
        when(activateSmsWakeUp.getDeviceMessageId()).thenReturn(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM.id());
        when(activateSmsWakeUp.getSpecification()).thenReturn(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(activateSmsWakeUp);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Activate_the_wakeup_mechanism/>");
    }

    @Test
    public void deActivateSmsWakeUpMechanismTest() {
        OfflineDeviceMessage deActivateSmsWakeUp = mock(OfflineDeviceMessage.class);
        when(deActivateSmsWakeUp.getDeviceMessageId()).thenReturn(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP.id());
        when(deActivateSmsWakeUp.getSpecification()).thenReturn(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(deActivateSmsWakeUp);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Deactive_the_wakeup_mechanism/>");
    }

    @Test
    public void gprsUserCredentialsMessageEntryTest() {
        final String myUserName = "MyTestUserN@me";
        final String myPassword = "MyDumm£T€stP@sswd";

        OfflineDeviceMessage gprsUserCredentials = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute userNameAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(userNameAttribute.getName()).thenReturn(usernameAttributeName);
        when(userNameAttribute.getValue()).thenReturn(myUserName);
        OfflineDeviceMessageAttribute passwordAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(passwordAttribute.getName()).thenReturn(passwordAttributeName);
        when(passwordAttribute.getValue()).thenReturn(myPassword);
        doReturn(Arrays.asList(userNameAttribute, passwordAttribute)).when(gprsUserCredentials).getDeviceMessageAttributes();
        when(gprsUserCredentials.getDeviceMessageId()).thenReturn(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS.id());
        when(gprsUserCredentials.getSpecification()).thenReturn(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(gprsUserCredentials);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<GPRS_modem_credentials Username=\"MyTestUserN@me\" Password=\"MyDumm£T€stP@sswd\"> </GPRS_modem_credentials>");
    }

    @Test
    public void apnCredentialsMessageEntryTest() {
        final String myUserName = "MyTestUserN@me";
        final String myPassword = "MyDumm£T€stP@sswd";
        final String myApn = "com.test.energyict.apn";

        OfflineDeviceMessage gprsApnCredentials = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute userNameAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(userNameAttribute.getName()).thenReturn(usernameAttributeName);
        when(userNameAttribute.getValue()).thenReturn(myUserName);
        OfflineDeviceMessageAttribute passwordAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(passwordAttribute.getName()).thenReturn(passwordAttributeName);
        when(passwordAttribute.getValue()).thenReturn(myPassword);
        OfflineDeviceMessageAttribute apnAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(apnAttribute.getName()).thenReturn(apnAttributeName);
        when(apnAttribute.getValue()).thenReturn(myApn);
        doReturn(Arrays.asList(userNameAttribute, passwordAttribute, apnAttribute)).when(gprsApnCredentials).getDeviceMessageAttributes();
        when(gprsApnCredentials.getDeviceMessageId()).thenReturn(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.id());
        when(gprsApnCredentials.getSpecification()).thenReturn(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(gprsApnCredentials);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<GPRS_modem_setup APN=\"com.test.energyict.apn\" Username=\"MyTestUserN@me\" Password=\"MyDumm£T€stP@sswd\"> </GPRS_modem_setup>");
    }

    @Test
    public void addPhoneNumbersToWhiteListTest() {
        final String allPhoneNumbers = "0477993322;+32485124578;00352478123";

        OfflineDeviceMessage whiteListPhoneNumbersDeviceMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute whiteListPhoneNumbersAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(whiteListPhoneNumbersAttribute.getName()).thenReturn(whiteListPhoneNumbersAttributeName);
        when(whiteListPhoneNumbersAttribute.getValue()).thenReturn(allPhoneNumbers);
        doReturn(Collections.singletonList(whiteListPhoneNumbersAttribute)).when(whiteListPhoneNumbersDeviceMessage).getDeviceMessageAttributes();
        when(whiteListPhoneNumbersDeviceMessage.getDeviceMessageId()).thenReturn(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST.id());
        when(whiteListPhoneNumbersDeviceMessage.getSpecification()).thenReturn(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(whiteListPhoneNumbersDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Phonenumbers_to_add Phonenumber1=\"0477993322\" Phonenumber2=\"+32485124578\" Phonenumber3=\"00352478123\"> </Phonenumbers_to_add>");
    }

    @Test
    public void sendCodeToP1PortTest() {
        final String p1CodeInformation = "dotdotdotdashdashdashdotdotdot";

        OfflineDeviceMessage p1CodeDeviceMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute p1CodeAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(p1CodeAttribute.getName()).thenReturn(p1InformationAttributeName);
        when(p1CodeAttribute.getValue()).thenReturn(p1CodeInformation);
        doReturn(Collections.singletonList(p1CodeAttribute)).when(p1CodeDeviceMessage).getDeviceMessageAttributes();
        when(p1CodeDeviceMessage.getDeviceMessageId()).thenReturn(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1.id());
        when(p1CodeDeviceMessage.getSpecification()).thenReturn(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(p1CodeDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Message_code_to_P1_port Code=\"dotdotdotdashdashdashdotdotdot\"> </Message_code_to_P1_port>");
    }

    @Test
    public void sendTextToP1PortTest() {
        final String p1TextInformation = "Sending out an S.O.S., Sending out an S.O.S., Sending out and S.O.S.";

        OfflineDeviceMessage p1TextDeviceMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute p1TextAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(p1TextAttribute.getName()).thenReturn(p1InformationAttributeName);
        when(p1TextAttribute.getValue()).thenReturn(p1TextInformation);
        doReturn(Collections.singletonList(p1TextAttribute)).when(p1TextDeviceMessage).getDeviceMessageAttributes();
        when(p1TextDeviceMessage.getDeviceMessageId()).thenReturn(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1.id());
        when(p1TextDeviceMessage.getSpecification()).thenReturn(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(p1TextDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Message_text_to_P1_port Text=\"Sending out an S.O.S., Sending out an S.O.S., Sending out and S.O.S.\"> </Message_text_to_P1_port>");
    }

    @Test
    public void globalMeterResetTest() {
        OfflineDeviceMessage globalMeterResetDeviceMessage = mock(OfflineDeviceMessage.class);
        when(globalMeterResetDeviceMessage.getDeviceMessageId()).thenReturn(DeviceActionMessage.GLOBAL_METER_RESET.id());
        when(globalMeterResetDeviceMessage.getSpecification()).thenReturn(DeviceActionMessage.GLOBAL_METER_RESET.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(globalMeterResetDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Global_Meter_Reset/>");
    }

    @Test
    public void configureLoadLimitParameters() {
        final String normalThreshold = "6";
        final String emergencyThreshold = "2";
        final String overThresholdDuration = "20";
        final String emergencyProfileId = "31";
        final String emergencyProfileDuration = "86400";
        final String emergencyProfileActivationDate = "1364988856654";

        OfflineDeviceMessage configureLoadLimitingParametersMessage = mock(OfflineDeviceMessage.class);

        OfflineDeviceMessageAttribute normalThresholdAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(normalThresholdAttribute.getName()).thenReturn(normalThresholdAttributeName);
        when(normalThresholdAttribute.getValue()).thenReturn(normalThreshold);

        OfflineDeviceMessageAttribute emergencyThresholdAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(emergencyThresholdAttribute.getName()).thenReturn(emergencyThresholdAttributeName);
        when(emergencyThresholdAttribute.getValue()).thenReturn(emergencyThreshold);

        OfflineDeviceMessageAttribute overThresholdDurationAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(overThresholdDurationAttribute.getName()).thenReturn(overThresholdDurationAttributeName);
        when(overThresholdDurationAttribute.getValue()).thenReturn(overThresholdDuration);

        OfflineDeviceMessageAttribute emergencyProfileIdAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(emergencyProfileIdAttribute.getName()).thenReturn(emergencyProfileIdAttributeName);
        when(emergencyProfileIdAttribute.getValue()).thenReturn(emergencyProfileId);

        OfflineDeviceMessageAttribute emergencyProfileDurationAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(emergencyProfileDurationAttribute.getName()).thenReturn(emergencyProfileDurationAttributeName);
        when(emergencyProfileDurationAttribute.getValue()).thenReturn(emergencyProfileDuration);

        OfflineDeviceMessageAttribute emergencyProfileActivationDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(emergencyProfileActivationDateAttribute.getName()).thenReturn(emergencyProfileActivationDateAttributeName);
        when(emergencyProfileActivationDateAttribute.getValue()).thenReturn(emergencyProfileActivationDate);

        doReturn(Arrays.asList(normalThresholdAttribute, emergencyThresholdAttribute,
                overThresholdDurationAttribute, emergencyProfileIdAttribute, emergencyProfileDurationAttribute, emergencyProfileActivationDateAttribute)).when(configureLoadLimitingParametersMessage).getDeviceMessageAttributes();

        when(configureLoadLimitingParametersMessage.getDeviceMessageId()).thenReturn(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS.id());
        when(configureLoadLimitingParametersMessage.getSpecification()).thenReturn(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(configureLoadLimitingParametersMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Configure_load_limiting Normal_Threshold=\"6\" Emergency_Threshold=\"2\" Over_Threshold_Duration=\"20\"><Emergency_Profile EP_Profile_Id=\"31\" EP_Activation_Time=\"1364988856654\" EP_Duration=\"86400\"> </Emergency_Profile> </Configure_load_limiting>");
    }

    @Test
    public void setEmergencyProfileGroupIdTest() {
        final int lookupId = 324532;

        OfflineDeviceMessage setEmergencyProfileIdsMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute lookupTableAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(lookupTableAttribute.getName()).thenReturn(emergencyProfileGroupIdListAttributeName);
        when(lookupTableAttribute.getValue()).thenReturn(String.valueOf(lookupId));
        doReturn(Collections.singletonList(lookupTableAttribute)).when(setEmergencyProfileIdsMessage).getDeviceMessageAttributes();
        when(setEmergencyProfileIdsMessage.getDeviceMessageId()).thenReturn(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS.id());
        when(setEmergencyProfileIdsMessage.getSpecification()).thenReturn(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(setEmergencyProfileIdsMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<EP_GroupIds Lookup_Table_ID=\"324532\"> </EP_GroupIds>");
    }

    @Test
    public void clearLoadLimitConfigurationTest() {
        OfflineDeviceMessage clearLoadLimitConfigurationDeviceMessage = mock(OfflineDeviceMessage.class);
        when(clearLoadLimitConfigurationDeviceMessage.getDeviceMessageId()).thenReturn(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION.id());
        when(clearLoadLimitConfigurationDeviceMessage.getSpecification()).thenReturn(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(clearLoadLimitConfigurationDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Disable_load_limitng/>");
    }

    @Test
    public void sendXmlConfigTest() {
        final String xmlString = "<SomeXml><></><bla>Tralalala<bla/><></><SomeXml/>";

        OfflineDeviceMessage xmlConfigDeviceMessage = mock(OfflineDeviceMessage.class);
        when(xmlConfigDeviceMessage.getDeviceMessageId()).thenReturn(AdvancedTestMessage.XML_CONFIG.id());
        when(xmlConfigDeviceMessage.getSpecification()).thenReturn(AdvancedTestMessage.XML_CONFIG.get(propertySpecService, nlsService, converter));
        OfflineDeviceMessageAttribute xmlConfigAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(xmlConfigAttribute.getName()).thenReturn(xmlConfigAttributeName);
        when(xmlConfigAttribute.getValue()).thenReturn(xmlString);
        doReturn(Collections.singletonList(xmlConfigAttribute)).when(xmlConfigDeviceMessage).getDeviceMessageAttributes();

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(xmlConfigDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<XMLConfig><SomeXml><></><bla>Tralalala<bla/><></><SomeXml/></XMLConfig>");
    }

    @Test
    public void sendPartialLoadProfileTest() {
        final String expectedMessageContent = "<PartialLoadProfile EndTime=\"13/03/2013 11:32:25\" LPId=\"821\" LPObisCode=\"0.0.98.1.0.255\" MSerial=\"SomeSerialNumber\" StartTime=\"06/02/2013 10:00:25\"><Channels><Ch ID=\"SomeSerialNumber\" Id=\"0\" MRID=\"0.0.1.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.1.8.1.255\" Unit=\"kWh\"/><Ch ID=\"SomeSerialNumber\" Id=\"1\" MRID=\"0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.1.8.2.255\" Unit=\"kWh\"/><Ch ID=\"SomeSerialNumber\" Id=\"2\" MRID=\"0.0.3.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.2.8.1.255\" Unit=\"kWh\"/><Ch ID=\"SomeSerialNumber\" Id=\"3\" MRID=\"0.0.4.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.2.8.2.255\" Unit=\"kWh\"/></Channels></PartialLoadProfile>";
        final String loadProfileAttributeValue = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><LoadProfile LPObisCode=\"0.0.98.1.0.255\" MSerial=\"SomeSerialNumber\" StartTime=\"06/02/2013 10:00:25\" EndTime=\"13/03/2013 11:32:25\" LPId=\"821\"><Channels><Ch Id=\"0\" MRID=\"0.0.1.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.1.8.1.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"1\" MRID=\"0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.1.8.2.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"2\" MRID=\"0.0.3.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.2.8.1.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"3\" MRID=\"0.0.4.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0\" Name=\"1.0.2.8.2.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /></Channels><RtuRegs><Reg ID=\"SomeSerialNumber\" OC=\"1.0.1.8.0.255\" RegID=\"1\"/><Reg ID=\"SomeSerialNumber\" OC=\"1.0.2.8.0.255\" RegID=\"2\"/></RtuRegs></LoadProfile>";

        OfflineDeviceMessage partialLoadProfileDeviceMessage = mock(OfflineDeviceMessage.class);
        when(partialLoadProfileDeviceMessage.getDeviceMessageId()).thenReturn(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST.id());
        when(partialLoadProfileDeviceMessage.getSpecification()).thenReturn(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST.get(propertySpecService, nlsService, converter));
        OfflineDeviceMessageAttribute loadProfileAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(loadProfileAttribute.getName()).thenReturn(loadProfileAttributeName);
        when(loadProfileAttribute.getValue()).thenReturn(loadProfileAttributeValue);
        OfflineDeviceMessageAttribute fromDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(fromDateAttribute.getName()).thenReturn(fromDateAttributeName);
        when(fromDateAttribute.getValue()).thenReturn("06/02/2013 10:00:25");
        OfflineDeviceMessageAttribute toDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(toDateAttribute.getName()).thenReturn(toDateAttributeName);
        when(toDateAttribute.getValue()).thenReturn("13/03/2013 11:32:25");

        doReturn(Arrays.asList(loadProfileAttribute, fromDateAttribute, toDateAttribute)).when(partialLoadProfileDeviceMessage).getDeviceMessageAttributes();

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(partialLoadProfileDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo(expectedMessageContent);
    }

    @Test
    public void sendRegisterRequestFromLoadProfilesTest() {
        final String expectedMessageContent = "<LoadProfileRegister LPId=\"821\" LPObisCode=\"0.0.98.1.0.255\" MSerial=\"SomeSerialNumber\" StartTime=\"06/02/2013 10:00:25\"><RtuRegs><Reg ID=\"SomeSerialNumber\" OC=\"1.0.1.8.0.255\" RegID=\"1\"/><Reg ID=\"SomeSerialNumber\" OC=\"1.0.2.8.0.255\" RegID=\"2\"/></RtuRegs></LoadProfileRegister>";
        final String loadProfileAttributeValue = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><LoadProfile LPObisCode=\"0.0.98.1.0.255\" MSerial=\"SomeSerialNumber\" StartTime=\"06/02/2013 10:00:25\" EndTime=\"13/03/2013 11:32:25\" LPId=\"821\"><Channels><Ch Id=\"0\" Name=\"1.0.1.8.1.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"1\" Name=\"1.0.1.8.2.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"2\" Name=\"1.0.2.8.1.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"3\" Name=\"1.0.2.8.2.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /></Channels><RtuRegs><Reg ID=\"SomeSerialNumber\" OC=\"1.0.1.8.0.255\" RegID=\"1\"/><Reg ID=\"SomeSerialNumber\" OC=\"1.0.2.8.0.255\" RegID=\"2\"/></RtuRegs></LoadProfile>";

        OfflineDeviceMessage registerRequestDeviceMessage = mock(OfflineDeviceMessage.class);
        when(registerRequestDeviceMessage.getDeviceMessageId()).thenReturn(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST.id());
        when(registerRequestDeviceMessage.getSpecification()).thenReturn(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST.get(propertySpecService, nlsService, converter));
        OfflineDeviceMessageAttribute loadProfileAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(loadProfileAttribute.getName()).thenReturn(loadProfileAttributeName);
        when(loadProfileAttribute.getValue()).thenReturn(loadProfileAttributeValue);
        OfflineDeviceMessageAttribute fromDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(fromDateAttribute.getName()).thenReturn(fromDateAttributeName);
        when(fromDateAttribute.getValue()).thenReturn("06/02/2013 10:00:25");
        doReturn(Arrays.asList(loadProfileAttribute, fromDateAttribute)).when(registerRequestDeviceMessage).getDeviceMessageAttributes();

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(registerRequestDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo(expectedMessageContent);
    }

    @Test
    public void changeEncryptionKeyTest() {

        OfflineDeviceMessage changeEncryptionKeyMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute newEncryptionKeyAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(newEncryptionKeyAttribute.getName()).thenReturn(newEncryptionKeyAttributeName);
        when(newEncryptionKeyAttribute.getValue()).thenReturn("00112233445566778899");

        doReturn(Collections.singletonList(newEncryptionKeyAttribute)).when(changeEncryptionKeyMessage).getDeviceMessageAttributes();
        when(changeEncryptionKeyMessage.getDeviceMessageId()).thenReturn(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY.id());
        when(changeEncryptionKeyMessage.getSpecification()).thenReturn(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(changeEncryptionKeyMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Change_DataTransportEncryptionKey NewEncryptionKey=\"00112233445566778899\"> </Change_DataTransportEncryptionKey>");
    }

    @Test
    public void changeAuthenticationKeyTest() {

        OfflineDeviceMessage changeAuthenticationKeyMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute newAuthenticationKeyAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(newAuthenticationKeyAttribute.getName()).thenReturn(newAuthenticationKeyAttributeName);
        when(newAuthenticationKeyAttribute.getValue()).thenReturn("00112233445566778899");

        doReturn(Collections.singletonList(newAuthenticationKeyAttribute)).when(changeAuthenticationKeyMessage).getDeviceMessageAttributes();
        when(changeAuthenticationKeyMessage.getDeviceMessageId()).thenReturn(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY.id());
        when(changeAuthenticationKeyMessage.getSpecification()).thenReturn(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(changeAuthenticationKeyMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Change_DataTransportAuthenticationKey NewAuthenticationKey=\"00112233445566778899\"> </Change_DataTransportAuthenticationKey>");
    }

    @Test
    public void changeHLSSecretTest() {
        OfflineDeviceMessage changePasswordMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute newAuthenticationKeyAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(newAuthenticationKeyAttribute.getName()).thenReturn(newPasswordAttributeName);
        when(newAuthenticationKeyAttribute.getValue()).thenReturn("00112233445566778899");

        doReturn(Collections.singletonList(newAuthenticationKeyAttribute)).when(changePasswordMessage).getDeviceMessageAttributes();
        when(changePasswordMessage.getDeviceMessageId()).thenReturn(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD.id());
        when(changePasswordMessage.getSpecification()).thenReturn(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = dsmr23MessageConverter.toMessageEntry(changePasswordMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Change_HLS_Secret HLSSecret=\"00112233445566778899\"> </Change_HLS_Secret>");
    }
}