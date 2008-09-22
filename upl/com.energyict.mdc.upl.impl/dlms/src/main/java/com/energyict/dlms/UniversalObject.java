package com.energyict.dlms;


import java.util.*;
import java.sql.*;
import java.io.*;
import com.energyict.obis.ObisCode;


public class UniversalObject implements DLMSCOSEMGlobals,Serializable {

    // Captured Object List
    private static final byte COL_CLASSID=0;
    private static final byte COL_LN_A=1;
    private static final byte COL_LN_B=2;
    private static final byte COL_LN_C=3;
    private static final byte COL_LN_D=4;
    private static final byte COL_LN_E=5;
    private static final byte COL_LN_F=6;
    private static final byte COL_ATTR_ID=7;
    private static final byte COL_ATTR_MODE=8;
    
    
    // Instantiated Object List
    private static final byte IOL_BASENAME=0;
    private static final byte IOL_CLASSID=1;
    private static final byte IOL_VERSION=2;
    private static final byte IOL_LN_A=3;
    private static final byte IOL_LN_B=4;
    private static final byte IOL_LN_C=5;
    private static final byte IOL_LN_D=6;
    private static final byte IOL_LN_E=7;
    private static final byte IOL_LN_F=8;
    
     
    public long[] fields;

    private static final int ASSOC_SN_OBJECT_LIST_STRUCTURE_SIZE=9;
    public static int getSNObjectListEntrySize() {
        return ASSOC_SN_OBJECT_LIST_STRUCTURE_SIZE;
    }

    public UniversalObject(byte[] ln, int classId, int version) {
        fields = new long[9];
        fields[0] = -1;
        fields[1] = classId;
        fields[2] = version;
        fields[3] = ln[0];
        fields[4] = ln[1];
        fields[5] = ln[2];
        fields[6] = ln[3];
        fields[7] = ln[4];
        fields[8] = ln[5];
    }
    
    
    public UniversalObject() {
       fields=null;    
    }
    
    public UniversalObject(List values, int reference) { //, List frmts) {
            
       if (reference == ProtocolLink.LN_REFERENCE) {
           fields = new long[values.size()+1];
           fields[0]=-1;
           for (int i=0;i<fields.length-1;i++)
               fields[i+1] = ((Long)values.get(i)).longValue();
       }
       else if (reference == ProtocolLink.SN_REFERENCE) {
           fields = new long[values.size()];
           for (int i=0;i<fields.length;i++)
               fields[i] = ((Long)values.get(i)).longValue();
       }
           
    }
    
    public UniversalObject(int iNROfItems) {
       fields = new long[iNROfItems];
    } 
    
    public void setField(int iIndex,long val) {
       fields[iIndex] = val;
    }
    
    public long getField(int iIndex) {
       return fields[iIndex];
    }

    
    public String getLN() {
        byte[] ln = new byte[6];
        ln[0] = (byte)getLNA();
        ln[1] = (byte)getLNB();
        ln[2] = (byte)getLNC();
        ln[3] = (byte)getLND();
        ln[4] = (byte)getLNE();
        ln[5] = (byte)getLNF();
        
        String strLN = 
                String.valueOf((int)ln[0]&0xff)+"."+
                String.valueOf((int)ln[1]&0xff)+"."+
                String.valueOf((int)ln[2]&0xff)+"."+
                String.valueOf((int)ln[3]&0xff)+"."+
                String.valueOf((int)ln[4]&0xff)+"."+
                String.valueOf((int)ln[5]&0xff);
        
        return strLN;
    }
    
    public byte[] getLNArray() {
        byte[] ln = new byte[6];
        ln[0] = (byte)getLNA();
        ln[1] = (byte)getLNB();
        ln[2] = (byte)getLNC();
        ln[3] = (byte)getLND();
        ln[4] = (byte)getLNE();
        ln[5] = (byte)getLNF();
        return ln;
    }
    
    public int getBaseName() {
        return (int)fields[IOL_BASENAME];
    }
    public int getClassID() {
        return (int)fields[IOL_CLASSID];
    }
    public int getVersion() {
        return (int)fields[IOL_VERSION];
    }

    int getLNA() {
        return (int)fields[IOL_LN_A];
    }
    int getLNB() {
        return (int)fields[IOL_LN_B];
    }
    int getLNC() {
        return (int)fields[IOL_LN_C];
    }
    int getLND() {
        return (int)fields[IOL_LN_D];
    }
    int getLNE() {
        return (int)fields[IOL_LN_E];
    }
    int getLNF() {
        return (int)fields[IOL_LN_F];
    }
    int getLNAco() {
        return (int)fields[COL_LN_A];
    }
    int getLNBco() {
        return (int)fields[COL_LN_B];
    }
    int getLNCco() {
        return (int)fields[COL_LN_C];
    }
    int getLNDco() {
        return (int)fields[COL_LN_D];
    }
    int getLNEco() {
        return (int)fields[COL_LN_E];
    }
    int getLNFco() {
        return (int)fields[COL_LN_F];
    }
    
    public void setLN(String strLN) {
       StringTokenizer st = new StringTokenizer(strLN,".");
       int iTokens = st.countTokens();
       for (int i=0;i<iTokens;i++) setField(IOL_LN_A+i,Long.parseLong(st.nextToken()));
    }
    
