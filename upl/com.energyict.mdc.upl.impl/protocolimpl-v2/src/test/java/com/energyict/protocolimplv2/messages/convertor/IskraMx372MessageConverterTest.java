package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372;
import org.junit.Test;

import java.text.ParseException;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link IskraMx372MessageConverter} component.
 *
 * @author sva
 * @since 25/10/13 - 10:12
 */
public class IskraMx372MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;


        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_LLS_SECRET_HEX);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_LLS_Secret LLSSecret'=\"FF00AA\"> </Change_LLS_Secret>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_modem_credentials Username=\"user\" Password=\"pass\"> </GPRS_modem_credentials>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_modem_setup><APN>apn</APN><Username>user</Username><Password>pass</Password></GPRS_modem_setup>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Mode>1</Mode>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_CLOSE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<connectLoad> </connectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<disconnectLoad> </disconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<UserFile ID of tariff program>1</UserFile ID of tariff program>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LoadBalanceDeviceMessage.ENABLE_LOAD_LIMITING_FOR_GROUP);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ApplyLoadLimiting><Threshold GroupId *>1</Threshold GroupId *><StartDate (dd/mm/yyyy HH:MM:SS)>01/10/2013 00:00:00</StartDate (dd/mm/yyyy HH:MM:SS)><EndDate (dd/mm/yyyy HH:MM:SS)>01/11/2013 00:00:00</EndDate (dd/mm/yyyy HH:MM:SS)></ApplyLoadLimiting>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Clear threshold - groupID>1</Clear threshold - groupID>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ConfigureLoadLimitingParameters><Parameter GroupId *>1</Parameter GroupId *><Threshold PowerLimit (W)>1</Threshold PowerLimit (W)><Contractual PowerLimit (W)>1</Contractual PowerLimit (W)></ConfigureLoadLimitingParameters>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.Commission);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Mbus_Install/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.DataReadout);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Mbus_DataReadout/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.Decommission);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Mbus_Remove/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Activate_the_wakeup_mechanism/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Phonenumbers_to_add><ManagedPhonenumber1>number1</ManagedPhonenumber1><ManagedPhonenumber2>number2</ManagedPhonenumber2></Phonenumbers_to_add>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Phonenumbers_to_add><Phonenumber1>number1</Phonenumber1><Phonenumber2>number2</Phonenumber2></Phonenumbers_to_add>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_INACTIVITY_TIMEOUT);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Inactivity_timeout>1</Inactivity_timeout>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new IskraMx372();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new IskraMx372MessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.usernameAttributeName:
                    return "user";
                case DeviceMessageConstants.passwordAttributeName:
                    return "pass";
                case DeviceMessageConstants.apnAttributeName:
                    return "apn";
                case DeviceMessageConstants.contactorModeAttributeName:
                case DeviceMessageConstants.loadLimitGroupIDAttributeName:
                case DeviceMessageConstants.inactivityTimeoutAttributeName:
                case DeviceMessageConstants.powerLimitThresholdAttributeName:
                case DeviceMessageConstants.contractualPowerLimitAttributeName:
                case DeviceMessageConstants.activityCalendarCodeTableAttributeName:
                    return 1;
                case DeviceMessageConstants.loadLimitStartDateAttributeName:
                    return europeanDateTimeFormat.parse("01/10/2013 00:00:00");
                case DeviceMessageConstants.loadLimitEndDateAttributeName:
                    return europeanDateTimeFormat.parse("01/11/2013 00:00:00");
                case DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName:
                case DeviceMessageConstants.whiteListPhoneNumbersAttributeName:
                    return "number1; number2";
                case DeviceMessageConstants.newHexPasswordAttributeName:
                    return new Password("FF00AA");
                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}
