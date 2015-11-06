package com.energyict.protocols.impl.channels.sms;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for the {@link OutboundProximusSmsConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (11:36)
 */
public class OutboundProximusConnectionProperties implements PersistentDomainExtension<ConnectionType> {

    public enum Fields {
        CONNECTION_TYPE {
            @Override
            public String javaName() {
                return "connectionType";
            }

            @Override
            public String databaseName() {
                return "CONNECTIONTYPE";
            }

            @Override
            public void addTo(Table table) {
                // Connection type is the domain extension and that is added by the CustomPropertySetService
            }
        },
        PHONE_NUMBER {
            @Override
            public String javaName() {
                return "phoneNumber";
            }

            @Override
            public String databaseName() {
                return "PHONENUMBER";
            }
        },
        SOURCE {
            @Override
            public String javaName() {
                return "source";
            }

            @Override
            public String databaseName() {
                return "SOURCE";
            }
        },
        AUTHENTICATION {
            @Override
            public String javaName() {
                return "authentication";
            }

            @Override
            public String databaseName() {
                return "AUTHENTICATION";
            }
        },
        SERVICE_CODE {
            @Override
            public String javaName() {
                return "serviceCode";
            }

            @Override
            public String databaseName() {
                return "SERVICECODE";
            }
        },
        CONNECTION_URL {
            @Override
            public String javaName() {
                return "connectionUrl";
            }

            @Override
            public String databaseName() {
                return "CONNECTIONURL";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .varChar()
                .notNull()
                .map(this.javaName())
                .add();
        }

        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.basicPropertySpec(this.javaName(), true, new StringFactory());
        }

    }

    @SuppressWarnings("unused")
    private Reference<ConnectionType> connectionType = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String phoneNumber;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String connectionUrl;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String source;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String authentication;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String serviceCode;

    @Override
    public void copyFrom(ConnectionType connectionType, CustomPropertySetValues propertyValues) {
        this.connectionType.set(connectionType);
        this.phoneNumber = (String) propertyValues.getProperty(DeviceProtocolProperty.PHONE_NUMBER.javaFieldName());
        this.connectionUrl = (String) propertyValues.getProperty(Fields.CONNECTION_URL.javaName());
        this.source = (String) propertyValues.getProperty(Fields.SOURCE.javaName());
        this.authentication = (String) propertyValues.getProperty(Fields.AUTHENTICATION.javaName());
        this.serviceCode = (String) propertyValues.getProperty(Fields.SERVICE_CODE.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(DeviceProtocolProperty.PHONE_NUMBER.javaFieldName(), this.phoneNumber);
        propertySetValues.setProperty(Fields.CONNECTION_URL.javaName(), this.connectionUrl);
        propertySetValues.setProperty(Fields.SOURCE.javaName(), this.source);
        propertySetValues.setProperty(Fields.AUTHENTICATION.javaName(), this.authentication);
        propertySetValues.setProperty(Fields.SERVICE_CODE.javaName(), this.serviceCode);
    }

}