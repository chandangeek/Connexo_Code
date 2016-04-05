/*
 * MTT3A.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.sctm.fbc;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.metcom.Metcom3FBC;

import java.io.IOException;
import java.util.ArrayList;
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
 * @endchanges
 */
public class FBC extends Metcom3FBC implements RegisterProtocol {
   
    FBCRegisters fbcRegisters;
    
    /** Creates a new instance of MTT3A */ 
    public FBC() {
        fbcRegisters = new FBCRegisters(this);
    }

    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
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
