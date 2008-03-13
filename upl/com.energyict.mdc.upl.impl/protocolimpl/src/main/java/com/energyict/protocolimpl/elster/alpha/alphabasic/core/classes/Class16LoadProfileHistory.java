/*
 * Class16LoadProfileHistory.java
 *
 * Created on 25 juli 2005, 11:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.elster.alpha.core.connection.*;
import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.cbo.*;

/**
 *
 * @author koen
 */
public class Class16LoadProfileHistory extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(16,48,false);
    
    private Date PFSTRTL; // 6 bytes start time/date of most recent power outage
    private Date PFSTPL; // 6 bytes end time/date of most recent power outage
    private Date PFSTRT2; // 6 bytes start time/date of next most recent power outage
    private Date PFSTP2; // 6 bytes end time/date of next most recent power outage
    private Date OLPMRD; // 5 bytes time/date of last optical lp data read
    private Date RLPMRD; // 5 bytes time/date of last remote lp data read
    private int FULLRED; // 2 bytes number of lp words available for a full load profile read (class 17) 
    private int PARTRED; // 2 bytes number of lp words available for a partial load profile read (class 18)
    private int MAXLPM; // 2 bytes maximum number of 16 bit words available for load profile recording (reserved for future use)
    //spares 8 set to zero
    
    int validDates=0;
    
    /** Creates a new instance of Class16LoadProfileHistory */
    public Class16LoadProfileHistory(ClassFactory classFactory) {
        super(classFactory);
    }
    
    public String toString() {
        return "Class16LoadProfileHistory: PFSTRTL="+PFSTRTL+", PFSTPL="+PFSTPL+", PFSTRT2="+PFSTRT2+", PFSTP2="+PFSTP2+", OLPMRD="+OLPMRD+", RLPMRD="+RLPMRD+", FULLRED="+FULLRED+", PARTRED="+PARTRED+", MAXLPM="+MAXLPM;
    }
    
    protected void parse(byte[] data) throws IOException {
        if (ProtocolUtils.getLong(data,0,6) != 0) validDates |= 0x01;
        if (ProtocolUtils.getLong(data,6,6) != 0) validDates |= 0x02;
        if (ProtocolUtils.getLong(data,12,6) != 0) validDates |= 0x04;
        if (ProtocolUtils.getLong(data,18,6) != 0) validDates |= 0x08;
        //if (ProtocolUtils.getLong(data,24,5) != 0) validDates |= 0x10;
        //if (ProtocolUtils.getLong(data,29,5) != 0) validDates |= 0x20;
        setPFSTRTL(ClassParseUtils.getDate6(data, 0, getClassFactory().getAlpha().getTimeZone()));
        setPFSTPL(ClassParseUtils.getDate6(data, 6, getClassFactory().getAlpha().getTimeZone()));
        setPFSTRT2(ClassParseUtils.getDate6(data, 12, getClassFactory().getAlpha().getTimeZone()));
        setPFSTP2(ClassParseUtils.getDate6(data, 18, getClassFactory().getAlpha().getTimeZone()));
        setOLPMRD(ClassParseUtils.getDate5(data, 24, getClassFactory().getAlpha().getTimeZone()));
        setRLPMRD(ClassParseUtils.getDate5(data, 29, getClassFactory().getAlpha().getTimeZone()));
        setFULLRED(ProtocolUtils.getInt(data,34, 2));
        setPARTRED(ProtocolUtils.getInt(data,36, 2));   
        setMAXLPM(ProtocolUtils.getInt(data,38, 2));   
        
        
        
    }

    
    public List getMeterEvents() {
        List meterEvents = new ArrayList();
        if ((validDates & 0x01) == 0x01)
            meterEvents.add(new MeterEvent(getPFSTRTL(),MeterEvent.POWERDOWN));
        if ((validDates & 0x02) == 0x02)
            meterEvents.add(new MeterEvent(getPFSTPL(),MeterEvent.POWERUP));
        if ((validDates & 0x04) == 0x04)
            meterEvents.add(new MeterEvent(getPFSTRT2(),MeterEvent.POWERDOWN));
        if ((validDates & 0x08) == 0x08)
            meterEvents.add(new MeterEvent(getPFSTP2(),MeterEvent.POWERUP));
        return meterEvents;
    }
    
    protected void prepareBuild() throws IOException {
//        classIdentification.setLength(classFactory.getClass15EventLogConfiguration().getEVSIZE());
    }
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }

    public Date getPFSTRTL() {
        return PFSTRTL;
    }

    public void setPFSTRTL(Date PFSTRTL) {
        this.PFSTRTL = PFSTRTL;
    }

    public Date getPFSTPL() {
        return PFSTPL;
    }

    public void setPFSTPL(Date PFSTPL) {
        this.PFSTPL = PFSTPL;
    }

    public Date getPFSTRT2() {
        return PFSTRT2;
    }

    public void setPFSTRT2(Date PFSTRT2) {
        this.PFSTRT2 = PFSTRT2;
    }

    public Date getPFSTP2() {
        return PFSTP2;
    }

    public void setPFSTP2(Date PFSTP2) {
        this.PFSTP2 = PFSTP2;
    }

    public Date getOLPMRD() {
        return OLPMRD;
    }

    public void setOLPMRD(Date OLPMRD) {
        this.OLPMRD = OLPMRD;
    }

    public Date getRLPMRD() {
        return RLPMRD;
    }

    public void setRLPMRD(Date RLPMRD) {
        this.RLPMRD = RLPMRD;
    }

    public int getFULLRED() {
        return FULLRED;
    }

    public void setFULLRED(int FULLRED) {
        this.FULLRED = FULLRED;
    }

    public int getPARTRED() {
        return PARTRED;
    }

    public void setPARTRED(int PARTRED) {
        this.PARTRED = PARTRED;
    }

    public int getMAXLPM() {
        return MAXLPM;
    }

    public void setMAXLPM(int MAXLPM) {
        this.MAXLPM = MAXLPM;
    }

    
}