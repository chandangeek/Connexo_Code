/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.sms;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for the {@link InboundProximusSmsConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (17:26)
 */
public class InboundProximusConnectionProperties extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ConnectionProvider> {

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
            public TranslationKey nameTranslationKey() {
                throw new UnsupportedOperationException("ConnectionProvider should not be exposed as a PropertySpec");
            }

            @Override
            public String databaseName() {
                return "CONNECTIONPROVIDER";
            }

            @Override
            public void addTo(Table table) {
                // Connection type is the domain extension and that is added by the CustomPropertySetService
            }
        },
        PHONE_NUMBER {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.INBOUND_PROXIMUS_PHONE_NUMBER.propertySpecName();
            }

            @Override
            public TranslationKey nameTranslationKey() {
                return ConnectionTypePropertySpecName.INBOUND_PROXIMUS_PHONE_NUMBER;
            }

            @Override
            public String databaseName() {
                return "PHONENUMBER";
            }
        },
        CALL_HOME_ID {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.INBOUND_PROXIMUS_CALL_HOME_ID.propertySpecName();
            }

            @Override
            public TranslationKey nameTranslationKey() {
                return ConnectionTypePropertySpecName.INBOUND_PROXIMUS_CALL_HOME_ID;
            }

            @Override
            public String databaseName() {
                return "CALLHOMEID";
            }
        };

        public String javaName() {
            return this.propertySpecName();
        }

        public abstract String propertySpecName();

        public abstract TranslationKey nameTranslationKey();

        public abstract String databaseName();

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .varChar()
                .map(this.javaName())
                .add();
        }

        public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .stringSpec()
                    .named(this.propertySpecName(), this.nameTranslationKey())
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish();
        }

    }

    @SuppressWarnings("unused")
    private Reference<ConnectionProvider> connectionProvider = Reference.empty();
    @Size(max = Table.MAX_STRING_LENGTH)
    private String phoneNumber;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String callHomeId;

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.connectionProvider.set(connectionProvider);
        this.phoneNumber = (String) propertyValues.getProperty(DeviceProtocolProperty.PHONE_NUMBER.javaFieldName());
        this.callHomeId = (String) propertyValues.getProperty(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(DeviceProtocolProperty.PHONE_NUMBER.javaFieldName(), this.phoneNumber);
        propertySetValues.setProperty(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName(), this.callHomeId);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}