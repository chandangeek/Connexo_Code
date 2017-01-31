/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.fcl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.metcom.Metcom3FCL;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author  Koen
 * @beginchanges
KV|18032004|add ChannelMap
KV|07052004|Extend for multibuffer with more then 1 channel per buffer. Also extend ChannelMap
KV|07032005|changes for setTime and use of 8 character SCTM ID
KV|17032005|Minor bugfixes and improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032007|Add support for FCM3 and FCR1.4W (Cegedel project)
 * @endchanges
 */
public class FCL extends Metcom3FCL implements RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "L&G FCL SCTM";
    }

    FCLRegisters fclRegisters;

    @Inject
    public FCL(PropertySpecService propertySpecService) {
        super(propertySpecService);
        fclRegisters = new FCLRegisters(this);
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
        return fclRegisters.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        //ObisCodeMapper ocm = new ObisCodeMapper(getDumpData(),getTimeZone(),regs);
        //return ocm.getRegisterValue(obisCode);
        return fclRegisters.readRegisterValue(obisCode);
    }

    public String getRegistersInfo(int extendedLogging) throws IOException {
        //return regs.getRegisterInfo();
        return fclRegisters.getRegisterInfo();
    }

}