package com.energyict.protocols.impl.channels.ip.socket.dsmr;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.channels.ip.OutboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.dsmr.OutboundTcpIpWithWakeUpConnectionType;
import com.energyict.mdc.common.protocol.ConnectionProvider;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Objects;

public class OutboundTcpIpWithWakeupConnectionProperties
        extends AbstractVersionedPersistentDomainExtension
        implements PersistentDomainExtension<ConnectionProvider>, PersistenceAware {

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

        ENDPOINT_ADDRESS {
            @Override
            public String propertySpecName() {
                return OutboundTcpIpWithWakeUpConnectionType.PROPERTY_ENDPOINT_ADDRESS;
            }

            @Override
            public String javaName() {
                return "endpointAddress";
            }

            @Override
            public String databaseName() {
                return "ENDPOINTADDRESS";
            }
        },


        SOAP_ACTION {
            @Override
            public String propertySpecName() {
                return OutboundTcpIpWithWakeUpConnectionType.PROPERTY_SOAP_ACTION;
            }

            @Override
            public String javaName() {
                return "soapAction";
            }

            @Override
            public String databaseName() {
                return "SOAPACTION";
            }
        },


        WS_CONNECT_TIME_OUT {
            @Override
            public String propertySpecName() {
                return OutboundTcpIpWithWakeUpConnectionType.PROPERTY_WS_CONNECT_TIME_OUT;
            }

            @Override
            public String javaName() {
                return "wsConnectTimeOut";
            }

            @Override
            public String databaseName() {
                return "WSCONNECTTIMEOUT";
            }
        },


        WS_REQUEST_TIME_OUT {
            @Override
            public String propertySpecName() {
                return OutboundTcpIpWithWakeUpConnectionType.PROPERTY_WS_REQUEST_TIME_OUT;
            }

            @Override
            public String javaName() {
                return "wsRequestTimeOut";
            }

            @Override
            public String databaseName() {
                return "WSREQUESTTIMEOUT";
            }
        },

        SOURCE_ID {
            @Override
            public String propertySpecName() {
                return OutboundTcpIpWithWakeUpConnectionType.PROPERTY_SOURCE_ID;
            }

            @Override
            public String javaName() {
                return "sourceId";
            }

            @Override
            public String databaseName() {
                return "SOURCEID";
            }
        },

        TRIGGER_TYPE {
            @Override
            public String propertySpecName() {
                return OutboundTcpIpWithWakeUpConnectionType.PROPERTY_TRIGGER_TYPE;
            }

            @Override
            public String javaName() {
                return "triggerType";
            }

            @Override
            public String databaseName() {
                return "TRIGGERTYPE";
            }
        },


        WS_USER_ID {
            @Override
            public String propertySpecName() {
                return OutboundTcpIpWithWakeUpConnectionType.PROPERTY_USER_ID;
            }

            @Override
            public String javaName() {
                return "wsUserId";
            }

            @Override
            public String databaseName() {
                return "WSUSERID";
            }
        },

        WS_USER_PASS {
            @Override
            public String propertySpecName() {
                return OutboundTcpIpWithWakeUpConnectionType.PROPERTY_USER_PASS;
            }

            @Override
            public String javaName() {
                return "wsUserPass";
            }

            @Override
            public String databaseName() {
                return "WSUSERPASS";
            }
        },
        ;

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

    @Size(max = Table.MAX_STRING_LENGTH)
    private String endpointAddress;

    @Size(max = Table.MAX_STRING_LENGTH)
    private String soapAction;

    private TimeDuration wsConnectTimeOut;

    private TimeDuration wsRequestTimeOut;

    @Size(max = Table.MAX_STRING_LENGTH)
    private String sourceId;

    @Size(max = Table.MAX_STRING_LENGTH)
    private String triggerType;

    @Size(max = Table.MAX_STRING_LENGTH)
    private String wsUserId;

    @Size(max = Table.MAX_STRING_LENGTH)
    private String wsUserPass;
    //TODO: refactor wsUserPass to private Reference<SecurityAccessorType> wsUserPass = Reference.empty();


    @Override
    public void postLoad() {
        /* ORM layer is configured with access paths to convert two database columns to one field
         * and will have created a TimeDuration to be able to inject the values from both columns
         * into the TimeDuration. If the time unit code is zero then it was actually a null value. */

        if (this.connectionTimeout != null && this.connectionTimeout.getTimeUnitCode() == 0) {
            this.connectionTimeout = null;
        }

        if (this.wsConnectTimeOut != null && this.wsConnectTimeOut.getTimeUnitCode() == 0) {
            this.wsConnectTimeOut = null;
        }

        if (this.wsRequestTimeOut != null && this.wsRequestTimeOut.getTimeUnitCode() == 0) {
            this.wsRequestTimeOut = null;
        }

    }

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.connectionProvider.set(connectionProvider);

        // OutboundTcpIp properties (inherited)
        this.copyHost(propertyValues);
        this.copyPort(propertyValues);
        this.copyConnectionTimeout(propertyValues);

        // WakeUp specific properties
        this.copyEndpointAddress(propertyValues);
        this.copySoapAction(propertyValues);
        this.copyWsConnectTimeout(propertyValues);
        this.copyWsRequestTimeout(propertyValues);
        this.copySourceId(propertyValues);
        this.copyTriggerType(propertyValues);
        this.copyUserId(propertyValues);
        this.copyUserPass(propertyValues);
    }

    protected void copyHost(CustomPropertySetValues propertyValues) {
        this.host = (String) propertyValues.getProperty(Fields.HOST.propertySpecName());
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

    protected void copyEndpointAddress(CustomPropertySetValues propertyValues) {
        this.endpointAddress = (String) propertyValues.getProperty(Fields.ENDPOINT_ADDRESS.propertySpecName());
    }

    protected void copySoapAction(CustomPropertySetValues propertyValues) {
        this.soapAction = (String) propertyValues.getProperty(Fields.SOAP_ACTION.propertySpecName());
    }

    protected void copyWsConnectTimeout(CustomPropertySetValues propertyValues) {
        this.wsConnectTimeOut = (TimeDuration) propertyValues.getProperty(Fields.WS_CONNECT_TIME_OUT.propertySpecName());
    }


    protected void copyWsRequestTimeout(CustomPropertySetValues propertyValues) {
        this.wsRequestTimeOut = (TimeDuration) propertyValues.getProperty(Fields.WS_REQUEST_TIME_OUT.propertySpecName());
    }

    protected void copySourceId(CustomPropertySetValues propertyValues) {
        this.sourceId = (String) propertyValues.getProperty(Fields.SOURCE_ID.propertySpecName());
    }

    protected void copyTriggerType(CustomPropertySetValues propertyValues) {
        this.triggerType = (String) propertyValues.getProperty(Fields.TRIGGER_TYPE.propertySpecName());
    }

    protected void copyUserId(CustomPropertySetValues propertyValues) {
        this.wsUserId = (String) propertyValues.getProperty(Fields.WS_USER_ID.propertySpecName());
    }


    protected void copyUserPass(CustomPropertySetValues propertyValues) {
        this.wsUserPass = (String) propertyValues.getProperty(Fields.WS_USER_PASS.propertySpecName());
    }


    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        this.copyNullablePropertyTo(propertySetValues, Fields.HOST, this.host);
        this.copyNullablePropertyTo(propertySetValues, Fields.PORT_NUMBER, this.portNumber);
        this.copyNullablePropertyTo(propertySetValues, Fields.CONNECTION_TIMEOUT, this.connectionTimeout);

        this.copyWakeUpPropertiesTo(propertySetValues);
    }


    private void copyWakeUpPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.copyNullablePropertyTo(propertySetValues, Fields.ENDPOINT_ADDRESS, this.endpointAddress);
        this.copyNullablePropertyTo(propertySetValues, Fields.SOAP_ACTION, this.soapAction);
        this.copyNullablePropertyTo(propertySetValues, Fields.WS_CONNECT_TIME_OUT, this.wsConnectTimeOut);
        this.copyNullablePropertyTo(propertySetValues, Fields.WS_REQUEST_TIME_OUT, this.wsRequestTimeOut);
        this.copyNullablePropertyTo(propertySetValues, Fields.SOURCE_ID, this.sourceId);
        this.copyNullablePropertyTo(propertySetValues, Fields.TRIGGER_TYPE, this.triggerType);
        this.copyNullablePropertyTo(propertySetValues, Fields.WS_USER_ID, this.wsUserId);
        this.copyNullablePropertyTo(propertySetValues, Fields.WS_USER_PASS, this.wsUserPass);

    }


    private void copyNullablePropertyTo(CustomPropertySetValues propertySetValues, Fields field, Object value) {
        if (value != null) {
            propertySetValues.setProperty(field.propertySpecName(), value);
        }
    }


    @Override
    public void validateDelete() {
        // Nothing to validate
    }
}