/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.fbc;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.metcom.Metcom3FBC;

import java.io.IOException;

/**
 *
 * @author  Koen
 * @beginchanges
KV|18032004|add ChannelMap
KV|07052004|Extend for multibuffer with more then 1 channel per buffer. Also extend ChannelMap
KV|07032005|changes for setTime and use of 8 character SCTM ID
KV|17032005|Minor bugfixes and improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
 * @endchanges
 */
public class FBC extends Metcom3FBC implements RegisterProtocol {

    private final FBCRegisters fbcRegisters;

    public FBC(PropertySpecService propertySpecService) {
        super(propertySpecService);
        fbcRegisters = new FBCRegisters(this);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return fbcRegisters.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return fbcRegisters.readRegisterValue(obisCode);
    }

    @Override
    public String getRegistersInfo(int extendedLogging) throws IOException {
        return fbcRegisters.getRegisterInfo();
    }

}