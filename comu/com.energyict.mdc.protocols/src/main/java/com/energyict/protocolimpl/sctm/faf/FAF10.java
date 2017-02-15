/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.faf;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.metcom.Metcom3FAF;

import javax.inject.Inject;
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
public class FAF10 extends Metcom3FAF implements RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "L&G FAF10 SCTM";
    }

    FAF10Registers fafRegisters=null;

    @Inject
    public FAF10(PropertySpecService propertySpecService) {
        super(propertySpecService);
        fafRegisters = new FAF10Registers(this);
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }


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

    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e
    *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        //return ObisCodeMapper.getRegisterInfo(obisCode);
        return fafRegisters.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        //ObisCodeMapper ocm = new ObisCodeMapper(getDumpData(),getTimeZone(),regs);
        //return ocm.getRegisterValue(obisCode);
        return fafRegisters.readRegisterValue(obisCode);
    }

    public String getRegistersInfo(int extendedLogging) throws IOException {
        //return regs.getRegisterInfo();
        return fafRegisters.getRegisterInfo();
    }

}
