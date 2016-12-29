/*
 * EnermetE70x.java
 *
 * Created on 30 september 2004, 16:04
 */

package com.energyict.protocolimpl.sctm.enermete70x;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.metcom.Metcom2;
import com.energyict.protocolimpl.sctm.base.GenericRegisters;

import java.io.IOException;
/**
 *
 * @author  Koen
 * @beginchanges
KV|30092004|Initial version
KV|15022005|Changed RegisterConfig to allow B field obiscodes != 1
KV|15022005|bugfix  RegisterConfig
KV|07032005|changes for setTime and use of 8 character SCTM ID
KV|17032005|Minor bugfixes and improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
KV|23092005|Changed intervalstate bits behaviour (EDP)
 * @endchanges
 */

public class EnermetE70x extends Metcom2 implements RegisterProtocol {

    private RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset
    private GenericRegisters genericRegisters;

    public EnermetE70x(PropertySpecService propertySpecService) {
        super(propertySpecService);
        genericRegisters = new GenericRegisters(this);
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
            ObisCodeMapper ocm = new ObisCodeMapper(getDumpData(obisCode.getB()-1),getTimeZone(),regs);
            return ocm.getRegisterValue(obisCode);
        }
    }

    @Override
    public String getRegistersInfo(int extendedLogging) throws IOException {
        return regs.getRegisterInfo()+"\n"+genericRegisters.getRegisterInfo();
    }

}