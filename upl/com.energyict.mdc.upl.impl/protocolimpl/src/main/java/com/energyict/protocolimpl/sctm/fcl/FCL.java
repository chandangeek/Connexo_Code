/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.fcl;

import com.energyict.protocolimpl.siemens7ED62.*;
import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.*;
import java.util.logging.*;
import com.energyict.cbo.*;
import com.energyict.protocolimpl.metcom.Metcom3FCL;

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
KV|30032007|Add support for FCM3 and FCR1.4W (Cegedel project)
 * @endchanges
 */
public class FCL extends Metcom3FCL implements RegisterProtocol {
   
    FCLRegisters fclRegisters;
    
    /** Creates a new instance of MTT3A */ 
    public FCL() {
        fclRegisters = new FCLRegisters(this);
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
        result.add("Software7E1");
        return result;
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
