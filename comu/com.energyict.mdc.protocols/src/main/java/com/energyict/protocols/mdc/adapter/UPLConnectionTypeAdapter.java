package com.energyict.protocols.mdc.adapter;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocols.mdc.adapter.cps.ConnectionTypeCustomPropertySetNameDetective;
import com.energyict.protocols.mdc.adapter.cps.UnableToCreateCustomPropertySet;
import com.energyict.protocols.mdc.adapter.cps.UnableToLoadCustomPropertySetClass;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * <p>
 * Adapts a given UPL ConnectionType to the CXO ConnectionType interface
 *
 * @author khe
 * @since 10/01/2017 - 16:33
 */
public class UPLConnectionTypeAdapter implements ConnectionType {

    private static ConnectionTypeCustomPropertySetNameDetective connectionTypeCustomPropertySetNameDetective;

    private final Injector injector;
    private final com.energyict.mdc.io.ConnectionType uplConnectionType;

    public UPLConnectionTypeAdapter(com.energyict.mdc.io.ConnectionType uplConnectionType, Injector injector) {
        this.uplConnectionType = uplConnectionType;
        this.injector = injector;
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        this.ensureConnectionTypeCustomPropertySetNameMappingLoaded();
        return Optional
                .ofNullable(connectionTypeCustomPropertySetNameDetective.customPropertySetClassNameFor(this.uplConnectionType.getClass()))
                .flatMap(this::loadClass)
                .map(this::toCustomPropertySet);
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        //TODO should we have both connect methods? or can I somehow merge them?
        return uplConnectionType.connect();
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        //TODO is this the best way to deal with the given properties?
        com.energyict.mdc.common.TypedProperties typedProperties = com.energyict.mdc.common.TypedProperties.empty();
        properties.stream().forEach(property -> typedProperties.setProperty(property.getName(), property.getValue()));
        copyProperties(typedProperties);

        return this.connect();
    }

    private void ensureConnectionTypeCustomPropertySetNameMappingLoaded() {
        if (connectionTypeCustomPropertySetNameDetective == null) {
            connectionTypeCustomPropertySetNameDetective = new ConnectionTypeCustomPropertySetNameDetective();
        }
    }

    private Optional<Class> loadClass(String className) {
        if (Checks.is(className).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(this.getClass().getClassLoader().loadClass(className));
            } catch (ClassNotFoundException e) {
                throw new UnableToLoadCustomPropertySetClass(e, className, ConnectionTypeCustomPropertySetNameDetective.MAPPING_PROPERTIES_FILE_NAME);
            }
        }
    }

    private CustomPropertySet toCustomPropertySet(Class cpsClass) {
        try {
            return (CustomPropertySet) this.injector.getInstance(cpsClass);
        } catch (ConfigurationException | ProvisionException e) {
            throw new UnableToCreateCustomPropertySet(e, cpsClass, ConnectionTypeCustomPropertySetNameDetective.MAPPING_PROPERTIES_FILE_NAME);
        }
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        uplConnectionType.disconnect(comChannel);
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return uplConnectionType.allowsSimultaneousConnections();
    }

    @Override
    public boolean supportsComWindow() {
        return uplConnectionType.supportsComWindow();
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return uplConnectionType.getSupportedComPortTypes();
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return uplConnectionType.getDirection();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        uplConnectionType.setUPLProperties(properties);
    }

    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return uplConnectionType.getUPLPropertySpecs();
    }

    @Override
    public String getVersion() {
        return uplConnectionType.getVersion();
    }

    @Override
    public void copyProperties(com.energyict.mdc.common.TypedProperties properties) {
        uplConnectionType.setUPLProperties(properties); //TODO catch exceptions? ????

    }
}