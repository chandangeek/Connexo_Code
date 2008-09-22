/*
 * ObjectReference.java
 *
 * Created on 7 oktober 2004, 11:17
 */

package com.energyict.dlms.cosem;

/**
 *
 * @author  Koen
 */
public class ObjectReference {
    
    public final int LN_REFERENCE=0;
    public final int SN_REFERENCE=1;
    
    byte[] ln=null;
    int classId=-1;
    int sn=-1;
    
    int reference=-1;
    
    public String toString() {
        return (sn!=-1?(sn+", "):"")+(classId!=-1?(classId+", "):"")+(ln!=null?(ln[0]+"."+ln[1]+"."+ln[2]+"."+ln[3]+"."+ln[4]+"."+ln[5]):"");
    }
    
    public boolean isAbstract() {
        return ((ln[0] == 0) && (ln[1] == 0));
    }
    
    /** Creates a new instance of ObjectReference */
    public ObjectReference(byte[] ln) {
        this(ln,-1);
    }
    public ObjectReference(byte[] ln, int classId) {
        reference = LN_REFERENCE;
        this.ln=ln;
        this.classId=classId;
    }
    public ObjectReference(int sn) {
        reference = SN_REFERENCE;
        this.sn=sn;
    }
    
    public boolean isSNReference() {
        return reference == SN_REFERENCE;
    }
    public boolean isLNReference() {
        return reference == LN_REFERENCE;
    }
    /**
     * Getter for property ln.
     * @return Value of property ln.
     */
    public byte[] getLn() {
        return this.ln;
    }
    
    /**
     * Getter for property sn.
     * @return Value of property sn.
     */
    public int getSn() {
        return sn;
    }
    
    /**
     * Getter for property classId.
     * @return Value of property classId.
     */
    public int getClassId() {
        return classId;
    }
}
