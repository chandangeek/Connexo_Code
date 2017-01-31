package com.energyict.mdc.channels.serial.direct.rxtx;

import com.energyict.mdc.channels.serial.AbstractSerialConnectionType;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link ConnectionType} interface for Serial communication.
 * <p>
 * Copyrights EnergyICT
 * Date: 13/08/12
 * Time: 11:14
 */
@XmlRootElement
public class RxTxSerialConnectionType extends AbstractSerialConnectionType {

    public RxTxSerialConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SerialPortComChannel connect() throws ConnectionException {

        SerialPortConfiguration serialPortConfiguration = new SerialPortConfiguration(getComPortName(getAllProperties()), getBaudRateValue(), getNrOfDataBitsValue(),
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

    /**
     * The RxTx library does not support the DSR/DTR flow out-of-the-box
     *
     * @return the property spec fo the RxTx flowControl property
     */
    protected PropertySpec flowControlPropertySpec() {
        return this.stringSpec(SerialPortConfiguration.FLOW_CONTROL_NAME, false,
                FlowControl.NONE.getFlowControl(),
                FlowControl.RTSCTS.getFlowControl(), FlowControl.XONXOFF.getFlowControl());
    }

}