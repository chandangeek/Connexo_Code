package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.messages.DeviceMessageSpecFactoryImpl;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
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
    public void formatAuthenticationLevelTest(){
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
    public void formatApnTest(){
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(apn);
        final String myApn = "com.test.energyict.apn";

        // business method
        final String formattedApn = smartWebRtuKpMessageConverter.format(propertySpec, myApn);

        // asserts
        assertThat(formattedApn).isEqualTo(myApn);
    }

    @Test
    public void formatUserNameTest(){
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(username);
        final String myUserName = "MyUser_N@me";

        // business method
        final String formattedUserName = smartWebRtuKpMessageConverter.format(propertySpec, myUserName);

        // asserts
        assertThat(formattedUserName).isEqualTo(myUserName);
    }

    @Test
    public void formatPasswordTest(){
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(password);
        final String myPassword = "MyPr1v@t€P@55wd";
        final Password gprsPassword = new Password(myPassword);

        // business method
        final String formattedPassword = smartWebRtuKpMessageConverter.format(propertySpec, gprsPassword);

        // asserts
        assertThat(formattedPassword).isEqualTo(myPassword);
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
    public void changeAuthenticationLevelTest(){
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
    public void changeDataTransportEncryptionKeyTest(){
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
    public void changeDataTransportAuthenticationKeyTest(){
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
    public void changeHlsSecretTest(){
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
    public void activateSmsWakeUpMechanismTest(){
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
    public void deActivateSmsWakeUpMechanismTest(){
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
    public void gprsUserCredentialsMessageEntryTest(){
        final String myUserName = "MyTestUserN@me";
        final String myPassword = "MyDumm£T€stP@sswd";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage gprsUserCredentials = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute userNameAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(userNameAttribute.getName()).thenReturn(username);
        when(userNameAttribute.getDeviceMessageAttributeValue()).thenReturn(myUserName);
        OfflineDeviceMessageAttribute passwordAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(passwordAttribute.getName()).thenReturn(password);
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
    public void apnCredentialsMessageEntryTest(){
        final String myUserName = "MyTestUserN@me";
        final String myPassword = "MyDumm£T€stP@sswd";
        final String myApn = "com.test.energyict.apn";

        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage gprsApnCredentials = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute userNameAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(userNameAttribute.getName()).thenReturn(username);
        when(userNameAttribute.getDeviceMessageAttributeValue()).thenReturn(myUserName);
        OfflineDeviceMessageAttribute passwordAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(passwordAttribute.getName()).thenReturn(password);
        when(passwordAttribute.getDeviceMessageAttributeValue()).thenReturn(myPassword);
        OfflineDeviceMessageAttribute apnAttribute =mock(OfflineDeviceMessageAttribute.class);
        when(apnAttribute.getName()).thenReturn(apn);
        when(apnAttribute.getDeviceMessageAttributeValue()).thenReturn(myApn);
        when(gprsApnCredentials.getDeviceMessageAttributes()).thenReturn(Arrays.asList(userNameAttribute, passwordAttribute, apnAttribute));
        when(gprsApnCredentials.getDeviceMessageSpecPrimaryKey()).thenReturn(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(gprsApnCredentials);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<GPRS_modem_setup APN=\"com.test.energyict.apn\" Username=\"MyTestUserN@me\" Password=\"MyDumm£T€stP@sswd\"> </GPRS_modem_setup>");
    }

}