package com.energyict.mdc.channels.dlms;


import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

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
    private final PropertySpecService propertySpecService;

    protected DlmsConnectionType(ConnectionType actualConnectionType, PropertySpecService propertySpecService) {
        this.actualConnectionType = actualConnectionType;
        this.propertySpecService = propertySpecService;
    }

    ConnectionType getActualConnectionType() {
        return actualConnectionType;
    }

    PropertySpec getAddressingModePropertySpec() {
        return this.bigDecimalSpec(PROPERTY_NAME_ADDRESSING_MODE, BigDecimal.valueOf(2),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(4));
    }

    PropertySpec getServerMacAddress() {
        return this.bigDecimalSpec(PROPERTY_NAME_SERVER_MAC_ADDRESS, new BigDecimal(1));
    }

    PropertySpec getServerUpperMacAddress() {
        return this.bigDecimalSpec(PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS, new BigDecimal(17));
    }

    PropertySpec getServerLowerMacAddress() {
        return this.bigDecimalSpec(PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS, new BigDecimal(1));
    }

    PropertySpec getConnectionPropertySpec() {
        return this.bigDecimalSpec(PROPERTY_NAME_CONNECTION, new BigDecimal(1));
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

    protected PropertySpec bigDecimalSpec(String name, BigDecimal defaultvalue, BigDecimal... possibleValues) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, false, this.propertySpecService::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultvalue);
        specBuilder.addValues(possibleValues);
        if (possibleValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }
}
