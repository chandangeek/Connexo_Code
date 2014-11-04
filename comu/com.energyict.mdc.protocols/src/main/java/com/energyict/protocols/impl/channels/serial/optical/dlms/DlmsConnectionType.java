package com.energyict.protocols.impl.channels.serial.optical.dlms;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;
import com.energyict.protocols.mdc.protocoltasks.ServerConnectionType;

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

    private final ServerConnectionType actualConnectionType;

    protected DlmsConnectionType(ServerConnectionType actualConnectionType) {
        this.actualConnectionType = actualConnectionType;
    }

    ConnectionType getActualConnectionType() {
        return actualConnectionType;
    }

    abstract PropertySpec getAddressingModePropertySpec();

    final PropertySpec getAddressingModePropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder = this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory());
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
        return this.getPropertySpecService().bigDecimalPropertySpec(PROPERTY_NAME_SERVER_MAC_ADDRESS, required, DEFAULT_SERVER_MAC_ADDRESS);
    }

    abstract PropertySpec getServerUpperMacAddress();

    final PropertySpec getServerUpperMacAddress(boolean required){
        return this.getPropertySpecService().bigDecimalPropertySpec(PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS, required, DEFAULT_SERVER_UPPER_MAC_ADDRESS);
    }

    abstract PropertySpec getServerLowerMacAddress();

    final PropertySpec getServerLowerMacAddress(boolean required) {
        return this.getPropertySpecService().bigDecimalPropertySpec(PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS, required, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    abstract PropertySpec getConnectionPropertySpec();
    final PropertySpec getConnectionPropertySpec(boolean required) {
        return this.getPropertySpecService().bigDecimalPropertySpec(PROPERTY_NAME_CONNECTION, required, DEFAULT_CONNECTION);
    }

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }
}