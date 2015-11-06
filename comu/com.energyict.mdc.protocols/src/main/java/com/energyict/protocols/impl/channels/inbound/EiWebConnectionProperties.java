package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.protocol.api.ConnectionType;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for the {@link EIWebConnectionType} and the {@link EIWebPlusConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:43)
 */
public class EIWebConnectionProperties implements PersistentDomainExtension<ConnectionType> {

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
        },
        IP_ADDRESS {
            @Override
            public String javaName() {
                return "ipAddress";
            }

            @Override
            public String databaseName() {
                return "IPADDRESS";
            }
        },
        MAC_ADDRESS {
            @Override
            public String javaName() {
                return "macAddress";
            }

            @Override
            public String databaseName() {
                return "MACADDRESS";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

    }

    @SuppressWarnings("unused")
    private Reference<ConnectionType> connectionType = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String ipAddress;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String macAddress;

    @Override
    public void copyFrom(ConnectionType connectionType, CustomPropertySetValues propertyValues) {
        this.connectionType.set(connectionType);
        this.ipAddress = (String) propertyValues.getProperty(Fields.IP_ADDRESS.javaName());
        this.macAddress = (String) propertyValues.getProperty(Fields.MAC_ADDRESS.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(Fields.IP_ADDRESS.javaName(), this.ipAddress);
        propertySetValues.setProperty(Fields.MAC_ADDRESS.javaName(), this.macAddress);
    }

}