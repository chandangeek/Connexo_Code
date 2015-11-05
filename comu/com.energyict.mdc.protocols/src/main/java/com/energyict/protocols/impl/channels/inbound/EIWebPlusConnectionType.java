package com.energyict.protocols.impl.channels.inbound;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.PropertySpec;

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

    private TypedProperties properties = TypedProperties.empty();

    public static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";
    private PropertySpecService propertySpecService;

    private PropertySpec ipAddressPropertySpec() {
        return propertySpecService.stringPropertySpec(IP_ADDRESS_PROPERTY_NAME, true, "");
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
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
        return EnumSet.of(ComPortType.TCP);
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        throw new UnsupportedOperationException("Calling connect is not allowed on an EIWebPlusConnectionType");
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(ipAddressPropertySpec());
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-07-08 10:38:12 +0200 (Tue, 08 Jul 2014) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }

}