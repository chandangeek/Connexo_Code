/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.mtt3a;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.metcom.Metcom3;
import com.energyict.protocolimpl.sctm.base.GenericRegisters;

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
KV|15022005|Changed RegisterConfig to allow B field obiscodes != 1
KV|15022005|bugfix  RegisterConfig
KV|07032005|changes for setTime and use of 8 character SCTM ID
KV|17032005|Minor bugfixes and improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
KV|23092005|Changed intervalstate bits behaviour (EDP)
KV|20042007|Fix registerreading
KV|14112007|Password mechanism
 * @endchanges
 */
public class MTT3A extends Metcom3 implements RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "L&G MTT3A SCTM";
    }

    RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset
    GenericRegisters genericRegisters;


    @Inject
    public MTT3A(PropertySpecService propertySpecService) {
        super(propertySpecService);
        genericRegisters = new GenericRegisters(this);
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
                    "AutoBillingPointNrOfDigits",
                    "TimeSetMethod",
                    "Software7E1");
    }

    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e
    *******************************************************************************************/

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        if (genericRegisters.isManufacturerSpecific(obisCode)) {
            return genericRegisters.getRegisterInfo(obisCode);
        }
        else {
            return ObisCodeMapper.getRegisterInfo(obisCode);
        }
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (genericRegisters.isManufacturerSpecific(obisCode)) {
            return genericRegisters.readRegisterValue(obisCode);
        }
        else {
            ObisCodeMapper ocm = new ObisCodeMapper(getDumpData(obisCode.getB()-1),getTimeZone(),regs,getAutoBillingPointNrOfDigits());
            return ocm.getRegisterValue(obisCode);
        }
    }

    public String getRegistersInfo(int extendedLogging) throws IOException {
        return regs.getRegisterInfo()+"\n"+genericRegisters.getRegisterInfo();
    }
}
