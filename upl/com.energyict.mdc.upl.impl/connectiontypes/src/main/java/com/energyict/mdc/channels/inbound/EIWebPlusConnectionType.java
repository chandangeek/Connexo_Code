package com.energyict.mdc.channels.inbound;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Specific ConnectionType used for the EIWeb plus Protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/12/12
 * Time: 15:46
 */
@XmlRootElement
public class EIWebPlusConnectionType implements ConnectionType {

    private TypedProperties properties = TypedProperties.empty();

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

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
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
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        throw new UnsupportedOperationException("Calling connect is not allowed on an EIWebPlusConnectionType");
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case IP_ADDRESS_PROPERTY_NAME:
                return this.ipAddressPropertySpec();
            default:
                return null;
        }
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return false;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        this.properties = TypedProperties.copyOf(properties);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(this.ipAddressPropertySpec());
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

    @Override
    public void injectConnectionTaskId(int connectionTaskId) {
        // Not interested in this
    }
}