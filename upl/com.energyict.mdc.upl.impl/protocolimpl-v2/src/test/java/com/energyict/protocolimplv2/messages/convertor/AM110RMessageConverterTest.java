package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by cisac on 8/17/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class AM110RMessageConverterTest extends AbstractMessageConverterTest{

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.SetAutoConnectMode);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Connection_Mode Mode=\"9\"> </Connection_Mode>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.WakeupParameters);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Wakeup_Parameters Calling_Window_Length=\"33\" Idle_Timeout=\"100\"> </Wakeup_Parameters>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.PreferredNetworkOperatorList);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<PreferredNetworkOperatorsList " +
                "Operator_1=\"Operator_x\" " +
                "Operator_2=\"Operator_x\" " +
                "Operator_3=\"Operator_x\" " +
                "Operator_4=\"Operator_x\" " +
                "Operator_5=\"Operator_x\" " +
                "Operator_6=\"Operator_x\" " +
                "Operator_7=\"Operator_x\" " +
                "Operator_8=\"Operator_x\" " +
                "Operator_9=\"Operator_x\" " +
                "Operator_10=\"Operator_x\"> </PreferredNetworkOperatorsList>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LogBookDeviceMessage.ReadDebugLogBook);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Debug_Logbook From_date=\"01/10/2013 00:00:00\" To_date=\"15/10/2013 00:00:00\"> </Debug_Logbook>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LogBookDeviceMessage.ReadManufacturerSpecificLogBook);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Elster_Specific_Logbook From_date=\"01/10/2013 00:00:00\" To_date=\"15/10/2013 00:00:00\"> </Elster_Specific_Logbook>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.DISABLE_WEBSERVER);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Disable_Webserver/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.ENABLE_WEBSERVER);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Enable_Webserver/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.CreateHANNetwork);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Create_Han_Network> </Create_Han_Network>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveHANNetwork);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_Han_Network> </Remove_Han_Network>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.BackUpZigBeeHANParameters);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Backup_ZigBee_Han_Parameters> </Backup_ZigBee_Han_Parameters>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RestoreZigBeeHANParameters);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Restore_ZigBee_Han_Parameters Restore_UserFile_ID=\"10\"> </Restore_ZigBee_Han_Parameters>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.JoinZigBeeSlaveFromDeviceType);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Join_ZigBee_Slave_Device ZigBee_IEEE_Address=\"ABC\" ZigBee_Link_Key=\"123\" ZigBee_Device_Type=\"3\"> </Join_ZigBee_Slave_Device>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveZigBeeSlaveDevice);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_ZigBee_Slave ZigBee_IEEE_Address=\"ABC\"> </Remove_ZigBee_Slave>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveAllZigBeeSlaveDevices);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_All_ZigBee_Slaves> </Remove_All_ZigBee_Slaves>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveMirror);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_Mirror Mirror_IEEE_Address=\"1\" Force_Removal=\"false\"> </Remove_Mirror>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.UpdateLinkKey);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Update_HAN_Link_Key ZigBee_IEEE_Address=\"ABC\"> </Update_HAN_Link_Key>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFile);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ZigBeeNCPFirmwareUpgrade><IncludedFile>Content</IncludedFile></ZigBeeNCPFirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFileAndActivate);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ZigBeeNCPFirmwareUpgrade><IncludedFile>Content</IncludedFile><ActivationDate>28/10/2013 10:30:00</ActivationDate></ZigBeeNCPFirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>Content</IncludedFile></FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>Content</IncludedFile><ActivationDate>28/10/2013 10:30:00</ActivationDate></FirmwareUpgrade>", messageEntry.getContent());

    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AM110R();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new AM110RMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.gprsModeAttributeName:
                    return new BigDecimal(9);
                case DeviceMessageConstants.wakeupPeriodAttributeName:
                    return new BigDecimal(33);
                case DeviceMessageConstants.inactivityTimeoutAttributeName:
                    return new BigDecimal(100);
                case DeviceMessageConstants.networkOperator + "_" + 1:
                case DeviceMessageConstants.networkOperator + "_" + 2:
                case DeviceMessageConstants.networkOperator + "_" + 3:
                case DeviceMessageConstants.networkOperator + "_" + 4:
                case DeviceMessageConstants.networkOperator + "_" + 5:
                case DeviceMessageConstants.networkOperator + "_" + 6:
                case DeviceMessageConstants.networkOperator + "_" + 7:
                case DeviceMessageConstants.networkOperator + "_" + 8:
                case DeviceMessageConstants.networkOperator + "_" + 9:
                case DeviceMessageConstants.networkOperator + "_" + 10:
                    return "Operator_x";
                case DeviceMessageConstants.fromDateAttributeName:
                    return europeanDateTimeFormat.parse("01/10/2013 00:00:00");
                case DeviceMessageConstants.toDateAttributeName:
                    return europeanDateTimeFormat.parse("15/10/2013 00:00:00");
                case DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName:
                    return "ABC";
                case DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName:
                    return new Password("123");
                case DeviceMessageConstants.ZigBeeConfigurationDeviceType:
                    return "3";
                case DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName:
                    return "1";
                case DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName:
                    return false;
                case DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName:
                case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                    UserFile mockedUserFile = mock(UserFile.class);
                    when(mockedUserFile.loadFileInByteArray()).thenReturn("Content".getBytes(Charset.forName("UTF-8")));
                    return mockedUserFile;
                case DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName:
                    mockedUserFile = mock(UserFile.class);
                    when(mockedUserFile.getId()).thenReturn(10);
                    return mockedUserFile;
                case DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeName:
                    return "A";
                case DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeName:
                    return "1";
                case DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeName:
                    return 2;
                case DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeName:
                    return new Boolean(true);
                case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                case DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName:
                    return europeanDateTimeFormat.parse("28/10/2013 10:30:00");
                case DeviceMessageConstants.xmlConfigAttributeName:
                    return "<XML>Content</XML>";
                default:
                    return "0";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}
