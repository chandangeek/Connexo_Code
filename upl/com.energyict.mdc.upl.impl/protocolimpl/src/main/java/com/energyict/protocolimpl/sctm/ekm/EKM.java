/*
 * EKM.java
 *
 * Created on 30 september 2004, 16:04
 */

package com.energyict.protocolimpl.sctm.ekm;

import com.energyict.protocolimpl.siemens7ED62.*;
import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.*;
import java.util.logging.*;
import com.energyict.cbo.*;
import com.energyict.protocolimpl.metcom.Metcom2;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.customerconfig.*;
import com.energyict.protocolimpl.sctm.base.GenericRegisters;
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
KV|06042006|Add IntervalStatusBehaviour custom property to correct power fail status
 * @endchanges
 */
//com.energyict.protocolimpl.sctm.enermete70x.EKM
public class EKM extends Metcom2 implements RegisterProtocol {
    

    RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset
    GenericRegisters genericRegisters;
    
    /** Creates a new instance of Metcom2 */
    public EKM() {
        genericRegisters = new GenericRegisters(this);
    }
 
    public List getOptionalKeys() { 
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("HalfDuplex");
        result.add("ExtendedLogging");
        result.add("RemovePowerOutageIntervals");
        result.add("LogBookReadCommand");
        result.add("ForcedDelay");
        result.add("IntervalStatusBehaviour");
        result.add("AutoBillingPointNrOfDigits");
        result.add("TimeSetMethod");
        return result;
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.17 $";
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
