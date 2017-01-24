/*
 * Class0ComputationalConfiguration.java
 *
 * Created on 8 juli 2005, 11:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class Class0ComputationalConfiguration extends AbstractClass {

    ClassIdentification classIdentification = new ClassIdentification(0,40,true);

    BigDecimal UKH;
    int UPR;
    BigDecimal UKE;
    int INTNORM;
    int INTTEST;
    int DPLOCE;
    int DPLOCD;
    int NUMSBI;
    BigDecimal VTRATIO;
    BigDecimal CTRATIO;
    BigDecimal XFACTOR;

    public String toString() {
        return "Class0ComputationalConfiguration: UKH="+UKH+", UPR="+UPR+", UKE="+UKE+", INTNORM="+INTNORM+", INTTEST="+INTTEST+", DPLOCE="+DPLOCE+", DPLOCD="+DPLOCD+", NUMSBI=0x"+Integer.toHexString(NUMSBI)+", VTRATIO="+VTRATIO+", CTRATIO="+CTRATIO+", XFACTOR="+XFACTOR;
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
        VTRATIO = BigDecimal.valueOf(ProtocolUtils.getBCD2Int(data, 14, 3),2);
        CTRATIO = BigDecimal.valueOf(ProtocolUtils.getBCD2Int(data, 17, 3),2);
        XFACTOR = BigDecimal.valueOf(ProtocolUtils.getBCD2Int(data, 20, 4));
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

    public BigDecimal getVTRATIO() {
        return VTRATIO;
    }

    public BigDecimal getCTRATIO() {
        return CTRATIO;
    }

    public BigDecimal getXFACTOR() {
        return XFACTOR;
    }
}
