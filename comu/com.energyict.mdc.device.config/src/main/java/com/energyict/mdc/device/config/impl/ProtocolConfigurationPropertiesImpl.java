package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ProtocolConfigurationProperties;
import com.energyict.mdc.device.config.exceptions.NoSuchPropertyException;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;

import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ProtocolConfigurationProperties} interface.
 * Is in fact a wrapper around the List of {@link ProtocolConfigurationProperty}
 * that are actually owned by {@link DeviceConfigurationImpl} from an ORM point of view.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (09:45)
 */
public class ProtocolConfigurationPropertiesImpl implements ProtocolConfigurationProperties {

    private final DeviceConfigurationImpl deviceConfiguration;
    private List<PropertySpec> propertySpecs;
    private TypedProperties properties;

    public ProtocolConfigurationPropertiesImpl(DeviceConfigurationImpl deviceConfiguration) {
        super();
        this.deviceConfiguration = deviceConfiguration;
    }

    @Override
    public DeviceConfigurationImpl getDeviceConfiguration() {
        return this.deviceConfiguration;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        if (this.propertySpecs == null) {
            this.propertySpecs = this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs();
        }
        return this.propertySpecs;
    }

    private DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        return this.deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass();
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return this.findPropertySpec(name).orElse(null);
    }

    private Optional<PropertySpec> findPropertySpec(String name) {
        return this.getPropertySpecs()
                .stream()
                .filter(s -> s.getName().equals(name))
                .findFirst();
    }

    @Override
    public TypedProperties getTypedProperties() {
        this.ensurePropertiesInitialized();
        return this.properties.getUnmodifiableView();
    }

    /**
     * Ensures that the {@link TypedProperties} are
     * initialized from the List of {@link ProtocolConfigurationProperty}
     * managed by the owning DeviceConfigurationImpl.
     */
    private void ensurePropertiesInitialized() {
        if (this.properties == null) {
            this.properties = this.initializeProperties();
        }
    }

    private TypedProperties initializeProperties() {
        TypedProperties properties = TypedProperties.empty();
        for (ProtocolConfigurationProperty property : this.deviceConfiguration.getProtocolPropertyList()) {
            ValueFactory<?> valueFactory = this.getPropertySpec(property.getName()).getValueFactory();
            properties.setProperty(property.getName(), valueFactory.fromStringValue(property.getValue()));
        }
        return properties;
    }

    @Override
    public Object getProperty(String name) {
        return this.getTypedProperties().getProperty(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        this.ensurePropertiesInitialized();
        if (value != null) {
            if (this.properties.hasValueFor(name)) {
                this.doRemoveProperty(name);
            }
            Optional<PropertySpec> propertySpec = this.findPropertySpec(name);
            if (propertySpec.isPresent()) {
                this.addProperty(name, value, propertySpec.get());
            }
            else {
                throw new NoSuchPropertyException(this.deviceConfiguration.getThesaurus(), this.getDeviceProtocolPluggableClass(), name);
            }
        }
        else {
            this.doRemoveProperty(name);
        }
    }

    @SuppressWarnings("unchecked")
    private void addProperty(String name, Object value, PropertySpec propertySpec) {
        String stringValue = propertySpec.getValueFactory().toStringValue(value);
        ProtocolConfigurationProperty property = ProtocolConfigurationProperty.forNameAndValue(name, stringValue, this.deviceConfiguration);
        this.deviceConfiguration.addProtocolProperty(property);
    }

    @Override
    public void removeProperty(String name) {
        this.ensurePropertiesInitialized();
        this.doRemoveProperty(name);
    }

    private void doRemoveProperty(String name) {
        this.findPropertySpec(name).orElseThrow(() -> new NoSuchPropertyException(this.deviceConfiguration.getThesaurus(), this.getDeviceProtocolPluggableClass(), name));
        if (this.deviceConfiguration.removeProtocolProperty(name)) {
            this.properties.removeProperty(name);
        }
    }

    private Optional<ProtocolConfigurationProperty> findProperty(String name) {
        return this.deviceConfiguration
                .getProtocolPropertyList()
                .stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
    }

}