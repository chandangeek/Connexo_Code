/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Register.java
 *
 * Created on 15 september 2006, 9:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.mdc.common.ObisCode;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Register {

    private int index;
    private ObisCode obisCode;
    private UnitTable unitTable;

    public Register(int index, ObisCode obisCode, UnitTable unitTable) {
        this.setIndex(index);
        this.setObisCode(obisCode);
        this.setUnitTable(unitTable);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Register: 0x"+Integer.toHexString(getIndex())+", "+getUnitTable()+", "+getObisCode()+"\n");
        return strBuff.toString();
    }

    public int getLength() throws IOException {
        return 6;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public UnitTable getUnitTable() {
        return unitTable;
    }

    public void setUnitTable(UnitTable unitTable) {
        this.unitTable = unitTable;
    }


}
