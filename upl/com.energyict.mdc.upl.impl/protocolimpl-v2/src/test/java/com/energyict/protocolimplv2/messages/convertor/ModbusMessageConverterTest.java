package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimpl.modbus.multilin.epm2200.EPM2200;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.ModbusConfigurationDeviceMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link ModbusMessageConverter} component.
 *
 * @author sva
 * @since 24/10/13 - 10:50
 */
@RunWith(MockitoJUnitRunner.class)
public class ModbusMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ModbusConfigurationDeviceMessage.WriteSingleRegisters);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WriteSingleRegisters>HEX,1A,AB</WriteSingleRegisters>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ModbusConfigurationDeviceMessage.WriteMultipleRegisters);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WriteMultipleRegisters>HEX,1A,AB</WriteMultipleRegisters>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new EPM2200();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new ModbusMessageConverter();
    }

    /**
     * Gets the value to use for the given {@link PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.RadixFormatAttributeName:
                return "HEX";
            case DeviceMessageConstants.RegisterAddressAttributeName:
                return "1A";
            case DeviceMessageConstants.RegisterValueAttributeName:
                return "AB";
            default:
                return "";
        }
    }
}
