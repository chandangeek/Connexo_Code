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

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the RxTx library.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:15)
 */
public abstract class RxTxSerialComponentServiceImpl extends AbstractSerialComponentServiceImpl {

    // For OSGi framework only
    protected RxTxSerialComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    protected RxTxSerialComponentServiceImpl(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration) {
        return new RxTxSerialPort(configuration);
    }

    protected PropertySpec flowControlPropertySpec() {
        return this.getPropertySpecService().newPropertySpecBuilder(new StringFactory())
                .name(SerialPortConfigurationPropertySpecNames.FLOW_CONTROL)
                .markExhaustive()
                .setDefaultValue(FlowControl.NONE.value())
                .addValues(FlowControl.RTSCTS.value(), FlowControl.XONXOFF.value())
                .finish();
    }

    protected final PropertySpec nrOfDataBitsPropertySpec(boolean required) {
        PropertySpecBuilder builder = this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory());
        builder.
                name(SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS).
                markExhaustive().
                setDefaultValue(NrOfDataBits.EIGHT.value()).
                addValues(NrOfDataBits.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    protected final PropertySpec nrOfStopBitsPropertySpec(boolean required) {
        PropertySpecBuilder builder = this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory());
        builder.
                name(SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS).
                markExhaustive().
                setDefaultValue(NrOfStopBits.ONE.value()).
                addValues(NrOfStopBits.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    protected final PropertySpec parityPropertySpec(boolean required) {
        PropertySpecBuilder builder = this.getPropertySpecService().newPropertySpecBuilder(new StringFactory());
        builder.
                name(SerialPortConfigurationPropertySpecNames.PARITY).
                markExhaustive().
                setDefaultValue(Parities.NONE.value()).
                addValues(Parities.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return  builder.finish();
    }

    protected final PropertySpec baudRatePropertySpec(boolean required) {
        PropertySpecBuilder builder = this.getPropertySpecService().newPropertySpecBuilder(new BigDecimalFactory());
        builder.
                name(SerialPortConfigurationPropertySpecNames.BAUDRATE).
                markExhaustive().
                setDefaultValue(BaudrateValue.BAUDRATE_57600.value()).
                addValues(BaudrateValue.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

}