package com.energyict.protocols.mdc.channels.serial.direct.serialio;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.BigDecimalFactory;
import com.energyict.mdc.protocol.dynamic.PropertySpecBuilder;
import com.energyict.protocols.mdc.channels.serial.AbstractSerialConnectionType;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType} interface for Serial communication.
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 11:39
 */
public class SioSerialConnectionType extends AbstractSerialConnectionType {

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        SerialPortConfiguration serialPortConfiguration = createSerialConfiguration(this.getComPortNameValue(), properties);
        return newSioSerialConnection(serialPortConfiguration);
    }

    protected SerialPortConfiguration createSerialConfiguration(String comPortName, List<ConnectionProperty> properties) {
        for (ConnectionProperty property : properties) {
            this.setProperty(property.getName(), property.getValue());
        }
        return new SerialPortConfiguration(
                        comPortName,
                        getBaudRateValue(),
                        getNrOfDataBitsValue(),
                        getNrOfStopBitsValue(),
                        getParityValue(),
                        getFlowControlValue());
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-04-09 16:49:44 +0200 (di, 09 apr 2013) $";
    }

    /**
     * SerialIO does not support all provided baudrates
     *
     * @return the property spec for the SerialIO baudrate property
     */
    @Override
    protected PropertySpec<BigDecimal> baudRatePropertySpec() {
        return
            PropertySpecBuilder.
                forClass(BigDecimal.class, new BigDecimalFactory()).
                name(SerialPortConfiguration.BAUDRATE_NAME).
                markExhaustive().
                markRequired().
                addValues(
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
                        BaudrateValue.BAUDRATE_460800.getBaudrate()).
                    finish();
    }

    /**
     * SerialIO does not support 1,5 stopBits
     *
     * @return the property spec for the SerialIO nrOfStopBits property
     */
    @Override
    protected PropertySpec<BigDecimal> nrOfStopBitsPropertySpec() {
        PropertySpecBuilder<BigDecimal> builder =
            PropertySpecBuilder.
                forClass(BigDecimal.class, new BigDecimalFactory()).
                name(SerialPortConfiguration.NR_OF_STOP_BITS_NAME).
                markRequired().
                markExhaustive().
                setDefaultValue(NrOfStopBits.ONE.getNrOfStopBits());
        for (NrOfStopBits nrOfStopBits : EnumSet.complementOf(EnumSet.of(NrOfStopBits.ONE_AND_HALF))) {
            builder.addValues(nrOfStopBits.getNrOfStopBits());
        }
        return builder.finish();
    }

    @Override
    protected PropertySpec<String> parityPropertySpec () {
        return this.parityPropertySpec(true);
    }

    @Override
    protected PropertySpec<BigDecimal> nrOfDataBitsPropertySpec () {
        return this.nrOfDataBitsPropertySpec(true);
    }

    @Override
    protected PropertySpec<String> flowControlPropertySpec () {
        return this.flowControlPropertySpec(false);
    }

}