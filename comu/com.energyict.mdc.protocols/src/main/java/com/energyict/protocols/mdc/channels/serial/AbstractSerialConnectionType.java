package com.energyict.protocols.mdc.channels.serial;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.ComPortType;
import com.energyict.mdc.protocol.SerialConnectionPropertyNames;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.impl.BigDecimalFactory;
import com.energyict.mdc.protocol.dynamic.impl.PropertySpecBuilder;
import com.energyict.mdc.protocol.dynamic.impl.StringFactory;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
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
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        this.ensurePropertySpecsInitialized();
        propertySpecs.addAll(this.propertySpecs.values());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        this.ensurePropertySpecsInitialized();
        return this.propertySpecs.get(name);
    }

    private void ensurePropertySpecsInitialized () {
        if (this.propertySpecs == null) {
            Map<String, PropertySpec> temp = new HashMap<>();
            this.initializePropertySpecs(temp);
            this.propertySpecs = temp;
        }
    }

    private void initializePropertySpecs (Map<String, PropertySpec> propertySpecs) {
        propertySpecs.put(SerialPortConfiguration.BAUDRATE_NAME, this.baudRatePropertySpec());
        propertySpecs.put(SerialPortConfiguration.PARITY_NAME, this.parityPropertySpec());
        propertySpecs.put(SerialPortConfiguration.NR_OF_STOP_BITS_NAME, this.nrOfStopBitsPropertySpec());
        propertySpecs.put(SerialPortConfiguration.NR_OF_DATA_BITS_NAME, this.nrOfDataBitsPropertySpec());
        propertySpecs.put(SerialPortConfiguration.FLOW_CONTROL_NAME, this.flowControlPropertySpec());
    }

    protected abstract PropertySpec<String> flowControlPropertySpec();

    protected final PropertySpec<String> flowControlPropertySpec(boolean required) {
        PropertySpecBuilder<String> builder = PropertySpecBuilder.forClass(String.class, new StringFactory());
        builder.
            name(SerialPortConfiguration.FLOW_CONTROL_NAME).
            markExhaustive().
            setDefaultValue(FlowControl.NONE.getFlowControl()).
            addValues(FlowControl.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    protected abstract PropertySpec<BigDecimal> nrOfDataBitsPropertySpec();

    protected final PropertySpec<BigDecimal> nrOfDataBitsPropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder = PropertySpecBuilder.forClass(BigDecimal.class, new BigDecimalFactory());
        builder.
            name(SerialPortConfiguration.NR_OF_DATA_BITS_NAME).
            markExhaustive().
            setDefaultValue(NrOfDataBits.EIGHT.getNrOfDataBits()).
            addValues(NrOfDataBits.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    protected abstract PropertySpec<BigDecimal> nrOfStopBitsPropertySpec();

    protected final PropertySpec<BigDecimal> nrOfStopBitsPropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder = PropertySpecBuilder.forClass(BigDecimal.class, new BigDecimalFactory());
        builder.
            name(SerialPortConfiguration.NR_OF_STOP_BITS_NAME).
            markExhaustive().
            setDefaultValue(NrOfStopBits.ONE.getNrOfStopBits()).
            addValues(NrOfStopBits.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    protected abstract PropertySpec<String> parityPropertySpec();

    protected final PropertySpec<String> parityPropertySpec(boolean required) {
        PropertySpecBuilder<String> builder = PropertySpecBuilder.forClass(String.class, new StringFactory());
        builder.
            name(SerialPortConfiguration.PARITY_NAME).
            markExhaustive().
            setDefaultValue(Parities.NONE.getParity()).
            addValues(Parities.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return  builder.finish();
    }

    protected abstract PropertySpec<BigDecimal> baudRatePropertySpec();

    protected final PropertySpec<BigDecimal> baudRatePropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder = PropertySpecBuilder.forClass(BigDecimal.class, new BigDecimalFactory());
        builder.
            name(SerialPortConfiguration.BAUDRATE_NAME).
            markExhaustive().
            setDefaultValue(BaudrateValue.BAUDRATE_57600.getBaudrate()).
            addValues(BaudrateValue.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    protected Parities getParityValue() {
        return Parities.valueFor((String) getProperty(SerialPortConfiguration.PARITY_NAME));
    }

    protected FlowControl getFlowControlValue() {
        return FlowControl.valueFor((String) getProperty(SerialPortConfiguration.FLOW_CONTROL_NAME));
    }

    protected NrOfStopBits getNrOfStopBitsValue() {
        return NrOfStopBits.valueFor((BigDecimal) getProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME));
    }

    protected NrOfDataBits getNrOfDataBitsValue() {
        return NrOfDataBits.valueFor((BigDecimal) getProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME));
    }

    protected BaudrateValue getBaudRateValue() {
        return BaudrateValue.valueFor((BigDecimal)getProperty(SerialPortConfiguration.BAUDRATE_NAME));
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

    public String getComPortNameValue () {
        return (String) this.getProperty(SerialConnectionPropertyNames.COMPORT_NAME_PROPERTY_NAME.propertyName());
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.SERIAL);
    }

}