package com.energyict.mdc.channels.inbound;

import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.TypedProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Specific ConnectionType used for the EIWeb plus Protocol.
 * <p>
 * Copyrights EnergyICT
 * Date: 13/12/12
 * Time: 15:46
 */
@XmlRootElement
public class EIWebPlusConnectionType implements ConnectionType {

    private TypedProperties properties = TypedProperties.empty();

    public static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";

    private final PropertySpecService propertySpecService;

    public EIWebPlusConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec ipAddressPropertySpec() {
        return this.propertySpecService
                    .stringSpec()
                    .named(IP_ADDRESS_PROPERTY_NAME, PropertyTranslationKeys.EIWEB_PLUS)
                    .describedAs(PropertyTranslationKeys.EIWEB_PLUS_DESCRIPTION)
                    .finish();
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
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.singletonList(this.ipAddressPropertySpec());
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.properties = TypedProperties.copyOf(properties);
    }
}