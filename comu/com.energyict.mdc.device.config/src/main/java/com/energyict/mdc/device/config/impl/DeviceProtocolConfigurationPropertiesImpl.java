package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.device.config.exceptions.NoSuchPropertyException;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;

import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceProtocolConfigurationProperties} interface.
 * Is in fact a wrapper around the List of {@link DeviceProtocolConfigurationProperty}
 * that are actually owned by {@link DeviceConfigurationImpl} from an ORM point of view.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (09:45)
 */
public class DeviceProtocolConfigurationPropertiesImpl implements DeviceProtocolConfigurationProperties {

    private final DeviceConfigurationImpl deviceConfiguration;
    private List<PropertySpec> propertySpecs;
    private TypedProperties properties;

    public DeviceProtocolConfigurationPropertiesImpl(DeviceConfigurationImpl deviceConfiguration) {
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
     * initialized from the List of {@link DeviceProtocolConfigurationProperty}
     * managed by the owning DeviceConfigurationImpl.
     */
    private void ensurePropertiesInitialized() {
        if (this.properties == null) {
            this.properties = this.initializeProperties();
        }
    }

    private TypedProperties initializeProperties() {
        TypedProperties defaultProperties = this.deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass().getProperties();
        TypedProperties properties = TypedProperties.inheritingFrom(defaultProperties);
        for (DeviceProtocolConfigurationProperty property : this.deviceConfiguration.getProtocolPropertyList()) {
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
                throw new NoSuchPropertyException(this.getDeviceProtocolPluggableClass(), name, this.deviceConfiguration.getThesaurus(), MessageSeeds.PROTOCOL_HAS_NO_SUCH_PROPERTY);
            }
        }
        else {
            this.doRemoveProperty(name);
        }
    }

    @SuppressWarnings("unchecked")
    private void addProperty(String name, Object value, PropertySpec propertySpec) {
        String stringValue = propertySpec.getValueFactory().toStringValue(value);
        DeviceProtocolConfigurationProperty property = DeviceProtocolConfigurationProperty.forNameAndValue(name, stringValue, this.deviceConfiguration);
        this.deviceConfiguration.addProtocolProperty(property);
        this.properties.setProperty(name, value);
    }

    @Override
    public void removeProperty(String name) {
        this.ensurePropertiesInitialized();
        this.doRemoveProperty(name);
    }

    private void doRemoveProperty(String name) {
        this.findPropertySpec(name).orElseThrow(() -> new NoSuchPropertyException(this.getDeviceProtocolPluggableClass(), name, this.deviceConfiguration.getThesaurus(), MessageSeeds.PROTOCOL_HAS_NO_SUCH_PROPERTY));
        if (this.deviceConfiguration.removeProtocolProperty(name)) {
            this.properties.removeProperty(name);
        }
    }

}