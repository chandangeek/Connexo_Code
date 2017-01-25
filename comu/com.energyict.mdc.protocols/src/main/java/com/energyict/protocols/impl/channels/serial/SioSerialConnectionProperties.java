package com.energyict.protocols.impl.channels.serial;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.modem.AbstractModemProperties;
import com.energyict.mdc.channels.serial.modem.TypedPEMPModemProperties;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * This class therefore needs to assume it knows all of the properties
 * returned by all of the currently existing SerialComponentServices.
 * Any property that is added to any of the SerialComponentServices
 * will need to be supported here as well.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (13:12)
 */
public class SioSerialConnectionProperties extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ConnectionProvider>, PersistenceAware {

    @SuppressWarnings("unused")
    private Reference<ConnectionProvider> connectionProvider = Reference.empty();
    private Parities parity;
    private FlowControl flowControl;
    private NrOfStopBits numberOfStopBits;
    private NrOfDataBits numberOfDataBits;
    private BaudrateValue baudRate;
    private TimeDuration delayBeforeSend;
    private TimeDuration delayAfterConnect;
    private TimeDuration commandTimeOut;
    private TimeDuration connectTimeOut;
    private BigDecimal commandAttempts;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String globalInits;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String inits;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String dialPrefix;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String addressSelector;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String postDialCommands;
    private TimeDuration dtrToggleDelay;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String pempConfigurationKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String phoneNumber;

    @Override
    public void postLoad() {
        this.delayBeforeSend = this.postLoad(this.delayBeforeSend);
        this.delayAfterConnect = this.postLoad(this.delayAfterConnect);
        this.commandTimeOut = this.postLoad(this.commandTimeOut);
        this.connectTimeOut = this.postLoad(this.connectTimeOut);
        this.dtrToggleDelay = this.postLoad(this.dtrToggleDelay);
    }

