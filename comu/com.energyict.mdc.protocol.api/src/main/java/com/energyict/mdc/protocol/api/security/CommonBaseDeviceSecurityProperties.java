package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.upl.meterdata.Device;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Defines the minimal behavior for any component that will
 * be providing security properties as a {@link PersistentDomainExtension}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (11:59)
 */
public abstract class CommonBaseDeviceSecurityProperties extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum Fields {
        DEVICE {
            @Override
            public String javaName() {
                return "device";
            }

            @Override
            public String databaseName() {
                return "DEVICE";
            }
        },
        PROPERTY_SPEC_PROVIDER {
            @Override
            public String javaName() {
                return "propertySpecProvider";
            }

            @Override
            public String databaseName() {
                return "PROPERTYSPECPROVIDER";
            }
        },
        COMPLETE {
            @Override
            public String javaName() {
                return "complete";
            }

            @Override
            public String databaseName() {
                return "COMPLETE";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

    }

    @NotNull
    private Reference<Device> device = Reference.empty();
    @NotNull
    private Reference<SecurityPropertySpecProvider> propertySpecProvider = Reference.empty();
    private boolean complete = false;

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        SecurityPropertySpecProvider propertySpecProvider = (SecurityPropertySpecProvider) propertyValues.getProperty(Fields.PROPERTY_SPEC_PROVIDER
                .javaName());
        this.propertySpecProvider.set(propertySpecProvider);
        Boolean complete = (Boolean) propertyValues.getProperty(Fields.COMPLETE.javaName());
        if (complete != null) {
            this.complete = complete;
        } else {
            this.complete = false;
        }
        this.copyActualPropertiesFrom(propertyValues);
    }

    protected abstract void copyActualPropertiesFrom(CustomPropertySetValues propertyValues);

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.PROPERTY_SPEC_PROVIDER.javaName(), this.propertySpecProvider.get());
        propertySetValues.setProperty(Fields.COMPLETE.javaName(), this.complete);
        this.copyActualPropertiesTo(propertySetValues);
    }

    protected abstract void copyActualPropertiesTo(CustomPropertySetValues propertySetValues);

    protected void setTypedPropertyValueTo(CustomPropertySetValues propertySetValues, String propertyName, Object propertyValue) {
        propertySpecProvider.get()
                .getPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getName().equals(propertyName))
                .findAny()
                .ifPresent(propertySpec -> propertySetValues.setProperty(propertySpec.getName(),
                        propertySpec.getValueFactory().valueFromDatabase(propertyValue)));
    }

    @SuppressWarnings("unchecked")
    protected Object getTypedPropertyValue(CustomPropertySetValues propertySetValues, String propertyName) {
        Optional<PropertySpec> propertySpec = propertySpecProvider.get()
                .getPropertySpecs()
                .stream()
                .filter(ps -> ps.getName().equals(propertyName))
                .findAny();
        if (propertySpec.isPresent()) {
            return propertySpec.get().getValueFactory().valueToDatabase(propertySetValues.getProperty(propertyName));
        } else {
            return null;
        }
    }

    protected void setPropertyIfNotNull(CustomPropertySetValues propertySetValues, String propertyName, Object propertyValue) {
        if (propertyValue != null) {
            setTypedPropertyValueTo(propertySetValues, propertyName, propertyValue);
        }
    }
}