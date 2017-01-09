package com.energyict.mdc.channels.serial.direct.rxtx;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.AbstractSerialConnectionType;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link com.energyict.mdc.io.ConnectionType} interface for Serial communication.
 * <p>
 * Copyrights EnergyICT
 * Date: 13/08/12
 * Time: 11:14
 */
@XmlRootElement
public class RxTxSerialConnectionType extends AbstractSerialConnectionType {

    @Override
    public ComChannel connect() throws ConnectionException {

        SerialPortConfiguration serialPortConfiguration = new SerialPortConfiguration(getComPortName(getAllProperties()), getBaudRateValue(), getNrOfDataBitsValue(),
                getNrOfStopBitsValue(), getParityValue(), getFlowControlValue());

        if (getPortOpenTimeOutValue() != null) {
            serialPortConfiguration.setSerialPortOpenTimeOut(getPortOpenTimeOutValue());
        }
        if (getPortReadTimeOutValue() != null) {
            serialPortConfiguration.setSerialPortReadTimeOut(getPortReadTimeOutValue());
        }

        ComChannel comChannel = newRxTxSerialConnection(serialPortConfiguration);
        comChannel.addProperties(createTypeProperty(ComChannelType.SerialComChannel));
        return comChannel;
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
        return UPLPropertySpecFactory.string(SerialPortConfiguration.FLOW_CONTROL_NAME, false,
                FlowControl.NONE.getFlowControl(),
                FlowControl.RTSCTS.getFlowControl(), FlowControl.XONXOFF.getFlowControl());
    }

}