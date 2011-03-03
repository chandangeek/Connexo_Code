package com.energyict.dlms;


import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.StringTokenizer;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;


public class UniversalObject implements DLMSCOSEMGlobals,Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5227477714920108186L;


	// Captured Object List
	private static final byte COL_CLASSID=0;
	private static final byte COL_LN_A=1;
	private static final byte COL_LN_B=2;
	private static final byte COL_LN_C=3;
	private static final byte COL_LN_D=4;
	private static final byte COL_LN_E=5;
	private static final byte COL_LN_F=6;


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


	private long[] fields;

	private static final int ASSOC_SN_OBJECT_LIST_STRUCTURE_SIZE=9;

	public static int getSNObjectListEntrySize() {
		return ASSOC_SN_OBJECT_LIST_STRUCTURE_SIZE;
	}

	public UniversalObject(byte[] ln, int classId, int version) {
		this.fields = new long[9];
		this.fields[0] = -1;
		this.fields[1] = classId;
		this.fields[2] = version;
		this.fields[3] = ln[0];
		this.fields[4] = ln[1];
		this.fields[5] = ln[2];
		this.fields[6] = ln[3];
		this.fields[7] = ln[4];
		this.fields[8] = ln[5];
	}


	public UniversalObject() {
		this.fields=null;
	}

	public UniversalObject(List values, int reference) { //, List frmts) {

		if (reference == ProtocolLink.LN_REFERENCE) {
			this.fields = new long[values.size()+1];
			this.fields[0]=-1;
			for (int i=0;i<this.fields.length-1;i++) {
				this.fields[i+1] = ((Long)values.get(i)).longValue();
			}
		}
		else if (reference == ProtocolLink.SN_REFERENCE) {
			this.fields = new long[values.size()];
			for (int i=0;i<this.fields.length;i++) {
				this.fields[i] = ((Long)values.get(i)).longValue();
			}
		}

	}

	public UniversalObject(int iNROfItems) {
		this.fields = new long[iNROfItems];
	}

	public void setField(int iIndex,long val) {
		this.fields[iIndex] = val;
	}

	public long getField(int iIndex) {
		return this.fields[iIndex];
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
			String.valueOf(ln[0]&0xff)+"."+
			String.valueOf(ln[1]&0xff)+"."+
			String.valueOf(ln[2]&0xff)+"."+
			String.valueOf(ln[3]&0xff)+"."+
			String.valueOf(ln[4]&0xff)+"."+
			String.valueOf(ln[5]&0xff);

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
		return (int)this.fields[IOL_BASENAME];
	}
	public int getClassID() {
		return (int)this.fields[IOL_CLASSID];
	}
	public int getVersion() {
		return (int)this.fields[IOL_VERSION];
	}

	int getLNA() {
		return (int)this.fields[IOL_LN_A];
	}
	int getLNB() {
		return (int)this.fields[IOL_LN_B];
	}
	int getLNC() {
		return (int)this.fields[IOL_LN_C];
	}
	int getLND() {
		return (int)this.fields[IOL_LN_D];
	}
	int getLNE() {
		return (int)this.fields[IOL_LN_E];
	}
	int getLNF() {
		return (int)this.fields[IOL_LN_F];
	}
	int getLNAco() {
		return (int)this.fields[COL_LN_A];
	}
	int getLNBco() {
		return (int)this.fields[COL_LN_B];
	}
	int getLNCco() {
		return (int)this.fields[COL_LN_C];
	}
	int getLNDco() {
		return (int)this.fields[COL_LN_D];
	}
	int getLNEco() {
		return (int)this.fields[COL_LN_E];
	}
	int getLNFco() {
		return (int)this.fields[COL_LN_F];
	}

	public void setLN(String strLN) {
		StringTokenizer st = new StringTokenizer(strLN,".");
		int iTokens = st.countTokens();
		for (int i=0;i<iTokens;i++) {
			setField(IOL_LN_A+i,Long.parseLong(st.nextToken()));
		}
	}

	public void setBaseName(int iVal) {
		this.fields[IOL_BASENAME]=iVal;
	}
	public void setClassID(int iVal) {
		this.fields[IOL_CLASSID]=iVal;
	}
	public void setVersion(int iVal) {
		this.fields[IOL_VERSION]=iVal;
	}
	void setLNA(int iVal) {
		this.fields[IOL_LN_A]=iVal;
	}
	void setLNB(int iVal) {
		this.fields[IOL_LN_B]=iVal;
	}
	void setLNC(int iVal) {
		this.fields[IOL_LN_C]=iVal;
	}
	void setLND(int iVal) {
		this.fields[IOL_LN_D]=iVal;
	}
	void setLNE(int iVal) {
		this.fields[IOL_LN_E]=iVal;
	}
	void setLNF(int iVal) {
		this.fields[IOL_LN_F]=iVal;
	}

	public boolean equals(UniversalObject uo) throws IOException {

		if ((getLNA() == uo.fields[COL_LN_A])  &&
				(getLNB() == uo.fields[COL_LN_B]) &&
				(getLNC() == uo.fields[COL_LN_C]) &&
				(getLND() == uo.fields[COL_LN_D]) &&
				(getLNE() == uo.fields[COL_LN_E]) &&
				(getLNF() == uo.fields[COL_LN_F]) &&
				(getClassID() == uo.fields[COL_CLASSID])) {
			return true;
		} else {
			return false;
		}
	}


	public boolean equals(DLMSConfig config) throws IOException {

		if (((getLNA() == config.getLNA()) || (config.getLNA()==-1)) &&
				((getLNB() == config.getLNB()) || (config.getLNB()==-1)) &&
				((getLNC() == config.getLNC()) || (config.getLNC()==-1)) &&
				((getLND() == config.getLND()) || (config.getLND()==-1)) &&
				((getLNE() == config.getLNE()) || (config.getLNE()==-1)) &&
				((getLNF() == config.getLNF()) || (config.getLNF()==-1)) &&
				(getClassID() == config.getClassID())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean equals(DLMSObis dlmsObis) throws IOException {
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

	}

	/*
	 *  Find ObisCode in instantiated object list.
	 *  All instantiated objects have f=255 and are unique for a classId.
	 */
	public boolean equals(ObisCode obisCode) {
		if ((getLNA() == obisCode.getA()) &&
				(getLNB() == obisCode.getB()) &&
				(getLNC() == obisCode.getC()) &&
				(getLND() == obisCode.getD()) &&
				(getLNE() == obisCode.getE())) {
			return true;
		} else {
			return false;
		}
	}

	public ObisCode getObisCode() {
		return ObisCode.fromString(getLN());
	}


	public int getValueAttributeOffset() throws IOException {
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
			throw new IOException("UniversalObject, wrong object for value attribute!");
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
			throw new IOException("UniversalObject, wrong object for scaler attribute!");
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
		sb.append(getDLMClassId()).append(", ");
		sb.append(getObisCode().getDescription());
		return  sb.toString();
	}

	@Override
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

    /**
     * Check if this universalObject has the same class type as the given DLMSClassId
     * @param dlmsClassId
     * @return
     */
    public boolean isClassType(DLMSClassId dlmsClassId) {
        return ((dlmsClassId != null) && (dlmsClassId.getClassId() == getClassID()));
    }

    /**
     * Get the DLMSClassId from the given id value
     * @return
     */
    public DLMSClassId getDLMClassId() {
        return DLMSClassId.findById(getClassID());
    }

} // class UniversalObjectList
