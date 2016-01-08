package com.energyict.protocols.impl.channels.serial.modem.serialio;

import com.energyict.mdc.channels.serial.modem.TypedPaknetModemProperties;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioSerialConnectionTypeTest;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

/**
 * @author sva
 * @since 29/04/13 - 16:28
 */
@RunWith(MockitoJUnitRunner.class)
public class SioPEMPModemConnectionTypePropertiesTest extends SioSerialConnectionTypeTest {

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