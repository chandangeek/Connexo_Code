package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * that requires username/password (like in basic authentication)
 * and is known to be used by {@link PasswordWithUserIdentificationSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (10:10)
 */
public class BasicAuthenticationSecurityProperties extends CommonBaseDeviceSecurityProperties {

    public enum ActualFields {
        PASSWORD(DeviceSecurityProperty.PASSWORD) {
            @Override
            public String javaName() {
                return "password";
            }

            @Override
            public String databaseName() {
                return "PASSWORD";
            }
        },
        USER_NAME(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER) {
            @Override
            public String javaName() {
                return "userName";
            }

            @Override
            public String databaseName() {
                return "USERNAME";
            }
        };

        private final DeviceSecurityProperty deviceSecurityProperty;

        ActualFields(DeviceSecurityProperty deviceSecurityProperty) {
            this.deviceSecurityProperty = deviceSecurityProperty;
        }

        public abstract String javaName();

        public abstract String databaseName();

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .varChar()
                .map(this.javaName())
                .add();
        }

        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return this.deviceSecurityProperty.getPropertySpec(propertySpecService);
        }

    }

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String userName;
    @Size(max = Table.MAX_STRING_LENGTH)

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        Password password = (Password) propertyValues.getProperty(DeviceSecurityProperty.PASSWORD.javaName());
        if (password != null) {
            this.password = password.getValue();
        }
        this.userName = (String) propertyValues.getProperty(SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.toString());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            propertySetValues.setProperty(DeviceSecurityProperty.PASSWORD.javaName(), new Password(this.password));
        }
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.toString(), this.userName);
    }

    private void setPropertyIfNotNull(CustomPropertySetValues propertySetValues, String propertyName, Object propertyValue) {
        if (propertyValue != null) {
            propertySetValues.setProperty(propertyName, propertyValue);
        }
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}