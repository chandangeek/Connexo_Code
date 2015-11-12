package com.energyict.protocols.impl.channels.serial;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ConnectionProvider;

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
public class SioSerialConnectionProperties implements PersistentDomainExtension<ConnectionProvider> {

    public enum FieldNames {
        CONNECTION_PROVIDER("connectionProvider", "CONNECTIONPROVIDER"),
        PARITY("parity", "PARITIES"),
        FLOW_CONTROL("flowControl", "FLOWCONTROL"),
        NUMBER_OF_STOP_BITS("numberOfStopBits", "NRSTOPBITS"),
        NUMBER_OF_DATA_BITS("numberOfDataBits", "NRDATABITS"),
        BAUD_RATE("baudRate", "BAUDRATE");

        FieldNames(String javaName, String databaseName) {
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
    }

    @SuppressWarnings("unused")
    private Reference<ConnectionProvider> connectionProvider = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;
    private Parities parity;
    private FlowControl flowControl;
    private NrOfStopBits numberOfStopBits;
    private NrOfDataBits numberOfDataBits;
    private BaudrateValue baudrate;

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues) {
        this.connectionProvider.set(connectionProvider);
        this.parity = (Parities) propertyValues.getProperty(SerialPortConfiguration.PARITY_NAME);
        this.flowControl = (FlowControl) propertyValues.getProperty(SerialPortConfiguration.FLOW_CONTROL_NAME);
        this.numberOfStopBits = (NrOfStopBits) propertyValues.getProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME);
        this.numberOfDataBits = (NrOfDataBits) propertyValues.getProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME);
        this.baudrate = (BaudrateValue) propertyValues.getProperty(SerialPortConfiguration.BAUDRATE_NAME);
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(SerialPortConfiguration.PARITY_NAME, this.parity);
        propertySetValues.setProperty(SerialPortConfiguration.FLOW_CONTROL_NAME, this.flowControl);
        propertySetValues.setProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME, this.numberOfStopBits);
        propertySetValues.setProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME, this.numberOfDataBits);
        propertySetValues.setProperty(SerialPortConfiguration.BAUDRATE_NAME, this.baudrate);
    }

}