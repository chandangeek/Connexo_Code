package com.energyict.protocolimplv2.messages.convertor;

import com.elster.protocolimpl.dlms.EK280;
import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;

/**
 * @author sva
 * @since 13/08/2015 - 17:01
 */
public class EK280MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion_ChangeCredentials() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_modem_setup APN=\"MyTestAPN\" Username=\"MyTestUserName\" Password=\"MyTestPassword\"> </GPRS_modem_setup>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_WriteNewPdrNumber() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.WriteNewPDRNumber);
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WritePDR PdrToWrite=\"PDR\"> </WritePDR>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ClearPassiveTariff() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF);
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ClearPassiveTariff/>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ChangeSecurityKeys() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_SECURITY_KEYS);
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ChangeKeys ClientId=\"1\" WrapperKey=\"MASTER_Key\" NewAuthenticationKey=\"AUTH_Key\" NewEncryptionKey=\"ENCR_Key\"> </ChangeKeys>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new EK280();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new EK280MessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
//        try {
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
            default:
                return "";
        }
//        } catch (ParseException e) {
//            return "";
//        }
    }
}