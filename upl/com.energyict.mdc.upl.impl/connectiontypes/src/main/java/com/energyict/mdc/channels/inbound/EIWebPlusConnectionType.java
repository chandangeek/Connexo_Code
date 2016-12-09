package com.energyict.mdc.channels.inbound;

import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Specific ConnectionType used for the EIWeb plus Protocol
 * <p>
 * Copyrights EnergyICT
 * Date: 13/12/12
 * Time: 15:46
 */
@XmlRootElement
public class EIWebPlusConnectionType implements ConnectionType {

    private TypedProperties properties = com.energyict.cpo.TypedProperties.empty();

    public static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";

    private PropertySpec ipAddressPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(IP_ADDRESS_PROPERTY_NAME);
    }

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

    protected Object getProperty(String propertyName) {
        return this.getAllProperties().getProperty(propertyName);
    }

    public String ipAddressValue() {
        return (String) this.getProperty(IP_ADDRESS_PROPERTY_NAME);
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return true;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.EXTERNAL_SERVLET);
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        throw new UnsupportedOperationException("Calling connect is not allowed on an EIWebPlusConnectionType");
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(this.ipAddressPropertySpec());
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties = properties;
    }
}