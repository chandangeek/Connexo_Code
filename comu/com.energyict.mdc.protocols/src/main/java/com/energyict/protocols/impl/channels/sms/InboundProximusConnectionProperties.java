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
 * for the {@link InboundProximusSmsConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (17:26)
 */
public class InboundProximusConnectionProperties implements PersistentDomainExtension<ConnectionType> {

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
        CALL_HOME_ID {
            @Override
            public String javaName() {
                return "callHomeId";
            }

            @Override
            public String databaseName() {
                return "CALLHOMEID";
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
    private String callHomeId;

    @Override
    public void copyFrom(ConnectionType connectionType, CustomPropertySetValues propertyValues) {
        this.connectionType.set(connectionType);
        this.phoneNumber = (String) propertyValues.getProperty(DeviceProtocolProperty.PHONE_NUMBER.javaFieldName());
        this.callHomeId = (String) propertyValues.getProperty(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(DeviceProtocolProperty.PHONE_NUMBER.javaFieldName(), this.phoneNumber);
        propertySetValues.setProperty(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName(), this.callHomeId);
    }

}