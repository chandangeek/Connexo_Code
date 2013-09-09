package com.energyict.mdc.channels.serial.direct.rxtx;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.dynamicattributes.StringFactory;
import com.energyict.mdc.channels.serial.AbstractSerialConnectionType;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType} interface for Serial communication.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/08/12
 * Time: 11:14
 */
public class RxTxSerialConnectionType extends AbstractSerialConnectionType {

    @Override
    public boolean isRequiredProperty(String name) {
        return SerialPortConfiguration.NR_OF_STOP_BITS_NAME.equals(name) || SerialPortConfiguration.PARITY_NAME.equals(name)
                || SerialPortConfiguration.BAUDRATE_NAME.equals(name) || SerialPortConfiguration.NR_OF_DATA_BITS_NAME.equals(name);
    }

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        for (ConnectionTaskProperty property : properties) {
            this.setProperty(property.getName(), property.getValue());
        }
        SerialPortConfiguration serialPortConfiguration = new SerialPortConfiguration(comPort.getName(), getBaudRateValue(), getNrOfDataBitsValue(),
                getNrOfStopBitsValue(), getParityValue(), getFlowControlValue());

        if (getPortOpenTimeOutValue() != null) {
            serialPortConfiguration.setSerialPortOpenTimeOut(getPortOpenTimeOutValue());
        }
        if (getPortReadTimeOutValue() != null) {
            serialPortConfiguration.setSerialPortReadTimeOut(getPortReadTimeOutValue());
        }

        return newRxTxSerialConnection(serialPortConfiguration);
    }

    @Override
    public String getVersion() {
        return "$Date: 2012-11-20 13:54:41 +0100 (di, 20 nov 2012) $";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>(4);
        propertySpecs.add(this.baudRatePropertySpec());
        propertySpecs.add(this.nrOfStopBitsPropertySpec());
        propertySpecs.add(this.parityPropertySpec());
        propertySpecs.add(this.nrOfDataBitsPropertySpec());
        return propertySpecs;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>(1);
        propertySpecs.add(this.flowControlPropertySpec());
        return propertySpecs;
    }

    /**
     * The RxTx library does not support the DSR/DTR flow out-of-the-box
     *
     * @return the property spec fo the RxTx flowControl property
     */
    @Override
    protected PropertySpec<String> flowControlPropertySpec() {
        return PropertySpecBuilder.
                forClass(String.class, new StringFactory()).
                name(SerialPortConfiguration.FLOW_CONTROL_NAME).
                markExhaustive().
                setDefaultValue(FlowControl.NONE.getFlowControl()).
                addValues(FlowControl.RTSCTS.getFlowControl(), FlowControl.XONXOFF.getFlowControl()).
                finish();
    }

}