    private TimeDuration postLoad(TimeDuration timeDuration) {
        if (timeDuration != null && timeDuration.getTimeUnitCode() == 0) {
            /* ORM layer is configured with access paths to convert two database columns to one field
             * and will have created a TimeDuration to be able to inject the values from both columns
             * into the TimeDuration. If the time unit code is zero then it was actually a null value. */
            return null;
        } else {
            return timeDuration;
        }
    }

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.connectionProvider.set(connectionProvider);
        this.copyParityFrom(propertyValues);
        this.copyFromControlFrom(propertyValues);
        this.copyNumberOfStopBitsFrom(propertyValues);
        this.copyNumberOfDataBitsFrom(propertyValues);
        this.copyBaudRateFrom(propertyValues);
        this.delayBeforeSend = (TimeDuration) propertyValues.getProperty(AbstractModemProperties.DELAY_BEFORE_SEND);
        this.delayAfterConnect = (TimeDuration) propertyValues.getProperty(AbstractModemProperties.DELAY_AFTER_CONNECT);
        this.commandTimeOut = (TimeDuration) propertyValues.getProperty(AbstractModemProperties.COMMAND_TIMEOUT);
        this.connectTimeOut = (TimeDuration) propertyValues.getProperty(AbstractModemProperties.CONNECT_TIMEOUT);
        this.commandAttempts = (BigDecimal) propertyValues.getProperty(AbstractModemProperties.COMMAND_TRIES);
        this.globalInits = (String) propertyValues.getProperty(AbstractModemProperties.MODEM_GLOBAL_INIT_STRINGS);
        this.inits = (String) propertyValues.getProperty(AbstractModemProperties.MODEM_INIT_STRINGS);
        this.dialPrefix = (String) propertyValues.getProperty(AbstractModemProperties.MODEM_DIAL_PREFIX);
        this.addressSelector = (String) propertyValues.getProperty(AbstractModemProperties.MODEM_ADDRESS_SELECTOR);
        this.postDialCommands = (String) propertyValues.getProperty(AbstractModemProperties.MODEM_POST_DIAL_COMMANDS);
        this.dtrToggleDelay = (TimeDuration) propertyValues.getProperty(AbstractModemProperties.DTR_TOGGLE_DELAY);
        this.pempConfigurationKey = (String) propertyValues.getProperty(TypedPEMPModemProperties.MODEM_CONFIGURATION_KEY);
        this.phoneNumber = (String) propertyValues.getProperty(AbstractModemProperties.PHONE_NUMBER_PROPERTY_NAME);
    }

    protected void copyParityFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfiguration.PARITY_NAME);
        // The PropertySpec is actually using StringFactory so generic clients will use String values
        if (propertyValue instanceof String) {
            this.parity = Parities.valueFor((String) propertyValue);
        } else {
            this.parity = (Parities) propertyValue;
        }
    }

    protected void copyFromControlFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfiguration.FLOW_CONTROL_NAME);
        // The PropertySpec is actually using StringFactory so generic clients will use String values
        if (propertyValue instanceof String) {
            this.flowControl = FlowControl.valueFor((String) propertyValue);
        } else {
            this.flowControl = (FlowControl) propertyValue;
        }
    }

    protected void copyNumberOfStopBitsFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME);
        // The PropertySpec is actually using BigDecimalFactory so generic clients will use BigDecimal values
        if (propertyValue instanceof BigDecimal) {
            this.numberOfStopBits = NrOfStopBits.values()[((BigDecimal) propertyValue).intValue()];
        } else {
            this.numberOfStopBits = (NrOfStopBits) propertyValue;
        }
    }

    protected void copyNumberOfDataBitsFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME);
        // The PropertySpec is actually using BigDecimalFactory so generic clients will use BigDecimal values
        if (propertyValue instanceof BigDecimal) {
            this.numberOfDataBits = NrOfDataBits.valueFor((BigDecimal) propertyValue);
        } else {
            this.numberOfDataBits = (NrOfDataBits) propertyValue;
        }
    }

    protected void copyBaudRateFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfiguration.BAUDRATE_NAME);
        // The PropertySpec is actually using BigDecimalFactory so generic clients will use BigDecimal values
        if (propertyValue instanceof BigDecimal) {
            this.baudRate = BaudrateValue.valueFor((BigDecimal) propertyValue);
        } else {
            this.baudRate = (BaudrateValue) propertyValue;
        }
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        // The PropertySpec of the following properties are actually using StringFactory so generic clients will expect String values
        if (this.parity != null) {
            this.copyTo(propertySetValues, SerialPortConfiguration.PARITY_NAME, this.parity.getParity());
        }
        if (this.flowControl != null) {
            this.copyTo(propertySetValues, SerialPortConfiguration.FLOW_CONTROL_NAME, this.flowControl.getFlowControl());
        }
        // The PropertySpec of the following properties are actually using BigDecimalFactory so generic clients will expect BigDecimal values
        if (this.numberOfStopBits != null) {
            this.copyTo(propertySetValues, SerialPortConfiguration.NR_OF_STOP_BITS_NAME, this.numberOfStopBits.getNrOfStopBits());
        }
        if (this.numberOfDataBits != null) {
            this.copyTo(propertySetValues, SerialPortConfiguration.NR_OF_DATA_BITS_NAME, this.numberOfDataBits.getNrOfDataBits());
        }
        if (this.baudRate != null) {
            this.copyTo(propertySetValues, SerialPortConfiguration.BAUDRATE_NAME, this.baudRate.getBaudrate());
        }
        this.copyTo(propertySetValues, AbstractModemProperties.DELAY_BEFORE_SEND, this.delayBeforeSend);
        this.copyTo(propertySetValues, AbstractModemProperties.DELAY_AFTER_CONNECT, this.delayAfterConnect);
        this.copyTo(propertySetValues, AbstractModemProperties.COMMAND_TIMEOUT, this.commandTimeOut);
        this.copyTo(propertySetValues, AbstractModemProperties.CONNECT_TIMEOUT, this.connectTimeOut);
        this.copyTo(propertySetValues, AbstractModemProperties.COMMAND_TRIES, this.commandAttempts);
        this.copyTo(propertySetValues, AbstractModemProperties.MODEM_GLOBAL_INIT_STRINGS, this.globalInits);
        this.copyTo(propertySetValues, AbstractModemProperties.MODEM_INIT_STRINGS, this.inits);
        this.copyTo(propertySetValues, AbstractModemProperties.MODEM_DIAL_PREFIX, this.dialPrefix);
        this.copyTo(propertySetValues, AbstractModemProperties.MODEM_ADDRESS_SELECTOR, this.addressSelector);
        this.copyTo(propertySetValues, AbstractModemProperties.MODEM_POST_DIAL_COMMANDS, this.postDialCommands);
        this.copyTo(propertySetValues, TypedPEMPModemProperties.MODEM_CONFIGURATION_KEY, this.pempConfigurationKey);
        this.copyTo(propertySetValues, AbstractModemProperties.PHONE_NUMBER_PROPERTY_NAME, this.phoneNumber);
    }

    private void copyTo(CustomPropertySetValues propertySetValues, String propertyName, Object propertyValue) {
        if (propertyValue != null) {
            propertySetValues.setProperty(propertyName, propertyValue);
        }
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    public enum Fields {
        PARITY("parity", "PARITIES") {
            @Override
            protected void addTo(Table table) {
                this.addAsEnumColumnTo(table);
            }
        },
        FLOW_CONTROL("flowControl", "FLOWCONTROL") {
            @Override
            protected void addTo(Table table) {
                this.addAsEnumColumnTo(table);
            }
        },
        NUMBER_OF_STOP_BITS("numberOfStopBits", "NRSTOPBITS") {
            @Override
            protected void addTo(Table table) {
                this.addAsEnumColumnTo(table);
            }
        },
        NUMBER_OF_DATA_BITS("numberOfDataBits", "NRDATABITS") {
            @Override
            protected void addTo(Table table) {
                this.addAsEnumColumnTo(table);
            }
        },
        PHONE_NUMBER("phoneNumber", "PHONENUMBER") {
            @Override
            protected void addTo(Table table) {
                this.addAsStringColumnTo(table);
            }
        },
        BAUD_RATE("baudRate", "BAUDRATE") {
            @Override
            protected void addTo(Table table) {
                this.addAsEnumColumnTo(table);
            }
        },
        DELAY_BEFORE_SEND("delayBeforeSend", "DELAYBEFORESEND") {
            @Override
            protected void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },
        DELAY_AFTER_CONNECT("delayAfterConnect", "DELAYAFTERCONNECT") {
            @Override
            protected void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },
        COMMAND_TIMEOUT("commandTimeOut", "COMMANDTIMEOUT") {
            @Override
            protected void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },
        CONNECT_TIMEOUT("connectTimeOut", "CONNECTTIMEOUT") {
            @Override
            protected void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },
        COMMAND_ATTEMPTS("commandAttempts", "COMMANDATTEMPTS") {
            @Override
            protected void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }
        },
        GLOBAL_INITS("globalInits", "GLOBALINITS") {
            @Override
            protected void addTo(Table table) {
                this.addAsStringColumnTo(table);
            }
        },
        INITS("inits", "INITS") {
            @Override
            protected void addTo(Table table) {
                this.addAsStringColumnTo(table);
            }
        },
        DIAL_PREFIX("dialPrefix", "DIALPREFIX") {
            @Override
            protected void addTo(Table table) {
                this.addAsStringColumnTo(table);
            }
        },
        ADDRESS_SELECTOR("addressSelector", "ADDRESSSELECTOR") {
            @Override
            protected void addTo(Table table) {
                this.addAsStringColumnTo(table);
            }
        },
        POST_DIAL_COMMANDS("postDialCommands", "POSTDIALCOMMANDS") {
            @Override
            protected void addTo(Table table) {
                this.addAsStringColumnTo(table);
            }
        },
        PEMP_CONFIGURATION_KEY("pempConfigurationKey", "PEMP_CONFKEY") {
            @Override
            protected void addTo(Table table) {
                this.addAsStringColumnTo(table);
            }
        },
        DTR_TOGGLE_DELAY("dtrToggleDelay", "DTRTOGGLEDELAY") {
            @Override
            protected void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },;

        private final String javaName;
        private final String databaseName;

        Fields(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }

        protected abstract void addTo(Table table);

        protected void addAsEnumColumnTo(Table table) {
            table
                    .column(this.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(this.javaName())
                    .add();
        }

        protected void addAsTimeDurationColumnTo(Table table) {
            table
                    .column(this.databaseName() + "VALUE")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + ".count")
                    .add();
            table
                    .column(this.databaseName() + "UNIT")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + ".timeUnitCode")
                    .add();
        }

        protected void addAsBigDecimalColumnTo(Table table) {
            table
                    .column(this.databaseName())
                    .number()
                    .map(this.javaName())
                    .add();
        }

        protected void addAsStringColumnTo(Table table) {
            table
                    .column(this.databaseName())
                    .varChar()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(this.javaName())
                    .add();
        }
    }

}