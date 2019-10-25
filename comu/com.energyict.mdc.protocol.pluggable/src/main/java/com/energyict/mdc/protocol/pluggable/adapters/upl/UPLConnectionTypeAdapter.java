package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.common.protocol.ConnectionProperty;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.cps.ConnectionTypeCustomPropertySetNameDetective;
import com.energyict.mdc.protocol.pluggable.adapters.upl.cps.UnableToLoadCustomPropertySetClass;
import com.energyict.mdc.protocols.tasks.AbstractConnectionTypeImpl;
import com.energyict.mdc.protocols.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.exceptions.ConnectionException;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * <p>
 * Adapts a given UPL ConnectionType to the CXO ConnectionType interface
 *
 * @author khe
 * @since 10/01/2017 - 16:33
 */
public class UPLConnectionTypeAdapter implements com.energyict.mdc.common.protocol.ConnectionType {

    private static ConnectionTypeCustomPropertySetNameDetective connectionTypeCustomPropertySetNameDetective;

    private com.energyict.mdc.upl.io.ConnectionType uplConnectionType;
    private CustomPropertySetInstantiatorService customPropertySetInstantiatorService;

    private Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> customPropertySet;

    public UPLConnectionTypeAdapter() {
    }

    public UPLConnectionTypeAdapter(com.energyict.mdc.upl.io.ConnectionType uplConnectionType, CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
        this.uplConnectionType = uplConnectionType;
        this.customPropertySetInstantiatorService = customPropertySetInstantiatorService;
    }

    @XmlElements({
            @XmlElement(type = OutboundTcpIpConnectionType.class),
            @XmlElement(type = OutboundUdpConnectionType.class),
            @XmlElement(type = RxTxOpticalConnectionType.class),
            @XmlElement(type = SioOpticalConnectionType.class),
    })
    public ConnectionType getUplConnectionType() {
        return uplConnectionType;
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        if (customPropertySet == null && customPropertySetInstantiatorService != null) {
            this.ensureConnectionTypeCustomPropertySetNameMappingLoaded();
            String cpsJavaClassName = connectionTypeCustomPropertySetNameDetective.customPropertySetClassNameFor(this.uplConnectionType.getClass());

            if (Checks.is(cpsJavaClassName).emptyOrOnlyWhiteSpace()) {
                return Optional.empty();
            } else {
                try {
                    customPropertySet = Optional.of(customPropertySetInstantiatorService.createCustomPropertySet(cpsJavaClassName));
                } catch (ClassNotFoundException e) {
                    throw new UnableToLoadCustomPropertySetClass(e, cpsJavaClassName, ConnectionTypeCustomPropertySetNameDetective.MAPPING_PROPERTIES_FILE_NAME);
                }
            }
        }
        return customPropertySet;
    }

    private void ensureConnectionTypeCustomPropertySetNameMappingLoaded() {
        if (connectionTypeCustomPropertySetNameDetective == null) {
            connectionTypeCustomPropertySetNameDetective = new ConnectionTypeCustomPropertySetNameDetective();
        }
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        return uplConnectionType.connect();
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        com.energyict.mdc.upl.TypedProperties typedProperties = com.energyict.mdc.upl.TypedProperties.empty();
        properties.stream().forEach(property -> typedProperties.setProperty(property.getName(), property.getValue()));
        copyProperties(typedProperties);

        return this.connect();
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        uplConnectionType.disconnect(comChannel);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean allowsSimultaneousConnections() {
        return uplConnectionType.allowsSimultaneousConnections();
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean supportsComWindow() {
        return uplConnectionType.supportsComWindow();
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public Set<ComPortType> getSupportedComPortTypes() {
        return uplConnectionType.getSupportedComPortTypes();
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public ConnectionType.ConnectionTypeDirection getDirection() {
        return uplConnectionType.getDirection();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(properties);
        uplConnectionType.setUPLProperties(adaptedProperties);
    }

    @JsonIgnore
    @XmlTransient
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return uplConnectionType.getUPLPropertySpecs();
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public String getVersion() {
        return uplConnectionType.getVersion();
    }

    @Override
    public void copyProperties(com.energyict.mdc.upl.TypedProperties properties) {
        try {
            TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(properties);
            uplConnectionType.setUPLProperties(adaptedProperties);
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UPLConnectionTypeAdapter) {
            return uplConnectionType.equals(((UPLConnectionTypeAdapter) obj).uplConnectionType);
        } else {
            return uplConnectionType.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return uplConnectionType != null ? uplConnectionType.hashCode() : 0;
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
}