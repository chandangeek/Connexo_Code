package com.energyict.protocols.mdc.channels.serial.direct.rxtx;

import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.PropertySpecBuilder;
import com.energyict.mdc.protocol.dynamic.StringFactory;
import com.energyict.protocols.mdc.channels.serial.AbstractSerialConnectionType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType} interface for Serial communication.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/08/12
 * Time: 11:14
 */
public class RxTxSerialConnectionType extends AbstractSerialConnectionType {

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        for (ConnectionProperty property : properties) {
            this.setProperty(property.getName(), property.getValue());
        }
        SerialPortConfiguration serialPortConfiguration =
                new SerialPortConfiguration(
                        this.getComPortNameValue(),
                        getBaudRateValue(),
                        getNrOfDataBitsValue(),
                        getNrOfStopBitsValue(),
                        getParityValue(),
                        getFlowControlValue());
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
    protected PropertySpec<BigDecimal> baudRatePropertySpec () {
        return this.baudRatePropertySpec(true);
    }

    @Override
    protected PropertySpec<String> parityPropertySpec () {
        return this.parityPropertySpec(true);
    }

    @Override
    protected PropertySpec<BigDecimal> nrOfStopBitsPropertySpec () {
        return this.nrOfStopBitsPropertySpec(true);
    }

    @Override
    protected PropertySpec<BigDecimal> nrOfDataBitsPropertySpec () {
        return this.nrOfDataBitsPropertySpec(true);
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