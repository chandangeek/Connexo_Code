/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for the {@link EIWebConnectionType} and the {@link EIWebPlusConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:43)
 */
public class EIWebConnectionProperties extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ConnectionProvider> {

    public enum Fields {
        CONNECTION_PROVIDER {
            @Override
            public String javaName() {
                return "connectionProvider";
            }

            @Override
            public String propertySpecName() {
                throw new UnsupportedOperationException("ConnectionProvider should not be exposed as a PropertySpec");
            }

            @Override
            public String databaseName() {
                return "CONNECTIONPROVIDER";
            }
        },
        IP_ADDRESS {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.EIWEB_IP_ADDRESS.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "IPADDRESS";
            }
        },
        MAC_ADDRESS {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.EIWEB_MAC_ADDRESS.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "MACADDRESS";
            }
        };

        public String javaName() {
            return this.propertySpecName();
        }

        public abstract String propertySpecName();

        public abstract String databaseName();

    }

    @SuppressWarnings("unused")
    private Reference<ConnectionProvider> connectionProvider = Reference.empty();
    @Size(max = Table.MAX_STRING_LENGTH)
    private String ipAddress;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String macAddress;

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.connectionProvider.set(connectionProvider);
        this.ipAddress = (String) propertyValues.getProperty(Fields.IP_ADDRESS.propertySpecName());
        this.macAddress = (String) propertyValues.getProperty(Fields.MAC_ADDRESS.propertySpecName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        if (!is(this.ipAddress).empty()) {
            propertySetValues.setProperty(Fields.IP_ADDRESS.propertySpecName(), this.ipAddress);
        }
        if (!is(this.macAddress).empty()) {
            propertySetValues.setProperty(Fields.MAC_ADDRESS.propertySpecName(), this.macAddress);
        }
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}