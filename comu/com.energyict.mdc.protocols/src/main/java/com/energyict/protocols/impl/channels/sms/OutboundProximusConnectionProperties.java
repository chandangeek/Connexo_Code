/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.sms;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.channels.sms.OutboundProximusSmsConnectionType;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for the {@link OutboundProximusSmsConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (11:36)
 */
public class OutboundProximusConnectionProperties extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ConnectionProvider> {

    @SuppressWarnings("unused")
    private Reference<ConnectionProvider> connectionProvider = Reference.empty();
    @Size(max = Table.MAX_STRING_LENGTH)
    private String SMS_phoneNumber;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String API_connectionURL;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String API_source;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String API_authentication;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String API_serviceCode;

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.connectionProvider.set(connectionProvider);
        this.SMS_phoneNumber = (String) propertyValues.getProperty(Fields.PHONE_NUMBER.propertySpecName());
        this.API_connectionURL = (String) propertyValues.getProperty(Fields.CONNECTION_URL.propertySpecName());
        this.API_source = (String) propertyValues.getProperty(Fields.SOURCE.propertySpecName());
        this.API_authentication = (String) propertyValues.getProperty(Fields.AUTHENTICATION.propertySpecName());
        this.API_serviceCode = (String) propertyValues.getProperty(Fields.SERVICE_CODE.propertySpecName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.PHONE_NUMBER.propertySpecName(), this.SMS_phoneNumber);
        propertySetValues.setProperty(Fields.CONNECTION_URL.propertySpecName(), this.API_connectionURL);
        propertySetValues.setProperty(Fields.SOURCE.propertySpecName(), this.API_source);
        propertySetValues.setProperty(Fields.AUTHENTICATION.propertySpecName(), this.API_authentication);
        propertySetValues.setProperty(Fields.SERVICE_CODE.propertySpecName(), this.API_serviceCode);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

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

            @Override
            public void addTo(Table table) {
                // Connection type is the domain extension and that is added by the CustomPropertySetService
            }
        },
        PHONE_NUMBER {
            @Override
            public String propertySpecName() {
                return OutboundProximusSmsConnectionType.PHONE_NUMBER_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "PHONENUMBER";
            }
        },
        SOURCE {
            @Override
            public String propertySpecName() {
                return OutboundProximusSmsConnectionType.SOURCE_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "SOURCE";
            }
        },
        AUTHENTICATION {
            @Override
            public String propertySpecName() {
                return OutboundProximusSmsConnectionType.AUTHENTICATION_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "AUTHENTICATION";
            }
        },
        SERVICE_CODE {
            @Override
            public String propertySpecName() {
                return OutboundProximusSmsConnectionType.SERVICE_CODE_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "SERVICECODE";
            }
        },
        CONNECTION_URL {
            @Override
            public String propertySpecName() {
                return OutboundProximusSmsConnectionType.CONNECTION_URL_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "CONNECTIONURL";
            }
        };

        public String javaName() {
            return this.propertySpecName();
        }

        public abstract String propertySpecName();

        public abstract String databaseName();

        public void addTo(Table table) {
            table
                    .column(this.databaseName())
                    .varChar()
                    .map(this.javaName())
                    .add();
        }
    }

}