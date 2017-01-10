package com.energyict.mdc.channels.inbound;

import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Specific ConnectionType used for the EIWeb Protocol
 * <p>
 * Copyrights EnergyICT
 * Date: 13/12/12
 * Time: 15:46
 */
@XmlRootElement
public class EIWebConnectionType implements ConnectionType {

    private TypedProperties properties = com.energyict.protocolimpl.properties.TypedProperties.empty();

    public static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";
    public static final String MAC_ADDRESS_PROPERTY_NAME = "macAddress";

    private final PropertySpecService propertySpecService;

    public EIWebConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec ipAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(IP_ADDRESS_PROPERTY_NAME, false, this.propertySpecService::stringSpec).finish();
    }

    private PropertySpec macAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(MAC_ADDRESS_PROPERTY_NAME, false, this.propertySpecService::stringSpec).finish();
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

    public String macAddressValue() {
        return (String) this.getProperty(MAC_ADDRESS_PROPERTY_NAME);
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
        return EnumSet.of(ComPortType.SERVLET);
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        throw new UnsupportedOperationException("Calling connect is not allowed on an EIWebConnectionType");
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
        return ConnectionTypeDirection.INBOUND;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(this.ipAddressPropertySpec(), this.macAddressPropertySpec());
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties = properties;

    }
}