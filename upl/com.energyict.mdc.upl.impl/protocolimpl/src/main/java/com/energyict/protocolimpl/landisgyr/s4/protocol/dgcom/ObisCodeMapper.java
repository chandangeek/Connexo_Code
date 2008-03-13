/*
 * ObisCodeMapper.java
 *
 * Created on 16 november 2005, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom;

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
    
    S4 s4;
    
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(S4 s4) {
        this.s4=s4;
    }
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode, true);
    }
    
    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        if (read) {
            return s4.getRegisterMapperFactory().getRegisterMapper().getRegisterValue(obisCode);
        }
        else {
            return obisCode.getDescription();
        }
    }
}
