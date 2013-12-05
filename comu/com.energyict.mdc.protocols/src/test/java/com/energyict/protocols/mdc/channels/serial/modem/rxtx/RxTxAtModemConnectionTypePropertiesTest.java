package com.energyict.protocols.mdc.channels.serial.modem.rxtx;

import com.energyict.mdc.channels.serial.modem.TypedAtModemProperties;
import com.energyict.protocols.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionTypeTest;

import java.util.Set;

/**
 * Tests the properties of the {@link com.energyict.protocols.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (14:00)
 */
public class RxTxAtModemConnectionTypePropertiesTest extends RxTxSerialConnectionTypeTest {

    @Override
    protected RxTxAtModemConnectionType newConnectionType () {
        return new RxTxAtModemConnectionType();
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