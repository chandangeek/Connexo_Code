/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.mtt3a;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.metcom.Metcom3;
import com.energyict.protocolimpl.sctm.base.GenericRegisters;

import java.io.IOException;
import java.util.ArrayList;
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
   
    RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset
    GenericRegisters genericRegisters;
    
            
    /** Creates a new instance of MTT3A */ 
    public MTT3A() {
        genericRegisters = new GenericRegisters(this);
    }

    public String getProtocolVersion() {
        return "$Date$";
    }    

    public List getOptionalKeys() { 
        List result = new ArrayList(); 
        result.add("Timeout");
        result.add("Retries");
        result.add("HalfDuplex");
        result.add("ChannelMap");
        result.add("ExtendedLogging");
        result.add("RemovePowerOutageIntervals");
        result.add("LogBookReadCommand");
        result.add("ForcedDelay");
        result.add("AutoBillingPointNrOfDigits");
        result.add("TimeSetMethod");
        return result;
    }
    
    
    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e 
    *******************************************************************************************/
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        if (genericRegisters.isManufacturerSpecific(obisCode))
            return genericRegisters.getRegisterInfo(obisCode);
        else
            return ObisCodeMapper.getRegisterInfo(obisCode);
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
