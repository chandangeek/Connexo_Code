package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.protocol.api.ConnectionProvider;

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
public class OutboundIpConnectionProperties implements PersistentDomainExtension<ConnectionProvider>, PersistenceAware {

    public enum Fields {
        CONNECTION_PROVIDER {
            @Override
            public String javaName() {
                return "connectionProvider";
            }

            @Override
            public String databaseName() {
                return "CONNECTIONPROVIDER";
            }
        },
        HOST {
            @Override
            public String javaName() {
                return "host";
            }

            @Override
            public String databaseName() {
                return "HOST";
            }
        },
        PORT {
            @Override
            public String javaName() {
                return "port";
            }

            @Override
            public String databaseName() {
                return "PORT";
            }
        },
        CONNECTION_TIMEOUT {
            @Override
            public String javaName() {
                return "connectionTimeout";
            }

            @Override
            public String databaseName() {
                return "CONNECTIONTIMEOUT";
            }
        },
        BUFFER_SIZE {
            @Override
            public String javaName() {
                return "bufferSize";
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
            public String javaName() {
                return "postDialDelayMillis";
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
            public String javaName() {
                return "postDialCommandAttempts";
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
            public String javaName() {
                return "postDialCommand";
            }

            @Override
            public String databaseName() {
                return "POSTDIALCOMMAND";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

    }

    @SuppressWarnings("unused")
    private Reference<ConnectionProvider> connectionProvider = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String host;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String port;
    private TimeDuration connectionTimeout;
    private int connectionTimeoutValue;
    private int connectionTimeoutUnit;
    private BigDecimal bufferSize;
    @DecimalMin(message = IpMessageSeeds.Keys.MUST_BE_POSITIVE, value = "0", inclusive = true, groups = {Save.Create.class, Save.Update.class})
    private BigDecimal postDialDelayMillis;
    @DecimalMin(message = IpMessageSeeds.Keys.MUST_BE_POSITIVE, value = "0", inclusive = true, groups = {Save.Create.class, Save.Update.class})
    private BigDecimal postDialCommandAttempts;
    private String postDialCommand;

    @Override
    public void postLoad() {
        this.connectionTimeout = new TimeDuration(this.connectionTimeoutValue, this.connectionTimeoutUnit);
    }

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues) {
        this.connectionProvider.set(connectionProvider);
        this.copyHost(propertyValues);
        this.copyPort(propertyValues);
        this.copyConnectionTimeout(propertyValues);
        this.copyBufferSize(propertyValues);
        this.copyPostDialProperties(propertyValues);
    }

    protected void copyHost(CustomPropertySetValues propertyValues) {
        this.host = (String) propertyValues.getProperty(Fields.HOST.javaName());
    }

    protected void copyPort(CustomPropertySetValues propertyValues) {
        this.port = (String) propertyValues.getProperty(Fields.PORT.javaName());
    }

    protected void copyConnectionTimeout(CustomPropertySetValues propertyValues) {
        this.setConnectionTimeout((TimeDuration) propertyValues.getProperty(Fields.CONNECTION_TIMEOUT.javaName()));
    }

    public void setConnectionTimeout(TimeDuration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        if (connectionTimeout != null) {
            this.connectionTimeoutValue = connectionTimeout.getCount();
            this.connectionTimeoutUnit = connectionTimeout.getTimeUnitCode();
        }
    }

    protected void copyBufferSize(CustomPropertySetValues propertyValues) {
        this.bufferSize = (BigDecimal) propertyValues.getProperty(Fields.BUFFER_SIZE.javaName());
    }

    protected void copyPostDialProperties(CustomPropertySetValues propertyValues) {
        this.postDialDelayMillis = (BigDecimal) propertyValues.getProperty(Fields.POST_DIAL_DELAY_MILLIS.javaName());
        this.postDialCommandAttempts = (BigDecimal) propertyValues.getProperty(Fields.POST_DIAL_COMMAND_ATTEMPTS.javaName());
        this.postDialCommand = (String) propertyValues.getProperty(Fields.POST_DIAL_COMMAND.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(Fields.HOST.javaName(), this.host);
        propertySetValues.setProperty(Fields.PORT.javaName(), this.port);
        this.copyNullablePropertyTo(propertySetValues, Fields.CONNECTION_TIMEOUT, this.connectionTimeout);
        this.copyNullablePropertyTo(propertySetValues, Fields.BUFFER_SIZE, this.bufferSize);
        this.copyPostDialProperties(propertySetValues);
    }

    private void copyNullablePropertyTo(CustomPropertySetValues propertySetValues, Fields field, Object value) {
        if (value != null) {
            propertySetValues.setProperty(field.javaName(), value);
        }
    }

    private void copyPostDialPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.copyNullablePropertyTo(propertySetValues, Fields.POST_DIAL_DELAY_MILLIS, this.postDialDelayMillis);
        this.copyNullablePropertyTo(propertySetValues, Fields.POST_DIAL_COMMAND_ATTEMPTS, this.postDialCommandAttempts);
        this.copyNullablePropertyTo(propertySetValues, Fields.POST_DIAL_COMMAND, this.postDialCommand);
    }

}