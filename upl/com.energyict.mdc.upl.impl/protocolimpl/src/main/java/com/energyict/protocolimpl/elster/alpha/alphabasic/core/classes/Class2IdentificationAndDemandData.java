/*
 * class1IdentificationAndDemandData.java
 *
 * Created on 12 juli 2005, 10:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.elster.alpha.core.connection.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.cbo.*;

/**
 *
 * @author Koen
 */
public class Class2IdentificationAndDemandData extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(2,52,true);
    
    
    static public final int IMPORT = 1;
    static public final int EXPORT = 0;
    
    
    long UMTRSN;
    String ACCTID;
    // pad 14 bytes
    int KWOVRL; 
    int KWTHRSA;
    int KWTHRSB;
    int KWTHRSC;
    int KWTHRSD;
    int E2KYZDV;
    int EMETFLG;
    int EATRVAL;
        
    
    public String toString() {
        return "Class2IdentificationAndDemandData: UMTRSN="+UMTRSN+", ACCTID="+ACCTID+", KWOVRL="+KWOVRL+", KWTHRSA="+KWTHRSA+", KWTHRSB="+KWTHRSB+", KWTHRSC="+KWTHRSC+", KWTHRSD="+KWTHRSD+", E2KYZDV="+E2KYZDV+
                ", EMETFLG=0x"+Integer.toHexString(EMETFLG)+", EATRVAL="+EATRVAL+", isSingleRate="+isSingleRate();
    }
    
    /** Creates a new instance of class1IdentificationAndDemandData */
    public Class2IdentificationAndDemandData(ClassFactory classFactory) {
        super(classFactory);
    }
    
    protected void parse(byte[] data) throws IOException {
        UMTRSN = ParseUtils.getBCD2Long(data,0, 5);
        ACCTID = new String(ProtocolUtils.getSubArray2(data, 5,14));
        KWOVRL = ProtocolUtils.getBCD2Int(data, 33, 3);
        KWTHRSA = ProtocolUtils.getBCD2Int(data, 36, 3);
        KWTHRSB = ProtocolUtils.getBCD2Int(data, 39, 3);
        KWTHRSC = ProtocolUtils.getBCD2Int(data, 42, 3);
        KWTHRSD = ProtocolUtils.getBCD2Int(data, 45, 3);
        E2KYZDV = ProtocolUtils.getInt(data,48,1);
        EMETFLG = ProtocolUtils.getInt(data,49,1);
        EATRVAL = ProtocolUtils.getInt(data,50,1);
    }
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification; 
    }
    
    
    public long getUMTRSN() {
        return UMTRSN;
    }

    public String getACCTID() {
        return ACCTID;
    }

    public int getKWOVRL() {
        return KWOVRL;
    }

    public int getKWTHRSA() {
        return KWTHRSA;
    }

    public int getKWTHRSB() {
        return KWTHRSB;
    }

    public int getKWTHRSC() {
        return KWTHRSC;
    }

    public int getKWTHRSD() {
        return KWTHRSD;
    }

    public int getE2KYZDV() {
        return E2KYZDV;
    }

    public int getEMETFLG() {
        return EMETFLG;
    }

    public int getEATRVAL() {
        return EATRVAL;
    }


    
    /*
     *  @result boolean single or 4 rate meter 
     */
    public boolean isSingleRate() {
        return (getEMETFLG() & 0x0400) == 0x0400;
    }
    
    /*
     *  @result int zero based single rate rate
     */
    public int getSingleRate() {
        return (getEMETFLG() >> 6) & 0x0003;
    }
    
    
    
    
    
}
