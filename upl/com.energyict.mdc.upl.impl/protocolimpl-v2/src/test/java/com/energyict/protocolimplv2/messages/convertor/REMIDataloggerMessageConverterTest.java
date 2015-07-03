package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.REMIDatalogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link REMIDataloggerMessageConverter} component.
 *
 * @author sva
 * @since 30/10/13 - 13:27
 */
@RunWith(MockitoJUnitRunner.class)
public class REMIDataloggerMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_modem_credentials Username=\"user\" Password=\"pass\"> </GPRS_modem_credentials>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_modem_setup APN=\"apn\" Username=\"user\" Password=\"pass\"> </GPRS_modem_setup>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ClockDeviceMessage.EnableOrDisableDST);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<EnableDST>1</EnableDST>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.ALARM_REGISTER_RESET);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Reset_Alarm_Register/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.ERROR_REGISTER_RESET);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Reset_Error_Register/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SetAlarmFilter);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Alarm_Filter>ABCDEF01</Alarm_Filter>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new REMIDatalogger();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new REMIDataloggerMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch(propertySpec.getName()) {
            case DeviceMessageConstants.usernameAttributeName:
                return "user";
            case DeviceMessageConstants.passwordAttributeName:
                return new Password("pass");
            case DeviceMessageConstants.apnAttributeName:
                return "apn";
            case DeviceMessageConstants.enableDSTAttributeName:
                return new Boolean(true);
            case DeviceMessageConstants.AlarmFilterAttributeName:
                return "ABCDEF01";
            default:
                return "";
        }
    }
}