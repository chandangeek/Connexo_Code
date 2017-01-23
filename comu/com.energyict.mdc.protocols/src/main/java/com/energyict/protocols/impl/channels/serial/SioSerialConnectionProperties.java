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
import com.energyict.mdc.channels.serial.modem.AbstractModemProperties;
import com.energyict.mdc.io.naming.ModemPropertySpecNames;
import com.energyict.mdc.io.naming.PEMPModemPropertySpecNames;
import com.energyict.mdc.io.naming.SerialPortConfigurationPropertySpecNames;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for all serial connection types. Ideally, this class should be in the mdc.io
 * bundle so that a {@link com.energyict.mdc.io.SerialComponentService}
 * could actually return the CustomPropertySet for the properties
 * that it defines. However, that would have introduced a cyclic dependency
 * between the protocol.api bundle and the mdc.io bundle that we were
 * not happy resolving due to time constraints.
 * This class therefore needs to assume it knows all of the properties
 * returned by all of the currently existing SerialComponentServices.
 * Any property that is added to any of the SerialComponentServices
 * will need to be supported here as well.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (13:12)
 */
public class SioSerialConnectionProperties extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ConnectionProvider>, PersistenceAware {

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
        },
        ;

        Fields(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

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
    @Size(max=Table.MAX_STRING_LENGTH)
    private String globalInits;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String inits;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String dialPrefix;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String addressSelector;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String postDialCommands;
    private TimeDuration dtrToggleDelay;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String pempConfigurationKey;
    @Size(max=Table.MAX_STRING_LENGTH)
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
        }
        else {
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
        this.delayBeforeSend = (TimeDuration) propertyValues.getProperty(ModemPropertySpecNames.DELAY_BEFORE_SEND);
        this.delayAfterConnect = (TimeDuration) propertyValues.getProperty(ModemPropertySpecNames.DELAY_AFTER_CONNECT);
        this.commandTimeOut = (TimeDuration) propertyValues.getProperty(ModemPropertySpecNames.COMMAND_TIMEOUT);
        this.connectTimeOut = (TimeDuration) propertyValues.getProperty(ModemPropertySpecNames.CONNECT_TIMEOUT);
        this.commandAttempts = (BigDecimal) propertyValues.getProperty(ModemPropertySpecNames.COMMAND_TRIES);
        this.globalInits = (String) propertyValues.getProperty(ModemPropertySpecNames.GLOBAL_INIT_STRINGS);
        this.inits = (String) propertyValues.getProperty(ModemPropertySpecNames.INIT_STRINGS);
        this.dialPrefix = (String) propertyValues.getProperty(ModemPropertySpecNames.DIAL_PREFIX);
        this.addressSelector = (String) propertyValues.getProperty(ModemPropertySpecNames.ADDRESS_SELECTOR);
        this.postDialCommands = (String) propertyValues.getProperty(ModemPropertySpecNames.POST_DIAL_COMMANDS);
        this.dtrToggleDelay = (TimeDuration) propertyValues.getProperty(ModemPropertySpecNames.DTR_TOGGLE_DELAY);
        this.pempConfigurationKey = (String) propertyValues.getProperty(PEMPModemPropertySpecNames.CONFIGURATION_KEY);
        this.phoneNumber = (String) propertyValues.getProperty(AbstractModemProperties.PHONE_NUMBER_PROPERTY_NAME);
    }

    protected void copyParityFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfigurationPropertySpecNames.PARITY);
        // The PropertySpec is actually using StringFactory so generic clients will use String values
        if (propertyValue instanceof String) {
            this.parity = Parities.valueFor((String) propertyValue);
        }
        else {
            this.parity = (Parities) propertyValue;
        }
    }

    protected void copyFromControlFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfigurationPropertySpecNames.FLOW_CONTROL);
        // The PropertySpec is actually using StringFactory so generic clients will use String values
        if (propertyValue instanceof String) {
            this.flowControl = FlowControl.valueFor((String) propertyValue);
        }
        else {
            this.flowControl = (FlowControl) propertyValue;
        }
    }

    protected void copyNumberOfStopBitsFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS);
        // The PropertySpec is actually using BigDecimalFactory so generic clients will use BigDecimal values
        if (propertyValue instanceof BigDecimal) {
            this.numberOfStopBits = NrOfStopBits.values()[((BigDecimal) propertyValue).intValue()];
        }
        else {
            this.numberOfStopBits = (NrOfStopBits) propertyValue;
        }
    }

    protected void copyNumberOfDataBitsFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS);
        // The PropertySpec is actually using BigDecimalFactory so generic clients will use BigDecimal values
        if (propertyValue instanceof BigDecimal) {
            this.numberOfDataBits = NrOfDataBits.valueFor((BigDecimal) propertyValue);
        }
        else {
            this.numberOfDataBits = (NrOfDataBits) propertyValue;
        }
    }

    protected void copyBaudRateFrom(CustomPropertySetValues propertyValues) {
        Object propertyValue = propertyValues.getProperty(SerialPortConfigurationPropertySpecNames.BAUDRATE);
        // The PropertySpec is actually using BigDecimalFactory so generic clients will use BigDecimal values
        if (propertyValue instanceof BigDecimal) {
            this.baudRate = BaudrateValue.valueFor((BigDecimal) propertyValue);
        }
        else {
            this.baudRate = (BaudrateValue) propertyValue;
        }
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        // The PropertySpec of the following properties are actually using StringFactory so generic clients will expect String values
        if (this.parity != null) {
            this.copyTo(propertySetValues, SerialPortConfigurationPropertySpecNames.PARITY, this.parity.getParity());
        }
        if (this.flowControl != null) {
            this.copyTo(propertySetValues, SerialPortConfigurationPropertySpecNames.FLOW_CONTROL, this.flowControl.getFlowControl());
        }
        // The PropertySpec of the following properties are actually using BigDecimalFactory so generic clients will expect BigDecimal values
        if (this.numberOfStopBits != null) {
            this.copyTo(propertySetValues, SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS, this.numberOfStopBits.getNrOfStopBits());
        }
        if (this.numberOfDataBits != null) {
            this.copyTo(propertySetValues, SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS, this.numberOfDataBits.getNrOfDataBits());
        }
        if (this.baudRate != null) {
            this.copyTo(propertySetValues, SerialPortConfigurationPropertySpecNames.BAUDRATE, this.baudRate.getBaudrate());
        }
        this.copyTo(propertySetValues, ModemPropertySpecNames.DELAY_BEFORE_SEND, this.delayBeforeSend);
        this.copyTo(propertySetValues, ModemPropertySpecNames.DELAY_AFTER_CONNECT, this.delayAfterConnect);
        this.copyTo(propertySetValues, ModemPropertySpecNames.COMMAND_TIMEOUT, this.commandTimeOut);
        this.copyTo(propertySetValues, ModemPropertySpecNames.CONNECT_TIMEOUT, this.connectTimeOut);
        this.copyTo(propertySetValues, ModemPropertySpecNames.COMMAND_TRIES, this.commandAttempts);
        this.copyTo(propertySetValues, ModemPropertySpecNames.GLOBAL_INIT_STRINGS, this.globalInits);
        this.copyTo(propertySetValues, ModemPropertySpecNames.INIT_STRINGS, this.inits);
        this.copyTo(propertySetValues, ModemPropertySpecNames.DIAL_PREFIX, this.dialPrefix);
        this.copyTo(propertySetValues, ModemPropertySpecNames.ADDRESS_SELECTOR, this.addressSelector);
        this.copyTo(propertySetValues, ModemPropertySpecNames.POST_DIAL_COMMANDS, this.postDialCommands);
        this.copyTo(propertySetValues, PEMPModemPropertySpecNames.CONFIGURATION_KEY, this.pempConfigurationKey);
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

}