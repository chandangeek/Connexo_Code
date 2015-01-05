package com.energyict.mdc.io.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.ServerSerialPort;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.StringFactory;

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
                .name(SerialPortConfiguration.BAUDRATE_NAME)
                .markExhaustive()
                .markRequired()
                .addValues(
                        BaudrateValue.BAUDRATE_150.getBaudrate(),
                        BaudrateValue.BAUDRATE_300.getBaudrate(),
                        BaudrateValue.BAUDRATE_600.getBaudrate(),
                        BaudrateValue.BAUDRATE_1200.getBaudrate(),
                        BaudrateValue.BAUDRATE_2400.getBaudrate(),
                        BaudrateValue.BAUDRATE_4800.getBaudrate(),
                        BaudrateValue.BAUDRATE_9600.getBaudrate(),
                        BaudrateValue.BAUDRATE_19200.getBaudrate(),
                        BaudrateValue.BAUDRATE_38400.getBaudrate(),
                        BaudrateValue.BAUDRATE_57600.getBaudrate(),
                        BaudrateValue.BAUDRATE_115200.getBaudrate(),
                        BaudrateValue.BAUDRATE_230400.getBaudrate(),
                        BaudrateValue.BAUDRATE_460800.getBaudrate())
                .finish();
    }

    protected PropertySpec parityPropertySpec () {
        return this.getPropertySpecService().newPropertySpecBuilder(new StringFactory())
            .name(SerialPortConfiguration.PARITY_NAME)
            .markExhaustive()
            .setDefaultValue(Parities.NONE.getParity())
            .addValues(Parities.getTypedValues())
            .markRequired()
            .finish();
    }

    protected PropertySpec nrOfStopBitsPropertySpec() {
        PropertySpecBuilder builder =
                this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory()).
                        name(SerialPortConfiguration.NR_OF_STOP_BITS_NAME).
                        markRequired().
                        markExhaustive().
                        setDefaultValue(NrOfStopBits.ONE.getNrOfStopBits());
        for (NrOfStopBits nrOfStopBits : EnumSet.complementOf(EnumSet.of(NrOfStopBits.ONE_AND_HALF))) {
            builder.addValues(nrOfStopBits.getNrOfStopBits());
        }
        return builder.finish();
    }

    protected PropertySpec nrOfDataBitsPropertySpec () {
        return this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory())
            .name(SerialPortConfiguration.NR_OF_DATA_BITS_NAME)
            .markExhaustive()
            .setDefaultValue(NrOfDataBits.EIGHT.getNrOfDataBits())
            .addValues(NrOfDataBits.getTypedValues())
            .markRequired()
            .finish();
    }

    protected PropertySpec flowControlPropertySpec () {
        return this.getPropertySpecService().newPropertySpecBuilder(new StringFactory())
            .name(SerialPortConfiguration.FLOW_CONTROL_NAME)
            .markExhaustive()
            .setDefaultValue(FlowControl.NONE.getFlowControl())
            .addValues(FlowControl.getTypedValues())
            .finish();
    }

}