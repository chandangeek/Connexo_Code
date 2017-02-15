/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Class9Status1.java
 *
 * Created on 20 juli 2005, 11:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;

import java.io.IOException;
import java.util.Date;


/**
 *
 * @author koen
 */
public class Class9Status1 extends AbstractClass {

    ClassIdentification classIdentification = new ClassIdentification(9,48,false);

    int XUOM;
    int SYSERR;
    int SYSWARN;
    int SYSSTAT;
    int CUMDDR;
    int CUMDPUL;
    int PWRLOG;
    Date PSTART;
    Date PEND;
    int SEARAT;
    int DOY;
    Date TD;
    int TRI;
    int DATATR;
    int DATREP;
    int DATMOD;
    int CUMDR;
    int CUMCOMM;
    int CUMOUT;

    public String toString() {
        return "Class9Status1: XUOM=0x"+Integer.toHexString(XUOM)+", "+
               "SYSERR=0x"+Integer.toHexString(SYSERR)+", "+
               "SYSWARN=0x"+Integer.toHexString(SYSWARN)+", "+
               "SYSSTAT=0x"+Integer.toHexString(SYSSTAT)+", "+
               "CUMDDR="+CUMDDR+", "+
               "CUMDPUL="+CUMDPUL+", "+
               "PWRLOG=0x"+Integer.toHexString(PWRLOG)+", "+
               "PSTART="+PSTART+", "+
               "PEND="+PEND+", "+
               "SEARAT="+Integer.toHexString(SEARAT)+", "+
               "DOY="+Integer.toHexString(DOY)+", "+
               "TD="+TD+", "+
               "TRI="+TRI+", "+
               "DATATR="+DATATR+", "+
               "DATREP="+DATREP+", "+
               "DATMOD="+DATMOD+", "+
               "CUMDR="+CUMDR+", "+
               "CUMCOMM="+CUMCOMM+", "+
               "CUMOUT="+CUMOUT;
    }

    /** Creates a new instance of Class9Status1 */
    public Class9Status1(ClassFactory classFactory) {
        super(classFactory);
    }

    protected void parse(byte[] data) throws IOException {
        XUOM = ProtocolUtils.getInt(data,0, 1);
        SYSERR = ProtocolUtils.getInt(data,1, 3);
        SYSWARN = ProtocolUtils.getInt(data,4, 1);
        SYSSTAT = ProtocolUtils.getInt(data,5, 1);
        CUMDDR = ProtocolUtils.getBCD2Int(data,6,1);
        CUMDPUL = ProtocolUtils.getBCD2Int(data,7,1);
        PWRLOG = ProtocolUtils.getInt(data,8, 4);
        PSTART = ClassParseUtils.getDate6(data,12, classFactory.alpha.getTimeZone());
        PEND = ClassParseUtils.getDate6(data,18, classFactory.alpha.getTimeZone());
        SEARAT = ProtocolUtils.getInt(data,24, 1);
        DOY = ProtocolUtils.getInt(data,25, 2);
        TD = ClassParseUtils.getDate6(data,27, classFactory.alpha.getTimeZone());
        TRI = ProtocolUtils.getBCD2Int(data,33,2);
        DATATR = ProtocolUtils.getBCD2Int(data,35,3);
        DATREP = ProtocolUtils.getBCD2Int(data,38,3);
        DATMOD = ProtocolUtils.getBCD2Int(data,41,3);
        CUMDR = ProtocolUtils.getBCD2Int(data,44,1);
        CUMCOMM = ProtocolUtils.getBCD2Int(data,45,1);
        CUMOUT = ProtocolUtils.getBCD2Int(data,46,2);
    }



    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }

    public int getXUOM() {
        return XUOM;
    }

    public int getSYSERR() {
        return SYSERR;
    }

    public int getSYSWARN() {
        return SYSWARN;
    }

    public int getSYSSTAT() {
        return SYSSTAT;
    }

    public int getCUMDDR() {
        return CUMDDR;
    }

    public int getCUMDPUL() {
        return CUMDPUL;
    }

    public int getPWRLOG() {
        return PWRLOG;
    }

    public Date getPSTART() {
        return PSTART;
    }

    public Date getPEND() {
        return PEND;
    }

    public int getSEARAT() {
        return SEARAT;
    }

    public int getDOY() {
        return DOY;
    }

    public Date getTD() {
        return TD;
    }

    public int getTRI() {
        return TRI;
    }

    public int getDATATR() {
        return DATATR;
    }

    public int getDATREP() {
        return DATREP;
    }

    public int getDATMOD() {
        return DATMOD;
    }

    public int getCUMDR() {
        return CUMDR;
    }

    public int getCUMCOMM() {
        return CUMCOMM;
    }

    public int getCUMOUT() {
        return CUMOUT;
    }

}
