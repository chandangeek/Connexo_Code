/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.faf;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.metcom.BufferStructure;
import com.energyict.protocolimpl.metcom.Metcom3FAF;

import java.io.IOException;

/**
 *
 * @author  Koen
 * @beginchanges
KV|07032005|changes for setTime and use of 8 character SCTM ID
KV|17032005|Minor bugfixes and improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
 * @endchanges
 */
public class FAF20 extends Metcom3FAF implements RegisterProtocol {

    private final FAF20Registers fafRegisters;

    public FAF20(PropertySpecService propertySpecService) {
        super(propertySpecService);
        fafRegisters = new FAF20Registers(this);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    protected BufferStructure getBufferStructure(int bufferNr) throws IOException {
        byte[] data = getRegister(REG_PROFILEINTERVAL).getBytes();
        int tm[] = new int[2];
        tm[0] = Integer.parseInt((new String(ProtocolUtils.getSubArray2(data, 0, 12))).trim());
        tm[1] = Integer.parseInt((new String(ProtocolUtils.getSubArray2(data, 12, 4))).trim());
        int profileInterval = tm[bufferNr];
        int digitsPerValue = Integer.parseInt(getRegister(DIGITS_PER_VALUE).trim());
        return new BufferStructure(getChannelMap().getBuffers()[bufferNr],digitsPerValue,profileInterval);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return fafRegisters.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return fafRegisters.readRegisterValue(obisCode);
    }

    @Override
    public String getRegistersInfo(int extendedLogging) throws IOException {
        return fafRegisters.getRegisterInfo();
    }

}