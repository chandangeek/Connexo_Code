package com.energyict.mdc.io.impl;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.ServerSerialPort;
import com.energyict.mdc.io.naming.SerialPortConfigurationPropertySpecNames;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the serialio library.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:10)
 */
public abstract class SerialIOComponentServiceImpl extends AbstractSerialComponentServiceImpl {

    // For OSGi framework only
    protected SerialIOComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    protected SerialIOComponentServiceImpl(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration) {
        return new SioSerialPort(configuration);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(this.baudRatePropertySpec());
        propertySpecs.add(this.parityPropertySpec());
        propertySpecs.add(this.nrOfStopBitsPropertySpec());
        propertySpecs.add(this.nrOfDataBitsPropertySpec());
        propertySpecs.add(this.flowControlPropertySpec());
        this.addModemComponentProperties(propertySpecs);
        return propertySpecs;
    }

    protected abstract void addModemComponentProperties(List<PropertySpec> propertySpecs);

    /**
     * SerialIO does not support all provided baudrates
     *
     * @return the property spec for the SerialIO baudrate property
     */
    protected PropertySpec baudRatePropertySpec() {
        return
            this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory())
                .name(SerialPortConfigurationPropertySpecNames.BAUDRATE)
                .markExhaustive()
                .markRequired()
                .addValues(
                        BaudrateValue.BAUDRATE_150.value(),
                        BaudrateValue.BAUDRATE_300.value(),
                        BaudrateValue.BAUDRATE_600.value(),
                        BaudrateValue.BAUDRATE_1200.value(),
                        BaudrateValue.BAUDRATE_2400.value(),
                        BaudrateValue.BAUDRATE_4800.value(),
                        BaudrateValue.BAUDRATE_9600.value(),
                        BaudrateValue.BAUDRATE_19200.value(),
                        BaudrateValue.BAUDRATE_38400.value(),
                        BaudrateValue.BAUDRATE_57600.value(),
                        BaudrateValue.BAUDRATE_115200.value(),
                        BaudrateValue.BAUDRATE_230400.value(),
                        BaudrateValue.BAUDRATE_460800.value())
                .finish();
    }

    protected PropertySpec parityPropertySpec () {
        return this.getPropertySpecService().newPropertySpecBuilder(new StringFactory())
            .name(SerialPortConfigurationPropertySpecNames.PARITY)
            .markExhaustive()
            .setDefaultValue(Parities.NONE.value())
            .addValues(Parities.getTypedValues())
            .markRequired()
            .finish();
    }

    protected PropertySpec nrOfStopBitsPropertySpec() {
        PropertySpecBuilder builder =
                this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory()).
                        name(SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS).
                        markRequired().
                        markExhaustive().
                        setDefaultValue(NrOfStopBits.ONE.value());
        for (NrOfStopBits nrOfStopBits : EnumSet.complementOf(EnumSet.of(NrOfStopBits.ONE_AND_HALF))) {
            builder.addValues(nrOfStopBits.value());
        }
        return builder.finish();
    }

    protected PropertySpec nrOfDataBitsPropertySpec () {
        return this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory())
            .name(SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS)
            .markExhaustive()
            .setDefaultValue(NrOfDataBits.EIGHT.value())
            .addValues(NrOfDataBits.getTypedValues())
            .markRequired()
            .finish();
    }

    protected PropertySpec flowControlPropertySpec () {
        return this.getPropertySpecService().newPropertySpecBuilder(new StringFactory())
            .name(SerialPortConfigurationPropertySpecNames.FLOW_CONTROL)
            .markExhaustive()
            .setDefaultValue(FlowControl.NONE.value())
            .addValues(FlowControl.getTypedValues())
            .finish();
    }

}