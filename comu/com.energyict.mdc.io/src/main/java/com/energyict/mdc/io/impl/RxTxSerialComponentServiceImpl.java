package com.energyict.mdc.io.impl;

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

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the RxTx library.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:15)
 */
public abstract class RxTxSerialComponentServiceImpl extends AbstractSerialComponentServiceImpl {

    @Override
    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration) {
        return new RxTxSerialPort(configuration);
    }

    protected PropertySpec<String> flowControlPropertySpec() {
        return this.getPropertySpecService().newPropertySpecBuilder(new StringFactory())
                .name(SerialPortConfiguration.FLOW_CONTROL_NAME)
                .markExhaustive()
                .setDefaultValue(FlowControl.NONE.getFlowControl())
                .addValues(FlowControl.RTSCTS.getFlowControl(), FlowControl.XONXOFF.getFlowControl())
                .finish();
    }

    protected final PropertySpec<BigDecimal> nrOfDataBitsPropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder = this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory());
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

    protected final PropertySpec<BigDecimal> nrOfStopBitsPropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder = this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory());
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

    protected final PropertySpec<String> parityPropertySpec(boolean required) {
        PropertySpecBuilder<String> builder = this.getPropertySpecService().newPropertySpecBuilder(new StringFactory());
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

    protected final PropertySpec<BigDecimal> baudRatePropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder = this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory());
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

}