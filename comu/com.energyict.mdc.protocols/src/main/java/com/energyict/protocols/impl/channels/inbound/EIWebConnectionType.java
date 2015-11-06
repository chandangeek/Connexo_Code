package com.energyict.protocols.impl.channels.inbound;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.protocols.mdc.protocoltasks.ServerConnectionType;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Specific ConnectionType used for the EIWeb Protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/12/12
 * Time: 15:46
 */
public class EIWebConnectionType implements ServerConnectionType {

    public static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";
    public static final String MAC_ADDRESS_PROPERTY_NAME = "macAddress";

    private TypedProperties properties = TypedProperties.empty();
    private final PropertySpecService propertySpecService;

    @Inject
    public EIWebConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec ipAddressPropertySpec() {
        return this.propertySpecService.basicPropertySpec(IP_ADDRESS_PROPERTY_NAME, false, StringFactory.class);
    }

    private PropertySpec macAddressPropertySpec() {
        return this.propertySpecService.basicPropertySpec(MAC_ADDRESS_PROPERTY_NAME, false, StringFactory.class);
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
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        throw new UnsupportedOperationException("Calling connect is not allowed on an EIWebConnectionType");
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-09-10 15:38:19 +0200 (Tue, 10 Sep 2013) $";
    }

    @Override
    public void copyProperties (TypedProperties properties) {
        this.properties = TypedProperties.copyOf(properties);
    }

    @Override
    @Obsolete
    public List<PropertySpec> getPropertySpecs () {
        return Arrays.asList(this.ipAddressPropertySpec(), this.macAddressPropertySpec());
    }

}