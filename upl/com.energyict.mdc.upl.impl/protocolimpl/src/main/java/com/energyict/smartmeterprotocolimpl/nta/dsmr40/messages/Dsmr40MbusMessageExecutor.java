package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.protocol.MessageResult;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor;

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