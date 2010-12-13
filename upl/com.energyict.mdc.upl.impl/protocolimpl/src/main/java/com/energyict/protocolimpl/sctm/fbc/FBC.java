/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.fbc;

import com.energyict.protocolimpl.siemens7ED62.*;
import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.*;
import java.util.logging.*;
import com.energyict.cbo.*;
import com.energyict.protocolimpl.metcom.Metcom3FBC;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.customerconfig.*;

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
   
    FBCRegisters fbcRegisters;
    
    /** Creates a new instance of MTT3A */ 
    public FBC() {
        fbcRegisters = new FBCRegisters(this);
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
        result.add("TimeSetMethod");
        return result;
    }        
    
    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e 
    *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        //return ObisCodeMapper.getRegisterInfo(obisCode);
        return fbcRegisters.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        //ObisCodeMapper ocm = new ObisCodeMapper(getDumpData(),getTimeZone(),regs);
        //return ocm.getRegisterValue(obisCode);
        return fbcRegisters.readRegisterValue(obisCode);
    }
    
    public String getRegistersInfo(int extendedLogging) throws IOException {
        //return regs.getRegisterInfo();
        return fbcRegisters.getRegisterInfo();
    }   

    
}
