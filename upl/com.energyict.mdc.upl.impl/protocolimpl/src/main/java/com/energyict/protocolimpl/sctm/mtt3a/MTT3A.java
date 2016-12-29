/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.mtt3a;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.metcom.Metcom3;
import com.energyict.protocolimpl.sctm.base.GenericRegisters;

import java.io.IOException;
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

    private final RegisterConfig regs;
    private final GenericRegisters genericRegisters;

    public MTT3A(PropertySpecService propertySpecService) {
        super(propertySpecService);
        genericRegisters = new GenericRegisters(this);
        regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        if (genericRegisters.isManufacturerSpecific(obisCode)) {
            return genericRegisters.getRegisterInfo(obisCode);
        } else {
            return ObisCodeMapper.getRegisterInfo(obisCode);
        }
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (genericRegisters.isManufacturerSpecific(obisCode)) {
            return genericRegisters.readRegisterValue(obisCode);
        }
        else {
            ObisCodeMapper ocm = new ObisCodeMapper(getDumpData(obisCode.getB()-1),getTimeZone(),regs,getAutoBillingPointNrOfDigits());
            return ocm.getRegisterValue(obisCode);
        }
    }

    @Override
    public String getRegistersInfo(int extendedLogging) throws IOException {
        return regs.getRegisterInfo()+"\n"+genericRegisters.getRegisterInfo();
    }

}