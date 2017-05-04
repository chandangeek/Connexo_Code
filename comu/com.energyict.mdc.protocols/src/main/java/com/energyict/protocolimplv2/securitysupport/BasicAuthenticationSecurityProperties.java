package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

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

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String user;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.password = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecName.PASSWORD.toString());
        this.user = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.getKey());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            setTypedPropertyValueTo(propertySetValues, SecurityPropertySpecName.PASSWORD.toString(), this.password);
        }
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.getKey(), this.user);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    public enum ActualFields {
        PASSWORD() {
            @Override
            public String javaName() {
                return "password";
            }

            @Override
            public String databaseName() {
                return "PASSWORD";
            }
        },
        USER_NAME() {
            @Override
            public String javaName() {
                return "user";
            }

            @Override
            public String databaseName() {
                return "USER_NME";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

        public void addTo(Table table) {
            table
                    .column(this.databaseName())
                    .varChar()
                    .map(this.javaName())
                    .add();
        }
    }
}