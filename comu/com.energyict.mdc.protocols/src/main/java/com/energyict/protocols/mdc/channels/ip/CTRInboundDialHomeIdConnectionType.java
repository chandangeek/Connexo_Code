package com.energyict.protocols.mdc.channels.ip;

import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.mdc.channels.VoidComChannel;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Inbound TCP connection type created for the CTR protocol base (as used by MTU155 and EK155 DeviceProtocols).<br></br>
 * Conform the CTR spec, this connectionType contains a required property for CallHomeId - as knocking devices are unique identified by their CallHomeID.
 * <p/>
 */
public class CTRInboundDialHomeIdConnectionType extends ConnectionTypeImpl {

    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";

    private PropertySpec callHomeIdPropertySpec() {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(CALL_HOME_ID_PROPERTY_NAME);
    }

    protected String callHomeIdPropertyValue() {
        return (String) this.getProperty(CALL_HOME_ID_PROPERTY_NAME);
    }

    @Override
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        propertySpecs.add(this.callHomeIdPropertySpec());
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
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-26 15:15:49 +0200 (Mit, 26 Jun 2013) $";
    }
}
