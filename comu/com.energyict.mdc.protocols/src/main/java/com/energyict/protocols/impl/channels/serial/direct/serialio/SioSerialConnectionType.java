/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial.direct.serialio;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.serial.AbstractSerialConnectionType;

import java.util.List;

public abstract class SioSerialConnectionType extends AbstractSerialConnectionType {

    public SioSerialConnectionType(String customPropertySetId, SerialComponentService serialComponentService, Thesaurus thesaurus) {
        super(customPropertySetId, serialComponentService, thesaurus);
    }

    @Override
    public SerialComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        SerialPortConfiguration serialPortConfiguration = createSerialConfiguration(properties);
        return newSioSerialConnection(this.getSerialComponentService(), serialPortConfiguration);
    }

    protected SerialPortConfiguration createSerialConfiguration(List<ConnectionProperty> properties) {
        for (ConnectionProperty property : properties) {
            this.setProperty(property.getName(), property.getValue());
        }
        return new SerialPortConfiguration(
                        this.getComPortNameValue(),
                        getBaudRateValue(),
                        getNrOfDataBitsValue(),
                        getNrOfStopBitsValue(),
                        getParityValue(),
                        getFlowControlValue());
    }

}