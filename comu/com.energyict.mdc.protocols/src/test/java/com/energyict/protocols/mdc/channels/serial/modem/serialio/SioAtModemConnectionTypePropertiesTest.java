package com.energyict.protocols.mdc.channels.serial.modem.serialio;

import com.energyict.protocols.mdc.channels.serial.direct.serialio.SioSerialConnectionTypeTest;
import com.energyict.protocols.mdc.channels.serial.modem.TypedAtModemProperties;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

/**
 * Tests the properties of the {@link com.energyict.protocols.mdc.channels.serial.modem.serialio.SioAtModemConnectionType} component.
 *
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 15:32
 */
@RunWith(MockitoJUnitRunner.class)
public class SioAtModemConnectionTypePropertiesTest extends SioSerialConnectionTypeTest {

    @Override
    protected SioAtModemConnectionType newConnectionType () {
        return new SioAtModemConnectionType();
    }

    @Override
    protected Set<String> requiredPropertyNames () {
        Set<String> propertyNames = super.requiredPropertyNames();
        propertyNames.add(TypedAtModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        return propertyNames;
    }

    @Override
    protected Set<String> optionalPropertyNames () {
        Set<String> propertyNames = super.optionalPropertyNames();
        propertyNames.add(TypedAtModemProperties.DELAY_BEFORE_SEND);
        propertyNames.add(TypedAtModemProperties.DELAY_AFTER_CONNECT);
        propertyNames.add(TypedAtModemProperties.AT_COMMAND_TIMEOUT);
        propertyNames.add(TypedAtModemProperties.AT_CONNECT_TIMEOUT);
        propertyNames.add(TypedAtModemProperties.AT_COMMAND_TRIES);
        propertyNames.add(TypedAtModemProperties.AT_MODEM_INIT_STRINGS);
        propertyNames.add(TypedAtModemProperties.AT_MODEM_DIAL_PREFIX);
        propertyNames.add(TypedAtModemProperties.AT_MODEM_ADDRESS_SELECTOR);
        propertyNames.add(TypedAtModemProperties.AT_MODEM_POST_DIAL_COMMANDS);
        propertyNames.add(TypedAtModemProperties.DTR_TOGGLE_DELAY);
        return propertyNames;
    }

}