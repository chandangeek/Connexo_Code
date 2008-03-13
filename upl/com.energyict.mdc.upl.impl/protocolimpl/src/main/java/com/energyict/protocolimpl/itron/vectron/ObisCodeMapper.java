/*
 * ObisCodeMapper.java
 *
 * Created on 15 september 2006, 13:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron;

import com.energyict.cbo.*;
import com.energyict.protocolimpl.itron.vectron.basepages.*;
import java.util.*;
import java.io.*;
import java.math.BigDecimal;

import com.energyict.obis.*;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {
    
    Vectron vectron;
    
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(Vectron vectron) {
        this.vectron=vectron;
    }
    
    
    public String getRegisterInfo() throws IOException {
        return  vectron.getRegisterFactory().getRegisterInfo();
    }
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }
    
    public RegisterValue getRegisterValue(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        Register register = vectron.getRegisterFactory().findRegisterByObisCode(obisCode);
        RegisterBasePage rbp = ((BasePagesFactory)vectron.getBasePagesFactory()).getRegisterBasePage(register);
        
        //System.out.println("KV_DEBUG> "+rbp);
        
        return new RegisterValue(register.getObisCode(),rbp.getQuantity(),rbp.getDate(),rbp.getSelfReadDate());
    }  
    

}
