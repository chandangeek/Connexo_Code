package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372;

import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link IskraMx372MessageConverter} component.
 *
 * @author sva
 * @since 25/10/13 - 10:12
 */
@RunWith(MockitoJUnitRunner.class)
public class IskraMx372MessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;


        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_LLS_SECRET_HEX.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_LLS_Secret LLSSecret'=\"FF00AA\"> </Change_LLS_Secret>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_modem_credentials Username=\"user\" Password=\"pass\"> </GPRS_modem_credentials>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_modem_setup><APN>apn</APN><Username>user</Username><Password>pass</Password></GPRS_modem_setup>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Mode>1</Mode>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_CLOSE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<connectLoad> </connectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<disconnectLoad> </disconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<UserFile ID of tariff program>1</UserFile ID of tariff program>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LoadBalanceDeviceMessage.ENABLE_LOAD_LIMITING_FOR_GROUP.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ApplyLoadLimiting><Threshold GroupId *>1</Threshold GroupId *><StartDate (dd/mm/yyyy HH:MM:SS)>01/10/2013 00:00:00</StartDate (dd/mm/yyyy HH:MM:SS)><EndDate (dd/mm/yyyy HH:MM:SS)>01/11/2013 00:00:00</EndDate (dd/mm/yyyy HH:MM:SS)></ApplyLoadLimiting>", messageEntry
                .getContent());

        offlineDeviceMessage = createMessage(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Clear threshold - groupID>1</Clear threshold - groupID>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ConfigureLoadLimitingParameters><Parameter GroupId *>1</Parameter GroupId *><Threshold PowerLimit (W)>1</Threshold PowerLimit (W)><Contractual PowerLimit (W)>1</Contractual PowerLimit (W)></ConfigureLoadLimitingParameters>", messageEntry
                .getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.Commission.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Mbus_Install/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.DataReadout.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Mbus_DataReadout/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.Decommission.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Mbus_Remove/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Activate_the_wakeup_mechanism/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Phonenumbers_to_add><ManagedPhonenumber1>number1</ManagedPhonenumber1><ManagedPhonenumber2>number2</ManagedPhonenumber2></Phonenumbers_to_add>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Phonenumbers_to_add><Phonenumber1>number1</Phonenumber1><Phonenumber2>number2</Phonenumber2></Phonenumbers_to_add>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_INACTIVITY_TIMEOUT.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Inactivity_timeout>1</Inactivity_timeout>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new IskraMx372(propertySpecService, calendarFinder, calendarExtractor, deviceMessageFileExtractor, deviceMessageFileFinder, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new IskraMx372MessageConverter(propertySpecService, this.nlsService, this.converter, this.loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.usernameAttributeName:
                    return "user";
                case DeviceMessageConstants.passwordAttributeName:
                    KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
                    when(keyAccessorTypeExtractor.passiveValueContent(keyAccessorType)).thenReturn("pass");
                    return keyAccessorType;
                case DeviceMessageConstants.apnAttributeName:
                    return "apn";
                case DeviceMessageConstants.contactorModeAttributeName:
                case DeviceMessageConstants.loadLimitGroupIDAttributeName:
                case DeviceMessageConstants.inactivityTimeoutAttributeName:
                case DeviceMessageConstants.powerLimitThresholdAttributeName:
                case DeviceMessageConstants.contractualPowerLimitAttributeName:
                case DeviceMessageConstants.activityCalendarAttributeName:
                    return 1;
                case DeviceMessageConstants.loadLimitStartDateAttributeName:
                    return europeanDateTimeFormat.parse("01/10/2013 00:00:00");
                case DeviceMessageConstants.loadLimitEndDateAttributeName:
                    return europeanDateTimeFormat.parse("01/11/2013 00:00:00");
                case DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName:
                case DeviceMessageConstants.whiteListPhoneNumbersAttributeName:
                    return "number1; number2";
                case DeviceMessageConstants.newHexPasswordAttributeName:
                    keyAccessorType = mock(KeyAccessorType.class);
                    when(keyAccessorTypeExtractor.passiveValueContent(keyAccessorType)).thenReturn("FF00AA");
                    return keyAccessorType;
                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}
