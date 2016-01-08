package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link IEC1107SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (16:47)
 */
public class IEC1107SecurityProperties extends CommonBaseDeviceSecurityProperties {

    public enum ActualFields {
        PASSWORD {
            @Override
            public String javaName() {
                return "password";
            }

            @Override
            public String databaseName() {
                return "PASSWORD";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

    }

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        Password password = (Password) propertyValues.getProperty(DeviceSecurityProperty.PASSWORD.javaName());
        if (password != null) {
            this.password = password.getValue();
        }
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            propertySetValues.setProperty(DeviceSecurityProperty.PASSWORD.javaName(), new Password(this.password));
        }
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}