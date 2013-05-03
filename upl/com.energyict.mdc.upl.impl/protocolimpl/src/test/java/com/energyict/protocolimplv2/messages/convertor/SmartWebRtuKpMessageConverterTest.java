package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.messages.DeviceMessageSpecFactoryImpl;
import com.energyict.mdw.amr.RegisterMapping;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.LoadProfileSpec;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SmartWebRtuKpMessageConverter} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 15:19
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartWebRtuKpMessageConverterTest {

    private static final String METER_SERIAL_NUMBER = "SomeSerialNumber";
    private static final ObisCode LOAD_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode OBISCODE1 = ObisCode.fromString("1.0.1.8.0.255");
    private static final ObisCode OBISCODE2 = ObisCode.fromString("1.0.2.8.0.255");
    private static final Unit UNIT = Unit.get("kWh");

    @Mock
    private ServerManager serverManager;

    @Before
    public void beforeEachTest() {
        when(serverManager.getDeviceMessageSpecFactory()).thenReturn(new DeviceMessageSpecFactoryImpl());
        ManagerFactory.setCurrent(serverManager);
    }

    @Test
    public void formatContactorModeTest() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(contactorModeAttributeName);
        final BigDecimal modeAttribute = new BigDecimal(3);

        // business method
        final String modeAttributeFormatting = smartWebRtuKpMessageConverter.format(propertySpec, modeAttribute);

        // asserts
        assertThat(modeAttributeFormatting).isEqualTo(modeAttribute.toString());
    }

    @Test
    public void formatActivationDateTest() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(contactorActivationDateAttributeName);
        long millis = 1363101865123L;
        Date currentDate = new Date(millis);

        // business method
        final String currentDateFormatting = smartWebRtuKpMessageConverter.format(propertySpec, currentDate);

        // asserts
        assertThat(currentDateFormatting).isEqualTo(String.valueOf(millis));
    }

    @Test
    public void formatFirmwareUpgradeActionDateTest() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(firmwareUpdateActivationDateAttributeName);
        long millis = 1363735265123L;
        Date currentDate = new Date(millis);

        // business method
        final String currentDateFormatting = smartWebRtuKpMessageConverter.format(propertySpec, currentDate);

        // asserts
        assertThat(currentDateFormatting).isEqualTo(String.valueOf(millis));
    }

    @Test
    public void formatFirmwareUpgradeUserFileTest() {
        final int userFileId = 324532;
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(firmwareUpdateUserFileAttributeName);
        UserFile userFile = mock(UserFile.class);
        when(userFile.getId()).thenReturn(userFileId);

        // business method
        final String userFileFormatting = smartWebRtuKpMessageConverter.format(propertySpec, userFile);

        // asserts
        assertThat(userFileFormatting).isEqualTo(String.valueOf(userFileId));
    }

    @Test
    public void formatActivityCalendarNameTest() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(activityCalendarNameAttributeName);
        final String calendarNameAttribute = "ThisIsMyTestCalendarName";

        // business method
        final String calendarNameFormatting = smartWebRtuKpMessageConverter.format(propertySpec, calendarNameAttribute);

        // asserts
        assertThat(calendarNameFormatting).isEqualTo(calendarNameAttribute);
    }

    @Test
    public void formatActivityCalendarCodeTableTest() {
        final int codeTableId = 324532;
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(activityCalendarCodeTableAttributeName);
        Code codeAttribute = mock(Code.class);
        when(codeAttribute.getId()).thenReturn(codeTableId);

        // business method
        final String codeTableFormatting = smartWebRtuKpMessageConverter.format(propertySpec, codeAttribute);

        // asserts
        assertThat(codeTableFormatting).isEqualTo(String.valueOf(codeTableId));
    }

    @Test
    public void formatActivityCalendarActivationDateTest() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(activityCalendarActivationDateAttributeName);
        long millis = 1363735265123L;
        Date currentDate = new Date(millis);

        // business method
        final String currentDateFormatting = smartWebRtuKpMessageConverter.format(propertySpec, currentDate);

        // asserts
        assertThat(currentDateFormatting).isEqualTo(String.valueOf(millis));
    }

    @Test
    public void formatEncryptionLevelTest() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec encryptionAttribute = mock(PropertySpec.class);
        when(encryptionAttribute.getName()).thenReturn(encryptionLevelAttributeName);

        // business method
        final String noEncryptionFormatting = smartWebRtuKpMessageConverter.format(encryptionAttribute, "No encryption");
        final String authenticationFormatting = smartWebRtuKpMessageConverter.format(encryptionAttribute, "Data authentication");
        final String encryptionFormatting = smartWebRtuKpMessageConverter.format(encryptionAttribute, "Data encryption");
        final String authenticationAndEncryptionFormatting = smartWebRtuKpMessageConverter.format(encryptionAttribute, "Data authentication and encryption");

        // asserts
        assertThat(noEncryptionFormatting).isEqualTo("0");
        assertThat(authenticationFormatting).isEqualTo("1");
        assertThat(encryptionFormatting).isEqualTo("2");
        assertThat(authenticationAndEncryptionFormatting).isEqualTo("3");
    }

    @Test
    public void formatAuthenticationLevelTest() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec authenticationAttribute = mock(PropertySpec.class);
        when(authenticationAttribute.getName()).thenReturn(authenticationLevelAttributeName);

        // business method
        final String noAuthentication = smartWebRtuKpMessageConverter.format(authenticationAttribute, "No authentiction");
        final String lowLevelAuthentication = smartWebRtuKpMessageConverter.format(authenticationAttribute, "Low level authentication");
        final String manufacturerAuthentication = smartWebRtuKpMessageConverter.format(authenticationAttribute, "Manufacturer specific");
        final String highLevelMd5 = smartWebRtuKpMessageConverter.format(authenticationAttribute, "High level authentication - MD5");
        final String highLevelSha1 = smartWebRtuKpMessageConverter.format(authenticationAttribute, "High level authentication - SHA-1");
        final String highLevelGmac = smartWebRtuKpMessageConverter.format(authenticationAttribute, "High level authentication - GMAC");

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
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(apnAttributeName);
        final String myApn = "com.test.energyict.apn";

        // business method
        final String formattedApn = smartWebRtuKpMessageConverter.format(propertySpec, myApn);

        // asserts
        assertThat(formattedApn).isEqualTo(myApn);
    }

    @Test
    public void formatUserNameTest() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(usernameAttributeName);
        final String myUserName = "MyUser_N@me";

        // business method
        final String formattedUserName = smartWebRtuKpMessageConverter.format(propertySpec, myUserName);

        // asserts
        assertThat(formattedUserName).isEqualTo(myUserName);
    }

    @Test
    public void formatPasswordTest() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(passwordAttributeName);
        final String myPassword = "MyPr1v@t€P@55wd";
        final Password gprsPassword = new Password(myPassword);

        // business method
        final String formattedPassword = smartWebRtuKpMessageConverter.format(propertySpec, gprsPassword);

        // asserts
        assertThat(formattedPassword).isEqualTo(myPassword);
    }

    @Test
    public void formatWhiteListPhoneNumbers() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(whiteListPhoneNumbersAttributeName);
        final String phoneNumbersOfWhiteList = "0477993322;+32485124578;00352478123";

        // business method
        final String formattedPhoneNumbersOfWhiteList = smartWebRtuKpMessageConverter.format(propertySpec, phoneNumbersOfWhiteList);

        // asserts
        assertThat(formattedPhoneNumbersOfWhiteList).isEqualTo(phoneNumbersOfWhiteList);
    }

    @Test
    public void formatP1Information() {
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(p1InformationAttributeName);
        final String p1Information = "SomeTextOrCodeToSendToTheP1Port";

        // business method
        final String formattedP1Information = smartWebRtuKpMessageConverter.format(propertySpec, p1Information);

        // asserts
        assertThat(formattedP1Information).isEqualTo(p1Information);
    }

    @Test
    public void formatNormalThresholdTest(){
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(normalThresholdAttributeName);
        final String normalThresholdValue = "6";
        final BigDecimal normalThreshold = new BigDecimal(normalThresholdValue);

        // business method
        final String formattedNormalThreshold = smartWebRtuKpMessageConverter.format(propertySpec, normalThreshold);

        // asserts
        assertThat(formattedNormalThreshold).isEqualTo(normalThresholdValue);
    }

    @Test
    public void formatEmergencyThresholdTest(){
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyThresholdAttributeName);
        final String emergencyThresholdValue = "2";
        final BigDecimal emergencyThreshold = new BigDecimal(emergencyThresholdValue);

        // business method
        final String formattedEmergencyThreshold = smartWebRtuKpMessageConverter.format(propertySpec, emergencyThreshold);

        // asserts
        assertThat(formattedEmergencyThreshold).isEqualTo(emergencyThresholdValue);
    }

    @Test
    public void formatOverThresholdDurationTest(){
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(overThresholdDurationAttributeName);
        final String overThresholdDurationValue = "3600";
        final TimeDuration overThresholdDuration = new TimeDuration(1, TimeDuration.HOURS);

        // business method
        final String formattedOverThresholdDuration = smartWebRtuKpMessageConverter.format(propertySpec, overThresholdDuration);

        // asserts
        assertThat(formattedOverThresholdDuration).isEqualTo(overThresholdDurationValue);
    }

    @Test
    public void formatEmergencyProfileIdTest(){
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyProfileIdAttributeName);
        final String emergencyProfileIdValue = "6543";
        final BigDecimal emergencyProfileId = new BigDecimal(emergencyProfileIdValue);

        // business method
        final String formattedEmergencyProfileId = smartWebRtuKpMessageConverter.format(propertySpec, emergencyProfileId);

        // asserts
        assertThat(formattedEmergencyProfileId).isEqualTo(emergencyProfileIdValue);
    }

    @Test
    public void formatEmergencyProfileActivationDateTest(){
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyProfileActivationDateAttributeName);
        final String timeInMilliSeconds = "1364983938654";
        final Date activationDate = new Date(Long.valueOf(timeInMilliSeconds));

        // business method
        final String formattedActivationDate = smartWebRtuKpMessageConverter.format(propertySpec, activationDate);

        // asserts
        assertThat(formattedActivationDate).isEqualTo(timeInMilliSeconds);
    }

    @Test
    public void formatEmergencyProfileDurationTest(){
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyProfileDurationAttributeName);
        final String emergencyProfileDurationValue = "259200";
        final TimeDuration emergencyProfileDuration = new TimeDuration(3, TimeDuration.DAYS);

        // business method
        final String formattedEmergencyProfileDuration = smartWebRtuKpMessageConverter.format(propertySpec, emergencyProfileDuration);

        // asserts
        assertThat(formattedEmergencyProfileDuration).isEqualTo(emergencyProfileDurationValue);
    }

    @Test
    public void formatEmergencyProfileLookupIdTest(){
        final int lookupId = 324532;
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(emergencyProfileIdLookupAttributeName);
        Lookup lookupAttribute = mock(Lookup.class);
        when(lookupAttribute.getId()).thenReturn(lookupId);

        // business method
        final String formattedLookupId = smartWebRtuKpMessageConverter.format(propertySpec, lookupAttribute);

        // asserts
        assertThat(formattedLookupId).isEqualTo(String.valueOf(lookupId));
    }

    @Test
    public void formatXmlConfigTest() {
        final String xmlString = "<xml>someXml<t>blabla</t></xml>";
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(xmlConfigAttributeName);

        // business method
        final String formattedXml = smartWebRtuKpMessageConverter.format(propertySpec, xmlString);

        // asserts
        assertThat(formattedXml).isEqualTo(xmlString);
    }

    @Test
    public void formatFromDateTest() {
        Date fromDate = new Date(1367581336000L);
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(fromDateAttributeName);

        // business method
        final String formattedFromDate = smartWebRtuKpMessageConverter.format(propertySpec, fromDate);

        // asserts
        assertThat(formattedFromDate).isEqualTo("2013/05/03 - 13:42:16 CEST");
    }

    @Test
    public void formatToDateTest() {
        Date fromDate = new Date(1367581336000L);
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(toDateAttributeName);

        // business method
        final String formattedFromDate = smartWebRtuKpMessageConverter.format(propertySpec, fromDate);

        // asserts
        assertThat(formattedFromDate).isEqualTo("2013/05/03 - 13:42:16 CEST");
    }

    @Test
    public void formatLoadProfileAttributeTest() {
        final String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><LoadProfile LPId=\"0\" LPObisCode=\"1.0.99.1.0.255\" MSerial=\"SomeSerialNumber\"><Channels><Ch ID=\"SomeSerialNumber\" Id=\"0\" Name=\"1.0.1.8.0.255\" Unit=\"kWh\"/><Ch ID=\"SomeSerialNumber\" Id=\"1\" Name=\"1.0.2.8.0.255\" Unit=\"kWh\"/></Channels><RtuRegs><Reg ID=\"SomeSerialNumber\" OC=\"1.0.1.8.0.255\"/><Reg ID=\"SomeSerialNumber\" OC=\"1.0.2.8.0.255\"/></RtuRegs></LoadProfile>";
        Device device = createdMockedDevice();
        Channel channel1 = createdMockedChannel(device, OBISCODE1);
        Channel channel2 = createdMockedChannel(device, OBISCODE2);
        LoadProfile loadProfile = createMockedLoadProfile();
        when(loadProfile.getRtu()).thenReturn(device);
        when(loadProfile.getAllChannels()).thenReturn(Arrays.asList(channel1, channel2));

        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(loadProfileAttributeName);

        // business method
        final String formattedLoadProfile = smartWebRtuKpMessageConverter.format(propertySpec, loadProfile);

        // assert
        assertThat(formattedLoadProfile).isEqualTo(expectedXml);
    }


    private LoadProfile createMockedLoadProfile() {
        LoadProfile loadProfile = mock(LoadProfile.class);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getObisCode()).thenReturn(LOAD_PROFILE_OBISCODE);
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        return loadProfile;
    }

    private Channel createdMockedChannel(Device device, ObisCode obisCode) {
        Channel channel = mock(Channel.class);
        RegisterMapping registerMapping = createMockedRegisterMapping(obisCode);
        when(channel.getRtu()).thenReturn(device);
        when(channel.getRtuRegisterMapping()).thenReturn(registerMapping);
        return channel;
    }

    private RegisterMapping createMockedRegisterMapping(ObisCode obisCode) {
        RegisterMapping registerMapping = mock(RegisterMapping.class);
        when(registerMapping.getObisCode()).thenReturn(obisCode);
        when(registerMapping.getUnit()).thenReturn(UNIT);
        return registerMapping;
    }

    private Device createdMockedDevice() {
        Device device = mock(Device.class);
        when(device.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        return device;
    }

    @Test
    public void contactorOpenTest() {
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        when(contactorOpen.getDeviceMessageSpecPrimaryKey()).thenReturn(ContactorDeviceMessage.CONTACTOR_OPEN.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<disconnectLoad> </disconnectLoad>");
    }

    @Test
    public void contactorOpenWithActivationDateTest() {
        final long millis = 1234567890321L;
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(contactorActivationDateAttributeName);
        when(offlineDeviceMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(millis));
        when(contactorOpen.getDeviceMessageAttributes()).thenReturn(Arrays.asList(offlineDeviceMessageAttribute));
        when(contactorOpen.getDeviceMessageSpecPrimaryKey()).thenReturn(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<disconnectLoad Activation_date=\"1234567890321\"> </disconnectLoad>");
    }

    @Test
    public void contactorCloseTest() {
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        when(contactorOpen.getDeviceMessageSpecPrimaryKey()).thenReturn(ContactorDeviceMessage.CONTACTOR_CLOSE.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<connectLoad> </connectLoad>");
    }

    @Test
    public void contactorCloseWithActivationDateTest() {
        final long millis = 1234567890321L;
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(contactorActivationDateAttributeName);
        when(offlineDeviceMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(millis));
        when(contactorOpen.getDeviceMessageAttributes()).thenReturn(Arrays.asList(offlineDeviceMessageAttribute));
        when(contactorOpen.getDeviceMessageSpecPrimaryKey()).thenReturn(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<connectLoad Activation_date=\"1234567890321\"> </connectLoad>");
    }

    @Test
    public void changeContactorModeTest() {
        final BigDecimal mode = new BigDecimal("3");
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(contactorModeAttributeName);
        when(offlineDeviceMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(mode.toString());
        when(contactorOpen.getDeviceMessageAttributes()).thenReturn(Arrays.asList(offlineDeviceMessageAttribute));
        when(contactorOpen.getDeviceMessageSpecPrimaryKey()).thenReturn(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Connect_control_mode Mode=\"3\"> </Connect_control_mode>");
    }

    @Test
    public void upgradeFirmwareWithUserFileTest() {
        final int userFileId = 324;
        final UserFile userFile = mock(UserFile.class);
        when(userFile.getId()).thenReturn(userFileId);
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage firmwareUpgrade = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(firmwareUpdateUserFileAttributeName);
        when(offlineDeviceMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(userFileId));
        when(firmwareUpgrade.getDeviceMessageAttributes()).thenReturn(Arrays.asList(offlineDeviceMessageAttribute));
        when(firmwareUpgrade.getDeviceMessageSpecPrimaryKey()).thenReturn(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(firmwareUpgrade);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<FirmwareUpgrade UserFileID=\"324\"> </FirmwareUpgrade>");
    }

    @Test
    public void upgradeFirmwareWithUserFileAndActivationDateTest() {
        final long millis = 1234567890321L;
        final int userFileId = 324;
        final UserFile userFile = mock(UserFile.class);
        when(userFile.getId()).thenReturn(userFileId);
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage firmwareUpgrade = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute userFileMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(userFileMessageAttribute.getName()).thenReturn(firmwareUpdateUserFileAttributeName);
        when(userFileMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(userFileId));
        OfflineDeviceMessageAttribute activationDateMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(activationDateMessageAttribute.getName()).thenReturn(firmwareUpdateActivationDateAttributeName);
        when(activationDateMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(millis));
        when(firmwareUpgrade.getDeviceMessageAttributes()).thenReturn(Arrays.asList(userFileMessageAttribute, activationDateMessageAttribute));
        when(firmwareUpgrade.getDeviceMessageSpecPrimaryKey()).thenReturn(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(firmwareUpgrade);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<FirmwareUpgrade UserFileID=\"324\" Activation_date=\"1234567890321\"> </FirmwareUpgrade>");
    }

    @Test
    public void activityCalendarConfigurationTest() {
        final String calendarName = "ActivityCalendarTestName";
        final int codeTableId = 324532;
        final Code codeTable = mock(Code.class);
        when(codeTable.getId()).thenReturn(codeTableId);
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage activityCalendarConfiguration = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute codeTableMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(codeTableMessageAttribute.getName()).thenReturn(activityCalendarCodeTableAttributeName);
        when(codeTableMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(codeTableId));
        OfflineDeviceMessageAttribute activityCalendarNameMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(activityCalendarNameMessageAttribute.getName()).thenReturn(activityCalendarNameAttributeName);
        when(activityCalendarNameMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(calendarName);
        when(activityCalendarConfiguration.getDeviceMessageAttributes()).thenReturn(Arrays.asList(activityCalendarNameMessageAttribute, codeTableMessageAttribute));
        when(activityCalendarConfiguration.getDeviceMessageSpecPrimaryKey()).thenReturn(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(activityCalendarConfiguration);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Activity_Calendar Calendar_Name=\"" + calendarName + "\" Code_Table=\"" + String.valueOf(codeTableId) + "\"> </Activity_Calendar>");
    }

    @Test
    public void activityCalendarConfigurationWithActivationDateTest() {
        final String calendarName = "ActivityCalendarTestName";
        final int codeTableId = 324532;
        final long millis = 1234567890321L;
        final Code codeTable = mock(Code.class);
        when(codeTable.getId()).thenReturn(codeTableId);
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage activityCalendarConfiguration = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute codeTableMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(codeTableMessageAttribute.getName()).thenReturn(activityCalendarCodeTableAttributeName);
        when(codeTableMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(codeTableId));
        OfflineDeviceMessageAttribute activityCalendarNameMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(activityCalendarNameMessageAttribute.getName()).thenReturn(activityCalendarNameAttributeName);
        when(activityCalendarNameMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(calendarName);
        OfflineDeviceMessageAttribute activationDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(activationDateAttribute.getName()).thenReturn(activityCalendarActivationDateAttributeName);
        when(activationDateAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(millis));
        when(activityCalendarConfiguration.getDeviceMessageAttributes()).thenReturn(Arrays.asList(activityCalendarNameMessageAttribute, codeTableMessageAttribute, activationDateAttribute));
        when(activityCalendarConfiguration.getDeviceMessageSpecPrimaryKey()).thenReturn(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATE.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(activityCalendarConfiguration);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Activity_Calendar Calendar_Name=\"" + calendarName + "\" Activation_Date=\""
                + String.valueOf(millis) + "\" Code_Table=\"" + String.valueOf(codeTableId) + "\"> </Activity_Calendar>");
    }

    @Test
    public void specialDayTableTest() {
        final int codeTableId = 324532;
        final Code codeTable = mock(Code.class);
        when(codeTable.getId()).thenReturn(codeTableId);
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage activityCalendarConfiguration = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute codeTableMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(codeTableMessageAttribute.getName()).thenReturn(activityCalendarCodeTableAttributeName);
        when(codeTableMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(codeTableId));
        when(activityCalendarConfiguration.getDeviceMessageAttributes()).thenReturn(Arrays.asList(codeTableMessageAttribute));
        when(activityCalendarConfiguration.getDeviceMessageSpecPrimaryKey()).thenReturn(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(activityCalendarConfiguration);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Special_Days Code_Table=\"" + String.valueOf(codeTableId) + "\"> </Special_Days>");
    }

    @Test
    public void activateEncryptionTest() {
        String encryptionLevel = "3";
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage activateEncryptionLevelMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute encryptionLevelAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(encryptionLevelAttribute.getName()).thenReturn(encryptionLevelAttributeName);
        when(encryptionLevelAttribute.getDeviceMessageAttributeValue()).thenReturn(encryptionLevel);
        when(activateEncryptionLevelMessage.getDeviceMessageAttributes()).thenReturn(Arrays.asList(encryptionLevelAttribute));
        when(activateEncryptionLevelMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(activateEncryptionLevelMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Activate_dataTransport_Security SecurityLevel=\"" + encryptionLevel + "\"> </Activate_dataTransport_Security>");
    }

    @Test
    public void changeAuthenticationLevelTest() {
        String authenticationLevel = "3";
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage changeAuthenticationLevel = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute authenticationLevelAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(authenticationLevelAttribute.getName()).thenReturn(authenticationLevelAttributeName);
        when(authenticationLevelAttribute.getDeviceMessageAttributeValue()).thenReturn(authenticationLevel);
        when(changeAuthenticationLevel.getDeviceMessageAttributes()).thenReturn(Arrays.asList(authenticationLevelAttribute));
        when(changeAuthenticationLevel.getDeviceMessageSpecPrimaryKey()).thenReturn(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(changeAuthenticationLevel);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Change_authentication_level AuthenticationLevel=\"" + authenticationLevel + "\"> </Change_authentication_level>");
    }

    @Test
    public void changeDataTransportEncryptionKeyTest() {
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage changeDataTransportEncryptionKey = mock(OfflineDeviceMessage.class);
        when(changeDataTransportEncryptionKey.getDeviceMessageSpecPrimaryKey()).thenReturn(SecurityMessage.CHANGE_ENCRYPTION_KEY.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(changeDataTransportEncryptionKey);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Change_DataTransportEncryptionKey/>");
    }

    @Test
    public void changeDataTransportAuthenticationKeyTest() {
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage changeDataTransportAuthenticationKey = mock(OfflineDeviceMessage.class);
        when(changeDataTransportAuthenticationKey.getDeviceMessageSpecPrimaryKey()).thenReturn(SecurityMessage.CHANGE_AUTHENTICATION_KEY.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(changeDataTransportAuthenticationKey);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Change_DataTransportAuthenticationKey/>");
    }

    @Test
    public void changeHlsSecretTest() {
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage changeHlsSecret = mock(OfflineDeviceMessage.class);
        when(changeHlsSecret.getDeviceMessageSpecPrimaryKey()).thenReturn(SecurityMessage.CHANGE_PASSWORD.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(changeHlsSecret);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Change_HLS_Secret/>");
    }

    @Test
    public void activateSmsWakeUpMechanismTest() {
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage activateSmsWakeUp = mock(OfflineDeviceMessage.class);
        when(activateSmsWakeUp.getDeviceMessageSpecPrimaryKey()).thenReturn(NetworkConnectivityMessage.ACTIVATE_SMS_WAKEUP.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(activateSmsWakeUp);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Activate_the_wakeup_mechanism/>");
    }

    @Test
    public void deActivateSmsWakeUpMechanismTest() {
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage deActivateSmsWakeUp = mock(OfflineDeviceMessage.class);
        when(deActivateSmsWakeUp.getDeviceMessageSpecPrimaryKey()).thenReturn(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(deActivateSmsWakeUp);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Deactive_the_wakeup_mechanism/>");
    }

    @Test
    public void gprsUserCredentialsMessageEntryTest() {
        final String myUserName = "MyTestUserN@me";
        final String myPassword = "MyDumm£T€stP@sswd";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage gprsUserCredentials = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute userNameAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(userNameAttribute.getName()).thenReturn(usernameAttributeName);
        when(userNameAttribute.getDeviceMessageAttributeValue()).thenReturn(myUserName);
        OfflineDeviceMessageAttribute passwordAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(passwordAttribute.getName()).thenReturn(passwordAttributeName);
        when(passwordAttribute.getDeviceMessageAttributeValue()).thenReturn(myPassword);
        when(gprsUserCredentials.getDeviceMessageAttributes()).thenReturn(Arrays.asList(userNameAttribute, passwordAttribute));
        when(gprsUserCredentials.getDeviceMessageSpecPrimaryKey()).thenReturn(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(gprsUserCredentials);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<GPRS_modem_credentials Username=\"MyTestUserN@me\" Password=\"MyDumm£T€stP@sswd\"> </GPRS_modem_credentials>");
    }

    @Test
    public void apnCredentialsMessageEntryTest() {
        final String myUserName = "MyTestUserN@me";
        final String myPassword = "MyDumm£T€stP@sswd";
        final String myApn = "com.test.energyict.apn";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage gprsApnCredentials = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute userNameAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(userNameAttribute.getName()).thenReturn(usernameAttributeName);
        when(userNameAttribute.getDeviceMessageAttributeValue()).thenReturn(myUserName);
        OfflineDeviceMessageAttribute passwordAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(passwordAttribute.getName()).thenReturn(passwordAttributeName);
        when(passwordAttribute.getDeviceMessageAttributeValue()).thenReturn(myPassword);
        OfflineDeviceMessageAttribute apnAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(apnAttribute.getName()).thenReturn(apnAttributeName);
        when(apnAttribute.getDeviceMessageAttributeValue()).thenReturn(myApn);
        when(gprsApnCredentials.getDeviceMessageAttributes()).thenReturn(Arrays.asList(userNameAttribute, passwordAttribute, apnAttribute));
        when(gprsApnCredentials.getDeviceMessageSpecPrimaryKey()).thenReturn(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(gprsApnCredentials);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<GPRS_modem_setup APN=\"com.test.energyict.apn\" Username=\"MyTestUserN@me\" Password=\"MyDumm£T€stP@sswd\"> </GPRS_modem_setup>");
    }

    @Test
    public void addPhoneNumbersToWhiteListTest() {
        final String allPhoneNumbers = "0477993322;+32485124578;00352478123";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage whiteListPhoneNumbersDeviceMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute whiteListPhoneNumbersAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(whiteListPhoneNumbersAttribute.getName()).thenReturn(whiteListPhoneNumbersAttributeName);
        when(whiteListPhoneNumbersAttribute.getDeviceMessageAttributeValue()).thenReturn(allPhoneNumbers);
        when(whiteListPhoneNumbersDeviceMessage.getDeviceMessageAttributes()).thenReturn(Arrays.asList(whiteListPhoneNumbersAttribute));
        when(whiteListPhoneNumbersDeviceMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(whiteListPhoneNumbersDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Phonenumbers_to_add Phonenumber1=\"0477993322\" Phonenumber2=\"+32485124578\" Phonenumber3=\"00352478123\"> </Phonenumbers_to_add>");
    }

    @Test
    public void sendCodeToP1PortTest() {
        final String p1CodeInformation = "dotdotdotdashdashdashdotdotdot";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage p1CodeDeviceMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute p1CodeAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(p1CodeAttribute.getName()).thenReturn(p1InformationAttributeName);
        when(p1CodeAttribute.getDeviceMessageAttributeValue()).thenReturn(p1CodeInformation);
        when(p1CodeDeviceMessage.getDeviceMessageAttributes()).thenReturn(Arrays.asList(p1CodeAttribute));
        when(p1CodeDeviceMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(p1CodeDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Message_code_to_P1_port Code=\"dotdotdotdashdashdashdotdotdot\"> </Message_code_to_P1_port>");
    }

    @Test
    public void sendTextToP1PortTest() {
        final String p1TextInformation = "Sending out an S.O.S., Sending out an S.O.S., Sending out and S.O.S.";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage p1TextDeviceMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute p1TextAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(p1TextAttribute.getName()).thenReturn(p1InformationAttributeName);
        when(p1TextAttribute.getDeviceMessageAttributeValue()).thenReturn(p1TextInformation);
        when(p1TextDeviceMessage.getDeviceMessageAttributes()).thenReturn(Arrays.asList(p1TextAttribute));
        when(p1TextDeviceMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(p1TextDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Message_text_to_P1_port Text=\"Sending out an S.O.S., Sending out an S.O.S., Sending out and S.O.S.\"> </Message_text_to_P1_port>");
    }

    @Test
    public void globalMeterResetTest(){
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage globalMeterResetDeviceMessage = mock(OfflineDeviceMessage.class);
        when(globalMeterResetDeviceMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(DeviceActionMessage.GLOBAL_METER_RESET.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(globalMeterResetDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Global_Meter_Reset/>");
    }

    @Test
    public void configureLoadLimitParameters(){
        final String normalThreshold = "6";
        final String emergencyThreshold = "2";
        final String overThresholdDuration = "20";
        final String emergencyProfileId = "31";
        final String emergencyProfileDuration = "86400";
        final String emergencyProfileActivationDate = "1364988856654";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage configureLoadLimitingParametersMessage = mock(OfflineDeviceMessage.class);

        OfflineDeviceMessageAttribute normalThresholdAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(normalThresholdAttribute.getName()).thenReturn(normalThresholdAttributeName);
        when(normalThresholdAttribute.getDeviceMessageAttributeValue()).thenReturn(normalThreshold);

        OfflineDeviceMessageAttribute emergencyThresholdAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(emergencyThresholdAttribute.getName()).thenReturn(emergencyThresholdAttributeName);
        when(emergencyThresholdAttribute.getDeviceMessageAttributeValue()).thenReturn(emergencyThreshold);

        OfflineDeviceMessageAttribute overThresholdDurationAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(overThresholdDurationAttribute.getName()).thenReturn(overThresholdDurationAttributeName);
        when(overThresholdDurationAttribute.getDeviceMessageAttributeValue()).thenReturn(overThresholdDuration);

        OfflineDeviceMessageAttribute emergencyProfileIdAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(emergencyProfileIdAttribute.getName()).thenReturn(emergencyProfileIdAttributeName);
        when(emergencyProfileIdAttribute.getDeviceMessageAttributeValue()).thenReturn(emergencyProfileId);

        OfflineDeviceMessageAttribute emergencyProfileDurationAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(emergencyProfileDurationAttribute.getName()).thenReturn(emergencyProfileDurationAttributeName);
        when(emergencyProfileDurationAttribute.getDeviceMessageAttributeValue()).thenReturn(emergencyProfileDuration);

        OfflineDeviceMessageAttribute emergencyProfileActivationDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(emergencyProfileActivationDateAttribute.getName()).thenReturn(emergencyProfileActivationDateAttributeName);
        when(emergencyProfileActivationDateAttribute.getDeviceMessageAttributeValue()).thenReturn(emergencyProfileActivationDate);

        when(configureLoadLimitingParametersMessage.getDeviceMessageAttributes()).thenReturn(Arrays.asList(normalThresholdAttribute, emergencyThresholdAttribute,
                overThresholdDurationAttribute, emergencyProfileIdAttribute, emergencyProfileDurationAttribute, emergencyProfileActivationDateAttribute));

        when(configureLoadLimitingParametersMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(configureLoadLimitingParametersMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Configure_load_limiting Normal_Threshold=\"6\" Emergency_Threshold=\"2\" Over_Threshold_Duration=\"20\"><Emergency_Profile EP_Profile_Id=\"31\" EP_Activation_Time=\"1364988856654\" EP_Duration=\"86400\"> </Emergency_Profile> </Configure_load_limiting>");
    }

    @Test
    public void setEmergencyProfileGroupIdTest(){
        final int lookupId = 324532;
        final Lookup lookupTable = mock(Lookup.class);
        when(lookupTable.getId()).thenReturn(lookupId);
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);

        OfflineDeviceMessage setEmergencyProfileIdsMessage = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute lookupTableAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(lookupTableAttribute.getName()).thenReturn(emergencyProfileIdLookupAttributeName);
        when(lookupTableAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(lookupId));

        when(setEmergencyProfileIdsMessage.getDeviceMessageAttributes()).thenReturn(Arrays.asList(lookupTableAttribute));
        when(setEmergencyProfileIdsMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(setEmergencyProfileIdsMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<EP_GroupIds Lookup_Table_ID=\"324532\"> </EP_GroupIds>");
    }

    @Test
    public void clearLoadLimitConfigurationTest(){
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage clearLoadLimitConfigurationDeviceMessage = mock(OfflineDeviceMessage.class);
        when(clearLoadLimitConfigurationDeviceMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(clearLoadLimitConfigurationDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<Disable_load_limitng/>");
    }

    @Test
    public void sendXmlConfigTest() {
        final String xmlString = "<SomeXml><></><bla>Tralalala<bla/><></><SomeXml/>";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage xmlConfigDeviceMessage = mock(OfflineDeviceMessage.class);
        when(xmlConfigDeviceMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(AdvancedTestMessage.XML_CONFIG.getPrimaryKey());
        OfflineDeviceMessageAttribute xmlConfigAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(xmlConfigAttribute.getName()).thenReturn(xmlConfigAttributeName);
        when(xmlConfigAttribute.getDeviceMessageAttributeValue()).thenReturn(xmlString);
        when(xmlConfigDeviceMessage.getDeviceMessageAttributes()).thenReturn(Arrays.asList(xmlConfigAttribute));

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(xmlConfigDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<XMLConfig><SomeXml><></><bla>Tralalala<bla/><></><SomeXml/></XMLConfig>");
    }

    @Test
    public void sendPartialLoadProfileTest() {
        final String expectedMessageContent = "<PartialLoadProfile EndTime=\"13/03/2013 11:32:25\" LPId=\"821\" LPObisCode=\"0.0.98.1.0.255\" MSerial=\"SomeSerialNumber\" StartTime=\"06/02/2013 10:00:25\"><Channels><Ch ID=\"SomeSerialNumber\" Id=\"0\" Name=\"1.0.1.8.1.255\" Unit=\"kWh\"/><Ch ID=\"SomeSerialNumber\" Id=\"1\" Name=\"1.0.1.8.2.255\" Unit=\"kWh\"/><Ch ID=\"SomeSerialNumber\" Id=\"2\" Name=\"1.0.2.8.1.255\" Unit=\"kWh\"/><Ch ID=\"SomeSerialNumber\" Id=\"3\" Name=\"1.0.2.8.2.255\" Unit=\"kWh\"/></Channels></PartialLoadProfile>";
        final String loadProfileAttributeValue = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><LoadProfile LPObisCode=\"0.0.98.1.0.255\" MSerial=\"SomeSerialNumber\" StartTime=\"06/02/2013 10:00:25\" EndTime=\"13/03/2013 11:32:25\" LPId=\"821\"><Channels><Ch Id=\"0\" Name=\"1.0.1.8.1.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"1\" Name=\"1.0.1.8.2.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"2\" Name=\"1.0.2.8.1.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"3\" Name=\"1.0.2.8.2.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /></Channels><RtuRegs><Reg ID=\"SomeSerialNumber\" OC=\"1.0.1.8.0.255\"/><Reg ID=\"SomeSerialNumber\" OC=\"1.0.2.8.0.255\"/></RtuRegs></LoadProfile>";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage partialLoadProfileDeviceMessage = mock(OfflineDeviceMessage.class);
        when(partialLoadProfileDeviceMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST.getPrimaryKey());
        OfflineDeviceMessageAttribute loadProfileAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(loadProfileAttribute.getName()).thenReturn(loadProfileAttributeName);
        when(loadProfileAttribute.getDeviceMessageAttributeValue()).thenReturn(loadProfileAttributeValue);
        OfflineDeviceMessageAttribute fromDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(fromDateAttribute.getName()).thenReturn(fromDateAttributeName);
        when(fromDateAttribute.getDeviceMessageAttributeValue()).thenReturn("06/02/2013 10:00:25");
        OfflineDeviceMessageAttribute toDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(toDateAttribute.getName()).thenReturn(toDateAttributeName);
        when(toDateAttribute.getDeviceMessageAttributeValue()).thenReturn("13/03/2013 11:32:25");
        when(partialLoadProfileDeviceMessage.getDeviceMessageAttributes()).thenReturn(Arrays.asList(loadProfileAttribute, fromDateAttribute, toDateAttribute));

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(partialLoadProfileDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo(expectedMessageContent);
    }

    @Test
    public void sendRegisterRequestFromLoadProfilesTest() {
        final String expectedMessageContent = "<LoadProfileRegister LPId=\"821\" LPObisCode=\"0.0.98.1.0.255\" MSerial=\"SomeSerialNumber\" StartTime=\"06/02/2013 10:00:25\"><RtuRegs><Reg ID=\"SomeSerialNumber\" OC=\"1.0.1.8.0.255\"/><Reg ID=\"SomeSerialNumber\" OC=\"1.0.2.8.0.255\"/></RtuRegs></LoadProfileRegister>";
        final String loadProfileAttributeValue = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><LoadProfile LPObisCode=\"0.0.98.1.0.255\" MSerial=\"SomeSerialNumber\" StartTime=\"06/02/2013 10:00:25\" EndTime=\"13/03/2013 11:32:25\" LPId=\"821\"><Channels><Ch Id=\"0\" Name=\"1.0.1.8.1.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"1\" Name=\"1.0.1.8.2.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"2\" Name=\"1.0.2.8.1.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /><Ch Id=\"3\" Name=\"1.0.2.8.2.255\" Unit=\"kWh\" ID=\"SomeSerialNumber\" /></Channels><RtuRegs><Reg ID=\"SomeSerialNumber\" OC=\"1.0.1.8.0.255\"/><Reg ID=\"SomeSerialNumber\" OC=\"1.0.2.8.0.255\"/></RtuRegs></LoadProfile>";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);

        OfflineDeviceMessage registerRequestDeviceMessage = mock(OfflineDeviceMessage.class);
        when(registerRequestDeviceMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST.getPrimaryKey());
        OfflineDeviceMessageAttribute loadProfileAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(loadProfileAttribute.getName()).thenReturn(loadProfileAttributeName);
        when(loadProfileAttribute.getDeviceMessageAttributeValue()).thenReturn(loadProfileAttributeValue);
        OfflineDeviceMessageAttribute fromDateAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(fromDateAttribute.getName()).thenReturn(fromDateAttributeName);
        when(fromDateAttribute.getDeviceMessageAttributeValue()).thenReturn("06/02/2013 10:00:25");
        when(registerRequestDeviceMessage.getDeviceMessageAttributes()).thenReturn(Arrays.asList(loadProfileAttribute, fromDateAttribute));

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(registerRequestDeviceMessage);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo(expectedMessageContent);
    }
}