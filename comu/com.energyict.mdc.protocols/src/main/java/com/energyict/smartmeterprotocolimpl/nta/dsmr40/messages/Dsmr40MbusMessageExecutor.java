package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.MeterData;
import com.energyict.mdc.protocol.api.device.data.MeterDataMessageResult;
import com.energyict.mdc.protocol.api.device.data.MeterReadingData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 17/04/13
 * Time: 17:37
 * Author: khe
 */
public class Dsmr40MbusMessageExecutor extends Dsmr23MbusMessageExecutor {


    public Dsmr40MbusMessageExecutor(final AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    /**
     * DSMR4.0 adds support for load profiles with channels that have the same obiscode but a different unit.
     * E.g.: gas value (attr 2) and gas capture time (attr 5), both come from the same extended register but are stored in 2 individual channels.
     * <p/>
     * They should be stored in 1 register only in EiServer, gas capture time is stored as event timestamp of this register.
     */
    protected MessageResult doReadLoadProfileRegisters(final MessageEntry msgEntry) {
        MessageResult messageResult = super.doReadLoadProfileRegisters(msgEntry);
        return new LoadProfileToRegisterParser().parse(messageResult);
    }
}