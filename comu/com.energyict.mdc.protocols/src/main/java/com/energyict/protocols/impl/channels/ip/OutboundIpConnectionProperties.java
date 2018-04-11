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
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.channels.ip.OutboundIpConnectionType;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.channels.ip.socket.TLSConnectionType;
import com.energyict.mdc.channels.ip.socket.TcpIpPostDialConnectionType;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Objects;

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
                return OutboundIpConnectionType.HOST_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "HOST";
            }
        },
        PORT_NUMBER {
            @Override
            public String propertySpecName() {
                return OutboundIpConnectionType.PORT_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "PORTNR";
            }
        },
        CONNECTION_TIMEOUT {
            @Override
            public String propertySpecName() {
                return OutboundIpConnectionType.CONNECTION_TIMEOUT_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "CONNECTIONTIMEOUT";
            }
        },
        BUFFER_SIZE {
            @Override
            public String propertySpecName() {
                return OutboundUdpConnectionType.BUFFER_SIZE_NAME;
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
                return TcpIpPostDialConnectionType.POST_DIAL_DELAY;
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
                return TcpIpPostDialConnectionType.POST_DIAL_TRIES;
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
                return TcpIpPostDialConnectionType.POST_DIAL_COMMAND;
            }

            @Override
            public String databaseName() {
                return "POSTDIALCOMMAND";
            }
        },

        TLS_CLIENT_CERTIFICATE {
            @Override
            public String javaName() {
                return "tlsClientCertificate";
            }

            @Override
            public String propertySpecName() {
                return TLSConnectionType.CLIENT_TLS_PRIVATE_KEY;
            }

            @Override
            public String databaseName() {
                return "TLS_CLI_CERT";
            }
        },

        TLS_SERVER_CERTIFICATE {
            @Override
            public String javaName() {
                return "tlsServerCertificate";
            }

            @Override
            public String propertySpecName() {
                return TLSConnectionType.SERVER_TLS_CERTIFICATE;
            }

            @Override
            public String databaseName() {
                return "TLS_SERV_CERT";
            }
        },

        TLS_PREFERRED_CIPHER_SUITES {
            @Override
            public String javaName() {
                return "preferredCipherSuites";
            }

            @Override
            public String propertySpecName() {
                return TLSConnectionType.PREFERRED_CIPHER_SUITES_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "TLS_CIPHER_SUITES";
            }
        },

        TLS_VERSION {
            @Override
            public String javaName() {
                return "tlsVersion";
            }

            @Override
            public String propertySpecName() {
                return TLSConnectionType.TLS_VERSION_PROPERTY_NAME;
            }

            @Override
            public String databaseName() {
                return "TLS_VERSION";
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
    private BigDecimal udpdatagrambuffersize;
    @DecimalMin(message = IpMessageSeeds.Keys.MUST_BE_POSITIVE, value = "0", inclusive = true, groups = {Save.Create.class, Save.Update.class})
    private BigDecimal postDialDelay;
    @DecimalMin(message = IpMessageSeeds.Keys.MUST_BE_POSITIVE, value = "0", inclusive = true, groups = {Save.Create.class, Save.Update.class})
    private BigDecimal postDialTries;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String postDialCommand;
    private Reference<SecurityAccessorType> tlsClientCertificate = Reference.empty();
    private Reference<SecurityAccessorType> tlsServerCertificate = Reference.empty();
    @Size(max = Table.MAX_STRING_LENGTH)
    private String preferredCipherSuites;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String tlsVersion;

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
        this.copyPreferredCipherSuites(propertyValues);
        this.copyTlsVersion(propertyValues);
        this.copyPort(propertyValues);
        this.copyConnectionTimeout(propertyValues);
        this.copyBufferSize(propertyValues);
        this.copyPostDialProperties(propertyValues);
        this.copyTlsClientCertificate(propertyValues);
        this.copyTlsServerCertificate(propertyValues);
    }

    protected void copyHost(CustomPropertySetValues propertyValues) {
        this.host = (String) propertyValues.getProperty(Fields.HOST.propertySpecName());
    }

    protected void copyPreferredCipherSuites(CustomPropertySetValues propertyValues) {
        this.preferredCipherSuites = (String) propertyValues.getProperty(Fields.TLS_PREFERRED_CIPHER_SUITES.propertySpecName());
    }

    protected void copyTlsVersion(CustomPropertySetValues propertyValues) {
        this.tlsVersion = (String) propertyValues.getProperty(Fields.TLS_VERSION.propertySpecName());
    }

    protected void copyPort(CustomPropertySetValues propertyValues) {
        Object property = propertyValues.getProperty(Fields.PORT_NUMBER.propertySpecName());
        if (Objects.nonNull(property)) {
            this.portNumber = new BigDecimal(property.toString());
        }
    }

    protected void copyConnectionTimeout(CustomPropertySetValues propertyValues) {
        this.connectionTimeout = (TimeDuration) propertyValues.getProperty(Fields.CONNECTION_TIMEOUT.propertySpecName());
    }

    protected void copyBufferSize(CustomPropertySetValues propertyValues) {
        this.udpdatagrambuffersize = (BigDecimal) propertyValues.getProperty(Fields.BUFFER_SIZE.propertySpecName());
    }

    protected void copyTlsClientCertificate(CustomPropertySetValues propertyValues) {
        this.tlsClientCertificate.set((SecurityAccessorType) propertyValues.getProperty(Fields.TLS_CLIENT_CERTIFICATE.propertySpecName()));
    }

    protected void copyTlsServerCertificate(CustomPropertySetValues propertyValues) {
        this.tlsServerCertificate.set((SecurityAccessorType) propertyValues.getProperty(Fields.TLS_SERVER_CERTIFICATE.propertySpecName()));
    }

    protected void copyPostDialProperties(CustomPropertySetValues propertyValues) {
        this.postDialDelay = (BigDecimal) propertyValues.getProperty(Fields.POST_DIAL_DELAY_MILLIS.propertySpecName());
        this.postDialTries = (BigDecimal) propertyValues.getProperty(Fields.POST_DIAL_COMMAND_ATTEMPTS.propertySpecName());
        this.postDialCommand = (String) propertyValues.getProperty(Fields.POST_DIAL_COMMAND.propertySpecName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        this.copyNullablePropertyTo(propertySetValues, Fields.HOST, this.host);
        this.copyNullablePropertyTo(propertySetValues, Fields.PORT_NUMBER, this.portNumber);
        this.copyNullablePropertyTo(propertySetValues, Fields.CONNECTION_TIMEOUT, this.connectionTimeout);
        this.copyNullablePropertyTo(propertySetValues, Fields.BUFFER_SIZE, this.udpdatagrambuffersize);
        this.copyPostDialPropertiesTo(propertySetValues);
        this.copyTlsPropertiesTo(propertySetValues);
    }

    private void copyTlsPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (this.tlsClientCertificate.isPresent()) {
            copyNullablePropertyTo(propertySetValues, Fields.TLS_CLIENT_CERTIFICATE, this.tlsClientCertificate.get());
        }
        if (this.tlsServerCertificate.isPresent()) {
            copyNullablePropertyTo(propertySetValues, Fields.TLS_SERVER_CERTIFICATE, this.tlsServerCertificate.get());
        }
        this.copyNullablePropertyTo(propertySetValues, Fields.TLS_PREFERRED_CIPHER_SUITES, this.preferredCipherSuites);
        this.copyNullablePropertyTo(propertySetValues, Fields.TLS_VERSION, this.tlsVersion);
    }

    private void copyNullablePropertyTo(CustomPropertySetValues propertySetValues, Fields field, Object value) {
        if (value != null) {
            propertySetValues.setProperty(field.propertySpecName(), value);
        }
    }

    private void copyPostDialPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.copyNullablePropertyTo(propertySetValues, Fields.POST_DIAL_DELAY_MILLIS, this.postDialDelay);
        this.copyNullablePropertyTo(propertySetValues, Fields.POST_DIAL_COMMAND_ATTEMPTS, this.postDialTries);
        this.copyNullablePropertyTo(propertySetValues, Fields.POST_DIAL_COMMAND, this.postDialCommand);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}