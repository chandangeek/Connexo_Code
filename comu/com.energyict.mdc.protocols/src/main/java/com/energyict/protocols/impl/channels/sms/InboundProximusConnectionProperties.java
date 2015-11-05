package com.energyict.protocols.impl.channels.sms;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for the {@link InboundProximusSmsConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (17:26)
 */
public class InboundProximusConnectionProperties implements PersistentDomainExtension<ConnectionType> {

    public enum FieldNames {
        CONNECTION_TYPE {
            @Override
            public String javaName() {
                return "connectionType";
            }

            @Override
            public String databaseName() {
                return "CONNECTIONTYPE";
            }
        },
        PHONE_NUMBER {
            @Override
            public String javaName() {
                return DeviceProtocolProperty.PHONE_NUMBER.javaFieldName();
            }

            @Override
            public String databaseName() {
                return DeviceProtocolProperty.PHONE_NUMBER.databaseColumnName();
            }
        },
        CALL_HOME_ID {
            @Override
            public String javaName() {
                return DeviceProtocolProperty.CALL_HOME_ID.javaFieldName();
            }

            @Override
            public String databaseName() {
                return DeviceProtocolProperty.CALL_HOME_ID.databaseColumnName();
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

    }

    @SuppressWarnings("unused")
    private Reference<ConnectionType> connectionType = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;
    private String phoneNumber;
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