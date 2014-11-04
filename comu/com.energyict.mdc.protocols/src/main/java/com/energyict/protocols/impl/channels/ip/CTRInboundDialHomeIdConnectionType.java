package com.energyict.protocols.impl.channels.ip;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.protocols.impl.channels.VoidComChannel;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Inbound TCP connection type created for the CTR protocol base (as used by MTU155 and EK155 DeviceProtocols).<br>
 * Conform the CTR spec, this connectionType contains a required property for CallHomeId - as knocking devices are unique identified by their CallHomeID.
 */
public class CTRInboundDialHomeIdConnectionType extends ConnectionTypeImpl {

    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";

    private final PropertySpecService propertySpecService;

    @Inject
    public CTRInboundDialHomeIdConnectionType(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec callHomeIdPropertySpec() {
        return this.propertySpecService.basicPropertySpec(CALL_HOME_ID_PROPERTY_NAME, true, new StringFactory());
    }

    protected String callHomeIdPropertyValue() {
        return (String) this.getProperty(CALL_HOME_ID_PROPERTY_NAME);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(this.callHomeIdPropertySpec());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (CALL_HOME_ID_PROPERTY_NAME.equals(name)) {
            return this.callHomeIdPropertySpec();
        }
        return null;
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return false;
    }

    @Override
    public boolean supportsComWindow() {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.TCP);
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for this CTRInboundDialHomeIdConnectionType
    }

    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-26 15:15:49 +0200 (Mit, 26 Jun 2013) $";
    }

}