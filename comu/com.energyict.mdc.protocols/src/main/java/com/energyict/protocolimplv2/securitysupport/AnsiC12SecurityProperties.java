package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import javax.validation.constraints.Size;
import java.math.BigDecimal;

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
                return "USER_NME";
            }
        },
        USER_ID_LEGACY {
            @Override
            public String javaName() {
                return "userIdLegacy";
            }

            @Override
            public String databaseName() {
                return "USERID";
            }

            @Override
            public void addTo(Table table) {
                table
                        .column(this.databaseName())
                        .varChar()                  //Old column type (up to 10.3) for this property is varchar
                        .map(this.javaName())
                        .upTo(Version.version(10, 3))
                        .add();
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

            @Override
            public void addTo(Table table) {
                table
                        .column(this.databaseName())
                        .number()                   //New column type (since 10.3) for this property is number
                        .map(this.javaName())
                        .since(Version.version(10, 3))
                        .add();
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
    private BigDecimal userId;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String userIdLegacy;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String calledApTitle;
    private Boolean binaryPassword;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String encryptionKey;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.password = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.PASSWORD.toString());
        this.user = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.ANSI_C12_USER.toString());
        this.userId = (BigDecimal) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.ANSI_C12_USER_ID.toString());
        this.calledApTitle = ((String) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.ANSI_CALLED_AP_TITLE.toString()));
        this.binaryPassword = ((Integer) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.BINARY_PASSWORD.toString())) != 0;
        this.encryptionKey = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            setTypedPropertyValueTo(propertySetValues, SecurityPropertySpecTranslationKeys.PASSWORD.toString(), this.password);
        }
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.ANSI_C12_USER.toString(), this.user);
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.ANSI_C12_USER_ID.toString(), this.userId);
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.ANSI_CALLED_AP_TITLE.toString(), this.calledApTitle);
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.BINARY_PASSWORD.toString(), this.binaryPassword);
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), this.encryptionKey);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}
