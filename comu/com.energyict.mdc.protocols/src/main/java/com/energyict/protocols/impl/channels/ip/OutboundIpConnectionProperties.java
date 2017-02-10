/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for the {@link OutboundIpConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (12:53)
 */
public class OutboundIpConnectionProperties extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ConnectionProvider>, PersistenceAware {

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
        HOST {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.OUTBOUND_IP_HOST.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "HOST";
            }
        },
        PORT_NUMBER {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.OUTBOUND_IP_PORT_NUMBER.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "PORTNR";
            }
        },
        CONNECTION_TIMEOUT {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.OUTBOUND_IP_CONNECTION_TIMEOUT.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "CONNECTIONTIMEOUT";
            }
        },
        BUFFER_SIZE {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.OUTBOUND_IP_BUFFER_SIZE.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "BUFFERSIZE";
            }
        },
        /**
         * The delay, expressed in milliseconds, to wait (after the connect) before sending the post dial command.
         */
        POST_DIAL_DELAY_MILLIS {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.OUTBOUND_IP_POST_DIAL_DELAY_MILLIS.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "POSTDIALDELAYMILLIS";
            }
        },
        /**
         * The number of times the post dial command must be transmitted.
         * E.g.: when set to 3, the post dial command will be transferred three times in a row.
         * By doing so, we increase probability the receiver has received at least once the post dial command.
         */
        POST_DIAL_COMMAND_ATTEMPTS {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.OUTBOUND_IP_POST_DIAL_COMMAND_ATTEMPTS.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "NBRPOSTDIALATTEMPTS";
            }
        },
        /**
         * The post dial command that must to be sent right after the connection has been established.
         */
        POST_DIAL_COMMAND {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.OUTBOUND_IP_POST_DIAL_COMMAND.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "POSTDIALCOMMAND";
            }
        },

        TLS_CLIENT_CERTIFICATE {
            @Override
            public String propertySpecName() {
                return ConnectionTypePropertySpecName.TLS_CLIENT_CERTIFICATE.propertySpecName();
            }

            @Override
            public String databaseName() {
                return "TLS_CL_CERT";
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
    private String host;
    private BigDecimal portNumber;
    private TimeDuration connectionTimeout;
    private BigDecimal bufferSize;
    @DecimalMin(message = IpMessageSeeds.Keys.MUST_BE_POSITIVE, value = "0", inclusive = true, groups = {Save.Create.class, Save.Update.class})
    private BigDecimal postDialDelayMillis;
    @DecimalMin(message = IpMessageSeeds.Keys.MUST_BE_POSITIVE, value = "0", inclusive = true, groups = {Save.Create.class, Save.Update.class})
    private BigDecimal postDialCommandAttempts;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String postDialCommand;
    private Reference<KeyAccessorType> tlsClientCertificate = Reference.empty();

    @Override
    public void postLoad() {
        if (this.connectionTimeout != null && this.connectionTimeout.getTimeUnitCode() == 0) {
            /* ORM layer is configured with access paths to convert two database columns to one field
             * and will have created a TimeDuration to be able to inject the values from both columns
             * into the TimeDuration. If the time unit code is zero then it was actually a null value. */
            this.connectionTimeout = null;
        }
    }

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.connectionProvider.set(connectionProvider);
        this.copyHost(propertyValues);
        this.copyPort(propertyValues);
        this.copyConnectionTimeout(propertyValues);
        this.copyBufferSize(propertyValues);
        this.copyPostDialProperties(propertyValues);
        this.copyTlsClientCertificate(propertyValues);
    }

    protected void copyHost(CustomPropertySetValues propertyValues) {
        this.host = (String) propertyValues.getProperty(Fields.HOST.propertySpecName());
    }

    protected void copyPort(CustomPropertySetValues propertyValues) {
        this.portNumber = (BigDecimal) propertyValues.getProperty(Fields.PORT_NUMBER.propertySpecName());
    }

    protected void copyConnectionTimeout(CustomPropertySetValues propertyValues) {
        this.connectionTimeout = (TimeDuration) propertyValues.getProperty(Fields.CONNECTION_TIMEOUT.propertySpecName());
    }

    protected void copyBufferSize(CustomPropertySetValues propertyValues) {
        this.bufferSize = (BigDecimal) propertyValues.getProperty(Fields.BUFFER_SIZE.propertySpecName());
    }

    protected void copyTlsClientCertificate(CustomPropertySetValues propertyValues) {
        this.tlsClientCertificate.set((KeyAccessorType) propertyValues.getProperty(Fields.TLS_CLIENT_CERTIFICATE.propertySpecName()));
    }

    protected void copyPostDialProperties(CustomPropertySetValues propertyValues) {
        this.postDialDelayMillis = (BigDecimal) propertyValues.getProperty(Fields.POST_DIAL_DELAY_MILLIS.propertySpecName());
        this.postDialCommandAttempts = (BigDecimal) propertyValues.getProperty(Fields.POST_DIAL_COMMAND_ATTEMPTS.propertySpecName());
        this.postDialCommand = (String) propertyValues.getProperty(Fields.POST_DIAL_COMMAND.propertySpecName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        this.copyNullablePropertyTo(propertySetValues, Fields.HOST, this.host);
        this.copyNullablePropertyTo(propertySetValues, Fields.PORT_NUMBER, this.portNumber );
        this.copyNullablePropertyTo(propertySetValues, Fields.CONNECTION_TIMEOUT, this.connectionTimeout);
        this.copyNullablePropertyTo(propertySetValues, Fields.BUFFER_SIZE, this.bufferSize);
        this.copyNullablePropertyTo(propertySetValues, Fields.TLS_CLIENT_CERTIFICATE, this.tlsClientCertificate.get());
        this.copyPostDialPropertiesTo(propertySetValues);
    }

    private void copyNullablePropertyTo(CustomPropertySetValues propertySetValues, Fields field, Object value) {
        if (value != null) {
            propertySetValues.setProperty(field.propertySpecName(), value);
        }
    }

    private void copyPostDialPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.copyNullablePropertyTo(propertySetValues, Fields.POST_DIAL_DELAY_MILLIS, this.postDialDelayMillis);
        this.copyNullablePropertyTo(propertySetValues, Fields.POST_DIAL_COMMAND_ATTEMPTS, this.postDialCommandAttempts);
        this.copyNullablePropertyTo(propertySetValues, Fields.POST_DIAL_COMMAND, this.postDialCommand);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}