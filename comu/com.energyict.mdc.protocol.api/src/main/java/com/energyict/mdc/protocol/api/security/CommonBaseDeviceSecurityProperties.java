package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import javax.validation.constraints.NotNull;

/**
 * Defines the minimal behavior for any component that will
 * be providing security properties as a {@link PersistentDomainExtension}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (11:59)
 */
public abstract class CommonBaseDeviceSecurityProperties implements PersistentDomainExtension<BaseDevice> {

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
        };

        public abstract String javaName();

        public abstract String databaseName();

    }
    @NotNull
    private Reference<BaseDevice> device = Reference.empty();
    @NotNull
    private Reference<SecurityPropertySpecProvider> propertySpecProvider = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;

    @Override
    public void copyFrom(BaseDevice device, CustomPropertySetValues propertyValues) {
        this.device.set(device);
        SecurityPropertySpecProvider propertySpecProvider = (SecurityPropertySpecProvider) propertyValues.getProperty(Fields.PROPERTY_SPEC_PROVIDER.javaName());
        this.propertySpecProvider.set(propertySpecProvider);
        this.copyActualPropertiesFrom(propertyValues);
    }

    protected abstract void copyActualPropertiesFrom(CustomPropertySetValues propertyValues);

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(Fields.PROPERTY_SPEC_PROVIDER.javaName(), this.propertySpecProvider.get());
        this.copyActualPropertiesTo(propertySetValues);
    }

    protected abstract void copyActualPropertiesTo(CustomPropertySetValues propertySetValues);

}