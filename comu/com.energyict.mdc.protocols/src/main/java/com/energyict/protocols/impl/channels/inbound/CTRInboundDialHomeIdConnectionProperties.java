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

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for the {@link CTRInboundDialHomeIdConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:43)
 */
public class CTRInboundDialHomeIdConnectionProperties extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ConnectionProvider> {

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
        DIAL_HOME_ID {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.CTR_INBOUND_DIAL_HOME_ID.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "DIALHOMEID";
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
    private String dialHomeId;

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.connectionProvider.set(connectionProvider);
        this.copyDialHomeId(propertyValues);
    }

    private void copyDialHomeId(CustomPropertySetValues propertyValues) {
        this.dialHomeId = (String) propertyValues.getProperty(Fields.DIAL_HOME_ID.propertySpecName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.DIAL_HOME_ID.propertySpecName(), this.dialHomeId);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}