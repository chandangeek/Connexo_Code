package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.cps.ConnectionTypeCustomPropertySetNameDetective;
import com.energyict.mdc.protocol.pluggable.adapters.upl.cps.UnableToLoadCustomPropertySetClass;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.exceptions.ConnectionException;

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
public class UPLConnectionTypeAdapter implements com.energyict.mdc.protocol.api.ConnectionType {

    private static ConnectionTypeCustomPropertySetNameDetective connectionTypeCustomPropertySetNameDetective;

    private final com.energyict.mdc.upl.io.ConnectionType uplConnectionType;
    private final CustomPropertySetInstantiatorService customPropertySetInstantiatorService;

    public UPLConnectionTypeAdapter(com.energyict.mdc.upl.io.ConnectionType uplConnectionType, CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
        this.uplConnectionType = uplConnectionType;
        this.customPropertySetInstantiatorService = customPropertySetInstantiatorService;
    }

    public ConnectionType getUplConnectionType() {
        return uplConnectionType;
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        this.ensureConnectionTypeCustomPropertySetNameMappingLoaded();
        String cpsJavaClassName = connectionTypeCustomPropertySetNameDetective.customPropertySetClassNameFor(this.uplConnectionType.getClass());

        if (Checks.is(cpsJavaClassName).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(customPropertySetInstantiatorService.createCustomPropertySet(cpsJavaClassName));
            } catch (ClassNotFoundException e) {
                throw new UnableToLoadCustomPropertySetClass(e, cpsJavaClassName, ConnectionTypeCustomPropertySetNameDetective.MAPPING_PROPERTIES_FILE_NAME);
            }
        }
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
        com.energyict.mdc.common.TypedProperties typedProperties = com.energyict.mdc.common.TypedProperties.empty();
        properties.stream().forEach(property -> typedProperties.setProperty(property.getName(), property.getValue()));
        copyProperties(typedProperties);

        return this.connect();
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
    public ConnectionType.ConnectionTypeDirection getDirection() {
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
        try {
            uplConnectionType.setUPLProperties(properties);
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }
    }
}