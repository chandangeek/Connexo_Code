package com.energyict.protocolimplv2.messages.convertor;

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
public class
        SmartWebRtuKpMessageConverterTest {

    private static final String activationDateAttributeName = "ContactorDeviceMessage.activationdate";
    private static final String contactorModeAttributeName = "ContactorDeviceMessage.changemode.mode";
    private static final String firmwareUpdateActivationDateAttributeName = "FirmwareDeviceMessage.upgrade.activationdate";
    private static final String firmwareUpdateUserFileAttributeName = "FirmwareDeviceMessage.upgrade.userfile";
    private static final String activityCalendarNameAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.name";
    private static final String activityCalendarCodeTableAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.codetable";
    private static final String activityCalendarActivationDateAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.activationdate";
    private static final String encryptionLevelAttributeName = "SecurityMessage.dlmsencryption.encryptionlevel";

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
        when(propertySpec.getName()).thenReturn(activationDateAttributeName);
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
        PropertySpec noEncryption = mock(PropertySpec.class);
        when(noEncryption.getName()).thenReturn(encryptionLevelAttributeName);

        // business method
        final String noEncryptionFormatting = smartWebRtuKpMessageConverter.format(noEncryption, "No encryption");
        final String authenticationFormatting = smartWebRtuKpMessageConverter.format(noEncryption, "Data authentication");
        final String encryptionFormatting = smartWebRtuKpMessageConverter.format(noEncryption, "Data encryption");
        final String authenticationAndEncryptionFormatting = smartWebRtuKpMessageConverter.format(noEncryption, "Data authentication and encryption");

        // asserts
        assertThat(noEncryptionFormatting).isEqualTo("0");
        assertThat(authenticationFormatting).isEqualTo("1");
        assertThat(encryptionFormatting).isEqualTo("2");
        assertThat(authenticationAndEncryptionFormatting).isEqualTo("3");
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
        assertThat(messageEntry.getContent()).isEqualTo("<connectLoad> </connectLoad>");
    }

    @Test
    public void contactorOpenWithActivationDateTest() {
        final long millis = 1234567890321L;
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(activationDateAttributeName);
        when(offlineDeviceMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(millis));
        when(contactorOpen.getDeviceMessageAttributes()).thenReturn(Arrays.asList(offlineDeviceMessageAttribute));
        when(contactorOpen.getDeviceMessageSpecPrimaryKey()).thenReturn(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<connectLoad Activation_date=\"1234567890321\"> </connectLoad>");
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
        assertThat(messageEntry.getContent()).isEqualTo("<disconnectLoad> </disconnectLoad>");
    }

    @Test
    public void contactorCloseWithActivationDateTest() {
        final long millis = 1234567890321L;
        Messaging smartMeterProtocol = new WebRTUKP();
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        smartWebRtuKpMessageConverter.setMessagingProtocol(smartMeterProtocol);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
        when(offlineDeviceMessageAttribute.getName()).thenReturn(activationDateAttributeName);
        when(offlineDeviceMessageAttribute.getDeviceMessageAttributeValue()).thenReturn(String.valueOf(millis));
        when(contactorOpen.getDeviceMessageAttributes()).thenReturn(Arrays.asList(offlineDeviceMessageAttribute));
        when(contactorOpen.getDeviceMessageSpecPrimaryKey()).thenReturn(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = smartWebRtuKpMessageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<disconnectLoad Activation_date=\"1234567890321\"> </disconnectLoad>");
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
}