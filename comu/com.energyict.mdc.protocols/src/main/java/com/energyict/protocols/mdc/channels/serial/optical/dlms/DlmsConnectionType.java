package com.energyict.protocols.mdc.channels.serial.optical.dlms;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.BigDecimalFactory;
import com.energyict.mdc.protocol.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.protocol.dynamic.PropertySpecBuilder;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 10/7/13
 * Time: 1:33 PM
 */
public abstract class DlmsConnectionType extends ConnectionTypeImpl {

    private static final BigDecimal DEFAULT_ADDRESSING_MODE = BigDecimal.valueOf(2);
    private static final BigDecimal DEFAULT_SERVER_MAC_ADDRESS = BigDecimal.ONE;
    private static final BigDecimal DEFAULT_SERVER_UPPER_MAC_ADDRESS = new BigDecimal(17);
    private static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.ONE;
    private static final BigDecimal DEFAULT_CONNECTION = BigDecimal.ONE;

    /**
     * The AddressingMode to use
     */
    public static final String PROPERTY_NAME_ADDRESSING_MODE = "AddressingMode";
    public static final String PROPERTY_NAME_SERVER_MAC_ADDRESS = "ServerMacAddress";
    public static final String PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    public static final String PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    public static final String PROPERTY_NAME_CONNECTION = "Connection";

    private final ConnectionType actualConnectionType;

    protected DlmsConnectionType(ConnectionType actualConnectionType) {
        this.actualConnectionType = actualConnectionType;
    }

    ConnectionType getActualConnectionType() {
        return actualConnectionType;
    }

    abstract PropertySpec getAddressingModePropertySpec();

    final PropertySpec getAddressingModePropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder = PropertySpecBuilder.forClass(BigDecimal.class, new BigDecimalFactory());
        builder.
            name(PROPERTY_NAME_ADDRESSING_MODE).
            markExhaustive().
            addValues(
                    BigDecimal.ONE,
                    DEFAULT_ADDRESSING_MODE,
                    BigDecimal.valueOf(4)).
            setDefaultValue(DEFAULT_ADDRESSING_MODE);
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    abstract PropertySpec getServerMacAddress();

    final PropertySpec getServerMacAddress(boolean required){
        if (required) {
            return RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(PROPERTY_NAME_SERVER_MAC_ADDRESS, DEFAULT_SERVER_MAC_ADDRESS);
        }
        else {
            return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(PROPERTY_NAME_SERVER_MAC_ADDRESS, DEFAULT_SERVER_MAC_ADDRESS);
        }
    }

    abstract PropertySpec getServerUpperMacAddress();

    final PropertySpec getServerUpperMacAddress(boolean required){
        if (required) {
            return RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS, DEFAULT_SERVER_UPPER_MAC_ADDRESS);
        }
        else {
            return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS, DEFAULT_SERVER_UPPER_MAC_ADDRESS);
        }
    }

    abstract PropertySpec getServerLowerMacAddress();

    final PropertySpec getServerLowerMacAddress(boolean required) {
        if (required) {
            return RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
        }
        else {
            return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
        }
    }

    abstract PropertySpec getConnectionPropertySpec();
    final PropertySpec getConnectionPropertySpec(boolean required) {
        if (required) {
            return RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(PROPERTY_NAME_CONNECTION, DEFAULT_CONNECTION);
        }
        else {
            return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(PROPERTY_NAME_CONNECTION, DEFAULT_CONNECTION);
        }
    }

}