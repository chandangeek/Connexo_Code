package com.energyict.mdc.channels.serial;

import com.energyict.mdc.io.ConnectionType.ConnectionTypeDirection;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.dynamicattributes.BigDecimalFactory;
import com.energyict.dynamicattributes.StringFactory;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 11:39
 */
public abstract class AbstractSerialConnectionType extends ConnectionTypeImpl {

    private Map<String, PropertySpec> propertySpecs;

    @Override
    public boolean allowsSimultaneousConnections() {
        return false;
    }

    @Override
    public boolean supportsComWindow() {
        return true;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        this.ensurePropertySpecsInitialized();
        return this.propertySpecs.get(name);
    }

    private void ensurePropertySpecsInitialized() {
        if (this.propertySpecs == null) {
            Map<String, PropertySpec> temp = new HashMap<String, PropertySpec>();
            this.initializePropertySpecs(temp);
            this.propertySpecs = temp;
        }
    }

    private void initializePropertySpecs(Map<String, PropertySpec> propertySpecs) {
        propertySpecs.put(SerialPortConfiguration.BAUDRATE_NAME, this.baudRatePropertySpec());
        propertySpecs.put(SerialPortConfiguration.PARITY_NAME, this.parityPropertySpec());
        propertySpecs.put(SerialPortConfiguration.NR_OF_STOP_BITS_NAME, this.nrOfStopBitsPropertySpec());
        propertySpecs.put(SerialPortConfiguration.NR_OF_DATA_BITS_NAME, this.nrOfDataBitsPropertySpec());
        propertySpecs.put(SerialPortConfiguration.FLOW_CONTROL_NAME, this.flowControlPropertySpec());
    }

    protected PropertySpec<String> flowControlPropertySpec() {
        return PropertySpecBuilder.
                forClass(String.class, new StringFactory()).
                name(SerialPortConfiguration.FLOW_CONTROL_NAME).
                markExhaustive().
                setDefaultValue(FlowControl.NONE.getFlowControl()).
                addValues(FlowControl.getTypedValues()).
                finish();
    }

    protected PropertySpec<BigDecimal> nrOfDataBitsPropertySpec() {
        return PropertySpecBuilder.
                forClass(BigDecimal.class, new BigDecimalFactory()).
                name(SerialPortConfiguration.NR_OF_DATA_BITS_NAME).
                markExhaustive().
                setDefaultValue(NrOfDataBits.EIGHT.getNrOfDataBits()).
                addValues(NrOfDataBits.getTypedValues()).finish();
    }

    protected PropertySpec<BigDecimal> nrOfStopBitsPropertySpec() {
        return PropertySpecBuilder.
                forClass(BigDecimal.class, new BigDecimalFactory()).
                name(SerialPortConfiguration.NR_OF_STOP_BITS_NAME).
                markExhaustive().
                setDefaultValue(NrOfStopBits.ONE.getNrOfStopBits()).
                addValues(NrOfStopBits.getTypedValues()).
                finish();
    }

    protected PropertySpec<String> parityPropertySpec() {
        return PropertySpecBuilder.
                forClass(String.class, new StringFactory()).
                name(SerialPortConfiguration.PARITY_NAME).
                markExhaustive().
                setDefaultValue(Parities.NONE.getParity()).
                addValues(Parities.getTypedValues()).
                finish();
    }

    protected PropertySpec<BigDecimal> baudRatePropertySpec() {
        return PropertySpecBuilder.
                forClass(BigDecimal.class, new BigDecimalFactory()).
                name(SerialPortConfiguration.BAUDRATE_NAME).
                markExhaustive().
                setDefaultValue(BaudrateValue.BAUDRATE_57600.getBaudrate()).
                addValues(BaudrateValue.getTypedValues()).
                finish();
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

    protected BigDecimal nrOfMilliSecondsOfTimeDuration(TimeDuration value) {
        if (value == null) {
            return new BigDecimal(0);
        } else {
            return new BigDecimal(value.getMilliSeconds());
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
}
