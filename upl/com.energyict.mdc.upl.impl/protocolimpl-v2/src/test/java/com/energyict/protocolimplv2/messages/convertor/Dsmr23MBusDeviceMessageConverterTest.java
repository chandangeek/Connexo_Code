package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.util.Date;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link Dsmr23MBusDeviceMessageConverter} component.
 *
 * @author sva
 * @since 30/10/13 - 9:06
 */
@RunWith(MockitoJUnitRunner.class)
public class Dsmr23MBusDeviceMessageConverterTest extends AbstractMessageConverterTest {

    private Date activityCalendarActivationDate;

    @Test
    public void testMessageConversion() {

        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;
        try {
            activityCalendarActivationDate = europeanDateTimeFormat.parse("30/10/2013 10:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_CLOSE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<connectLoad> </connectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<connectLoad Activation_date=\""+activityCalendarActivationDate.getTime() / 1000+"\"> </connectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<disconnectLoad> </disconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<disconnectLoad Activation_date=\""+activityCalendarActivationDate.getTime() / 1000+"\"> </disconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Connect_control_mode Mode=\"1\"> </Connect_control_mode>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.Decommission);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Decommission/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.SetEncryptionKeys);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Set_Encryption_keys Open_Key_Value=\"open\" Transfer_Key_Value=\"transfer\"> </Set_Encryption_keys>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.UseCorrectedValues);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Corrected_values/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.UseUncorrectedValues);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<UnCorrected_values/>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new MbusDevice();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new Dsmr23MBusDeviceMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.contactorActivationDateAttributeName:
                return activityCalendarActivationDate;
            case DeviceMessageConstants.contactorModeAttributeName:
                return 1;
            case DeviceMessageConstants.openKeyAttributeName:
                return new Password("open");
            case DeviceMessageConstants.transferKeyAttributeName:
                return new Password("transfer");
            default:
                return "";
        }
    }
}
