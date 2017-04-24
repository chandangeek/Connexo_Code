/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IntegrationConstant.java
 *
 * Created on 11 oktober 2006, 9:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class IntegrationConstant {

    static List list = new ArrayList();
    static {

        list.add(new IntegrationConstant(0,"69.28",10,"0.025","0.060","0.025"));
        list.add(new IntegrationConstant(1,"69.28",20,"0.025","0.060","0.025"));
        list.add(new IntegrationConstant(2,"120",10,"0.025","0.060","0.025"));
        list.add(new IntegrationConstant(3,"120",20,"0.050","0.060","0.025"));
        list.add(new IntegrationConstant(4,"240",10,"0.050","0.240","0.025"));
        list.add(new IntegrationConstant(5,"240",20,"0.100","0.240","0.025"));
        list.add(new IntegrationConstant(6,"277",10,"0.050","0.480","0.025"));
        list.add(new IntegrationConstant(7,"277",20,"0.100","0.480","0.025"));
        list.add(new IntegrationConstant(8,"120",10,"0.025","0.060","0.025"));
        list.add(new IntegrationConstant(9,"120",20,"0.050","0.060","0.025"));
        list.add(new IntegrationConstant(10,"240",10,"0.050","0.240","0.025"));
        list.add(new IntegrationConstant(11,"240",20,"0.100","0.240","0.025"));
        list.add(new IntegrationConstant(12,"480",10,"0.100","0.960","0.025"));
        list.add(new IntegrationConstant(13,"480",20,"0.200","0.960","0.025"));
        list.add(new IntegrationConstant(14,"69",10,"0.050","0.060","0.025"));
        list.add(new IntegrationConstant(15,"69",20,"0.050","0.060","0.025"));
        list.add(new IntegrationConstant(16,"120",10,"0.050","0.060","0.025"));
        list.add(new IntegrationConstant(17,"120",20,"0.100","0.060","0.025"));
        list.add(new IntegrationConstant(18,"240",10,"0.100","0.240","0.025"));
        list.add(new IntegrationConstant(19,"240",20,"0.200","0.240","0.025"));
        list.add(new IntegrationConstant(20,"277",10,"0.100","0.480","0.025"));
        list.add(new IntegrationConstant(21,"277",20,"0.200","0.480","0.025"));
        list.add(new IntegrationConstant(22,"240",10,"0.050","0.480","0.025"));
        list.add(new IntegrationConstant(23,"240",20,"0.100","0.480","0.025"));
        list.add(new IntegrationConstant(24,"480",10,"0.100","1.920","0.025"));
        list.add(new IntegrationConstant(25,"480",20,"0.200","1.920","0.025"));
    }

    private int offset;
    private BigDecimal voltage;
    private int classId;
    private BigDecimal demandAndEnergy;
    private BigDecimal voltSquare;
    private BigDecimal ampSquare;


    /** Creates a new instance of IntegrationConstant */
    private IntegrationConstant(int offset, String voltage, int classId, String demandAndEnergy, String voltSquare, String ampSquare) {
        this.setOffset(offset);
        this.setVoltage(new BigDecimal(voltage));
        this.setClassId(classId);
        this.setDemandAndEnergy(new BigDecimal(demandAndEnergy));
        this.setVoltSquare(new BigDecimal(voltSquare));
        this.setAmpSquare(new BigDecimal(ampSquare));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("IntegrationConstant:\n");
        strBuff.append("   ampSquare="+getAmpSquare()+"\n");
        strBuff.append("   classId="+getClassId()+"\n");
        strBuff.append("   demandAndEnergy="+getDemandAndEnergy()+"\n");
        strBuff.append("   offset="+getOffset()+"\n");
        strBuff.append("   voltSquare="+getVoltSquare()+"\n");
        strBuff.append("   voltage="+getVoltage()+"\n");
        return strBuff.toString();

    }


    static public IntegrationConstant findIntegrationConstants(int offset) throws IOException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            IntegrationConstant i = (IntegrationConstant)it.next();
            if (i.getOffset() == offset)
                return i;
        }

        throw new IOException("IntegrationConstants, findIntegrationConstants, invalid offset "+offset);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public BigDecimal getVoltage() {
        return voltage;
    }

    public void setVoltage(BigDecimal voltage) {
        this.voltage = voltage;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public BigDecimal getDemandAndEnergy() {
        return demandAndEnergy;
    }

    public void setDemandAndEnergy(BigDecimal demandAndEnergy) {
        this.demandAndEnergy = demandAndEnergy;
    }

    public BigDecimal getVoltSquare() {
        return voltSquare;
    }

    public void setVoltSquare(BigDecimal voltSquare) {
        this.voltSquare = voltSquare;
    }

    public BigDecimal getAmpSquare() {
        return ampSquare;
    }

    public void setAmpSquare(BigDecimal ampSquare) {
        this.ampSquare = ampSquare;
    }

}
