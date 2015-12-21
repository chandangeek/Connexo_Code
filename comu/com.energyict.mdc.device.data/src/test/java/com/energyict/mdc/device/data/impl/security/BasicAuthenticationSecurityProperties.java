package com.energyict.mdc.device.data.impl.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * that requires username/password (like in basic authentication).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (11:13)
 */
public class BasicAuthenticationSecurityProperties extends CommonBaseDeviceSecurityProperties {

    public enum ActualFields {
        PASSWORD("password"),
        USER_NAME("userName");

        private final String name;

        ActualFields(String name) {
            this.name = name;
        }

        public String javaName() {
            return this.name;
        };

        public String databaseName() {
            return this.javaName().toUpperCase();
        };

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .varChar()
                .map(this.javaName())
                .add();
        }

        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new StringFactory()).
                    name(javaName(), javaName()).
                    finish();
        }

    }

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String userName;
    @Size(max = Table.MAX_STRING_LENGTH)

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        Password password = (Password) propertyValues.getProperty(ActualFields.PASSWORD.javaName());
        if (password != null) {
            this.password = password.getValue();
        }
        this.userName = (String) propertyValues.getProperty(ActualFields.USER_NAME.javaName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            propertySetValues.setProperty(ActualFields.PASSWORD.javaName(), new Password(this.password));
        }
        this.setPropertyIfNotNull(propertySetValues, ActualFields.USER_NAME.javaName(), this.userName);
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