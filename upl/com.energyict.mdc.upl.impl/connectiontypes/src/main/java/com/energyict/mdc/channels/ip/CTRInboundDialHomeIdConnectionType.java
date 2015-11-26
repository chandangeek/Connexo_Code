package com.energyict.mdc.channels.ip;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import javax.xml.bind.annotation.XmlRootElement;
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
@XmlRootElement
public class CTRInboundDialHomeIdConnectionType extends ConnectionTypeImpl {

    private PropertySpec callHomeIdPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
    }

    protected String callHomeIdPropertyValue() {
        return (String) this.getProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME.equals(name)) {
            return this.callHomeIdPropertySpec();
        }
        return null;
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME.equals(name);
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
        return true;
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
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }
}
