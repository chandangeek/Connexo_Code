package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link AnsiC12SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (13:22)
 */
public class AnsiC12SecurityProperties extends CommonBaseDeviceSecurityProperties {

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
        },
        USER {
            @Override
            public String javaName() {
                return "user";
            }

            @Override
            public String databaseName() {
                return "USER";
            }
        },
        USER_ID {
            @Override
            public String javaName() {
                return "userId";
            }

            @Override
            public String databaseName() {
                return "USERID";
            }
        },
        CALLED_AP_TITLE {
            @Override
            public String javaName() {
                return "calledApTitle";
            }

            @Override
            public String databaseName() {
                return "CALLEDAPTITLE";
            }
        },
        BINARY_PASSWORD {
            @Override
            public String javaName() {
                return "binaryPassword";
            }

            @Override
            public String databaseName() {
                return "BINARYPASSWORD";
            }

            @Override
            public void addTo(Table table) {
                table
                    .column(this.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2BOOLEAN)
                    .map(this.javaName())
                    .add();
            }
        },
        ENCRYPTION_KEY {
            @Override
            public String javaName() {
                return "encryptionKey";
            }

            @Override
            public String databaseName() {
                return "ENCRYPTIONKEY";
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

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String user;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String userId;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String calledApTitle;
    private Boolean binaryPassword;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String encryptionKey;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        Password password = (Password) propertyValues.getProperty(DeviceSecurityProperty.PASSWORD.javaName());
        if (password != null) {
            this.password = password.getValue();
        }
        this.user = (String) propertyValues.getProperty(DeviceSecurityProperty.ANSI_C12_USER.javaName());
        this.userId = (String) propertyValues.getProperty(DeviceSecurityProperty.ANSI_C12_USER_ID.javaName());
        this.calledApTitle = ((String) propertyValues.getProperty(DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.javaName()));
        this.binaryPassword = (Boolean) propertyValues.getProperty(DeviceSecurityProperty.BINARY_PASSWORD.javaName());
        this.encryptionKey = (String) propertyValues.getProperty(DeviceSecurityProperty.ENCRYPTION_KEY.javaName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            propertySetValues.setProperty(DeviceSecurityProperty.PASSWORD.javaName(), new Password(this.password));
        }
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.ANSI_C12_USER.javaName(), this.user);
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.ANSI_C12_USER_ID.javaName(), this.userId);
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.javaName(), this.calledApTitle);
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.BINARY_PASSWORD.javaName(), this.binaryPassword);
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.ENCRYPTION_KEY.javaName(), this.encryptionKey);
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