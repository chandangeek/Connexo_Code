/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.Thesaurus;
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

import java.math.BigDecimal;

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
    protected RxTxSerialComponentServiceImpl(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration) {
        return new RxTxSerialPort(configuration);
    }

    protected PropertySpec flowControlPropertySpec() {
        return this.getPropertySpecService()
                .specForValuesOf(new StringFactory())
                .named(TranslationKeys.SERIAL_FLOWCONTROL)
                .describedAs(TranslationKeys.SERIAL_FLOWCONTROL_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markExhaustive()
                .setDefaultValue(FlowControl.NONE.value())
                .addValues(FlowControl.RTSCTS.value(), FlowControl.XONXOFF.value())
                .finish();
    }

    protected final PropertySpec nrOfDataBitsPropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder =
                this.getPropertySpecService()
                        .specForValuesOf(new BigDecimalFactory())
                        .named(TranslationKeys.SERIAL_NUMBEROFDATABITS)
                        .describedAs(TranslationKeys.SERIAL_NUMBEROFDATABITS_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus());
        builder
            .markExhaustive()
            .setDefaultValue(NrOfDataBits.EIGHT.value())
            .addValues(NrOfDataBits.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    protected final PropertySpec nrOfStopBitsPropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder =
                this.getPropertySpecService()
                        .specForValuesOf(new BigDecimalFactory())
                        .named(TranslationKeys.SERIAL_NUMBEROFSTOPBITS)
                        .describedAs(TranslationKeys.SERIAL_NUMBEROFSTOPBITS_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus());
        builder
            .markExhaustive()
            .setDefaultValue(NrOfStopBits.ONE.value())
            .addValues(NrOfStopBits.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    protected final PropertySpec parityPropertySpec(boolean required) {
        PropertySpecBuilder<String> builder =
                this.getPropertySpecService()
                        .specForValuesOf(new StringFactory())
                        .named(TranslationKeys.SERIAL_PARITY)
                        .describedAs(TranslationKeys.SERIAL_PARITY_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus());
        builder
            .markExhaustive()
            .setDefaultValue(Parities.NONE.value())
            .addValues(Parities.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return  builder.finish();
    }

    protected final PropertySpec baudRatePropertySpec(boolean required) {
        PropertySpecBuilder<BigDecimal> builder =
                this.getPropertySpecService()
                        .specForValuesOf(new BigDecimalFactory())
                        .named(TranslationKeys.SERIAL_BAUDRATE)
                        .describedAs(TranslationKeys.SERIAL_BAUDRATE_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus());
        builder
            .markExhaustive()
            .setDefaultValue(BaudrateValue.BAUDRATE_57600.value())
            .addValues(BaudrateValue.getTypedValues());
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

}