    public void setBaseName(int iVal) {
        fields[IOL_BASENAME]=iVal;
    }
    public void setClassID(int iVal) {
        fields[IOL_CLASSID]=iVal;
    }
    public void setVersion(int iVal) {
        fields[IOL_VERSION]=iVal;
    }
    void setLNA(int iVal) {
        fields[IOL_LN_A]=iVal;
    }
    void setLNB(int iVal) {
        fields[IOL_LN_B]=iVal;
    }
    void setLNC(int iVal) {
        fields[IOL_LN_C]=iVal;
    }
    void setLND(int iVal) {
        fields[IOL_LN_D]=iVal;
    }
    void setLNE(int iVal) { 
        fields[IOL_LN_E]=iVal;
    }
    void setLNF(int iVal) {
        fields[IOL_LN_F]=iVal;
    }
    
    public boolean equals(UniversalObject uo) throws IOException {
        
        if ((getLNA() == uo.fields[COL_LN_A])  &&
            (getLNB() == uo.fields[COL_LN_B]) &&
            (getLNC() == uo.fields[COL_LN_C]) &&
            (getLND() == uo.fields[COL_LN_D]) &&
            (getLNE() == uo.fields[COL_LN_E]) &&
            (getLNF() == uo.fields[COL_LN_F]) &&
            (getClassID() == uo.fields[COL_CLASSID]))
            return true;
        else
            return false;
    }
    
    
    public boolean equals(DLMSConfig config) throws IOException {
        
        if (((getLNA() == config.getLNA()) || (config.getLNA()==-1)) &&
            ((getLNB() == config.getLNB()) || (config.getLNB()==-1)) &&
            ((getLNC() == config.getLNC()) || (config.getLNC()==-1)) &&
            ((getLND() == config.getLND()) || (config.getLND()==-1)) &&
            ((getLNE() == config.getLNE()) || (config.getLNE()==-1)) &&
            ((getLNF() == config.getLNF()) || (config.getLNF()==-1)) &&
            (getClassID() == config.getClassID()))
            return true;
        else
            return false;
    }
    
    public boolean equals(DLMSObis dlmsObis) throws IOException {
        if ((getLNA() == dlmsObis.getLNA()) &&
            (getLNB() == dlmsObis.getLNB()) &&
            (getLNC() == dlmsObis.getLNC()) &&
            (getLND() == dlmsObis.getLND()) &&
            (getLNE() == dlmsObis.getLNE()) &&
            (getLNF() == dlmsObis.getLNF()) &&
            (getClassID() == dlmsObis.getDLMSClass())) 
            return true;
        else
            return false;
        
    }
    
    /*
     *  Find ObisCode in instantiated object list.
     *  All instantiated objects have f=255 and are unique for a classId.
     */
    public boolean equals(ObisCode obisCode) throws IOException {
        if ((getLNA() == obisCode.getA()) &&
            (getLNB() == obisCode.getB()) &&
            (getLNC() == obisCode.getC()) &&
            (getLND() == obisCode.getD()) &&
            (getLNE() == obisCode.getE()))
            return true;
        else
            return false;
    }
    
    public ObisCode getObisCode() {
        return ObisCode.fromString(getLN());
    }
    
    
    public int getValueAttributeOffset() throws IOException {
        if (getClassID() == ICID_REGISTER) return 8;
        else if (getClassID() == ICID_EXTENDED_REGISTER) return 8;
        else if (getClassID() == ICID_DEMAND_REGISTER) return 16; // last average value
        else if (getClassID() == ICID_DATA) return 8;
        else if (getClassID() == ICID_PROFILE_GENERIC) return 8;
        else if (getClassID() == ICID_CLOCK) return 8;
        else throw new IOException("UniversalObject, wrong object for value attribute!");
    }
    public int getScalerAttributeOffset() throws IOException {
        if (getClassID() == ICID_REGISTER) return 16;
        else if (getClassID() == ICID_EXTENDED_REGISTER) return 16;
        else if (getClassID() == ICID_DEMAND_REGISTER) return 24;
        else if (getClassID() == ICID_DATA) return 8;
        else throw new IOException("UniversalObject, wrong object for scaler attribute!");
    }
    
    public boolean isCapturedObjectNotAbstract() {
        if ((fields[COL_LN_A] >= 1) && (fields[COL_LN_B] >= 1) && (fields[COL_LN_B] <= 64)) return true;
        else return false;
    }
    
    // 1,4 5 6 7 8 9
    public boolean isCapturedObjectElectricity() {
        if ((fields[COL_LN_A] == 1) && (fields[COL_LN_B] >= 1) && (fields[COL_LN_B] <= 64)) return true;
        else return false;
    }
    
    public boolean isCapturedObjectPulses() {
        if (fields[COL_LN_C] == 82) return true;
        else return false;
    }
    
    public boolean isCapturedObjectCumulative() {
        if ((fields[COL_LN_C] != 0) &&
            (fields[COL_LN_C] != 96) &&    
            (fields[COL_LN_C] != 97) &&    
            (fields[COL_LN_C] != 98) &&    
            (fields[COL_LN_C] != 99) && 
            (fields[COL_LN_D] == 8)) 
            return true;
        else return false;
    }
    
    public String toString() {
        return this.getLNA()+"."+
               this.getLNB()+"."+
               this.getLNC()+"."+
               this.getLND()+"."+
               this.getLNE()+"."+
               this.getLNF()+"."+
               this.getClassID();
    }
    
    public String toStringCo() {
        return this.getLNAco()+"."+
               this.getLNBco()+"."+
               this.getLNCco()+"."+
               this.getLNDco()+"."+
               this.getLNEco()+"."+
               this.getLNFco();
    }
    
    /** Getter for property fields.
     * @return Value of property fields.
     *
     */
    public long[] getIFields() {
        return this.fields;
    }
    
    /** Setter for property fields.
     * @param fields New value of property fields.
     *
     */
    public void setIFields(long[] fields) {
        this.fields = fields;
    }
    
} // class UniversalObjectList
