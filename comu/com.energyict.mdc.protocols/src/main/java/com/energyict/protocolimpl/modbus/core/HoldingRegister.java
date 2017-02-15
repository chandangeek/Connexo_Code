/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HoldingRegister.java
 *
 * Created on 30 maart 2007, 16:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

/**
 *
 * @author Koen
 */
public class HoldingRegister extends AbstractRegister {

    public HoldingRegister(int reg,int range) {
        super(reg,range,null,"");
    }

    public HoldingRegister(int reg,int range,String name) {
        super(reg,range,null,name);
    }

    public HoldingRegister(int reg,int range,ObisCode obisCode) {
       super(reg,range,obisCode,obisCode.getUnitElectricity(0),obisCode.getDescription());
    }

    public HoldingRegister(int reg,int range,ObisCode obisCode,String name) {
       super(reg,range,obisCode, Unit.get(""),name);
    }

    public HoldingRegister(int reg,int range,ObisCode obisCode,Unit unit) {
       super(reg,range,obisCode,unit,obisCode.getDescription());
    }

    public HoldingRegister(int reg,int range,ObisCode obisCode,Unit unit,String name) {
        super(reg,range,obisCode,unit,name);
    }



} // public class HoldingRegister extends AbstractRegister
