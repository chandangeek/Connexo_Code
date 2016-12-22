package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link Dsmr40MessageConverter} component.
 *
 * @author sva
 * @since 30/10/13 - 14:22
 */
public class Dsmr40MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceActionMessage.RESTORE_FACTORY_SETTINGS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Restore_Factory_Settings/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ChangeAdministrativeStatus);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Administrative_Status Status=\"1\"> </Change_Administrative_Status>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P0);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Disable_authentication_level_P0 AuthenticationLevel=\"1\"> </Disable_authentication_level_P0>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P3);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Disable_authentication_level_P3 AuthenticationLevel=\"1\"> </Disable_authentication_level_P3>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P0);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Enable_authentication_level_P0 AuthenticationLevel=\"1\"> </Enable_authentication_level_P0>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P3);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Enable_authentication_level_P3 AuthenticationLevel=\"1\"> </Enable_authentication_level_P3>", messageEntry.getContent());

        // Test the DSMR2.3 CHANGE_DLMS_AUTHENTICATION_LEVEL message is removed
        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertTrue(messageEntry.getContent().isEmpty());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new E350();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new Dsmr40MessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.AdministrativeStatusAttributeName:
                return 1;
            case DeviceMessageConstants.authenticationLevelAttributeName:
                return "Low level authentication";
            default:
                return "";
        }
    }
}