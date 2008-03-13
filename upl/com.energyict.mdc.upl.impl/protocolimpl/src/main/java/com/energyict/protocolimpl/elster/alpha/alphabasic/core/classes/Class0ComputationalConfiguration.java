/*
 * Class0ComputationalConfiguration.java
 *
 * Created on 8 juli 2005, 11:12
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
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.elster.alpha.core.connection.*;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.AbstractClass;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.ClassIdentification;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.ClassFactory;
/**
 *
 * @author Koen
 */
public class Class0ComputationalConfiguration extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(0,15,true);
    
    BigDecimal UKH; 
    int UPR;
    BigDecimal UKE;
    int INTNORM;
    int INTTEST;
    int DPLOCE;  // decimal location applied to all energy values, 1 byte; 0 = no decimal displayed, 1 to 4 = decimal location on display beginning at the right.
    int DPLOCD; //decimal location applied to all demand values, 1 byte; 0 = no decimal displayed, 1 to 4 = decimal location on display beginning at the right.
    int NUMSBI;
        
    public String toString() {
        return "Class0ComputationalConfiguration: UKH="+UKH+", UPR="+UPR+", UKE="+UKE+", INTNORM="+INTNORM+", INTTEST="+INTTEST+", DPLOCE="+DPLOCE+", DPLOCD="+DPLOCD+", NUMSBI=0x"+Integer.toHexString(NUMSBI);
    }
    
    /** Creates a new instance of Class0ComputationalConfiguration */
    public Class0ComputationalConfiguration(ClassFactory classFactory) {
        super(classFactory);
    }
    
    protected void parse(byte[] data) throws IOException {
        UKH = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, 0, 3),3);
        UPR = ProtocolUtils.getBCD2Int(data, 3, 1);
        UKE = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, 4, 5),6);
        INTNORM = ProtocolUtils.getInt(data,9,1);
        INTTEST = ProtocolUtils.getInt(data,10,1);
        DPLOCE = ProtocolUtils.getInt(data,11,1);
        DPLOCD = ProtocolUtils.getInt(data,12,1);
        NUMSBI = ProtocolUtils.getInt(data,13,1);
    }
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification; 
    }

    public BigDecimal getUKH() {
        return UKH;
    }

    public int getUPR() {
        return UPR;
    }

    public BigDecimal getUKE() {
        return UKE;
    }

    public int getINTNORM() {
        return INTNORM;
    }

    public int getINTTEST() {
        return INTTEST;
    }

    public int getDPLOCE() {
        return DPLOCE;
    }

    public int getDPLOCD() {
        return DPLOCD;
    }

    public int getNUMSBI() {
        return NUMSBI;
    }

}
