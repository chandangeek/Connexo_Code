package com.energyict.mdc.channels.ip;

import com.energyict.mdc.channels.VoidComChannel;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Inbound TCP connection type created for the CTR protocol base (as used by MTU155 and EK155 DeviceProtocols).<br></br>
 * Conform the CTR spec, this connectionType contains a required property for CallHomeId - as knocking devices are unique identified by their CallHomeID.
 * <p>
 */
@XmlRootElement
public class CTRInboundDialHomeIdConnectionType extends ConnectionTypeImpl {

    private final PropertySpecService propertySpecService;


    public CTRInboundDialHomeIdConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec callHomeIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, true, this.propertySpecService::stringSpec).finish();
    }

    protected String callHomeIdPropertyValue() {
        return (String) this.getProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.singletonList(callHomeIdPropertySpec());
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        return new VoidComChannel();
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
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }
}
