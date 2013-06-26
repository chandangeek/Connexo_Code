package com.energyict.mdc.channels.ip;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import java.util.ArrayList;
import java.util.Collections;
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
        return PropertySpecFactory.stringPropertySpec(CALL_HOME_ID_PROPERTY_NAME);
    }

    protected String callHomeIdPropertyValue() {
        return (String) this.getProperty(CALL_HOME_ID_PROPERTY_NAME);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (CALL_HOME_ID_PROPERTY_NAME.equals(name)) {
            return this.callHomeIdPropertySpec();
        }
        return null;
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return CALL_HOME_ID_PROPERTY_NAME.equals(name);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> requiredProperties = new ArrayList<>(1);
        requiredProperties.add(this.callHomeIdPropertySpec());
        return requiredProperties;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
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
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}
