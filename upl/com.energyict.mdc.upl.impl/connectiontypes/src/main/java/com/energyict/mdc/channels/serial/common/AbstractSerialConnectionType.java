/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.channels.serial.common;

import com.energyict.mdc.channel.serial.BaudrateValue;
import com.energyict.mdc.channel.serial.FlowControl;
import com.energyict.mdc.channel.serial.NrOfDataBits;
import com.energyict.mdc.channel.serial.NrOfStopBits;
import com.energyict.mdc.channel.serial.Parities;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocols.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractSerialConnectionType extends ConnectionTypeImpl {

    private PropertySpecService propertySpecService;

    public AbstractSerialConnectionType(){
        super();
    }

    public AbstractSerialConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

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
        return this.stringWithDefaultSpec(SerialPortConfiguration.FLOW_CONTROL_NAME, PropertyTranslationKeys.SERIAL_FLOWCONTROL, false, FlowControl.NONE.getFlowControl(), FlowControl.getTypedValues());
    }

    protected PropertySpec nrOfDataBitsPropertySpec() {
        return this.bigDecimalSpec(SerialPortConfiguration.NR_OF_DATA_BITS_NAME, PropertyTranslationKeys.SERIAL_NUMBEROFDATABITS, true, NrOfDataBits.EIGHT.getNrOfDataBits(), NrOfDataBits.getTypedValues());
    }

    protected PropertySpec nrOfStopBitsPropertySpec() {
        return this.bigDecimalSpec(SerialPortConfiguration.NR_OF_STOP_BITS_NAME, PropertyTranslationKeys.SERIAL_NUMBEROFSTOPBITS, true, NrOfStopBits.ONE.getNrOfStopBits(), NrOfStopBits.getTypedValues());
    }

    protected PropertySpec parityPropertySpec() {
        return this.stringWithDefaultSpec(SerialPortConfiguration.PARITY_NAME, PropertyTranslationKeys.SERIAL_PARITY, true, Parities.NONE.getParity(), Parities.getTypedValues());
    }

    protected PropertySpec baudRatePropertySpec() {
        return this.bigDecimalSpec(SerialPortConfiguration.BAUDRATE_NAME, PropertyTranslationKeys.SERIAL_BAUDRATE, true, BaudrateValue.BAUDRATE_57600.getBaudrate(), BaudrateValue.getTypedValues());
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
            return new BigDecimal(((Duration) value).toMillis());
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

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected PropertySpec stringWithDefaultSpec(String name, TranslationKey translationKey, boolean required, String defaultValue, String... validValues) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::stringSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

    protected PropertySpec stringSpec(String name, TranslationKey translationKey, boolean required, String... validValues) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::stringSpec);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

    protected PropertySpec bigDecimalSpec(String name, TranslationKey translationKey, boolean required, BigDecimal defaultValue, BigDecimal... validValues) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

}
