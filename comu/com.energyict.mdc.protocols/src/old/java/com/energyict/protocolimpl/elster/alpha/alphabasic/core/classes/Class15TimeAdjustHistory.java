/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Class15TimeAdjustHistory.java
 *
 * Created on 25 juli 2005, 11:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;

import java.io.IOException;
import java.util.Date;
/**
 *
 * @author koen
 */
public class Class15TimeAdjustHistory extends AbstractClass {

    ClassIdentification classIdentification = new ClassIdentification(15,56,false);


    private Date TDPRIL; // 6 bytes time/date prior to latest time adjust
    private Date TDAFTL; // 6 bytes time/date after latest time adjust
    private int APARTP; // 2 bytes chan A partial count prior to time adj
    private int APARTA; // 2 bytes chan A partial count after time adj
    private int BPARTP; // 2 bytes chan B partial count prior to time adj
    private int BPARTA; // 2 bytes chan B partial count after time adj
    private int CPARTP; // 2 bytes chan C partial count prior to time adj
    private int CPARTA; // 2 bytes chan C partial count after time adj
    private int DPARTP; // 2 bytes chan D partial count prior to time adj
    private int DPARTA; // 2 bytes chan D partial count after time adj
    // spares 16 bytes set to zero
    private Date TDPRI2;  // 6 bytes time/date prior to previous time adj
    private Date TDAFT2;  // 6 bytes time/date after previous time adj

    /** Creates a new instance of Class15EventLogConfiguration */
    public Class15TimeAdjustHistory(ClassFactory classFactory) {
        super(classFactory);
    }

    public String toString() {
        return "Class15TimeAdjustHistory: TDPRIL="+getTDPRIL()+", TDAFTL="+getTDAFTL()+", APARTP="+getAPARTP()+", APARTA="+getAPARTA()+", BPARTP="+getBPARTP()+", BPARTA="+getBPARTA()+", CPARTP="+getCPARTP()+", CPARTA="+getCPARTA()+", DPARTP="+getDPARTP()+", DPARTA="+getDPARTA()+", TDPRI2="+getTDPRI2()+", TDAFT2="+getTDAFT2();
    }

    protected void parse(byte[] data) throws IOException {
        setTDPRIL(ClassParseUtils.getDate6(data, 0, getClassFactory().getAlpha().getTimeZone()));
        setTDAFTL(ClassParseUtils.getDate6(data, 6, getClassFactory().getAlpha().getTimeZone()));
        setAPARTP(ProtocolUtils.getInt(data,12, 2));
        setAPARTA(ProtocolUtils.getInt(data,14, 2));
        setBPARTP(ProtocolUtils.getInt(data,16, 2));
        setBPARTA(ProtocolUtils.getInt(data,18, 2));
        setCPARTP(ProtocolUtils.getInt(data,20, 2));
        setCPARTA(ProtocolUtils.getInt(data,22, 2));
        setDPARTP(ProtocolUtils.getInt(data,24, 2));
        setDPARTA(ProtocolUtils.getInt(data,26, 2));
        // spare 16 bytes
        setTDPRI2(ClassParseUtils.getDate6(data, 44, getClassFactory().getAlpha().getTimeZone()));
        setTDAFT2(ClassParseUtils.getDate6(data, 50, getClassFactory().getAlpha().getTimeZone()));

    }



    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }

    public Date getTDPRIL() {
        return TDPRIL;
    }

    public void setTDPRIL(Date TDPRIL) {
        this.TDPRIL = TDPRIL;
    }

    public Date getTDAFTL() {
        return TDAFTL;
    }

    public void setTDAFTL(Date TDAFTL) {
        this.TDAFTL = TDAFTL;
    }

    public int getAPARTP() {
        return APARTP;
    }

    public void setAPARTP(int APARTP) {
        this.APARTP = APARTP;
    }

    public int getAPARTA() {
        return APARTA;
    }

    public void setAPARTA(int APARTA) {
        this.APARTA = APARTA;
    }

    public int getBPARTP() {
        return BPARTP;
    }

    public void setBPARTP(int BPARTP) {
        this.BPARTP = BPARTP;
    }

    public int getBPARTA() {
        return BPARTA;
    }

    public void setBPARTA(int BPARTA) {
        this.BPARTA = BPARTA;
    }

    public int getCPARTP() {
        return CPARTP;
    }

    public void setCPARTP(int CPARTP) {
        this.CPARTP = CPARTP;
    }

    public int getCPARTA() {
        return CPARTA;
    }

    public void setCPARTA(int CPARTA) {
        this.CPARTA = CPARTA;
    }

    public int getDPARTP() {
        return DPARTP;
    }

    public void setDPARTP(int DPARTP) {
        this.DPARTP = DPARTP;
    }

    public int getDPARTA() {
        return DPARTA;
    }

    public void setDPARTA(int DPARTA) {
        this.DPARTA = DPARTA;
    }

    public Date getTDPRI2() {
        return TDPRI2;
    }

    public void setTDPRI2(Date TDPRI2) {
        this.TDPRI2 = TDPRI2;
    }

    public Date getTDAFT2() {
        return TDAFT2;
    }

    public void setTDAFT2(Date TDAFT2) {
        this.TDAFT2 = TDAFT2;
    }

}
