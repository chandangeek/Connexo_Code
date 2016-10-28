/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.fag;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.metcom.Metcom3FAG;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author  Koen
 * @beginchanges
KV|07032005|changes for setTime and use of 8 character SCTM ID
KV|17032005|Minor bugfixes and improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
 * @endchanges
 */
public class FAG extends Metcom3FAG implements RegisterProtocol {

    FAGRegisters fagRegisters;

    /** Creates a new instance of MTT3A */
    public FAG() {
        fagRegisters = new FAGRegisters(this);
    }

    /**
     * The Protocol version
     */
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
     public List<String> getOptionalKeys() {
        return Arrays.asList(
                    "Timeout",
                    "Retries",
                    "HalfDuplex",
                    "ChannelMap",
                    "ExtendedLogging",
                    "RemovePowerOutageIntervals",
                    "LogBookReadCommand",
                    "ForcedDelay",
                    "TimeSetMethod",
                    "Software7E1");
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return fagRegisters.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return fagRegisters.readRegisterValue(obisCode);
    }

    public String getRegistersInfo(int extendedLogging) throws IOException {
        return fagRegisters.getRegisterInfo();
    }

}
