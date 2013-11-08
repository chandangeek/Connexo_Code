/*
 * ObisCodeMapper.java
 *
 * Created on 16 november 2005, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.cbo.*;
import com.energyict.protocolimpl.base.ObisUtils;
import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocolimpl.ansi.c12.tables.*;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {
    
    Sentinel sentinel;
    
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(Sentinel sentinel) {
        this.sentinel=sentinel;
    }
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException { 
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode, true);
    }
    
    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        if (read) {
            try {
               return sentinel.getObisCodeInfoFactory().getRegister(obisCode);
            }
            catch(ResponseIOException e) {
                if (e.getReason()==AbstractResponse.IAR) // table does not exist!
                   throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! ("+e.toString()+")");
                else 
                   throw e;
            }
        }
        else {
            return sentinel.getObisCodeInfoFactory().getRegisterInfo(obisCode);
        }
    }
}
