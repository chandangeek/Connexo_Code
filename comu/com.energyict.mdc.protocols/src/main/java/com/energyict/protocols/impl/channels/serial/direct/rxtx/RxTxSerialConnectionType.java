/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial.direct.rxtx;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.serial.AbstractSerialConnectionType;

import java.util.List;

public abstract class RxTxSerialConnectionType extends AbstractSerialConnectionType {

    protected RxTxSerialConnectionType(String serialComponentServiceId, SerialComponentService serialComponentService, Thesaurus thesaurus) {
        super(serialComponentServiceId, serialComponentService, thesaurus);
    }

    @Override
    public SerialComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
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
        return newRxTxSerialConnection(this.getSerialComponentService(), serialPortConfiguration);
    }

}