package com.energyict.mdc.channels.serial;

import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 11:39
 */
public abstract class AbstractSerialConnectionType extends ConnectionTypeImpl {

    @Override
    public boolean allowsSimultaneousConnections() {
        return false;
    }

    @Override
    public boolean supportsComWindow() {
        return true;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(baudRatePropertySpec(), parityPropertySpec(), nrOfStopBitsPropertySpec(), nrOfDataBitsPropertySpec(), flowControlPropertySpec());
    }

    protected PropertySpec flowControlPropertySpec() {
        return  UPLPropertySpecFactory.stringWithDefault(SerialPortConfiguration.FLOW_CONTROL_NAME, false, FlowControl.NONE.getFlowControl(), FlowControl.getTypedValues());
    }

    protected PropertySpec nrOfDataBitsPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(SerialPortConfiguration.NR_OF_DATA_BITS_NAME, true, NrOfDataBits.EIGHT.getNrOfDataBits(), NrOfDataBits.getTypedValues());
    }

    protected PropertySpec nrOfStopBitsPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(SerialPortConfiguration.NR_OF_STOP_BITS_NAME, true, NrOfStopBits.ONE.getNrOfStopBits(), NrOfStopBits.getTypedValues());
    }

    protected PropertySpec parityPropertySpec() {
        return UPLPropertySpecFactory.stringWithDefault(SerialPortConfiguration.PARITY_NAME, true, Parities.NONE.getParity(), Parities.getTypedValues());
    }

    protected PropertySpec baudRatePropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(SerialPortConfiguration.BAUDRATE_NAME, true, BaudrateValue.BAUDRATE_57600.getBaudrate(), BaudrateValue.getTypedValues());
    }

    protected Parities getParityValue() {
        Parities value = Parities.valueFor((String) getProperty(SerialPortConfiguration.PARITY_NAME));
        return value != null ? value : Parities.NONE;
    }

    protected FlowControl getFlowControlValue() {
        FlowControl value = FlowControl.valueFor((String) getProperty(SerialPortConfiguration.FLOW_CONTROL_NAME));
        return value != null ? value : FlowControl.NONE;
    }

    protected NrOfStopBits getNrOfStopBitsValue() {
        NrOfStopBits value = NrOfStopBits.valueFor((BigDecimal) getProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME));
        return value != null ? value : NrOfStopBits.ONE;
    }

    protected NrOfDataBits getNrOfDataBitsValue() {
        NrOfDataBits value = NrOfDataBits.valueFor((BigDecimal) getProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME));
        return value != null ? value : NrOfDataBits.EIGHT;
    }

    protected BaudrateValue getBaudRateValue() {
        BaudrateValue value = BaudrateValue.valueFor((BigDecimal) getProperty(SerialPortConfiguration.BAUDRATE_NAME));
        return value != null ? value : BaudrateValue.BAUDRATE_57600;
    }

    protected BigDecimal getPortOpenTimeOutValue() {
        return this.nrOfMilliSecondsOfTimeDuration(SerialPortConfiguration.DEFAULT_SERIAL_PORT_OPEN_TIMEOUT);
    }

    protected BigDecimal getPortReadTimeOutValue() {
        return this.nrOfMilliSecondsOfTimeDuration(SerialPortConfiguration.DEFAULT_SERIAL_PORT_READ_TIMEOUT);
    }

    protected BigDecimal nrOfMilliSecondsOfTimeDuration(TemporalAmount value) {
        if (value == null) {
            return new BigDecimal(0);
        } else {
            return new BigDecimal(value.get(ChronoUnit.MILLIS));
        }
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.SERIAL);
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

    /**
     * Provides the name of the OS hardware port.
     *
     * @param properties the properties that should contain this name
     * @return the hardware comport name
     */
    protected String getComPortName(TypedProperties properties) {
        return properties.getTypedProperty(Property.COMP_PORT_NAME.getName());
    }
}
