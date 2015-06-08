package com.energyict.mdc.channels.serial.direct.serialio;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.dynamicattributes.BigDecimalFactory;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.AbstractSerialConnectionType;
import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.ServerLoggableComChannel;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType} interface for Serial communication.
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 11:39
 */
@XmlRootElement
public class SioSerialConnectionType extends AbstractSerialConnectionType {

    @Override
    public boolean isRequiredProperty(String name) {
        return SerialPortConfiguration.NR_OF_STOP_BITS_NAME.equals(name) || SerialPortConfiguration.PARITY_NAME.equals(name)
                || SerialPortConfiguration.BAUDRATE_NAME.equals(name) || SerialPortConfiguration.NR_OF_DATA_BITS_NAME.equals(name);
    }

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        SerialPortConfiguration serialPortConfiguration = createSerialConfiguration(comPort, properties);
        ServerLoggableComChannel comChannel = newSioSerialConnection(serialPortConfiguration);
        comChannel.setComPort(comPort);
        comChannel.addProperties(createTypeProperty(ComChannelType.SerialComChannel));
        return comChannel;
    }

    protected SerialPortConfiguration createSerialConfiguration(ComPort comPort, List<ConnectionTaskProperty> properties) {
        for (ConnectionTaskProperty property : properties) {
            this.setProperty(property.getName(), property.getValue());
        }
        SerialPortConfiguration serialPortConfiguration = new SerialPortConfiguration(comPort.getName(), getBaudRateValue(), getNrOfDataBitsValue(),
                getNrOfStopBitsValue(), getParityValue(), getFlowControlValue());
        return serialPortConfiguration;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-04-09 16:49:44 +0200 (di, 09 apr 2013) $";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>(4);
        propertySpecs.add(this.baudRatePropertySpec());
        propertySpecs.add(this.nrOfStopBitsPropertySpec());
        propertySpecs.add(this.parityPropertySpec());
        propertySpecs.add(this.nrOfDataBitsPropertySpec());
        return propertySpecs;
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
                markExhaustive().
                setDefaultValue(NrOfStopBits.ONE.getNrOfStopBits());
        for (NrOfStopBits nrOfStopBits : EnumSet.complementOf(EnumSet.of(NrOfStopBits.ONE_AND_HALF))) {
            builder.addValues(nrOfStopBits.getNrOfStopBits());
        }
        return builder.finish();
    }

    @Override
    protected BaudrateValue getBaudRateValue() {
        return BaudrateValue.valueFor((BigDecimal) getProperty(SerialPortConfiguration.BAUDRATE_NAME));
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>(1);
        propertySpecs.add(this.flowControlPropertySpec());
        return propertySpecs;
    }
}
