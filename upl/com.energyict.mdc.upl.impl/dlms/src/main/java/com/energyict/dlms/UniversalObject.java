package com.energyict.dlms;


import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <b>Warning: </b> Apparently UniversalObjects can be created in two ways: either as 'Captured object list entry'
 * or as 'Instantiated Object List entry'. Depending on which constructor/static method you used to create the UniversalObject,
 * the 'fields' array is filled up different. As a result, for each type other methods should be used (e.g. method #getLNAco in case
 * of captured object list and #getLNA in case of instantiated object list).<br/>
 * <b>As a matter of fact, depending on which type you use, some methods produce erroneous output; so keep this in mind!</b>
 */
public class UniversalObject implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5227477714920108186L;


    // Captured Object List
    private static final byte COL_CLASSID = 0;
    private static final byte COL_LN_A = 1;
    private static final byte COL_LN_B = 2;
    private static final byte COL_LN_C = 3;
    private static final byte COL_LN_D = 4;
    private static final byte COL_LN_E = 5;
    private static final byte COL_LN_F = 6;


    // Instantiated Object List
    private static final byte IOL_BASENAME = 0;
    private static final byte IOL_CLASSID = 1;
    private static final byte IOL_VERSION = 2;
    private static final byte IOL_LN_A = 3;
    private static final byte IOL_LN_B = 4;
    private static final byte IOL_LN_C = 5;
    private static final byte IOL_LN_D = 6;
    private static final byte IOL_LN_E = 7;
    private static final byte IOL_LN_F = 8;


    private long[] fields;

    private static final int ASSOC_SN_OBJECT_LIST_STRUCTURE_SIZE = 9;

    public static int getSNObjectListEntrySize() {
        return ASSOC_SN_OBJECT_LIST_STRUCTURE_SIZE;
    }

    public UniversalObject(ObisCode obisCode, DLMSClassId classId) {
        this(obisCode.getLN(), classId.getClassId(), -1);
    }

    public UniversalObject(ObisCode obisCode, DLMSClassId classId, int baseName) {
        this(obisCode.getLN(), classId.getClassId(), -1, baseName);
    }

    public UniversalObject(byte[] ln, int classId, int version) {
        this(ln, classId, version, -1);
    }

    public UniversalObject(byte[] ln, int classId, int version, int baseName) {
        this.fields = new long[9];
        this.fields[0] = baseName;
        this.fields[1] = classId;
        this.fields[2] = version;
        this.fields[3] = ln[0] & 0xFF;
        this.fields[4] = ln[1] & 0xFF;
        this.fields[5] = ln[2] & 0xFF;
        this.fields[6] = ln[3] & 0xFF;
        this.fields[7] = ln[4] & 0xFF;
        this.fields[8] = ln[5] & 0xFF;
    }


    public UniversalObject() {
        this.fields = null;
    }

    public UniversalObject(List values, int reference) { //, List frmts) {

        if (reference == ProtocolLink.LN_REFERENCE) {
            this.fields = new long[values.size() + 1];
            this.fields[0] = -1;
            for (int i = 0; i < this.fields.length - 1; i++) {
                this.fields[i + 1] = ((Long) values.get(i)).longValue();
            }
        } else if (reference == ProtocolLink.SN_REFERENCE) {
            this.fields = new long[values.size()];
            for (int i = 0; i < this.fields.length; i++) {
                this.fields[i] = ((Long) values.get(i)).longValue();
            }
        }

    }

    /**
     * Method who will create a universalObject in form 'Capture object list' <br/>
     * <b>Note:</b> always keep in mind that the returned UniversalObject is in the form of 'Captured Objects',
     * thus all further operations should only do 'captured objects' operations (e.g.: method #getLNAco instead of #getLNA).
     * When directly accessing the UniversalObject 'fields' array, this has also to be taken into account (so the correct array positions are used).
     */
    public static UniversalObject createCaptureObject(int classId, ObisCode obisCode) {
        UniversalObject uo = new UniversalObject();
        uo.fields = new long[7];
        uo.fields[0] = classId;
        uo.fields[1] = obisCode.getA();
        uo.fields[2] = obisCode.getB();
        uo.fields[3] = obisCode.getC();
        uo.fields[4] = obisCode.getD();
        uo.fields[5] = obisCode.getE();
        uo.fields[6] = obisCode.getF();
        return uo;
    }

    public UniversalObject(int iNROfItems) {
        this.fields = new long[iNROfItems];
    }

    public void setField(int iIndex, long val) {
        this.fields[iIndex] = val;
    }

    public long getField(int iIndex) {
        return this.fields[iIndex];
    }


    public String getLN() {
        byte[] ln = new byte[6];
        ln[0] = (byte) getLNA();
        ln[1] = (byte) getLNB();
        ln[2] = (byte) getLNC();
        ln[3] = (byte) getLND();
        ln[4] = (byte) getLNE();
        ln[5] = (byte) getLNF();

        String strLN =
                String.valueOf(ln[0] & 0xff) + "." +
                        String.valueOf(ln[1] & 0xff) + "." +
                        String.valueOf(ln[2] & 0xff) + "." +
                        String.valueOf(ln[3] & 0xff) + "." +
                        String.valueOf(ln[4] & 0xff) + "." +
                        String.valueOf(ln[5] & 0xff);

        return strLN;
    }

    public byte[] getLNArray() {
        byte[] ln = new byte[6];
        ln[0] = (byte) getLNA();
        ln[1] = (byte) getLNB();
        ln[2] = (byte) getLNC();
        ln[3] = (byte) getLND();
        ln[4] = (byte) getLNE();
        ln[5] = (byte) getLNF();
        return ln;
    }

    /**
     * @return the ShortName linked to this obisCode
     */
    public int getBaseName() {
        return (int) this.fields[IOL_BASENAME];
    }

    public int getClassID() {
        return (int) this.fields[IOL_CLASSID];
    }

    public int getClassIDco() {
        return (int) this.fields[COL_CLASSID];
    }

    public int getVersion() {
        return (int) this.fields[IOL_VERSION];
    }

    int getLNA() {
        return (int) this.fields[IOL_LN_A];
    }

    int getLNB() {
        return (int) this.fields[IOL_LN_B];
    }

    int getLNC() {
        return (int) this.fields[IOL_LN_C];
    }

    int getLND() {
        return (int) this.fields[IOL_LN_D];
    }

    int getLNE() {
        return (int) this.fields[IOL_LN_E];
    }

    int getLNF() {
        return (int) this.fields[IOL_LN_F];
    }

    int getLNAco() {
        return (int) this.fields[COL_LN_A];
    }

    int getLNBco() {
        return (int) this.fields[COL_LN_B];
    }

    int getLNCco() {
        return (int) this.fields[COL_LN_C];
    }

    int getLNDco() {
        return (int) this.fields[COL_LN_D];
    }

    int getLNEco() {
        return (int) this.fields[COL_LN_E];
    }

    int getLNFco() {
        return (int) this.fields[COL_LN_F];
    }

    public void setLN(String strLN) {
        StringTokenizer st = new StringTokenizer(strLN, ".");
        int iTokens = st.countTokens();
        for (int i = 0; i < iTokens; i++) {
            setField(IOL_LN_A + i, Long.parseLong(st.nextToken()));
        }
    }

    public void setBaseName(int iVal) {
        this.fields[IOL_BASENAME] = iVal;
    }

    public void setClassID(int iVal) {
        this.fields[IOL_CLASSID] = iVal;
    }

    public void setVersion(int iVal) {
        this.fields[IOL_VERSION] = iVal;
    }

    void setLNA(int iVal) {
        this.fields[IOL_LN_A] = iVal;
    }

    void setLNB(int iVal) {
        this.fields[IOL_LN_B] = iVal;
    }

    void setLNC(int iVal) {
        this.fields[IOL_LN_C] = iVal;
    }

    void setLND(int iVal) {
        this.fields[IOL_LN_D] = iVal;
    }

    void setLNE(int iVal) {
        this.fields[IOL_LN_E] = iVal;
    }

    void setLNF(int iVal) {
        this.fields[IOL_LN_F] = iVal;
    }

    public boolean equals(UniversalObject uo) {
        if (getIFields().length == uo.getIFields().length) {
            return Arrays.equals(getIFields(), uo.getIFields());
        } else if (getIFields().length == (IOL_LN_F + 1)) {    // Then this instance contains an instantiated object list entry - uo contains a captured object list entry
            return ((getLNA() == uo.fields[COL_LN_A]) &&
                    (getLNB() == uo.fields[COL_LN_B]) &&
                    (getLNC() == uo.fields[COL_LN_C]) &&
                    (getLND() == uo.fields[COL_LN_D]) &&
                    (getLNE() == uo.fields[COL_LN_E]) &&
                    (getLNF() == uo.fields[COL_LN_F]) &&
                    (getClassID() == uo.fields[COL_CLASSID]));
        } else { // else this instance contains a captured object list entry - uo contains an instantiated object list entry
            return ((getLNAco() == uo.fields[IOL_LN_A]) &&
                    (getLNBco() == uo.fields[IOL_LN_B]) &&
                    (getLNCco() == uo.fields[IOL_LN_C]) &&
                    (getLNDco() == uo.fields[IOL_LN_D]) &&
                    (getLNEco() == uo.fields[IOL_LN_E]) &&
                    (getLNFco() == uo.fields[IOL_LN_F]) &&
                    (getClassIDco() == uo.fields[IOL_CLASSID]));
        }
    }


    public boolean equals(DLMSConfig config) {

        if (((getLNA() == config.getLNA()) || (config.getLNA() == -1)) &&
                ((getLNB() == config.getLNB()) || (config.getLNB() == -1)) &&
                ((getLNC() == config.getLNC()) || (config.getLNC() == -1)) &&
                ((getLND() == config.getLND()) || (config.getLND() == -1)) &&
                ((getLNE() == config.getLNE()) || (config.getLNE() == -1)) &&
                ((getLNF() == config.getLNF()) || (config.getLNF() == -1)) &&
                (getClassID() == config.getClassID())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean equals(DLMSObis dlmsObis) {
        try {
            if ((getLNA() == dlmsObis.getLNA()) &&
                    (getLNB() == dlmsObis.getLNB()) &&
                    (getLNC() == dlmsObis.getLNC()) &&
                    (getLND() == dlmsObis.getLND()) &&
                    (getLNE() == dlmsObis.getLNE()) &&
                    (getLNF() == dlmsObis.getLNF()) &&
                    (getClassID() == dlmsObis.getDLMSClass())) {
                return true;
            } else {
                return false;
            }
        } catch (ProtocolException e) {
            return false;
        }
    }

    /*
     *  Find ObisCode in instantiated object list.
     *  All instantiated objects have f=255 and are unique for a classId.
     */
    public boolean equals(ObisCode obisCode) {
        if (obisCode == null) {
            return false;
        }
        if (((getLNA() & 0xFF) == obisCode.getA()) &&
                ((getLNB() & 0xFF) == obisCode.getB()) &&
                ((getLNC() & 0xFF) == obisCode.getC()) &&
                ((getLND() & 0xFF) == obisCode.getD()) &&
                ((getLNE() & 0xFF) == obisCode.getE())) {
            return true;
        } else {
            return false;
        }
    }

    public ObisCode getObisCode() {
        return ObisCode.fromString(getLN());
    }


    public int getValueAttributeOffset() {
        if (getClassID() == DLMSClassId.REGISTER.getClassId()) {
            return 8;
        } else if (getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            return 8;
        } else if (getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            return 16; // last average value
        } else if (getClassID() == DLMSClassId.DATA.getClassId()) {
            return 8;
        } else if (getClassID() == DLMSClassId.PROFILE_GENERIC.getClassId()) {
            return 8;
        } else if (getClassID() == DLMSClassId.CLOCK.getClassId()) {
            return 8;
        } else {
            ProtocolException protocolException = new ProtocolException("UniversalObject, wrong object for value attribute!");
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(protocolException);
        }
    }

    public int getScalerAttributeOffset() throws IOException {
        if (getClassID() == DLMSClassId.REGISTER.getClassId()) {
            return 16;
        } else if (getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            return 16;
        } else if (getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            return 24;
        } else if (getClassID() == DLMSClassId.DATA.getClassId()) {
            return 8;
        } else {
            throw new ProtocolException("UniversalObject, wrong object for scaler attribute!");
        }
    }

    public boolean isCapturedObjectNotAbstract() {
        if ((this.fields[COL_LN_A] >= 1) && (this.fields[COL_LN_B] >= 1) && (this.fields[COL_LN_B] <= 64)) {
            return true;
        } else {
            return false;
        }
    }

    // 1,4 5 6 7 8 9
    public boolean isCapturedObjectElectricity() {
        if ((this.fields[COL_LN_A] == 1) && (this.fields[COL_LN_B] >= 1) && (this.fields[COL_LN_B] <= 64)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCapturedObjectPulses() {
        if (this.fields[COL_LN_C] == 82) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCapturedObjectCumulative() {
        if ((this.fields[COL_LN_C] != 0) &&
                (this.fields[COL_LN_C] != 96) &&
                (this.fields[COL_LN_C] != 97) &&
                (this.fields[COL_LN_C] != 98) &&
                (this.fields[COL_LN_C] != 99) &&
                (this.fields[COL_LN_D] == 8)) {
            return true;
        } else {
            return false;
        }
    }

    public String getDescription() {
        StringBuffer sb = new StringBuffer();
        if (getBaseName() != -1) {
            sb.append("[").append(getBaseName()).append("], ");
        }
        sb.append(getObisCode()).append(", ");
        sb.append(getDLMSClassId()).append(", ");
        sb.append(getObisCode().getDescription());
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.getBaseName() + "-" +
                String.valueOf(this.getLNA() & 0xff) + "." +
                String.valueOf(this.getLNB() & 0xff) + "." +
                String.valueOf(this.getLNC() & 0xff) + "." +
                String.valueOf(this.getLND() & 0xff) + "." +
                String.valueOf(this.getLNE() & 0xff) + "." +
                String.valueOf(this.getLNF() & 0xff) + "." +
                this.getClassID();
    }

    public String toStringCo() {
        return this.getLNAco() + "." +
                this.getLNBco() + "." +
                this.getLNCco() + "." +
                this.getLNDco() + "." +
                this.getLNEco() + "." +
                this.getLNFco();
    }

    /**
     * Getter for property fields.
     *
     * @return Value of property fields.
     */
    public long[] getIFields() {
        return this.fields;
    }

    /**
     * Setter for property fields.
     *
     * @param fields New value of property fields.
     */
    public void setIFields(long[] fields) {
        this.fields = fields;
    }

    /**
     * Check if this universalObject has the same class type as the given DLMSClassId
     *
     * @param dlmsClassId
     * @return
     */
    public boolean isClassType(DLMSClassId dlmsClassId) {
        return ((dlmsClassId != null) && (dlmsClassId.getClassId() == getClassID()));
    }

    /**
     * Get the DLMSClassId from the given id value
     *
     * @return
     */
    public DLMSClassId getDLMSClassId() {
        return DLMSClassId.findById(getClassID());
    }

} // class UniversalObjectList
