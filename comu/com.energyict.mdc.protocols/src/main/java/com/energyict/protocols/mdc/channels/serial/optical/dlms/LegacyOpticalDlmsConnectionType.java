package com.energyict.protocols.mdc.channels.serial.optical.dlms;

import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 10/7/13
 * Time: 1:32 PM
 */
public class LegacyOpticalDlmsConnectionType extends DlmsConnectionType {

    public LegacyOpticalDlmsConnectionType() {
        super(new SioOpticalConnectionType());
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return getActualConnectionType().allowsSimultaneousConnections();
    }

    @Override
    public boolean supportsComWindow() {
        return getActualConnectionType().supportsComWindow();
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return getActualConnectionType().getSupportedComPortTypes();
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return getActualConnectionType().connect(properties);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name){
            case PROPERTY_NAME_ADDRESSING_MODE:
                return this.getAddressingModePropertySpec();
            case PROPERTY_NAME_CONNECTION:
                return this.getConnectionPropertySpec();
            case PROPERTY_NAME_SERVER_MAC_ADDRESS:
                return this.getServerMacAddress();
            case PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS:
                return this.getServerLowerMacAddress();
            case PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS:
                return this.getServerUpperMacAddress();
            default:
                return getActualConnectionType().getPropertySpec(name);
        }
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        propertySpecs.addAll(this.getActualConnectionType().getPropertySpecs());
        propertySpecs.add(this.getAddressingModePropertySpec());
        propertySpecs.add(this.getConnectionPropertySpec());
        propertySpecs.add(this.getServerMacAddress());
        propertySpecs.add(this.getServerLowerMacAddress());
        propertySpecs.add(this.getServerUpperMacAddress());
    }

    @Override
    PropertySpec getServerLowerMacAddress () {
        return this.getServerLowerMacAddress(false);
    }

    @Override
    PropertySpec getServerUpperMacAddress () {
        return this.getServerUpperMacAddress(false);
    }

    @Override
    PropertySpec getServerMacAddress () {
        return this.getServerMacAddress(false);
    }

    @Override
    PropertySpec getAddressingModePropertySpec () {
        return this.getAddressingModePropertySpec(false);
    }

    PropertySpec getConnectionPropertySpec() {
        return this.getPropertySpecService().bigDecimalPropertySpec(PROPERTY_NAME_CONNECTION, false, BigDecimal.ZERO);
    }

}