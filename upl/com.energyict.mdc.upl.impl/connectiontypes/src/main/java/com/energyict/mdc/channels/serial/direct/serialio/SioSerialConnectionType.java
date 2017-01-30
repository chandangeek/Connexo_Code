package com.energyict.mdc.channels.serial.direct.serialio;

import com.energyict.mdc.channels.serial.AbstractSerialConnectionType;
import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link ConnectionType} interface for Serial communication.
 * <p>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 11:39
 */
@XmlRootElement
public class SioSerialConnectionType extends AbstractSerialConnectionType {

    public SioSerialConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SerialPortComChannel connect() throws ConnectionException {
        SerialPortConfiguration serialPortConfiguration = createSerialConfiguration(getComPortName(getAllProperties()), getAllProperties());
        return newSioSerialConnection(serialPortConfiguration);
    }

    protected SerialPortConfiguration createSerialConfiguration(String comPortName, TypedProperties properties) {
        for (String propertyName : properties.propertyNames()) {
            this.setProperty(propertyName, properties.getProperty(propertyName));
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
    protected PropertySpec baudRatePropertySpec() {
        return this.bigDecimalSpec(SerialPortConfiguration.BAUDRATE_NAME, true, BaudrateValue.BAUDRATE_57600.getBaudrate(),
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
                BaudrateValue.BAUDRATE_460800.getBaudrate());
    }

    /**
     * SerialIO does not support 1,5 stopBits
     *
     * @return the property spec for the SerialIO nrOfStopBits property
     */
    @Override
    protected PropertySpec nrOfStopBitsPropertySpec() {
        return this.bigDecimalSpec(SerialPortConfiguration.NR_OF_STOP_BITS_NAME, true, NrOfStopBits.ONE.getNrOfStopBits(), NrOfStopBits.ONE.getNrOfStopBits(), NrOfStopBits.TWO.getNrOfStopBits());
    }

    @Override
    protected BaudrateValue getBaudRateValue() {
        return BaudrateValue.valueFor((BigDecimal) getProperty(SerialPortConfiguration.BAUDRATE_NAME));
    }
}
