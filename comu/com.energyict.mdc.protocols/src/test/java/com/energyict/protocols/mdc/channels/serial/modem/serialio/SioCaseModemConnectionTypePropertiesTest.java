package com.energyict.protocols.mdc.channels.serial.modem.serialio;

import com.energyict.protocols.mdc.channels.serial.modem.TypedCaseModemProperties;
import com.energyict.protocols.mdc.channels.serial.direct.serialio.SioSerialConnectionTypeTest;

import java.util.Set;

/**
 * Tests the properties of the {@link SioCaseModemConnectionType} component.
 *
 * @author sva
 * @since 30/04/13 - 14:42
 */
public class SioCaseModemConnectionTypePropertiesTest extends SioSerialConnectionTypeTest {

    @Override
    protected SioCaseModemConnectionType newConnectionType () {
        return new SioCaseModemConnectionType();
    }

    @Override
    protected Set<String> requiredPropertyNames () {
        Set<String> propertyNames = super.requiredPropertyNames();
        propertyNames.add(TypedCaseModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        return propertyNames;
    }

    @Override
    protected Set<String> optionalPropertyNames () {
        Set<String> propertyNames = super.optionalPropertyNames();
        propertyNames.add(TypedCaseModemProperties.MODEM_DIAL_PREFIX);
        propertyNames.add(TypedCaseModemProperties.CONNECT_TIMEOUT);
        propertyNames.add(TypedCaseModemProperties.DELAY_AFTER_CONNECT);
        propertyNames.add(TypedCaseModemProperties.DELAY_BEFORE_SEND);
        propertyNames.add(TypedCaseModemProperties.COMMAND_TIMEOUT);
        propertyNames.add(TypedCaseModemProperties.COMMAND_TRIES);
        propertyNames.add(TypedCaseModemProperties.MODEM_INIT_STRINGS);
        propertyNames.add(TypedCaseModemProperties.MODEM_ADDRESS_SELECTOR);
        propertyNames.add(TypedCaseModemProperties.DTR_TOGGLE_DELAY);
        return propertyNames;
    }

}
