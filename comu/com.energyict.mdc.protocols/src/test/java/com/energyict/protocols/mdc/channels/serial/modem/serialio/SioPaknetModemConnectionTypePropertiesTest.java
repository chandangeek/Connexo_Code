package com.energyict.protocols.mdc.channels.serial.modem.serialio;

import com.energyict.protocols.mdc.channels.serial.direct.serialio.SioSerialConnectionTypeTest;
import com.energyict.protocols.mdc.channels.serial.modem.TypedPaknetModemProperties;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

/**
 * Tests the properties of the {@link com.energyict.protocols.mdc.channels.serial.modem.serialio.SioPaknetModemConnectionType} component.
 *
 * @author sva
 * @since 15/04/13 - 11:09
 */
@RunWith(MockitoJUnitRunner.class)
public class SioPaknetModemConnectionTypePropertiesTest extends SioSerialConnectionTypeTest {

    @Override
    protected SioPaknetModemConnectionType newConnectionType () {
        return new SioPaknetModemConnectionType();
    }

    @Override
    protected Set<String> requiredPropertyNames () {
        Set<String> propertyNames = super.requiredPropertyNames();
        propertyNames.add(TypedPaknetModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        return propertyNames;
    }

    @Override
    protected Set<String> optionalPropertyNames () {
        Set<String> propertyNames = super.optionalPropertyNames();
        propertyNames.add(TypedPaknetModemProperties.MODEM_DIAL_PREFIX);
        propertyNames.add(TypedPaknetModemProperties.CONNECT_TIMEOUT);
        propertyNames.add(TypedPaknetModemProperties.DELAY_AFTER_CONNECT);
        propertyNames.add(TypedPaknetModemProperties.DELAY_BEFORE_SEND);
        propertyNames.add(TypedPaknetModemProperties.COMMAND_TIMEOUT);
        propertyNames.add(TypedPaknetModemProperties.COMMAND_TRIES);
        propertyNames.add(TypedPaknetModemProperties.MODEM_INIT_STRINGS);
        propertyNames.add(TypedPaknetModemProperties.DTR_TOGGLE_DELAY);
        return propertyNames;
    }

}