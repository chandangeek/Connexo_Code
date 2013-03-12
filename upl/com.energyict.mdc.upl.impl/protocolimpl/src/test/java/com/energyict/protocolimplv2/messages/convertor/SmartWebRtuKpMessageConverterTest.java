package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.messages.DeviceMessageSpecFactoryImpl;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
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
public class SmartWebRtuKpMessageConverterTest {

    private static final String activationDateAttributeName = "ContactorDeviceMessage.activationdate";
    private static final String contactorModeAttributeName = "ContactorDeviceMessage.changemode.mode";
    private static final String firmwareUpdateActivationDateAttributeName = "FirmwareDeviceMessage.upgrade.activationdate";
    private static final String firmwareUpdateUserFileAttributeName = "FirmwareDeviceMessage.upgrade.userfile";

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
    public void formatFirmwareUpgradeActionDateTest(){
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
    public void formatFirmwareUpgradeUserFileTest(){
        final int userFileId = 324532;
        final SmartWebRtuKpMessageConverter smartWebRtuKpMessageConverter = new SmartWebRtuKpMessageConverter();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(firmwareUpdateUserFileAttributeName);
        UserFile userFile = mock(UserFile.class);
        when(userFile.getId()).thenReturn(userFileId);

        // business method
        final String currentDateFormatting = smartWebRtuKpMessageConverter.format(propertySpec, userFile);

        // asserts
        assertThat(currentDateFormatting).isEqualTo(String.valueOf(userFileId));
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
    public void contactorCloseWithActivationDateTest(){
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
    public void changeContactorModeTest(){
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
    public void upgradeFirmwareWithUserFileTest(){
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

    //
    @Test
    public void upgradeFirmwareWithUserFileAndActivationDateTest(){
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
}