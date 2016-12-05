package com.energyict.mdc.channels.dlms;


import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

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
        return UPLPropertySpecFactory.bigDecimal(PROPERTY_NAME_ADDRESSING_MODE, false, BigDecimal.valueOf(2),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(4));
    }

    PropertySpec getServerMacAddress() {
        return UPLPropertySpecFactory.bigDecimal(PROPERTY_NAME_SERVER_MAC_ADDRESS, false, new BigDecimal(1));
    }

    PropertySpec getServerUpperMacAddress() {
        return UPLPropertySpecFactory.bigDecimal(PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS, false, new BigDecimal(17));
    }

    PropertySpec getServerLowerMacAddress() {
        return UPLPropertySpecFactory.bigDecimal(PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS, false, new BigDecimal(1));
    }

    PropertySpec getConnectionPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(PROPERTY_NAME_CONNECTION, false, new BigDecimal(1));
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }
}
