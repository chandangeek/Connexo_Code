/*
 * class1IdentificationAndDemandData.java
 *
 * Created on 12 juli 2005, 10:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Class2IdentificationAndDemandData extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(2,64,true);
    
    
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
    int[] EBLKCF=new int[2];
        
    
    public String toString() {
        return "Class2IdentificationAndDemandData: UMTRSN="+UMTRSN+", ACCTID="+ACCTID+", KWOVRL="+KWOVRL+", KWTHRSA="+KWTHRSA+", KWTHRSB="+KWTHRSB+", KWTHRSC="+KWTHRSC+", KWTHRSD="+KWTHRSD+", E2KYZDV="+E2KYZDV+
                ", EMETFLG=0x"+Integer.toHexString(EMETFLG)+", EATRVAL="+EATRVAL+", EBLKCF1=0x"+Integer.toHexString(EBLKCF[0])+", EBLKCF2=0x"+Integer.toHexString(EBLKCF[1])+", isSingleRate="+isSingleRate();
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
        EMETFLG = ProtocolUtils.getInt(data,49,2);
        EATRVAL = ProtocolUtils.getInt(data,51,1);
        EBLKCF[0] = ProtocolUtils.getInt(data,52,1);
        EBLKCF[1] = ProtocolUtils.getInt(data,53,1);
        //block1DemandOverloadValue = new Quantity
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

    public int getEBLKCF1() {
        return EBLKCF[0];
    }

    public int getEBLKCF2() {
        return EBLKCF[1];
    }
    
    public int getEBLKCF(int block) {
        return EBLKCF[block];
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
    
    /*
     *  @result 1 = import, 0 = export
     */
    public int getDirection(int block) throws IOException {
        if (((getEBLKCF(block) >> 6) & 0x03) == 0x1)
            return 0;
        else if (((getEBLKCF(block) >> 6) & 0x03) == 0x2)
            return 1;
        else throw new IOException("CLASS2, getDirection(), delivered AND receved are both active? (EBLKCF1=0x"+Integer.toHexString(getEBLKCF1())+" EBLKCF2=0x"+Integer.toHexString(getEBLKCF2()));
    }
    
    public int getQuadrantInfo(int block) throws IOException {
        return (getEBLKCF(block) & 0x0F);
    }
    
    public boolean isVAImport(int block) throws IOException {
        return getQuadrantInfo(block)==0x9;
    }
    public boolean isVAExport(int block) throws IOException {
        return getQuadrantInfo(block)==0x6;
    }
    public boolean isVASum(int block) throws IOException {
        return getQuadrantInfo(block)==0xF;
    }
    public boolean isvarImport(int block) throws IOException {
        return getQuadrantInfo(block)==0x3;
    }
    public boolean isvarExport(int block) throws IOException {
        return getQuadrantInfo(block)==0xC;
    }
    public boolean isvarSum(int block) throws IOException {
        return getQuadrantInfo(block)==0xF;
    }
    public boolean isOnlyQ1(int block) throws IOException {
        return getQuadrantInfo(block)==0x1;
    }
    public boolean isOnlyQ2(int block) throws IOException {
        return getQuadrantInfo(block)==0x2;
    }
    public boolean isOnlyQ3(int block) throws IOException {
        return getQuadrantInfo(block)==0x4;
    }
    public boolean isOnlyQ4(int block) throws IOException {
        return getQuadrantInfo(block)==0x8;
    }
    public boolean isOnlyQ1Q4(int block) throws IOException {
        return getQuadrantInfo(block)==0x9;
    }
    public boolean isNoQuadrants(int block) throws IOException {
        return getQuadrantInfo(block)==0x0;
    }
    
    
}
