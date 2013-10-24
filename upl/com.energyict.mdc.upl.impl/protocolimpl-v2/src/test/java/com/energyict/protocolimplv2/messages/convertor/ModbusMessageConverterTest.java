package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimpl.modbus.multilin.epm2200.EPM2200;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.ModbusConfigurationDeviceMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message, using the {@link ModbusMessageConverter}.
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
        messageEntry = new WriteModbusRegisterMessage().createMessageEntry(new EPM2200(), offlineDeviceMessage);
        assertEquals("<WriteSingleRegisters>HEX,1A,AB</WriteSingleRegisters>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ModbusConfigurationDeviceMessage.WriteMultipleRegisters);
        messageEntry = new WriteModbusRegisterMessage().createMessageEntry(new EPM2200(), offlineDeviceMessage);
        assertEquals("<WriteMultipleRegisters>HEX,1A,AB</WriteMultipleRegisters>", messageEntry.getContent());
    }

    /**
     * getter for the {@link LegacyMessageConverter} which will be purpose of the test
     */
    protected LegacyMessageConverter getMessageConverter() {
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
