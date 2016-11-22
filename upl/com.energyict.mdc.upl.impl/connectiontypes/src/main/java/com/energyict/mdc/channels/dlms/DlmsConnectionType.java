package com.energyict.mdc.channels.dlms;


import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dynamicattributes.BigDecimalFactory;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 10/7/13
 * Time: 1:33 PM
 */
public abstract class DlmsConnectionType extends ConnectionTypeImpl {
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

    PropertySpec getAddressingModePropertySpec() {
        return PropertySpecBuilder.
                forClass(BigDecimal.class, new BigDecimalFactory()).
                name(PROPERTY_NAME_ADDRESSING_MODE).
                markExhaustive().
                addValues(
                        BigDecimal.valueOf(1),
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(4)).
                setDefaultValue(BigDecimal.valueOf(2)).
                finish();
    }

    PropertySpec getServerMacAddress() {
        return PropertySpecFactory.bigDecimalPropertySpec(PROPERTY_NAME_SERVER_MAC_ADDRESS, new BigDecimal(1));
    }

    PropertySpec getServerUpperMacAddress() {
        return PropertySpecFactory.bigDecimalPropertySpec(PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS, new BigDecimal(17));
    }

    PropertySpec getServerLowerMacAddress() {
        return PropertySpecFactory.bigDecimalPropertySpec(PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS, new BigDecimal(1));
    }

    PropertySpec getConnectionPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(PROPERTY_NAME_CONNECTION, new BigDecimal(1));
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }
}
